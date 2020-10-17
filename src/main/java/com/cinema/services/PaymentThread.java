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
			PaymentService pmService = PaymentService.getInstance();
			isPaymentDone = pmService.makePayment(userDetails);
			logg.log(Level.INFO, "Payment result:: {0}", String.valueOf(isPaymentDone));
		} catch (InterruptedException ie) {
			logg.log(Level.SEVERE, "Payment timeout");
		} catch (Exception e) {
			logg.log(Level.SEVERE, "Exception::: ", e);
			e.printStackTrace();
			try {
				throw e;
			} catch (Exception exception) {
				exception.printStackTrace();
				logg.log(Level.SEVERE, "Error while throwing Exception::: ", e);
			}
		}
	}
	
}
