package com.cinema.services;

import com.cinema.util.RedisUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingService implements Serializable {

	Logger logg = Logger.getLogger(this.getClass().getName());

	public static BookingService bookingService = null;

	private BookingService() {}

	public static BookingService getInstance() {
		if(bookingService==null) {
			bookingService = new BookingService();
		}
		return bookingService;
	}

	/* Checks the booking time and seat priorities*/
	public synchronized Boolean bookTickets(Long showId, String[] seatNumArr, String userName, Long mobileNumber, Long bookingStartTime) throws Exception {

		logg.log(Level.INFO, "Starting bookTickets()");
		Boolean canBookTickets = Boolean.TRUE;
		String redisIndex = String.valueOf(showId);
		List<String> currUser = Arrays.asList(seatNumArr);
		HashSet<String> commonSet =  new HashSet<>(currUser);

		if(RedisUtil.hasKeyInRedis(redisIndex)) {
			JSONArray bookingQueue = new JSONArray(String.valueOf(RedisUtil.getValueFromRedis(redisIndex)));
			for(int i=0;i<bookingQueue.length();i++) {
				JSONObject userDetails = bookingQueue.getJSONObject(i);

				//Avoid checking for same user
				if(!userName.equals(userDetails.getString("userName")) && !mobileNumber.equals(userDetails.getLong("mobileNumber"))) {
					//Check if booking time is same (FIFO)
					if(bookingStartTime.compareTo(userDetails.getLong("bookingTime")) > 0) {
						canBookTickets = Boolean.FALSE;
					}
					//Checking if any seat is common
					List<String> iterateUser = Arrays.asList(userDetails.getString("seatNumbers"));
					commonSet.retainAll(iterateUser);
					if(!commonSet.isEmpty() && (currUser.size() < iterateUser.size())) {
						canBookTickets = Boolean.FALSE;
					}
				}
				if(canBookTickets == Boolean.FALSE) {
					return canBookTickets;
				}
			}
		}
		RedisUtil.deleteKeyFromRedis(redisIndex);
		return canBookTickets;
	}

}
