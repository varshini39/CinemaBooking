package com.cinema.services;

import com.cinema.util.AppConfig;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAuth {

	Logger logg = Logger.getLogger(this.getClass().getName());
	private String USERNAME = AppConfig.get("auth.username");
	private String PASSWORD = AppConfig.get("auth.password");
	private String AUTH_STR = USERNAME + ":" + PASSWORD;

	public Boolean authenticateUser(String authString) {
		if(authString==null) {
			return Boolean.FALSE;
		}
		String[] authStringArr = authString.split(" ");
		if(authStringArr.length > 1 && authStringArr[0].equals("Basic")) {
			String authDigest = authStringArr[1];
			try {
				byte[] digestByte = Base64.getDecoder().decode(authDigest);
				String decodeAuth = new String(digestByte, StandardCharsets.UTF_8);
				if (decodeAuth.equals(AUTH_STR)) {
					return Boolean.TRUE;
				}
			} catch (IllegalArgumentException e) {
				// Not a server error - just an untrusted client sending a malformed Basic Auth header
				logg.log(Level.WARNING, "Rejected malformed Basic Auth header");
			}
		}
		return Boolean.FALSE;
	}

}
