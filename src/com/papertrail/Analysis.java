package com.papertrail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	
	private String extractKeywords(String summary) throws IOException, JSONException {
		
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
		
		String query = "";
		for (int i = 0; i < Math.min(5, keywords.length()); i++) {
			  query = query + keywords.getJSONObject(i).get("text");
			  if (i != Math.min(5, keywords.length()) - 1)
					  query = query + ",";
		}
		String html = "<script type=\"text/javascript\" src=\"" +
				      "//www.google.com/trends/embed.js?hl=en-US&q=" +
					  query + "&cmpt=q&tz=Etc/GMT%2B5&tz=Etc/GMT%2B5&content=1&cid=TIMESERIES_GRAPH_0&export=5&w=500&h=330\"></script>";
		PrintWriter writer = new PrintWriter("trend.html", "UTF-8");
		writer.println(html);
		writer.close();
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
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

}
