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

package org.springframework.security.web.webauthn.api;

/**
 * <a href="https://www.w3.org/TR/webauthn-3/#webauthn-relying-party">WebAuthn Relying
 * Parties</a> may use <a href=
 * "https://www.w3.org/TR/webauthn-3/#enumdef-attestationconveyancepreference">AttestationConveyancePreference</a>
 * to specify their preference regarding attestation conveyance during credential
 * generation.
 *
 * @author Rob Winch
 * @since 6.4
 */
public final class AttestationConveyancePreference {

	/**
	 * The <a href=
	 * "https://www.w3.org/TR/webauthn-3/#dom-attestationconveyancepreference-none">none</a>
	 * preference indicates that the Relying Party is not interested in
	 * <a href="https://www.w3.org/TR/webauthn-3/#authenticator">authenticator</a>
	 * <a href="https://www.w3.org/TR/webauthn-3/#attestation">attestation</a>.
	 */
	public static final AttestationConveyancePreference NONE = new AttestationConveyancePreference("none");

	/**
	 * The <a href=
	 * "https://www.w3.org/TR/webauthn-3/#dom-attestationconveyancepreference-indirect">indirect</a>
	 * preference indicates that the Relying Party wants to receive a verifiable
	 * <a href="https://www.w3.org/TR/webauthn-3/#attestation-statement">attestation
	 * statement</a>, but allows the client to decide how to obtain such an attestation
	 * statement.
	 */
	public static final AttestationConveyancePreference INDIRECT = new AttestationConveyancePreference("indirect");

	/**
	 * The <a href=
	 * "https://www.w3.org/TR/webauthn-3/#dom-attestationconveyancepreference-direct">direct</a>
	 * preference indicates that the Relying Party wants to receive the
	 * <a href="https://www.w3.org/TR/webauthn-3/#attestation-statement">attestation
	 * statement</a> as generated by the
	 * <a href="https://www.w3.org/TR/webauthn-3/#authenticator">authenticator</a>.
	 */
	public static final AttestationConveyancePreference DIRECT = new AttestationConveyancePreference("direct");

	/**
	 * The <a href=
	 * "https://www.w3.org/TR/webauthn-3/#dom-attestationconveyancepreference-enterprise">enterprise</a>
	 * preference indicates that the Relying Party wants to receive an
	 * <a href="https://www.w3.org/TR/webauthn-3/#attestation-statement">attestation
	 * statement</a> that may include uniquely identifying information.
	 */
	public static final AttestationConveyancePreference ENTERPRISE = new AttestationConveyancePreference("enterprise");

	private final String value;

	AttestationConveyancePreference(String value) {
		this.value = value;
	}

	/**
	 * Gets the String value of the preference.
	 * @return the String value of the preference.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Gets an instance of {@link AttestationConveyancePreference}
	 * @param value the {@link #getValue()}
	 * @return an {@link AttestationConveyancePreference}
	 */
	public static AttestationConveyancePreference valueOf(String value) {
		switch (value) {
			case "none":
				return NONE;
			case "indirect":
				return INDIRECT;
			case "direct":
				return DIRECT;
			case "enterprise":
				return ENTERPRISE;
			default:
				return new AttestationConveyancePreference(value);
		}
	}

}
