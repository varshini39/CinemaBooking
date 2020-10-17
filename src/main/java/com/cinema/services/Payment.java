package com.cinema.services;

import com.cinema.model.Users;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/payment")
public class Payment {

	Logger logg = Logger.getLogger(this.getClass().getName());
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response makePayment(String paymentDetailsJSON, @HeaderParam("authorization") String authString) throws JSONException {
		JSONObject jsonMsg = new JSONObject();

		UserAuth authenticateUser = new UserAuth();
		Boolean isUserAuth = authenticateUser.authenticateUser(authString);
		if(!isUserAuth) {
			try {
				jsonMsg.put("STATUS", "ERROR");
				jsonMsg.put("MESSAGE", "UNAUTHORIZED");
			} catch(Exception e) {
				logg.log(Level.SEVERE, "Error while forming JSON::: ", e);
			}
			return Response.status(Response.Status.UNAUTHORIZED).entity(jsonMsg.toString()).build();
		}
		
		try {
			JSONObject paymentDetails = new JSONObject(paymentDetailsJSON);
			Long userId = paymentDetails.getLong("userId");
			Users userDetails = new Users(userId);
			userDetails.populateUserDetails();
			
			PaymentThread payInstance = new PaymentThread(userDetails);
			Thread pyThread = new Thread(payInstance);
			logg.log(Level.INFO, "Starting payment thread");
			pyThread.start();
			logg.log(Level.INFO, "Joining payment thread");
			pyThread.join(120000);
			
			Boolean paymentStatus = payInstance.isPaymentDone;
			if(!paymentStatus) {
				logg.log(Level.INFO, "Interrupting payment thread");
				pyThread.interrupt();
				jsonMsg.put("STATUS", "Error");
				jsonMsg.put("MESSAGE", "Payment Timeout");
				return Response.status(Response.Status.GATEWAY_TIMEOUT).entity(jsonMsg.toString()).build();
			} else {
				jsonMsg.put("STATUS", "Success");
				jsonMsg.put("MESSAGE", "Payment done successfully");
				return Response.status(Response.Status.OK).entity(jsonMsg.toString()).build();
			}			
			
		} catch (Exception e) {
			logg.log(Level.SEVERE, "Error while making payments", e);
			jsonMsg.put("STATUS", "Error");
			jsonMsg.put("MESSAGE", "Error occurred while making payment");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonMsg.toString()).build();
		}
	}
	
}
