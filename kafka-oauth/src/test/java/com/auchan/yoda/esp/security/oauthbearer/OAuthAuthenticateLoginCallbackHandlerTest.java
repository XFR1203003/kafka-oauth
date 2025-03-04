/*
Copyright © 2019 BlackRock Inc.

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

import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * The type O auth authenticate login callback handler test.
 */
public class OAuthAuthenticateLoginCallbackHandlerTest {

	/**
	 * Handle callback successful token.
	 *
	 * @throws IOException the io exception
	 */
	@Test
	public void handleCallback_SuccessfulToken() throws IOException {
		OAuthAuthenticateLoginCallbackHandler loginCallbackHandler =  Mockito.spy(new OAuthAuthenticateLoginCallbackHandler());
		OAuthServiceImpl oauthServiceImplSpy = Mockito.spy(new OAuthServiceImpl());
		Map<String, Object> response = new HashMap<>();
		response.put("active", true);
		response.put("jti", "");
		response.put("iat", 1);
		response.put("exp", 1);

		OAuthBearerTokenJwt jwt = new OAuthBearerTokenJwt(response, "test");
		Mockito.doReturn(oauthServiceImplSpy).when(loginCallbackHandler).getOauthService();
		Mockito.doReturn(jwt).when(oauthServiceImplSpy).requestAccessToken();
		OAuthBearerTokenCallback oauthBearerTokenCallback = new OAuthBearerTokenCallback();
		loginCallbackHandler.handleCallback(oauthBearerTokenCallback);

		assertEquals(jwt, oauthBearerTokenCallback.token());
	}

	/**
	 * Handle callback token not null.
	 *
	 * @throws IOException the io exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void handleCallback_TokenParamNotNull() throws IOException {
		OAuthAuthenticateLoginCallbackHandler loginCallbackHandler =  Mockito.spy(new OAuthAuthenticateLoginCallbackHandler());

		Map<String, Object> response = new HashMap<>();
		response.put("active", true);
		response.put("jti", "");
		response.put("iat", 1);
		response.put("exp", 1);

		OAuthBearerTokenJwt jwt = new OAuthBearerTokenJwt(response, "test");

		OAuthBearerTokenCallback oauthBearerTokenCallback = new OAuthBearerTokenCallback();
		oauthBearerTokenCallback.token(jwt);
		loginCallbackHandler.handleCallback(oauthBearerTokenCallback);

	}

	@Test(expected = IllegalArgumentException.class)
	public void handleCallback_TokenResultNull() throws IOException {
		OAuthAuthenticateLoginCallbackHandler loginCallbackHandler =  Mockito.spy(new OAuthAuthenticateLoginCallbackHandler());
		OAuthServiceImpl oauthServiceImplSpy = Mockito.spy(new OAuthServiceImpl());
		Map<String, Object> response = new HashMap<>();
		response.put("active", true);
		response.put("jti", "");
		response.put("iat", 1);
		response.put("exp", 1);

		Mockito.doReturn(oauthServiceImplSpy).when(loginCallbackHandler).getOauthService();
		Mockito.doReturn(null).when(oauthServiceImplSpy).requestAccessToken();
		OAuthBearerTokenCallback oauthBearerTokenCallback = new OAuthBearerTokenCallback();
		loginCallbackHandler.handleCallback(oauthBearerTokenCallback);

	}
}