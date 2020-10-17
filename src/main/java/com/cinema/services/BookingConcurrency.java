package com.cinema.services;

import com.cinema.util.RedisUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingConcurrency implements Runnable {

	Logger logg = Logger.getLogger(this.getClass().getName());

	Long showId;
	String[] seatNumArr;
	String userName;
	Long mobileNumber;
	Long bookingStartTime;
	Boolean isBooked;

	public BookingConcurrency(Long showId, String[] seatNumArr, String userName, Long mobileNumber, Long bookingStartTime) {
		logg.log(Level.INFO, "Initializing Booking Concurrency!");
		this.showId = showId;
		this.seatNumArr = seatNumArr;
		this.userName = userName;
		this.mobileNumber = mobileNumber;
		this.bookingStartTime = bookingStartTime;
	}

	public Boolean getBookedStatus() {
		return isBooked;
	}

	@Override
	public void run() {
		//Sleep for 1 second, to check the bookings at same time
		try {
			logg.log(Level.INFO, "Thread in sleep");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			isBooked = bookTickets();
			logg.log(Level.INFO, "Booked ticket result:: {0}", String.valueOf(isBooked));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* Checks the booking time and seat priorities*/
	public synchronized Boolean bookTickets() throws Exception {

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
