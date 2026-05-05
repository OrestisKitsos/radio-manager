package last_fm;

import java.io.*;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import io.github.cdimascio.dotenv.Dotenv;

public class LastFmAlbum {
	private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("LASTFM_API_KEY");
	private static final String API_URL = "http://ws.audioscrobbler.com/2.0/";

	static {
	    if (API_KEY == null || API_KEY.isEmpty()) {
	        throw new RuntimeException("Missing LASTFM_API_KEY in .env file.");
	    }
	}
	
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

    // --- Search albums by genre ---
    public static List<JsonNode> searchByGenre(String genresInput, boolean printResults) {
        try {
            String[] genres = genresInput.split(",");
            Set<String> seen = new HashSet<>();
            List<JsonNode> combinedAlbums = new ArrayList<>();

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

                // Step 2: For each artist, fetch their top albums
                for (JsonNode artist : artists) {
                    String artistName = artist.path("name").asText();

                    String albumUrl = API_URL + "?method=artist.gettopalbums&artist=" + URLEncoder.encode(artistName, "UTF-8")
                            + "&autocorrect=1&limit=3&api_key=" + API_KEY + "&format=json";
                    JsonNode albumRoot = getResponse(albumUrl);
                    JsonNode albums = albumRoot.path("topalbums").path("album");

                    if (!albums.isArray()) continue;

                    for (JsonNode album : albums) {
                        String albumName = album.path("name").asText();
                        String key = (artistName + " - " + albumName).toLowerCase();

                        if (!seen.contains(key)) {
                            ((ObjectNode) album).put("artist_name", artistName);
                            ((ObjectNode) album).put("genre_tag", genre);
                            combinedAlbums.add(album);
                            seen.add(key);
                        }
                    }
                }
            }

            // Optional printing
            if (printResults && !combinedAlbums.isEmpty()) {
                int index = 1;
                for (JsonNode album : combinedAlbums) {
                    String title = album.path("name").asText("Unknown");
                    String artist = album.path("artist_name").asText("Unknown");
                    List<String> tags = getAlbumTags(title, artist);
                    System.out.printf("%d. %s - %s | Genres: %s%n",
                            index++, artist, title, tags.isEmpty() ? "-" : String.join(", ", tags));
                }
            }

            return combinedAlbums;

        } catch (Exception e) {
            System.err.println("Error in searchByGenre: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // --- Get tags for a specific album ---
    public static List<String> getAlbumTags(String albumName, String artistName) {
        try {
            String url = API_URL + "?method=album.gettoptags"
                    + "&artist=" + URLEncoder.encode(artistName, "UTF-8")
                    + "&album=" + URLEncoder.encode(albumName, "UTF-8")
                    + "&autocorrect=1"
                    + "&api_key=" + API_KEY + "&format=json";

            JsonNode root = getResponse(url);
            JsonNode tags = root.path("toptags").path("tag");

            List<String> tagList = new ArrayList<>();
            for (JsonNode tag : tags) tagList.add(tag.path("name").asText());
            return tagList;

        } catch (Exception e) {
            System.err.println("Error fetching album tags: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
