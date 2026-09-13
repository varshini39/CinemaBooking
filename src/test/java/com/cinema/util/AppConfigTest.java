package com.cinema.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Assumes a fresh checkout where none of the DB_*, REDIS_*, AUTH_* env vars
 * are set, so config.properties' own defaults are what's in effect.
 */
class AppConfigTest {

	@Test
	void get_returnsValueFromPropertiesFile() {
		assertEquals("ticketAdmin", AppConfig.get("auth.username"));
		assertEquals("127.0.0.1", AppConfig.get("redis.host"));
	}

	@Test
	void get_returnsNullForUnknownKey() {
		assertNull(AppConfig.get("no.such.key"));
	}

	@Test
	void getInt_parsesNumericProperty() {
		assertTrue(AppConfig.getInt("redis.port") > 0);
		assertEquals(6379, AppConfig.getInt("redis.port"));
	}

}
