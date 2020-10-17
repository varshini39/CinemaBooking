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

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
	public Response fetchBookingDetails() {
		JSONObject bookingDetails = new JSONObject();

		try {
			Users userDetails = new Users(2L);
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response bookTickets(@FormParam("showId") Long showId,@FormParam("seatNumbers") String seatNumbers, @FormParam("userName") String userName, @FormParam("mobileNumber") Long mobileNumber) throws JSONException {
		JSONObject jsonMsg = new JSONObject();
		
		try {
			Long bookingInitiatedTime = System.currentTimeMillis();

			String[] seatNumArr = seatNumbers.split(",");
			if(seatNumArr.length>6) {
				jsonMsg.put("STATUS", "Error");
				jsonMsg.put("MESSAGE", "Maximum of 6 seats can be booked");
				responseMsg = Response.status(Response.Status.NOT_ACCEPTABLE).entity(jsonMsg.toString()).build();
				return responseMsg;
			}

			final BookingConcurrency currentBooking = new BookingConcurrency();
			
			currentBooking.addBookingQueue(showId, seatNumArr, userName, mobileNumber, bookingInitiatedTime);
						
			new Thread() {
				@Override
				public void run() {
					try {
						//Sleep for 1 second, to check the bookings at same time
						sleep(1000);
						Boolean statusResponse = currentBooking.bookTickets(showId, seatNumArr, userName, mobileNumber, bookingInitiatedTime);
						if(statusResponse) {
							jsonMsg.put("STATUS", "Success");
							jsonMsg.put("MESSAGE", "Maximum of 6 seats can be booked");
							responseMsg = Response.status(Response.Status.OK).entity(jsonMsg.toString()).build();
						} else {
							jsonMsg.put("STATUS", "Error");
							jsonMsg.put("MESSAGE", "Tcikets already booked, please try again!");
							responseMsg = Response.status(Response.Status.OK).entity(jsonMsg.toString()).build();
						}
						
					} catch (Exception e) {
						logg.log(Level.SEVERE, "Error while booking tickets", e);
						try {
							jsonMsg.put("STATUS", "Error");
							jsonMsg.put("MESSAGE", "Error while booking tickets");
							responseMsg = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonMsg.toString()).build();
						} catch (Exception ex) {
							logg.log(Level.SEVERE, "Error while creating error json:: ", ex);
						}
					}
				}
			}.start();
			
		} catch(Exception e) {
			logg.log(Level.SEVERE, "Error while booking tickets", e);
			jsonMsg.put("STATUS", "Error");
			jsonMsg.put("MESSAGE", "Error occurred");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonMsg.toString()).build();
		}
		return Response.status(Response.Status.OK).entity(jsonMsg.toString()).build();
	}
}
