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

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.server.authorizer.Action;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.authorizer.AuthorizationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomYodaAuthorizerTest {

	@Mock
	AuthorizableRequestContext authorizableRequestContext;

	@Mock
	OAuthBearerTokenJwt jwt;

	@Mock
	CustomPrincipal customPrincipal;

	@InjectMocks
	CustomYodaAuthorizer customYodaAuthorizer;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void authorize() {
		Mockito.when(authorizableRequestContext.principal()).thenReturn(customPrincipal);
		Mockito.when(customPrincipal.getOauthBearerTokenJwt()).thenReturn(jwt);

		Set<String> set = new HashSet<>();
		set.add("urn:kafka:topic:test:write");
		ResourcePattern resourcePattern=new ResourcePattern(ResourceType.fromString("topic"),"test",PatternType.fromString("any"));
		Action action=new Action(AclOperation.fromString("write"),resourcePattern,1,true,true);
		List<Action> actions =new ArrayList<>();
		actions.add(action);

			Mockito.when(jwt.scope()).thenReturn(set);
			//Mockito.when(resourcePattern.name()).thenReturn("test");
			//Mockito.when(resourcePattern.resourceType()).thenReturn(ResourceType.fromString("topic"));
			//Mockito.when(action.operation()).thenReturn(AclOperation.fromString("write"));
		List<AuthorizationResult> result = customYodaAuthorizer.authorize(authorizableRequestContext, actions);

		assertTrue(result.size()>0);
	}

	@Test
	public void checkAuthorization() {
		List<OAuthScope> list = new ArrayList<>();
		OAuthScope scope = new OAuthScope();
		scope.setOperation("Write");
		scope.setResourceName("test");
		scope.setResourceType("topic");
		list.add(scope);

		ResourcePattern resource = new ResourcePattern(ResourceType.fromString("Topic"), "TEST", PatternType.fromString("any"));

		CustomYodaAuthorizer authorizer = new CustomYodaAuthorizer();

		AuthorizationResult result = authorizer.checkAuthorization(list, resource, "write");

		assertNotNull(result);

	}

	@Test
	public void parseScopes() {
		Set<String> set = new HashSet<>();
		set.add("urn:kafka:topic:test:write");
		set.add("urn:kafka:group:test:read");
		CustomYodaAuthorizer authorizer = new CustomYodaAuthorizer();
		List<OAuthScope> scopes = authorizer.parseScopes(set);

		assertEquals(2,scopes.size());
		assertEquals("write",scopes.get(0).getOperation());
		assertEquals("test",scopes.get(0).getResourceName());
		assertEquals("topic",scopes.get(0).getResourceType());
	}

	@Test
	public void parseBadScope() {
		Set<String> set = new HashSet<>();
		set.add("urn:test:write");
		CustomYodaAuthorizer authorizer = new CustomYodaAuthorizer();
		List<OAuthScope> scopes = authorizer.parseScopes(set);

		assertEquals(0,scopes.size());
	}
}