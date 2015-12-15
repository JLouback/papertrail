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
		String recommendations;
        try {
            Socket clientSocket = new Socket(host, port);
            PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer.println("test abs\n");
            recommendations = inFromServer.readLine();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //Fake for testing
        JSONArray citations = new JSONArray(); 
        JSONObject item = new JSONObject();
        JSONObject item2 = new JSONObject();
		try {
			item.put("title", "Model-Driven Data Acquisition in Sensor Networks.");
			item.put("author", "Amol Deshpande,Carlos Guestrin,Samuel Madden,Joseph M. Hellerstein,Wei Hong");
			item.put("year", "2004");
			item.put("summary", "Declarative queries are proving to be an attractive paradigm for ineracting with networks of wireless sensors. The metaphor that the sensornet is a database is problematic, however, because sensors do not exhaustively represent the data in the real world. In order to map the raw sensor readings onto physical reality, a model of that reality is required to complement the readings. In this paper, we enrich interactive sensor querying with statistical modeling techniques. We demonstrate that such models can help provide answers that are both more meaningful, and, by introducing approximations with probabilistic confidences, significantly more efficient to compute in both time and energy. Utilizing the combination of a model and live data acquisition raises the challenging optimization problem of selecting the best sensor readings to acquire, balancing the increase in the confidence of our answer against the communication and data acquisition costs in the network. We describe an exponential time algorithm for finding the optimal solution to this optimization problem, and a polynomial-time heuristic for identifying solutions that perform well in practice. We evaluate our approach on several real-world sensor-network data sets, taking into account the real measured data and communication quality, demonstrating that our model-based approach provides a high-fidelity representation of the real phenomena and leads to significant performance gains versus traditional data acquisition techniques.");
			item2.put("title", "Semantic Checking of Questions Expressed in Predicate Calculus Language.");
			item2.put("author", "Robert Demolombe");
			item2.put("year", "1979");
			item2.put("summary", "Not all predicate calculus WFF correspond to meaningful questions, In order to avoid this problem, +different authors have defined syntactically the WFF classes which are known to be significative. These restrictions are generally more severe than is necessary, and we have defined a much wider class of WFF : the evaluable formula. We prove that these WFF have a clearly defined sense. Moreover, we can easily test a formula to see if it is evaluable. Finally, we show how it is possible to deduce from a formula the conditions which have to be fulfilled by the predicate argument validity domains in order to obtain-answers which are not an empty set. We can thus reject questions which have a defined sense but which, in the context of a clearly determined application, cannot have an answer.");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		citations.put(item);
		citations.put(item2);
        
		// Join data
		JSONObject analysis = new JSONObject();
		try {
			analysis.put("trends", trends);
			analysis.put("citations", citations);
		} catch (JSONException e) {
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
