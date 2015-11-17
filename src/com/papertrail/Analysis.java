package com.papertrail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@Path("/analysis")
public class Analysis {
	
	private String apikey = "";
	private String urlPattern = "http://gateway-a.watsonplatform.net/calls/text/TextGetRankedKeywords?apikey=%s&text=%s&outputMode=json";
	
	private String extractKeywords(String summary) throws IOException {
		
		// Create an AlchemyAPI object.
		summary = summary.replaceAll(" ", "%20");
		String alchemyUrl = String.format(urlPattern, apikey, summary);
		System.out.println("URL: " + alchemyUrl);

		URL url = new URL(alchemyUrl);
		URLConnection connection = url.openConnection();

		JSONArray keywords = null;
		try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			final JSONObject json = new JSONObject(response.toString());
			keywords = json.getJSONArray("keywords");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keywords.toString();
	}

	@POST
	public String analysis(
			@FormParam("title") String title,
			@FormParam("keywords") String keywords,
			@FormParam("summary") String summary) {		
		String response = "error";
		try {
			response = extractKeywords(summary);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

}