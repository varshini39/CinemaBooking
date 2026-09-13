package com.cinema.services;

import com.cinema.util.AppConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAuthTest {

	private final UserAuth userAuth = new UserAuth();

	private String basicHeaderFor(String username, String password) {
		String credentials = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void authenticateUser_acceptsConfiguredCredentials() {
		String header = basicHeaderFor(AppConfig.get("auth.username"), AppConfig.get("auth.password"));
		assertTrue(userAuth.authenticateUser(header));
	}

	@Test
	void authenticateUser_rejectsWrongPassword() {
		String header = basicHeaderFor(AppConfig.get("auth.username"), "not-the-password");
		assertFalse(userAuth.authenticateUser(header));
	}

	@Test
	void authenticateUser_rejectsWrongUsername() {
		String header = basicHeaderFor("not-the-user", AppConfig.get("auth.password"));
		assertFalse(userAuth.authenticateUser(header));
	}

	@Test
	void authenticateUser_rejectsMissingHeader() {
		assertFalse(userAuth.authenticateUser(null));
	}

	@Test
	void authenticateUser_rejectsNonBasicScheme() {
		assertFalse(userAuth.authenticateUser("Bearer sometoken"));
	}

	@Test
	void authenticateUser_rejectsMalformedBase64() {
		assertFalse(userAuth.authenticateUser("Basic not-valid-base64!!"));
	}

	@Test
	void authenticateUser_rejectsSchemeWithNoToken() {
		assertFalse(userAuth.authenticateUser("Basic"));
	}

}
