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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * The type Utils test.
 */
public class UtilsTest {

	/**
	 * Is uri valid.
	 */
	@Test
	public void isURIValid() {
		assertTrue(Utils.isURIValid("www.google.com"));
		assertFalse(Utils.isURIValid(null));
	}

	/**
	 * Is null or empty.
	 */
	@Test
	public void isNullOrEmpty() {
		assertTrue(Utils.isNullOrEmpty(null));
		assertTrue(Utils.isNullOrEmpty(""));
		assertFalse(Utils.isNullOrEmpty("test"));
	}

	/**
	 * Create basic authorization header.
	 */
	@Test
	public void createBasicAuthorizationHeader() {
		assertNotNull(Utils.createBasicAuthorizationHeader("clientId", "clientSecret"));
	}

	/**
	 * Create bearer header.
	 */
	@Test
	public void createBearerHeader() {
		assertEquals(Utils.createBearerHeader("token"), "Bearer token");
	}
}