package com.cinema.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBOperations implements Serializable {

	Connection connectDB = null;
	String databaseURL = "jdbc:mysql://localhost:3306/cinema";
	String username = "movie";
	String password = "test";
	Logger logg = Logger.getLogger(this.getClass().getName());

	public static DBOperations dbOperations = null;

	private DBOperations() {}

	public static DBOperations getInstance() {
		if(dbOperations==null) {
			dbOperations = new DBOperations();
		}
		return dbOperations;
	}
	
	public Boolean establishDBConnection() {
		try	{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch(ClassNotFoundException c)	{
			System.out.println("Error while initializing driver");
			logg.log(Level.SEVERE, "Error while initializing driver ::: ", c);
		}
		try {
			connectDB = DriverManager.getConnection(databaseURL, username, password);
			if (connectDB != null) {
				System.out.println("Database Connected!");
				return Boolean.TRUE;
			}
		} catch (SQLException ex) {
			System.out.println("Error while connecting Database");
			logg.log(Level.SEVERE, "Error while connecting Database ::: ", ex);
		}
		return Boolean.FALSE;
	}
	
	public void closeDBConnection() {
		if (connectDB != null) {
			try {
				connectDB.close();
			} catch (SQLException e) {
				System.out.println("Error in closing connection");
				logg.log(Level.SEVERE, "Error in closing connection ::: ", e);
			}
		}
	}
	
	/**
	* This method is used to insert rows to table
	* @param tableName Name of the table
	* @param columns Column names (with comma separated)
	* @param values Values (with comma separated and each value inside quotes) ***/
	public Long insertRows(String tableName, String columns, String[] values) throws Exception {

		Long primKeyId = null;
		
		// This is to avoid sql injections
		StringBuilder valPlaceHolder = new StringBuilder();
		int columnNum = values.length;
		for(int i=0;i<columnNum;i++) {
			valPlaceHolder.append("?");
			if(i!=columnNum-1) {
				valPlaceHolder.append(",");
			}
		}
				
		String insertQuery = "INSERT into `"+tableName+"`("+columns+") VALUES("+valPlaceHolder+")";
		PreparedStatement preStmtDB = connectDB.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
		
		for(int i=0;i<columnNum;i++) {
			preStmtDB.setObject(i+1, values[i]);
		}
		System.out.println("PREPARED STATEMENT::: "+preStmtDB);
		preStmtDB.executeUpdate();
		
		ResultSet rs = preStmtDB.getGeneratedKeys();
		if(rs.next()) {
			primKeyId = rs.getLong(1);
		}
		return primKeyId;
		
	}

	/**
	 * This method is used to insert rows to table
	 * @param tableName Name of the table
	 * @param updateColumns Column name to be updated
	 * @param updateValues Value to be updated
	 * @param criteriaCol Column name to be updated for reference
	 * @param criteriaVal Column value to be updated for reference ***/
	public void updateRows(String tableName, String[] updateColumns, String[] updateValues, String criteriaCol, String criteriaVal) throws Exception {
		
		String setQuery = "";
		for(String colName : updateColumns) {
			setQuery = colName+"=?,";
		}
		setQuery = setQuery.substring(0, setQuery.length() - 1);
		
		String updateQuery = "UPDATE `"+tableName+"` SET "+setQuery+" WHERE "+criteriaCol+"=?";
		PreparedStatement preStmtDB = connectDB.prepareStatement(updateQuery);
		
		int i=1;
		for(String colVal : updateValues) {
			preStmtDB.setObject(i, colVal);
			i++;
		}
		preStmtDB.setObject(i, criteriaVal);

		System.out.println("PREPARED STATEMENT::: "+preStmtDB);
		int affectedRows = preStmtDB.executeUpdate();

		System.out.println("Number of rows updated::: "+affectedRows);

	}

	/**
	 * This method is used to insert rows to table
	 * @param tableName Name of the table
	 * @param displayColNames Column names (with comma separated) or "*" for All
	 * @param criteriaCol Column name to be updated for reference
	 * @param criteriaVal Column value to be updated for reference ***/
	public JSONArray viewRows(String tableName, String displayColNames, String criteriaCol, String criteriaVal) throws Exception {

		JSONArray jarr = new JSONArray();
		
		String selectQuery = "SELECT "+displayColNames+" from `"+tableName+"` WHERE "+criteriaCol+"=?";
		PreparedStatement preStmtDB = connectDB.prepareStatement(selectQuery);

		preStmtDB.setObject(1, criteriaVal);
		System.out.println("PREPARED STATEMENT::: "+preStmtDB);
		ResultSet rs = preStmtDB.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();

		while(rs.next()) {
			JSONObject  jobj = new JSONObject();
			int colCount = rsmd.getColumnCount();
			for(int i=0; i<colCount; i++) {
				jobj.put(rsmd.getColumnName(i+1), rs.getString(i+1));
			}
			jarr.put(jobj);
		}
		
		return jarr;

	}
}
