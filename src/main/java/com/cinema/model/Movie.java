package com.cinema.model;

import com.cinema.util.DBOperations;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Movie {

	final Logger logg = Logger.getLogger(this.getClass().getName());
	final DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");
	
	private Long movieId;
	private String name;
	private double duration;
	private String genre;
	private static final String TABLE_NAME = "Movie";
	private static final String TABLE_PKCOL = "MOVIE_ID";
	private static final String[] TABLE_COLUMNS = {"NAME", "DURATION", "GENRE"};

	public Movie(Long movieId, String name, float duration, String genre) {
		this.movieId = movieId;
		this.name = name;
		this.duration = duration;
		this.genre = genre;
	}

	public Movie(Long movieId) {
		this.movieId = movieId;
	}

	public Movie(String name, float duration, String genre) {
		this.name = name;
		this.duration = duration;
		this.genre = genre;
	}

	public Long getMovieId() {
		return movieId;
	}

	public void setMovieId(Long movieId) {
		this.movieId = movieId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public void displayMovieDetails() {
		System.out.println("The movie you are about to see is " +name+ " of duration "+duration+". It is a "+genre+" genre");
	}
	
	public void addMovieDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {name, String.valueOf(duration), genre};
				Long resultMovieId = dbMovieOp.insertRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), updateValues);
				setMovieId(resultMovieId);
			}

			dbMovieOp.closeDBConnection();
			System.out.println("MOVIE ADDED SUCCESSFULLY!!!");
		} catch(Exception e) {
			System.out.println("Error while adding movie details");
			logg.log(Level.SEVERE, "Error while adding movie details ::: ", e);
		}
	}
	
	public void updateMovieDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			if(isConnSuccess) {
				String[] updateValues = {name, String.valueOf(duration), genre};
				dbMovieOp.updateRows(TABLE_NAME, TABLE_COLUMNS, updateValues, TABLE_PKCOL, String.valueOf(movieId));
			}

			dbMovieOp.closeDBConnection();
			System.out.println("MOVIE UPDATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while updating movie details");
			logg.log(Level.SEVERE, "Error while updating movie details ::: ", e);
		}
	}
	
	public void populateMovieDetails() {
		try {
			DBOperations dbMovieOp = DBOperations.getInstance();
			Boolean isConnSuccess = dbMovieOp.establishDBConnection();

			JSONArray jArr = null;
			if(isConnSuccess) {
				jArr = dbMovieOp.viewRows(TABLE_NAME, String.join(",", TABLE_COLUMNS), TABLE_PKCOL, String.valueOf(movieId));
			}

			if(jArr!=null && jArr.length()>0) {
				JSONObject jObj = jArr.getJSONObject(0);
				name = jObj.getString(TABLE_COLUMNS[0]);
				duration = Double.parseDouble(twoDecimalFormat.format(jObj.getDouble(TABLE_COLUMNS[1])));
				genre = jObj.getString(TABLE_COLUMNS[2]);
			} else {
				logg.log(Level.SEVERE, "Data not found!");
				throw new Exception("Data not found");
			}

			dbMovieOp.closeDBConnection();
			System.out.println("MOVIE DETAILS POPULATED SUCCESSFULLY!!!");
		} catch (Exception e) {
			System.out.println("Error while populating movie details");
			logg.log(Level.SEVERE, "Error while populating movie details ::: ", e);
		}
	}
}
