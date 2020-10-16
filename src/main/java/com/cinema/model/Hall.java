package com.cinema.model;

import com.cinema.util.DBOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Hall {

	final Logger logg = Logger.getLogger(this.getClass().getName());

	private Long hallId;
	private String name;
	private int totalSeats;
	private int seatsBooked = 0;
	private int rate;
	private static final String TABLE_NAME = "Hall";
	private static final String TABLE_PKCOL = "HALL_ID";
	private static final String[] TABLE_COLUMNS = {"NAME", "TOTAL_SEATS", "SEATS_BOOKED", "RATE"};

	public Hall(Long hallId, String name, int totalSeats, int seatsBooked, int rate) {
		this.hallId = hallId;
		this.name = name;
		this.totalSeats = totalSeats;
		this.seatsBooked = seatsBooked;
		this.rate = rate;
	}

	public Hall(Long hallId) {
		this.hallId = hallId;
	}

	public Hall(String name, int totalSeats, int rate) {
		this.name = name;
		this.totalSeats = totalSeats;
		this.rate = rate;
	}

	public Long getHallId() {
		return hallId;
	}

	public void setHallId(Long hallId) {
		this.hallId = hallId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getTotalSeats() {
		return totalSeats;
	}

	public void setTotalSeats(int totalSeats) {
		this.totalSeats = totalSeats;
	}

	public int getSeatsBooked() {
		return seatsBooked;
	}

	public void setSeatsBooked(int seatsBooked) {
		this.seatsBooked = seatsBooked;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public void displayHallDetails() {
		System.out.println("This "+name+" hall has"+seatsBooked+" number of seats booked and each ticket costs "+rate);
	}
	
	public void addHallDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();;
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {name, String.valueOf(totalSeats), String.valueOf(seatsBooked), String.valueOf(rate)};
				Long resultHallId = dbMovieOp.insertRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), updateValues);
				setHallId(resultHallId);
			}

			dbMovieOp.closeDBConnection();
			System.out.println("HALL ADDED SUCCESSFULLY!!!");
		} catch(Exception e) {
			System.out.println("Error while adding hall details");
			logg.log(Level.SEVERE, "Error while adding hall details ::: ", e);
		}
	}

	public void updateHallDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {name, String.valueOf(totalSeats), String.valueOf(seatsBooked), String.valueOf(rate)};
				dbMovieOp.updateRows(TABLE_NAME, TABLE_COLUMNS, updateValues, TABLE_PKCOL, String.valueOf(hallId));
			}

			dbMovieOp.closeDBConnection();
			System.out.println("HALL UPDATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while updating Hall details");
			logg.log(Level.SEVERE, "Error while updating hall details ::: ", e);
		}
	}

	public void populateHallDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			JSONArray jArr = null;
			if(isConnSuccess) {
				jArr = dbMovieOp.viewRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), TABLE_PKCOL, String.valueOf(hallId));
			}

			if(jArr!=null && jArr.length()>0) {
				JSONObject jObj = jArr.getJSONObject(0);
				name = jObj.getString(TABLE_COLUMNS[0]);
				totalSeats = jObj.getInt(TABLE_COLUMNS[1]);
				seatsBooked = jObj.getInt(TABLE_COLUMNS[2]);
				rate = jObj.getInt(TABLE_COLUMNS[3]);
			} else {
				logg.log(Level.SEVERE, "Data not found");
				throw new Exception("Data not found");
			}

			dbMovieOp.closeDBConnection();
			System.out.println("HALLS DETAILS POPULATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while populating hall details");
			logg.log(Level.SEVERE, "Error while populating hall details ::: ", e);
		}
	}
}
