package com.cinema.model;

import com.cinema.util.DBOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Logger;
import java.util.logging.Level;

public class Screen {

	final Logger logg = Logger.getLogger(this.getClass().getName());
	
	private Long screenId;
	private String type;
	private String timing;
	private static final String TABLE_NAME = "Screen";
	private static final String TABLE_PKCOL = "SCREEN_ID";
	private static final String[] TABLE_COLUMNS = {"TYPE", "TIMING"};

	public Screen(Long screenId, String type, String timing) {
		this.screenId = screenId;
		this.type = type;
		this.timing = timing;
	}

	public Screen(Long screenId) {
		this.screenId = screenId;
	}

	public Screen(String type, String timing) {
		this.type = type;
		this.timing = timing;
	}

	public Long getScreenId() {
		return screenId;
	}

	public void setScreenId(Long screenId) {
		this.screenId = screenId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTiming() {
		return timing;
	}

	public void setTiming(String timing) {
		this.timing = timing;
	}
	
	public void displayScreenDetails() {
		System.out.println("This "+type+" show screens at "+timing);
	}
	
	public void addScreenDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {type, timing};
				Long resultScreenId = dbMovieOp.insertRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), updateValues);
				setScreenId(resultScreenId);
			}

			dbMovieOp.closeDBConnection();
			System.out.println("SCREEN DETAILS ADDED SUCCESSFULLY!!!");
		} catch(Exception e) {
			System.out.println("Error while adding screen details");
			logg.log(Level.SEVERE, "Error while adding screen details ::: ", e);
		}
	}

	public void updateScreenDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {type, timing};
				dbMovieOp.updateRows(TABLE_NAME, TABLE_COLUMNS, updateValues, TABLE_PKCOL, String.valueOf(screenId));
			}

			dbMovieOp.closeDBConnection();
			System.out.println("SCREEN DETAILS UPDATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while updating screen details");
			logg.log(Level.SEVERE, "Error while updating screen details ::: ", e);
		}
	}

	public void populateScreenDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			JSONArray jArr = null;
			if(isConnSuccess) {
				jArr = dbMovieOp.viewRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), TABLE_PKCOL, String.valueOf(screenId));
			}

			if(jArr!=null && jArr.length()>0) {
				JSONObject jObj = jArr.getJSONObject(0);
				type = jObj.getString(TABLE_COLUMNS[0]);
				timing = jObj.getString(TABLE_COLUMNS[1]);
			} else {
				logg.log(Level.SEVERE, "Data not found");
				throw new Exception("Data not found");
			}

			dbMovieOp.closeDBConnection();
			System.out.println("SCREEN DETAILS POPULATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while populating screen details");
			logg.log(Level.SEVERE, "Error while populating screen details ::: ", e);
		}
	}
}
