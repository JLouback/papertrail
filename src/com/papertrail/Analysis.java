package com.papertrail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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

	private String apikey = System.getenv("ALCHEMY_KEY");
	private String urlPattern = "http://gateway-a.watsonplatform.net/calls/text/TextGetRankedKeywords?apikey=%s&text=%s&outputMode=json";
	
	// Standardize keywords
	private String clean(String word) {
		return word.toLowerCase().trim();
	}

	// Processes a summary (abstract) with the Alchemy Keyword Extractor
	private JSONArray alchemyKeywords(String summary) throws IOException, JSONException {
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
			e.printStackTrace();
		}
		return keywords;
	}
	
	// Full processing of summary, including trend analysis and citation recommendation
	private String process(String summary) {
		Utils utils = new Utils();
		JSONArray trends = new JSONArray();
		String serverdata = summary + "||";
		try {
			JSONArray keywords = alchemyKeywords(summary);
			for (int i = 0; i < keywords.length(); i++) {
				if (keywords.getJSONObject(i).getDouble("relevance") < 0.6) {
					break;
				}
				JSONObject trend = utils.queryTerm(keywords.getJSONObject(i).getString("text"));

				JSONObject item = new JSONObject();
				String keyword = clean(keywords.getJSONObject(i).getString("text"));
				item.put("text", keyword);
				item.put("relevance", keywords.getJSONObject(i).getString("relevance"));
				item.put("trend", trend);
				serverdata += keyword + ":" +
							  keywords.getJSONObject(i).getString("relevance") + ";";
				trends.put(item);
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		
		// Send data to server for citation prediction
		String host = System.getenv("SERVER_HOST");
		int port = Integer.valueOf(System.getenv("SERVER_PORT"));
		String citations;
		// Join data
		JSONObject analysis = new JSONObject();
        try {
            Socket clientSocket = new Socket(host, port);
            PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.println(serverdata);
            
            citations = inFromServer.readLine();
            analysis.put("trends", trends.toString());
			analysis.put("citations", citations);
            System.out.println("From server:");
            System.out.print(citations);
            inFromServer.close();
			clientSocket.close();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return analysis.toString();
	}

	@POST
	public String analysis(
			@FormParam("title") String title,
			@FormParam("keywords") String keywords,
			@FormParam("summary") String summary) {		
		String response = "error";
		response = process(summary);
		return response;
	}

}
