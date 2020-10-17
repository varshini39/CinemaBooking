package com.cinema.services;

import com.cinema.model.Hall;
import com.cinema.model.Shows;
import com.cinema.model.Users;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentThread implements Runnable {

	Users userDetails;
	Boolean isPaymentDone = Boolean.FALSE;
	Logger logg = Logger.getLogger(this.getClass().getName());

	public PaymentThread(Users userDetails) {
		this.userDetails = userDetails;
	}

	@Override
	public void run() {
		try {
			isPaymentDone = makePayment();
			logg.log(Level.INFO, "Payment result:: {0}", String.valueOf(isPaymentDone));
		} catch (InterruptedException ie) {
			logg.log(Level.SEVERE, "Payment timeout");
		} catch (Exception e) {
			logg.log(Level.SEVERE, "Exception::: ", e);
			e.printStackTrace();
		}
	}

	public Boolean makePayment() throws Exception {
		Boolean isPaymentSuccess = Boolean.FALSE;
		String seatsBooked = userDetails.getSeatsBooked();
		String[] seatsArr = seatsBooked.split(",");
		
		 Boolean paymentDone = makePaymentUsingThirdParty();
		if(paymentDone) {
			
			userDetails.setIsPaid(Boolean.TRUE);
			userDetails.updateUserDetails();
			
			Shows showDetails = userDetails.getShowDetails();
			showDetails.populateShowDetails();
			
			Hall hallDetails = showDetails.getHallDetails();
			hallDetails.populateHallDetails();
			int seatsNumBooked = hallDetails.getSeatsBooked();
			seatsNumBooked += seatsArr.length;
			hallDetails.setSeatsBooked(seatsNumBooked);
			hallDetails.updateHallDetails();
			
			isPaymentSuccess = Boolean.TRUE;
			
			logg.log(Level.INFO, "Payment Done successfully");
		}
		
		return isPaymentSuccess;
	}
	
	//This method is used to call the third party payment services
	public  Boolean makePaymentUsingThirdParty() throws Exception {
		//Thread.sleep(180000);  //For testing purpose
		return Boolean.TRUE;
	}
	
}
