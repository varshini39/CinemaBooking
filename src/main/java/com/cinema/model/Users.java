package com.cinema.model;

import com.cinema.util.DBOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Users {

	final Logger logg = Logger.getLogger(this.getClass().getName());

	private Long userId;
	private String name;
	private Long mobileNumber;
	private String seatsBooked;
	private Shows showDetails;
	private Boolean isPaid = Boolean.FALSE;
	private static final String TABLE_NAME = "Users";
	private static final String TABLE_PKCOL = "USER_ID";
	private static final String[] TABLE_COLUMNS = {"NAME", "MOBILE_NUMBER", "SEATS_BOOKED", "SHOW_ID", "IS_PAID"};

	public Users(Long userId, String name, Long mobileNumber, String seatsBooked, Shows showDetails) {
		this.userId = userId;
		this.name = name;
		this.mobileNumber = mobileNumber;
		this.seatsBooked = seatsBooked;
		this.showDetails = showDetails;
	}

	public Users(String name, Long mobileNumber, String seatsBooked, Shows showDetails) {
		this.name = name;
		this.mobileNumber = mobileNumber;
		this.seatsBooked = seatsBooked;
		this.showDetails = showDetails;
	}

	public Users(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(Long mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	
	public String getSeatsBooked() {
		return seatsBooked;
	}

	public void setSeatsBooked(String seatsBooked) {
		this.seatsBooked = seatsBooked;
	}

	public Shows getShowDetails() {
		return showDetails;
	}

	public void setShowDetails(Shows showDetails) {
		this.showDetails = showDetails;
	}

	public Boolean getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
	}

	public void fetchBookingDetails() {
		String movieName = showDetails.getMovieDetails().getName();
		String hallName = showDetails.getHallDetails().getName();
		System.out.println("The user "+name+" has booked ticket for "+seatsBooked+" members for the movie "+movieName+" at "+hallName);
	}

	public void addUserDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();;
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				int paidVal = isPaid? 1 : 0;
				String[] updateValues = {name, String.valueOf(mobileNumber), String.valueOf(seatsBooked), String.valueOf(showDetails.getShowId()), String.valueOf(paidVal)};
				Long resultUserId = dbMovieOp.insertRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), updateValues);
				setUserId(resultUserId);
			}

			dbMovieOp.closeDBConnection();
			System.out.println("USER ADDED SUCCESSFULLY!!!");
		} catch(Exception e) {
			System.out.println("Error while adding user details");
			logg.log(Level.SEVERE, "Error while adding user details ::: ", e);
		}
	}

	public void updateUserDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				int paidVal = isPaid? 1 : 0;
				String[] updateValues = {name, String.valueOf(mobileNumber), String.valueOf(seatsBooked), String.valueOf(showDetails.getShowId()), String.valueOf(paidVal)};
				dbMovieOp.updateRows(TABLE_NAME, TABLE_COLUMNS, updateValues, TABLE_PKCOL, String.valueOf(userId));
			}

			dbMovieOp.closeDBConnection();
			System.out.println("USER DETAILS UPDATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while updating user details");
			logg.log(Level.SEVERE, "Error while updating user details ::: ", e);
		}
	}
	
	public void addOrUpdateUserDetails() {
		try {
			int paidVal = isPaid? 1 : 0;
			String[] updateValues = {name, String.valueOf(mobileNumber), String.valueOf(seatsBooked), String.valueOf(showDetails.getShowId()), String.valueOf(paidVal)};
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				HashMap<String, String> criteria = new HashMap<>();
				criteria.put("NAME", name); 
				criteria.put("MOBILE_NUMBER", String.valueOf(mobileNumber)); 
				criteria.put("SHOW_ID", String.valueOf(showDetails.getShowId())); 
				
				JSONArray jArr = dbMovieOp.viewRows(TABLE_NAME, String.join(",", TABLE_COLUMNS)+",USER_ID", criteria);
				if(jArr!=null && jArr.length()>0) {
					JSONObject jObj = jArr.getJSONObject(0);
					Long userId = jObj.getLong("USER_ID");
					setUserId(userId);
					logg.log(Level.INFO, "Updating the row");
					dbMovieOp.updateRows(TABLE_NAME, TABLE_COLUMNS, updateValues, TABLE_PKCOL, String.valueOf(userId));
				} else {
					logg.log(Level.SEVERE, "Adding new row");
					Long resultUserId = dbMovieOp.insertRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), updateValues);
					setUserId(resultUserId);
				}
			}

			dbMovieOp.closeDBConnection();
			System.out.println("USER DETAILS ADDED/UPDATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while adding/updating user details");
			logg.log(Level.SEVERE, "Error while adding/updating user details ::: ", e);
		}
	}

	public void populateUserDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			JSONArray jArr = null;
			if(isConnSuccess) {
				jArr = dbMovieOp.viewRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), TABLE_PKCOL, String.valueOf(userId));
			}

			if(jArr!=null && jArr.length()>0) {
				JSONObject jObj = jArr.getJSONObject(0);
				name = jObj.getString(TABLE_COLUMNS[0]);
				mobileNumber = jObj.getLong(TABLE_COLUMNS[1]);
				seatsBooked = jObj.getString(TABLE_COLUMNS[2]);
				showDetails = new Shows(jObj.getLong(TABLE_COLUMNS[3]));
				System.out.println(TABLE_COLUMNS[4]);
				isPaid = Boolean.valueOf(jObj.getString(TABLE_COLUMNS[4]));
			} else {
				logg.log(Level.SEVERE, "Data not found");
				throw new Exception("Data not found");
			}

			dbMovieOp.closeDBConnection();
			System.out.println("USER DETAILS POPULATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while populating user details");
			logg.log(Level.SEVERE, "Error while populating user details ::: ", e);
		}
	}
}
