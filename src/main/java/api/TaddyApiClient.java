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
                System.err.println("❌ ERRORI API TADDY: " + root.getAsJsonArray("errors").toString());
                return new ArrayList<>();
            }

            if (!root.has("data") || root.get("data").isJsonNull()) return new ArrayList<>();

            JsonObject data = root.getAsJsonObject("data");

            // ✅ Con getPodcastSeries, il risultato è un singolo oggetto o nullo
            if (data.get("getPodcastSeries").isJsonNull()) {
                System.out.println("Nessun podcast trovato con questo nome.");
                return new ArrayList<>();
            }

            JsonObject podcastObj = data.getAsJsonObject("getPodcastSeries");
            List<podcast> results = new ArrayList<>();

            podcast p = new podcast();
            p.setUuid(podcastObj.has("uuid") ? podcastObj.get("uuid").getAsString() : "");
            p.setName(podcastObj.has("name") ? podcastObj.get("name").getAsString() : "Sconosciuto");
            p.setDescription(podcastObj.has("description") ? podcastObj.get("description").getAsString() : "");

            results.add(p);
            return results;

        } catch (Exception e) {
            System.err.println("❌ Errore parsing: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String searchPodcastRaw(String nameQuery) {
        try {
            URL url = new URL("https://api.taddy.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-KEY", apiKey);
            conn.setRequestProperty("X-USER-ID", userId);
            conn.setDoOutput(true);

            // ✅ QUERY BASATA SULLA DOC CHE HAI TROVATO
            // Usiamo getPodcastSeries(name: "...")
            String query = "{ getPodcastSeries(name: \"" + nameQuery.replace("\"", "") + "\") { uuid name description } }";

            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("query", query);

            String finalJson = jsonBody.toString();
            System.out.println("DEBUG - Invio query: " + finalJson);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(finalJson.getBytes(StandardCharsets.UTF_8));
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
}