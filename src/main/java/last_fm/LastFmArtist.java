package last_fm;

import java.io.*;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

public class LastFmArtist {

    private static final String API_KEY = "958ae828c6ccebb1a6568ed258ea4a62";
    private static final String API_URL = "http://ws.audioscrobbler.com/2.0/";

    private static JsonNode getResponse(String url) throws Exception {
    	URI uri = URI.create(url);
    	HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.toString());
    }

    // Searching artist based on genre/tag
    public static List<JsonNode> searchByGenre(String genresInput, boolean printResults) {
        try {
            String[] genres = genresInput.split(",");
            Set<String> seen = new HashSet<>();
            List<JsonNode> combinedArtists = new ArrayList<>();

            for (String genre : genres) {
                genre = genre.trim().toLowerCase();
                if (genre.isEmpty()) continue;

                String url = API_URL + "?method=tag.gettopartists&tag=" + URLEncoder.encode(genre, "UTF-8")
                        + "&limit=10&api_key=" + API_KEY + "&format=json";

                JsonNode root = getResponse(url);
                JsonNode artists = root.path("topartists").path("artist");

                if (!artists.isArray() || artists.size() == 0) {
                    System.out.println("Error: genre '" + genre + "' not found.");
                    continue;
                }

                for (JsonNode artist : artists) {
                    String name = artist.path("name").asText();
                    if (!seen.contains(name)) {
                        ((ObjectNode) artist).put("genre_tag", genre);
                        combinedArtists.add(artist);
                        seen.add(name);
                    }
                }
            }

            // Print only if requested
            if (printResults) {
                int index = 1;
                for (JsonNode artist : combinedArtists) {
                    String name = artist.path("name").asText("Unknown");
                    List<String> tags = LastFmArtist.getArtistTags(name);
                    System.out.printf("%d. %s | Genres: %s%n", index++, name, String.join(", ", tags));
                }
            }

            return combinedArtists;

        } catch (Exception e) {
            System.err.println("Error in searchByGenre: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    // Return tags for the chosen artist
    public static List<String> getArtistTags(String artistName) {
        try {
            String url = API_URL + "?method=artist.gettoptags&artist=" + URLEncoder.encode(artistName, "UTF-8")
                    + "&api_key=" + API_KEY + "&format=json";

            JsonNode root = getResponse(url);
            JsonNode tags = root.path("toptags").path("tag");

            List<String> tagList = new ArrayList<>();
            for (JsonNode tag : tags) {
                tagList.add(tag.path("name").asText());
            }

            return tagList;

        } catch (Exception e) {
            System.err.println("Error fetching artist tags: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
}
