package api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TaddyApiClient {

    private String apiKey;
    private String userId;

    public TaddyApiClient(String apiKey, String userId) {
        this.apiKey = apiKey;
        this.userId = userId;
    }

    public List<podcast> searchPodcasts(String nameQuery) {
        String jsonResponse = searchPodcastRaw(nameQuery);
        if (jsonResponse == null) return new ArrayList<>();

        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (root.has("errors")) {
                System.err.println("ERRORI API TADDY: " + root.getAsJsonArray("errors").toString());
                return new ArrayList<>();
            }

            JsonObject data = root.getAsJsonObject("data");
            JsonElement seriesElement = data.get("getPodcastSeries");
            JsonElement searchElement = data.get("searchPodcasts");

            List<podcast> results = new ArrayList<>();

            if (seriesElement != null && !seriesElement.isJsonNull()) {
                results.add(parsePodcastFromObject(seriesElement.getAsJsonObject()));
            } else if (searchElement != null && !searchElement.isJsonNull()) {
                JsonArray searchArray = searchElement.getAsJsonArray();
                for (JsonElement el : searchArray) {
                    results.add(parsePodcastFromObject(el.getAsJsonObject()));
                }
            }

            return results;

        } catch (Exception e) {
            System.err.println("Errore parsing JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String searchPodcastRaw(String nameQuery) {
        String queryType = nameQuery.contains(" ") ? "getPodcastSeries(name: \"" : "searchPodcasts(term: \"";
        String graphQuery = "{ " + queryType + nameQuery.replace("\"", "") + "\") { uuid name description imageUrl } }";
        return executeQuery(graphQuery);
    }

    private String executeQuery(String query) {
        try {
            URL url = new URL("https://api.taddy.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-KEY", apiKey);
            conn.setRequestProperty("X-USER-ID", userId);
            conn.setDoOutput(true);

            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("query", query);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream()
            ));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private podcast parsePodcastFromObject(JsonObject obj) {
        podcast p = new podcast();
        p.setUuid(obj.has("uuid") && !obj.get("uuid").isJsonNull() ? obj.get("uuid").getAsString() : "");
        p.setName(obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "Sconosciuto");
        p.setDescription(obj.has("description") && !obj.get("description").isJsonNull() ? obj.get("description").getAsString() : "");
        p.setImageUrl(obj.has("imageUrl") && !obj.get("imageUrl").isJsonNull() ? obj.get("imageUrl").getAsString() : "");
        return p;
    }
}
