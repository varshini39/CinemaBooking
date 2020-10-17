package com.cinema.services;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAuth {

	Logger logg = Logger.getLogger(this.getClass().getName());
	private String USERNAME = "ticketAdmin";
	private String PASSWORD = "admin@123";
	private String AUTH_STR = USERNAME + ":" + PASSWORD;

	private void getAuthorization() {
		String encodeAuth = new BASE64Encoder().encode(AUTH_STR.getBytes());
		//System.out.println("AUTHENTICATION encoded ::: "+encodeAuth);
	}
	
	public Boolean authenticateUser(String authString) {
		getAuthorization();
		if(authString==null) {
			return Boolean.FALSE;
		}
		String[] authStringArr = authString.split(" ");
		if(authStringArr[0].equals("Basic")) {
			String authDigest = authStringArr[1];
			try {
				byte[] digestByte = new BASE64Decoder().decodeBuffer(authDigest);
				String decodeAuth = new String(digestByte);
				//System.out.println("DECODE AUTH::: " + decodeAuth);
				if (decodeAuth.equals(AUTH_STR)) {
					return Boolean.TRUE;
				}
			} catch (Exception e) {
				logg.log(Level.SEVERE, "Error while decoding digest", e);
				e.printStackTrace();
			}
		}
		return Boolean.FALSE;
	}

}
