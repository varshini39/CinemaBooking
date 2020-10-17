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
			BookingService bkservice = BookingService.getInstance();
			isBooked = bkservice.bookTickets(showId, seatNumArr, userName, mobileNumber, bookingStartTime);
			logg.log(Level.INFO, "Booked ticket result:: {0}", String.valueOf(isBooked));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
