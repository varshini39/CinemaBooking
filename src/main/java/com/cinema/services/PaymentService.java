package com.cinema.services;

import com.cinema.model.Hall;
import com.cinema.model.Shows;
import com.cinema.model.Users;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentService implements Serializable {

	Logger logg = Logger.getLogger(this.getClass().getName());

	public static PaymentService paymentService = null;

	private PaymentService() {}

	public static PaymentService getInstance() {
		if(paymentService==null) {
			paymentService = new PaymentService();
		}
		return paymentService;
	}

	public Boolean makePayment(Users userDetails) throws Exception {
		Boolean isPaymentSuccess = Boolean.FALSE;
		String seatsBooked = userDetails.getSeatsBooked();
		if(seatsBooked==null || seatsBooked.equals("")) {
			throw new Exception("Data not Found");
		}
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
