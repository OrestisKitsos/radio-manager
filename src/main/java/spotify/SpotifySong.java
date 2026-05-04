package spotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;

public class SpotifySong {

    private static final String API_URL = "https://api.spotify.com/v1/";

    private static HttpURLConnection createConnection(String endpoint) throws Exception {
        String token = SpotifyAuthManager.getAccessToken(); 
        URI uri = URI.create(endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestMethod("GET");
        return conn;
    }

    // Search Song 
    public static List<JsonNode> searchSong(String songName) {
        try {
            String query = API_URL + "search?q=" + songName.replace(" ", "%20") + "&type=track&limit=10";
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
            JsonNode songs = root.path("tracks").path("items");

            if (!songs.isArray() || songs.size() == 0) {
                System.out.println("No songs found.");
                return Collections.emptyList();
            }

            List<JsonNode> songList = new ArrayList<>();
            System.out.println("\n🎵 Found " + songs.size() + " songs:");
            for (int i = 0; i < songs.size(); i++) {
                JsonNode s = songs.get(i);
                songList.add(s);
                String title = s.path("name").asText();
                String artist = s.path("artists").get(0).path("name").asText();
                String album = s.path("album").path("name").asText();
                int durationMs = s.path("duration_ms").asInt();
                int minutes = durationMs / 60000;
                int seconds = (durationMs % 60000) / 1000;
                String releaseDate = s.path("album").path("release_date").asText();
                String year = releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : releaseDate;

                System.out.printf("%d. %s - %s (%s, %s) [%d:%02d]%n",
                        (i + 1), artist, title, album, year, minutes, seconds);
               
            }

            return songList;

        } catch (Exception e) {
            System.err.println("Error searching song: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Get Song Details 
    public static void getSongDetails(String songId) {
        try {
            // Fetch song info 
            String query = API_URL + "tracks/" + songId;
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
            JsonNode song = mapper.readTree(response.toString());

            String title = song.path("name").asText();
            String artistId = song.path("artists").get(0).path("id").asText();
            String artistName = song.path("artists").get(0).path("name").asText();
            String albumName = song.path("album").path("name").asText();
            Integer discNumber = song.path("disc_number").asInt();
            Integer trackNumber = song.path("track_number").asInt();
            String releaseDate = song.path("album").path("release_date").asText();
            boolean explicit = song.path("explicit").asBoolean();
            int durationMs = song.path("duration_ms").asInt();
            int minutes = durationMs / 60000;
            int seconds = (durationMs % 60000) / 1000;

            // Fetch artist genres 
            String artistUrl = API_URL + "artists/" + artistId;
            HttpURLConnection artistConn = createConnection(artistUrl);

            BufferedReader artistIn = new BufferedReader(new InputStreamReader(artistConn.getInputStream()));
            StringBuilder artistResponse = new StringBuilder();
            String artistLine;
            while ((artistLine = artistIn.readLine()) != null) artistResponse.append(artistLine);
            artistIn.close();

            JsonNode artistData = mapper.readTree(artistResponse.toString());
            List<String> genres = new ArrayList<>();
            artistData.path("genres").forEach(g -> genres.add(g.asText()));
            String genreString = genres.isEmpty() ? "" : String.join(", ", genres);
            
            // Adding hints
            if (title.toLowerCase().contains("remix")) genreString += ",remix";
            if (title.toLowerCase().contains("live")) genreString += ",live";
            if (title.toLowerCase().contains("acoustic")) genreString += ",acoustic";
            if (albumName.toLowerCase().contains("remix")) genreString += ",remix";
            if (albumName.toLowerCase().contains("live")) genreString += ",live";
            if (albumName.toLowerCase().contains("acoustic")) genreString += ",acoustic";


            // Print details 
            System.out.println("\n");
            System.out.println("🎶 Song Details");
            System.out.println("Title: " + title);
            System.out.println("Artist: " + artistName);
            System.out.println("Album: " + albumName);
            System.out.println("Disc Number: " + discNumber);
            System.out.println("Track Number: " + trackNumber);
            System.out.println("Genres: " + genreString);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Duration: " + minutes + " minutes, " + seconds + " seconds");
            System.out.println("Explicit: " + explicit);
           

        } catch (Exception e) {
            System.err.println("Error fetching song details: " + e.getMessage());
        }
    }
    
    // Display song info by name along with Last.fm tags
    public static void displaySongWithLastFmTags(String songName, List<String> lastFmTags) {
        try {
            // Search song on Spotify by name
            String query = API_URL + "search?q=" + URLEncoder.encode(songName, "UTF-8") + "&type=track&limit=1";
            HttpURLConnection conn = createConnection(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode track = root.path("tracks").path("items").get(0);

            if (track == null || track.isMissingNode()) {
                System.out.println("No matching song found for '" + songName + "'.");
                return;
            }

            // Extract basic info
            String title = track.path("name").asText("Unknown");
            String artistName = track.path("artists").get(0).path("name").asText("Unknown");
            String albumName = track.path("album").path("name").asText("Unknown");
            String releaseDate = track.path("album").path("release_date").asText("Unknown");
            int discNumber = track.path("disc_number").asInt(0);
            int trackNumber = track.path("track_number").asInt(0);
            boolean explicit = track.path("explicit").asBoolean(false);
            int durationMs = track.path("duration_ms").asInt(0);
            int minutes = durationMs / 60000;
            int seconds = (durationMs % 60000) / 1000;

            // Combine genres from Last.fm
            String genreString = lastFmTags.isEmpty() ? "-" : String.join(", ", lastFmTags);

            // Print details
            System.out.println("\n🎶 Song Details");
            System.out.println("Title: " + title);
            System.out.println("Artist: " + artistName);
            System.out.println("Album: " + albumName);
            System.out.println("Disc Number: " + discNumber);
            System.out.println("Track Number: " + trackNumber);
            System.out.println("Genres: " + genreString);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Duration: " + minutes + " minutes, " + seconds + " seconds");
            System.out.println("Explicit: " + explicit);

        } catch (Exception e) {
            System.err.println("Error displaying song info: " + e.getMessage());
        }
    }

    // Search songs by release decade
    public static List<JsonNode> searchByDecade(int startYear, int endYear) {
        List<JsonNode> results = new ArrayList<>();
        try {
            String query = API_URL + "search?q=year:" + startYear + "-" + endYear + "&type=track&limit=50";
            HttpURLConnection conn = createConnection(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode items = root.path("tracks").path("items");

            if (!items.isArray() || items.size() == 0) return results;

            // Ignore remasters, live, deluxe, reissues etc
            List<String> blacklist = Arrays.asList("remaster", "remastered", "live", "deluxe", "reissue");

            for (JsonNode track : items) {
                String albumName = track.path("album").path("name").asText("").toLowerCase();
                boolean skip = blacklist.stream().anyMatch(albumName::contains);
                if (skip) continue;

                results.add(track);
                if (results.size() >= 10) break;
            }

        } catch (Exception e) {
            System.err.println("Error in searchByDecade: " + e.getMessage());
        }
        return results;
    }
    
 // Get Song Details with Last.fm tags 
    public static void getSongDetailsWithTags(String songId, List<String> tags) {
        try {
            // Fetch song info
            String query = API_URL + "tracks/" + songId;
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
            JsonNode song = mapper.readTree(response.toString());

            String title = song.path("name").asText();
            String artistName = song.path("artists").get(0).path("name").asText();
            String albumName = song.path("album").path("name").asText();
            Integer discNumber = song.path("disc_number").asInt();
            Integer trackNumber = song.path("track_number").asInt();
            String releaseDate = song.path("album").path("release_date").asText();
            boolean explicit = song.path("explicit").asBoolean();
            int durationMs = song.path("duration_ms").asInt();
            int minutes = durationMs / 60000;
            int seconds = (durationMs % 60000) / 1000;

            String genreString = tags.isEmpty() ? "-" : String.join(", ", tags);

            System.out.println("\n");
            System.out.println("🎶 Song Details");
            System.out.println("Title: " + title);
            System.out.println("Artist: " + artistName);
            System.out.println("Album: " + albumName);
            System.out.println("Disc Number: " + discNumber);
            System.out.println("Track Number: " + trackNumber);
            System.out.println("Genres: " + genreString);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Duration: " + minutes + " minutes, " + seconds + " seconds");
            System.out.println("Explicit: " + explicit);

        } catch (Exception e) {
            System.err.println("Error displaying song details: " + e.getMessage());
        }
    }
}
