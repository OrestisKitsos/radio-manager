package spotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;

import last_fm.LastFmArtist;



public class SpotifyArtist {

    private static final String API_URL = "https://api.spotify.com/v1/";

    // Utility method to create connections with token from SpotifyAuthManager
    private static HttpURLConnection createConnection(String endpoint) throws Exception {
        String token = SpotifyAuthManager.getAccessToken(); 
        URI uri = URI.create(endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestMethod("GET");
        return conn;
    }

 // Search Artist 
    public static List<JsonNode> searchArtist(String artistName) {
        try {
            String query = API_URL + "search?q=" + artistName.replace(" ", "%20") + "&type=artist&limit=10";
            HttpURLConnection conn = createConnection(query);

            if (conn.getResponseCode() >= 400) {
                System.err.println("Error: " + conn.getResponseCode() + " - " + conn.getResponseMessage());
                return Collections.emptyList();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode artists = root.path("artists").path("items");

            if (!artists.isArray() || artists.size() == 0) {
                System.out.println("No artists found.");
                return Collections.emptyList();
            }

            List<JsonNode> artistList = new ArrayList<>();
            System.out.println("\n🎵 Found " + artists.size() + " artists:");
            for (int i = 0; i < artists.size(); i++) {
                JsonNode a = artists.get(i);
                artistList.add(a);
                String name = a.path("name").asText();
                
                
                List<String> genres = new ArrayList<>();
                a.path("genres").forEach(g -> genres.add(g.asText()));

                // Use Last.fm tags if available
                List<String> tags = LastFmArtist.getArtistTags(name);
                List<String> displayGenres = tags.isEmpty() ? genres : tags;

                System.out.printf("%d. %s  | Genres: %s%n",
                        (i + 1), name, displayGenres.isEmpty() ? "-" : String.join(", ", displayGenres));

            }

            return artistList;

        } catch (Exception e) {
            System.err.println("Error searching artist: " + e.getMessage());
            return Collections.emptyList();
        }
    }



 // Get Artist Details
    public static void getArtistDetails(String artistId) {
        try {
            String query = API_URL + "artists/" + artistId;
            HttpURLConnection conn = createConnection(query);

            if (conn.getResponseCode() >= 400) {
                System.err.println("Error: " + conn.getResponseCode() + " - " + conn.getResponseMessage());
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode artist = mapper.readTree(response.toString());
            String artistName = artist.path("name").asText();

            // Fetch Last.fm tags if available
            List<String> tags = LastFmArtist.getArtistTags(artistName);

            System.out.println("\n");
            System.out.println("🎤 Artist: " + artistName);
            System.out.println("🌍 Genres: " + (tags.isEmpty() ? artist.path("genres") : tags));

        } catch (Exception e) {
            System.err.println("Error fetching artist details: " + e.getMessage());
        }
    }

    // Fetch Top Tracks 
    public static List<JsonNode> fetchTopTracks(String artistId, boolean printResults) {
        List<JsonNode> trackList = new ArrayList<>();
        try {
            String query = API_URL + "artists/" + artistId + "/top-tracks?market=US";
            HttpURLConnection conn = createConnection(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode tracks = mapper.readTree(response.toString()).path("tracks");

            for (int i = 0; i < tracks.size(); i++) {
                trackList.add(tracks.get(i));
            }

            if (printResults) {
                System.out.println("\n🎶 Top Tracks:");
                for (int i = 0; i < Math.min(tracks.size(), 5); i++) {
                    JsonNode t = tracks.get(i);
                    System.out.printf("- %s (%s)%n",
                            t.get("name").asText(),
                            t.path("album").path("release_date").asText());
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching top tracks: " + e.getMessage());
        }
        return trackList;
    }

    
 // Fetch Top Albums 
    public static List<JsonNode> fetchTopAlbums(String artistId, boolean printResults) {
        List<JsonNode> albumList = new ArrayList<>();

        try {
            String query = API_URL + "artists/" + artistId + "/albums?include_groups=album&limit=50";
            HttpURLConnection conn = createConnection(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode albums = mapper.readTree(response.toString()).path("items");

            for (int i = 0; i < albums.size(); i++) {
                albumList.add(albums.get(i));
            }

            // Sort albums by release date descending (newest first)
            albumList.sort((a, b) -> {
                String dateA = a.path("release_date").asText("");
                String dateB = b.path("release_date").asText("");
                return dateB.compareTo(dateA);
            });

            if (printResults) {
                System.out.println("\n🔥 Top Albums:");
                for (int i = 0; i < Math.min(albumList.size(), 5); i++) {
                    JsonNode a = albumList.get(i);
                    System.out.printf("- %s (%s)%n",
                            a.path("name").asText(),
                            a.path("release_date").asText("-"));
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching top albums: " + e.getMessage());
        }

        return albumList;
    }


    public static void displayArtistWithLastFmTags(String artistName, List<String> lastFmTags) {
        try {
            // Searching artist on Spotify
            String query = API_URL + "search?q=" + URLEncoder.encode(artistName, "UTF-8") + "&type=artist&limit=1";
            HttpURLConnection conn = createConnection(query);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode artist = root.path("artists").path("items").get(0);
            String artistId = artist.path("id").asText();

            System.out.println("\n");
            System.out.println("🎤 Artist: " + artist.path("name").asText());
            System.out.println("🌍 Genres: " + lastFmTags);

            fetchTopTracks(artistId,true);
            fetchTopAlbums(artistId,true);

        } catch (Exception e) {
            System.err.println("Error displaying artist info: " + e.getMessage());
        }
    }
    
 // Get Spotify Artist ID by Name 
    public static String getArtistIdByName(String artistName) {
        try {
            String query = API_URL + "search?q=" + URLEncoder.encode(artistName, "UTF-8") + "&type=artist&limit=1";
            HttpURLConnection conn = createConnection(query);

            if (conn.getResponseCode() >= 400) {
                System.err.println("Error fetching artist ID: " + conn.getResponseCode() + " - " + conn.getResponseMessage());
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode artist = root.path("artists").path("items");

            if (!artist.isArray() || artist.size() == 0) {
                System.out.println("Artist not found on Spotify: " + artistName);
                return null;
            }

            return artist.get(0).path("id").asText();

        } catch (Exception e) {
            System.err.println("Error in getArtistIdByName: " + e.getMessage());
            return null;
        }
    }
}