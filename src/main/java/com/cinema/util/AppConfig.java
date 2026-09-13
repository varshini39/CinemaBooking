package com.cinema.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads settings from config.properties on the classpath, with each key
 * overridable by an environment variable (db.url -> DB_URL).
 */
public class AppConfig {

	private static final Logger logg = Logger.getLogger(AppConfig.class.getName());
	private static final Properties properties = new Properties();

	static {
		try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (in != null) {
				properties.load(in);
			} else {
				logg.log(Level.WARNING, "config.properties not found on classpath");
			}
		} catch (IOException e) {
			logg.log(Level.SEVERE, "Error while loading config.properties ::: ", e);
		}
	}

	private AppConfig() {}

	public static String get(String key) {
		String envKey = key.toUpperCase().replace('.', '_');
		String envValue = System.getenv(envKey);
		if (envValue != null && !envValue.isEmpty()) {
			return envValue;
		}
		return properties.getProperty(key);
	}

	public static int getInt(String key) {
		return Integer.parseInt(get(key));
	}

}
