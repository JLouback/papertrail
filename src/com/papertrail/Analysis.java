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

	private String apikey = System.getenv("ALCHEMY_KEY");
	private String urlPattern = "http://gateway-a.watsonplatform.net/calls/text/TextGetRankedKeywords?apikey=%s&text=%s&outputMode=json";

	private JSONArray alchemyKeywords(String summary) throws IOException, JSONException {
		
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
		return keywords;
	}
	
	private String process(String summary) {
		Utils utils = new Utils();
		JSONArray processed = new JSONArray();
		try {
			JSONArray keywords = alchemyKeywords(summary);
			for (int i = 0; i < keywords.length(); i++) {
				if (keywords.getJSONObject(i).getDouble("relevance") < 0.65) {
					break;
				}
				JSONObject trend = utils.queryTerm(keywords.getJSONObject(i).getString("text"));
				System.out.println(trend.toString());
				JSONObject item = new JSONObject();
				item.put("text", keywords.getJSONObject(i).getString("text"));
				item.put("relevance", keywords.getJSONObject(i).getString("relevance"));
				item.put("trend", trend);
				processed.put(item);
			}
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return processed.toString();
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

	
	public static void main(String[] args) {
		Analysis an = new Analysis();
		System.out.println(an.process("An empirical study of human perception of halftoned images was conducted to determine which of five different halftoning algorithms generated the best images. The subjects viewed each of the 20 stimuli (halftoned images) at two distances, and although all images were more preferred at the far distance, the rating of the pictures was dependent upon the algorithm used in generation. Images containing a high level of detail were rated highest when halftoned by the neural network and the simulated annealing algorithms of [4], whereas pictures that had little detail and many smooth surfaces were rated highest under the Floyd-Steinberg model [3]."));
	}
}
