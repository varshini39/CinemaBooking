package com.cinema.services;

import com.cinema.model.Hall;
import com.cinema.model.Movie;
import com.cinema.model.Screen;
import com.cinema.model.Shows;
import com.cinema.model.Users;
import com.cinema.util.RedisUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/booking")
public class Booking {

	Logger logg = Logger.getLogger(this.getClass().getName());
	Response responseMsg = null;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchBookingDetails(@QueryParam("userId") Long userId) {
		JSONObject bookingDetails = new JSONObject();

		try {
			Users userDetails = new Users(userId);
			userDetails.populateUserDetails();
			bookingDetails.put("USER_NAME", userDetails.getName());
			bookingDetails.put("USER_CONTACT", userDetails.getMobileNumber());
			bookingDetails.put("SEAT_DETAILS", userDetails.getSeatsBooked());

			Shows showDetails = userDetails.getShowDetails();
			showDetails.populateShowDetails();

			Hall hallDetails = showDetails.getHallDetails();
			hallDetails.populateHallDetails();
			bookingDetails.put("HALL_NAME", hallDetails.getName());

			Screen screenDetails = showDetails.getScreenDetails();
			screenDetails.populateScreenDetails();
			bookingDetails.put("SHOW_TYPE", screenDetails.getType());
			bookingDetails.put("SHOW_TIMINGS", screenDetails.getTiming());

			Movie movieDetails = showDetails.getMovieDetails();
			movieDetails.populateMovieDetails();
			bookingDetails.put("MOVIE_NAME", movieDetails.getName());
			bookingDetails.put("MOVIE_DURATION", movieDetails.getDuration());
			bookingDetails.put("MOVIE_GENRE", movieDetails.getGenre());

		} catch (Exception e) {
			logg.log(Level.SEVERE, "Error while fetching booking details", e);
		}
		System.out.println("BOOKING DETAILS::: "+bookingDetails);
		logg.log(Level.INFO, "Booking Details::: "+bookingDetails);
		//return bookingDetails;
		return Response.status(Response.Status.OK).entity(bookingDetails.toString()).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response bookTickets(String bookingDetailsJSON) throws JSONException {
		JSONObject jsonMsg = new JSONObject();
		
		try {
			JSONObject bookingDetails = new JSONObject(bookingDetailsJSON);
			Long showId = Long.valueOf(bookingDetails.getString("showId"));
			String seatNumbers = bookingDetails.getString("seatNumbers");
			String userName = bookingDetails.getString("userName");
			Long mobileNumber = Long.valueOf(bookingDetails.getString("mobileNumber"));
			Long bookingInitiatedTime = System.currentTimeMillis();

			String[] seatNumArr = seatNumbers.split(",");
			if(seatNumArr.length>6) {
				jsonMsg.put("STATUS", "Error");
				jsonMsg.put("MESSAGE", "Maximum of 6 seats can be booked");
				responseMsg = Response.status(Response.Status.NOT_ACCEPTABLE).entity(jsonMsg.toString()).build();
				return responseMsg;
			}

			//Add the user to booking queue
			try {
				JSONObject userDetails = new JSONObject();
				userDetails.put("userName", userName);
				userDetails.put("mobileNumber", mobileNumber);
				userDetails.put("seatNumbers", seatNumArr);
				userDetails.put("bookingTime", bookingInitiatedTime);

				JSONArray bookingQueue = new JSONArray();
				String redisIndex = String.valueOf(showId);
				if (RedisUtil.hasKeyInRedis(redisIndex)) {
					bookingQueue = new JSONArray(String.valueOf(RedisUtil.getValueFromRedis(redisIndex)));
					RedisUtil.deleteKeyFromRedis(redisIndex);
				}
				bookingQueue.put(userDetails);
				
				RedisUtil.storeValueToRedis(redisIndex, bookingQueue);
				
			} catch (Exception e) {
				logg.log(Level.SEVERE, "Error while adding booking details:: ", e);
			}

			final BookingConcurrency currentBooking = new BookingConcurrency(showId, seatNumArr, userName, mobileNumber, bookingInitiatedTime);
			
			logg.log(Level.INFO, "Starting booking thread");
			Thread bookingThread = new Thread(currentBooking);
			bookingThread.start();
			logg.log(Level.INFO, "Joining booking thread");
			bookingThread.join();
			Boolean statusResponse = currentBooking.getBookedStatus();
			logg.log(Level.INFO, "Response for booking thread::: {0}", String.valueOf(statusResponse));	
			
			if(statusResponse) {
				Users currentUser = new Users(userName, mobileNumber, String.join(",", seatNumArr), new Shows(showId));
				currentUser.addOrUpdateUserDetails();
				
				jsonMsg.put("STATUS", "Success");
				jsonMsg.put("MESSAGE", "Booking successful. Proceeding to payment!");
				responseMsg = Response.status(Response.Status.OK).entity(jsonMsg.toString()).build();
			} else {
				jsonMsg.put("STATUS", "Error");
				jsonMsg.put("MESSAGE", "Tickets already booked, please try again!");
				responseMsg = Response.status(Response.Status.OK).entity(jsonMsg.toString()).build();
			}
			
		} catch(Exception e) {
			logg.log(Level.SEVERE, "Error while booking tickets", e);
			jsonMsg.put("STATUS", "Error");
			jsonMsg.put("MESSAGE", "Error occurred while booking tickets");
			responseMsg = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonMsg.toString()).build();
		}
		return responseMsg;
	}
}
