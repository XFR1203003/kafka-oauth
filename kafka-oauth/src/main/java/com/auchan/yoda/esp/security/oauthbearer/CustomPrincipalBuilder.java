/*
Copyright © 2020 BlackRock Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.auchan.yoda.esp.security.oauthbearer;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.security.auth.AuthenticationContext;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.auth.KafkaPrincipalBuilder;
import org.apache.kafka.common.security.auth.KafkaPrincipalSerde;
import org.apache.kafka.common.security.auth.SaslAuthenticationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Custom principal builder.
 */
public class CustomPrincipalBuilder implements KafkaPrincipalBuilder, KafkaPrincipalSerde {

	private final Logger log = LoggerFactory.getLogger(CustomPrincipalBuilder.class);

	// TODO frequently called => put cache ?
	@Override
	public CustomPrincipal build(AuthenticationContext authenticationContext) throws KafkaException {
		try {
			CustomPrincipal customPrincipal;

			if (!(authenticationContext instanceof SaslAuthenticationContext)) {
				throw new KafkaException("Failed to build CustomPrincipal. SaslAuthenticationContext is required.");
			}

			SaslAuthenticationContext context = (SaslAuthenticationContext) authenticationContext;
			log.debug("Context server is {}", context.server().toString());

			OAuthBearerTokenJwt token = (OAuthBearerTokenJwt) context.server().getNegotiatedProperty("OAUTHBEARER.token");

			customPrincipal = new CustomPrincipal("User", token.principalName());
			customPrincipal.setOauthBearerTokenJwt(token);

			log.info("TOKEN:" + token.toString());

			return customPrincipal;
		} catch (Exception ex) {
			throw new KafkaException("Failed to build CustomPrincipal due to: ", ex);
		}
	}

	// FIXME KafkaPrincipalSerde : what are really doing these methods ???
	// pretending to serialize using a "toString" and deserializing by putting it
	// into the principalType ? and "Serialize" as a name ??

	@Override
	public byte[] serialize(KafkaPrincipal principal) throws SerializationException {
		log.debug("Serializing principal {}", principal);
		return principal.toString().getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public KafkaPrincipal deserialize(byte[] bytes) throws SerializationException {
		log.debug("Deserializing bytes {}", bytes.toString());
		return new CustomPrincipal(bytes.toString(), "Serialize");
	}
}
