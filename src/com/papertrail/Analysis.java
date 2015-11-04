package com.papertrail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@Path("/analysis")
public class Analysis {

	@GET
	public String analysis(
			@FormParam("title") String title,
			@FormParam("keywords") String keywords,
			@FormParam("summary") String summary) throws SQLException, JSONException {		

		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
		JSONObject sqldb;
		JSONObject credentials;
		String url = "";
		String user = "";
		String pswd = "";
		if (VCAP_SERVICES != null) {
			try {
				JSONObject data = new JSONObject(VCAP_SERVICES);
				sqldb = (JSONObject) data.get("sqldb");
				credentials = (JSONObject) sqldb.get("credentials");
				url = credentials.getString("jdbcurl");
				user = credentials.getString("username");
				pswd = credentials.getString("password");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, pswd);
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		String statement = "SELECT \"authors\",\"categories\",\"title\"" 
				+ " FROM \"USER09041\".\"abstracts\""
				+ " WHERE \"categories\" LIKE '%cs.CV%'";
		Statement stmt = conn.createStatement();
		stmt.execute(statement);
		ResultSet rs = stmt.getResultSet();

		JSONArray data = new JSONArray();
		while(rs.next() ) {
			JSONObject row = new JSONObject();
			JSONArray authors = new JSONArray(rs.getString(1));
			JSONObject author = (JSONObject) authors.get(0);
			row.put("authors", author.getString("fullname"));
			row.put("categories", rs.getString(2));
			row.put("title", rs.getString(3));
			data.put(row);
		}
		return data.toString();
	}
}
