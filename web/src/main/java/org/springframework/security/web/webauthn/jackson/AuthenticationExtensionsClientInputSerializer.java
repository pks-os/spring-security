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

package org.springframework.security.web.webauthn.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.springframework.security.web.webauthn.api.AuthenticationExtensionsClientInput;

/**
 * Provides Jackson serialization of {@link AuthenticationExtensionsClientInput}.
 *
 * @author Rob Winch
 * @since 6.4
 */
class AuthenticationExtensionsClientInputSerializer extends StdSerializer<AuthenticationExtensionsClientInput> {

	/**
	 * Creates a new instance.
	 */
	AuthenticationExtensionsClientInputSerializer() {
		super(AuthenticationExtensionsClientInput.class);
	}

	@Override
	public void serialize(AuthenticationExtensionsClientInput input, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {
		jgen.writeObjectField(input.getExtensionId(), input.getInput());
	}

}
