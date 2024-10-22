/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.config.annotation.web.configurers;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.authentication.ui.DefaultResourcesFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsFilter;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationProvider;
import org.springframework.security.web.webauthn.management.MapPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.MapUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;
import org.springframework.security.web.webauthn.registration.DefaultWebAuthnRegistrationPageGeneratingFilter;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsFilter;
import org.springframework.security.web.webauthn.registration.WebAuthnRegistrationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Configures WebAuthn for Spring Security applications
 *
 * @param <H> the type of builder
 * @author Rob Winch
 * @since 6.4
 */
public class WebAuthnConfigurer<H extends HttpSecurityBuilder<H>>
		extends AbstractHttpConfigurer<WebAuthnConfigurer<H>, H> {

	private String rpId;

	private String rpName;

	private Set<String> allowedOrigins = new HashSet<>();

	/**
	 * The Relying Party id.
	 * @param rpId the relying party id
	 * @return the {@link WebAuthnConfigurer} for further customization
	 */
	public WebAuthnConfigurer<H> rpId(String rpId) {
		this.rpId = rpId;
		return this;
	}

	/**
	 * Sets the relying party name
	 * @param rpName the relying party name
	 * @return the {@link WebAuthnConfigurer} for further customization
	 */
	public WebAuthnConfigurer<H> rpName(String rpName) {
		this.rpName = rpName;
		return this;
	}

	/**
	 * Convenience method for {@link #allowedOrigins(Set)}
	 * @param allowedOrigins the allowed origins
	 * @return the {@link WebAuthnConfigurer} for further customization
	 * @see #allowedOrigins(Set)
	 */
	public WebAuthnConfigurer<H> allowedOrigins(String... allowedOrigins) {
		return allowedOrigins(Set.of(allowedOrigins));
	}

	/**
	 * Sets the allowed origins.
	 * @param allowedOrigins the allowed origins
	 * @return the {@link WebAuthnConfigurer} for further customization
	 * @see #allowedOrigins(String...)
	 */
	public WebAuthnConfigurer<H> allowedOrigins(Set<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
		return this;
	}

	@Override
	public void configure(H http) throws Exception {
		UserDetailsService userDetailsService = getSharedOrBean(http, UserDetailsService.class).orElseGet(() -> {
			throw new IllegalStateException("Missing UserDetailsService Bean");
		});
		PublicKeyCredentialUserEntityRepository userEntities = getSharedOrBean(http,
				PublicKeyCredentialUserEntityRepository.class)
			.orElse(userEntityRepository());
		UserCredentialRepository userCredentials = getSharedOrBean(http, UserCredentialRepository.class)
			.orElse(userCredentialRepository());
		WebAuthnRelyingPartyOperations rpOperations = webAuthnRelyingPartyOperations(userEntities, userCredentials);
		WebAuthnAuthenticationFilter webAuthnAuthnFilter = new WebAuthnAuthenticationFilter();
		webAuthnAuthnFilter.setAuthenticationManager(
				new ProviderManager(new WebAuthnAuthenticationProvider(rpOperations, userDetailsService)));
		http.addFilterBefore(webAuthnAuthnFilter, BasicAuthenticationFilter.class);
		http.addFilterAfter(new WebAuthnRegistrationFilter(userCredentials, rpOperations), AuthorizationFilter.class);
		http.addFilterBefore(new PublicKeyCredentialCreationOptionsFilter(rpOperations), AuthorizationFilter.class);
		http.addFilterAfter(new DefaultWebAuthnRegistrationPageGeneratingFilter(userEntities, userCredentials),
				AuthorizationFilter.class);
		http.addFilterBefore(new PublicKeyCredentialRequestOptionsFilter(rpOperations), AuthorizationFilter.class);
		DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = http
			.getSharedObject(DefaultLoginPageGeneratingFilter.class);
		if (loginPageGeneratingFilter != null) {
			ClassPathResource webauthn = new ClassPathResource(
					"org/springframework/security/spring-security-webauthn.js");
			AntPathRequestMatcher matcher = antMatcher(HttpMethod.GET, "/login/webauthn.js");

			Constructor<DefaultResourcesFilter> constructor = DefaultResourcesFilter.class
				.getDeclaredConstructor(RequestMatcher.class, ClassPathResource.class, MediaType.class);
			constructor.setAccessible(true);
			DefaultResourcesFilter resourcesFilter = constructor.newInstance(matcher, webauthn,
					MediaType.parseMediaType("text/javascript"));
			http.addFilter(resourcesFilter);
			DefaultLoginPageGeneratingFilter loginGeneratingFilter = http
				.getSharedObject(DefaultLoginPageGeneratingFilter.class);
			loginGeneratingFilter.setPasskeysEnabled(true);
			loginGeneratingFilter.setResolveHeaders((request) -> {
				CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
				return Map.of(csrfToken.getHeaderName(), csrfToken.getToken());
			});
		}
	}

	private <C> Optional<C> getSharedOrBean(H http, Class<C> type) {
		C shared = http.getSharedObject(type);
		return Optional.ofNullable(shared).or(() -> getBeanOrNull(type));
	}

	private <T> Optional<T> getBeanOrNull(Class<T> type) {
		ApplicationContext context = getBuilder().getSharedObject(ApplicationContext.class);
		if (context == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(context.getBean(type));
		}
		catch (NoSuchBeanDefinitionException ex) {
			return Optional.empty();
		}
	}

	private MapUserCredentialRepository userCredentialRepository() {
		return new MapUserCredentialRepository();
	}

	private PublicKeyCredentialUserEntityRepository userEntityRepository() {
		return new MapPublicKeyCredentialUserEntityRepository();
	}

	private WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations(
			PublicKeyCredentialUserEntityRepository userEntities, UserCredentialRepository userCredentials) {
		Optional<WebAuthnRelyingPartyOperations> webauthnOperationsBean = getBeanOrNull(
				WebAuthnRelyingPartyOperations.class);
		if (webauthnOperationsBean.isPresent()) {
			return webauthnOperationsBean.get();
		}
		Webauthn4JRelyingPartyOperations result = new Webauthn4JRelyingPartyOperations(userEntities, userCredentials,
				PublicKeyCredentialRpEntity.builder().id(this.rpId).name(this.rpName).build(), this.allowedOrigins);
		return result;
	}

}
