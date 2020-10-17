package com.cinema.model;

import com.cinema.util.DBOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Shows {

	final Logger logg = Logger.getLogger(this.getClass().getName());

	private Long showId;
	private Movie movieDetails;
	private Screen screenDetails;
	private Hall hallDetails;
	private static final String TABLE_NAME = "Shows";
	private static final String TABLE_PKCOL = "SHOW_ID";
	private static final String[] TABLE_COLUMNS = {"MOVIE_ID", "SCREEN_ID", "HALL_ID"};

	public Shows(Long showId) {
		this.showId = showId;
	}

	public Shows(Movie movieDetails, Screen screenDetails, Hall hallDetails) {
		this.movieDetails = movieDetails;
		this.screenDetails = screenDetails;
		this.hallDetails = hallDetails;
	}

	public Shows(Long showId, Movie movieDetails, Screen screenDetails, Hall hallDetails) {
		this.showId = showId;
		this.movieDetails = movieDetails;
		this.screenDetails = screenDetails;
		this.hallDetails = hallDetails;
	}

	public Long getShowId() {
		return showId;
	}

	public void setShowId(Long showId) {
		this.showId = showId;
	}

	public Movie getMovieDetails() {
		return movieDetails;
	}

	public void setMovieDetails(Movie movieDetails) {
		this.movieDetails = movieDetails;
	}

	public Screen getScreenDetails() {
		return screenDetails;
	}

	public void setScreenDetails(Screen screenDetails) {
		this.screenDetails = screenDetails;
	}

	public Hall getHallDetails() {
		return hallDetails;
	}

	public void setHallDetails(Hall hallDetails) {
		this.hallDetails = hallDetails;
	}

	public void fetchNowShowing() {
		movieDetails.populateMovieDetails();
		String movieName = movieDetails.getName();
		screenDetails.populateScreenDetails();
		String screenName = screenDetails.getType();
		String timing = screenDetails.getTiming();
		hallDetails.populateHallDetails();
		String hallName = hallDetails.getName();
		System.out.println("Now Showing::: "+movieName+" in "+screenName+", "+hallName+" at "+timing);
	}

	public void addShowDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();;
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {String.valueOf(movieDetails.getMovieId()), String.valueOf(screenDetails.getScreenId()), String.valueOf(hallDetails.getHallId())};
				Long resultShowId = dbMovieOp.insertRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), updateValues);
				setShowId(resultShowId);
			}

			dbMovieOp.closeDBConnection();
			System.out.println("SHOW DETAILS ADDED SUCCESSFULLY!!!");
		} catch(Exception e) {
			System.out.println("Error while adding show details");
			logg.log(Level.SEVERE, "Error while adding show details ::: ", e);
		}
	}

	public void updateShowDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {String.valueOf(movieDetails.getMovieId()), String.valueOf(screenDetails.getScreenId()), String.valueOf(hallDetails.getHallId())};
				dbMovieOp.updateRows(TABLE_NAME, TABLE_COLUMNS, updateValues, TABLE_PKCOL, String.valueOf(showId));
			}

			dbMovieOp.closeDBConnection();
			System.out.println("SHOW DETAILS UPDATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while updating show details");
			logg.log(Level.SEVERE, "Error while updating show details ::: ", e);
		}
	}

	public void populateShowDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			JSONArray jArr = null;
			if(isConnSuccess) {
				jArr = dbMovieOp.viewRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), TABLE_PKCOL, String.valueOf(showId));
			}

			if(jArr!=null && jArr.length()>0) {
				JSONObject jObj = jArr.getJSONObject(0);
				movieDetails = new Movie(jObj.getLong(TABLE_COLUMNS[0]));
				screenDetails = new Screen(jObj.getLong(TABLE_COLUMNS[1]));
				hallDetails = new Hall(jObj.getLong(TABLE_COLUMNS[2]));
			} else {
				logg.log(Level.SEVERE, "Data not found");
				throw new Exception("Data not found");
			}

			dbMovieOp.closeDBConnection();
			System.out.println("SHOW DETAILS POPULATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while populating show details");
			logg.log(Level.SEVERE, "Error while populating show details ::: ", e);
		}
	}
}
