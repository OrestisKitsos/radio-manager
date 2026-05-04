package last_fm;

import java.io.*;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

public class LastFmSong {

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

    // --- Search songs by genre ---
    public static List<JsonNode> searchByGenre(String genresInput, boolean printResults) {
        try {
            String[] genres = genresInput.split(",");
            Set<String> seen = new HashSet<>();
            List<JsonNode> combinedSongs = new ArrayList<>();

            for (String genre : genres) {
                genre = genre.trim().toLowerCase();
                if (genre.isEmpty()) continue;

                // Step 1: Get top artists for the genre
                String artistUrl = API_URL + "?method=tag.gettopartists&tag=" + URLEncoder.encode(genre, "UTF-8")
                        + "&autocorrect=1&limit=5&api_key=" + API_KEY + "&format=json";
                JsonNode artistRoot = getResponse(artistUrl);
                JsonNode artists = artistRoot.path("topartists").path("artist");

                if (!artists.isArray() || artists.size() == 0) {
                    System.out.println("Error: genre '" + genre + "' not found or has no results.");
                    continue;
                }

                // Step 2: For each artist, fetch their top tracks
                for (JsonNode artist : artists) {
                    String artistName = artist.path("name").asText();

                    String trackUrl = API_URL + "?method=artist.gettoptracks&artist=" + URLEncoder.encode(artistName, "UTF-8")
                            + "&autocorrect=1&limit=3&api_key=" + API_KEY + "&format=json";
                    JsonNode trackRoot = getResponse(trackUrl);
                    JsonNode tracks = trackRoot.path("toptracks").path("track");

                    if (!tracks.isArray()) continue;

                    for (JsonNode track : tracks) {
                        String songName = track.path("name").asText();
                        String key = (artistName + " - " + songName).toLowerCase();

                        if (!seen.contains(key)) {
                            ((ObjectNode) track).put("artist_name", artistName);
                            ((ObjectNode) track).put("genre_tag", genre);
                            combinedSongs.add(track);
                            seen.add(key);
                        }
                    }
                }
            }

            // Optional printing
            if (printResults && !combinedSongs.isEmpty()) {
                int index = 1;
                for (JsonNode song : combinedSongs) {
                    String title = song.path("name").asText("Unknown");
                    String artist = song.path("artist_name").asText("Unknown");
                    List<String> tags = getSongTags(title, artist);
                    System.out.printf("%d. %s - %s | Genres: %s%n",
                            index++, artist, title, tags.isEmpty() ? "-" : String.join(", ", tags));
                }
            }

            return combinedSongs;

        } catch (Exception e) {
            System.err.println("Error in searchByGenre: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // --- Get tags for a specific song ---
    public static List<String> getSongTags(String songName, String artistName) {
        try {
            String url = API_URL + "?method=track.gettoptags"
                    + "&artist=" + URLEncoder.encode(artistName, "UTF-8")
                    + "&track=" + URLEncoder.encode(songName, "UTF-8")
                    + "&autocorrect=1"
                    + "&api_key=" + API_KEY + "&format=json";

            JsonNode root = getResponse(url);
            JsonNode tags = root.path("toptags").path("tag");

            List<String> tagList = new ArrayList<>();
            for (JsonNode tag : tags) tagList.add(tag.path("name").asText());
            return tagList;

        } catch (Exception e) {
            System.err.println("Error fetching song tags: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
