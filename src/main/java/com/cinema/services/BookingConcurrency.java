package com.cinema.services;

import com.cinema.util.RedisUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class BookingConcurrency {

	/*Adds the user to the booking queue*/
	public synchronized void addBookingQueue(Long showId, String[] seatNumArr, String userName, Long mobileNumber, Long bookingStartTime) throws Exception {

		JSONObject userDetails = new JSONObject();
		userDetails.put("userName", userName);
		userDetails.put("mobileNumber", mobileNumber);
		userDetails.put("seatNumbers", seatNumArr);
		userDetails.put("bookingTime", bookingStartTime);
		
		JSONArray bookingQueue = new JSONArray();
		String redisIndex = String.valueOf(showId);
		if(RedisUtil.hasKeyInRedis(redisIndex)) {
			bookingQueue = new JSONArray(String.valueOf(RedisUtil.getValueFromRedis(redisIndex)));
		}
		bookingQueue.put(userDetails);
		
	}
	
	/* Checks the booking time and seat priorities*/
	public synchronized Boolean bookTickets(Long showId, String[] seatNumArr, String userName, Long mobileNumber, Long bookingStartTime) throws Exception {

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
