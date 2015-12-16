package com.papertrail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.db2.jcc.DB2SimpleDataSource;
import com.ibm.nosql.json.api.BasicDBList;
import com.ibm.nosql.json.api.BasicDBObject;
import com.ibm.nosql.json.util.JSON;

public class Utils {

	private String databaseHost;
	private int port;
	private String databaseName;
	private String user;
	private String password;

	// Processes DB access credentials for future use
	private boolean processVCAP() {
		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
		if (VCAP_SERVICES != null) {
			// parse the VCAP JSON structure
			BasicDBObject obj = (BasicDBObject) JSON.parse(VCAP_SERVICES);
			String thekey = null;
			Set<String> keys = obj.keySet();
			for (String eachkey : keys) {
				if (eachkey.toUpperCase().contains("SQLDB")) {
					thekey = eachkey;
				}
			}
			if (thekey == null) {
				return false;
			}
			BasicDBList list = (BasicDBList) obj.get(thekey);
			obj = (BasicDBObject) list.get("0");
			// parse all the credentials from the vcap env variable
			obj = (BasicDBObject) obj.get("credentials");
			databaseHost = (String) obj.get("host");
			databaseName = (String) obj.get("db");
			port = (int)obj.get("port");
			user = (String) obj.get("username");
			password = (String) obj.get("password");
		} else {
			return false;
		}
		return true;
	}
	
	// Returns the trend of a given term according to the dataset.
	protected JSONObject queryTerm(String term) {
		JSONObject trend = new JSONObject();
		if (processVCAP()) {
			// Connect to the Database
			Connection con = null;
			try {
				System.out.println("Connecting to the database");
				DB2SimpleDataSource dataSource = new DB2SimpleDataSource();
				dataSource.setServerName(databaseHost);
				dataSource.setPortNumber(port);
				dataSource.setDatabaseName(databaseName);
				dataSource.setUser(user);
				dataSource.setPassword (password);
				dataSource.setDriverType(4);
				con=dataSource.getConnection();
				con.setAutoCommit(false);
			} catch (SQLException e) { 
				System.out.println("Failed connection to DB");
			} 
			try {
				Statement stmt = con.createStatement();
				String sqlStatement = "SELECT COUNT, KEYWORD, YEAR FROM \"USER11556\".\"kw_trend\" WHERE KEYWORD LIKE \'%" + term + "%\'";
				ResultSet rs = stmt.executeQuery(sqlStatement);
	
				// Process the result set
				int count;
				String year;
				while (rs.next()) {
					count = Integer.valueOf(rs.getObject(1).toString());
					year = rs.getObject(3).toString();
					System.out.println("Found:" + count + " " +  rs.getObject(2).toString() + " "+ year);
					try {
						if(trend.has(year)) {
							count = count + trend.getInt(year);
						}
						trend.put(year, count);
					} catch (JSONException e) { System.out.println(e);}
				}
				rs.close();
				try {
					stmt.close();
					con.commit();
					con.close();
		
				} catch (SQLException e) {
					System.out.println("SQL Exception: " + e);
				}

			} catch (SQLException e) {
				System.out.println(e);
			}
		}
		return trend;
	}

}
