package spotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;

public class SpotifyAlbum {

    private static final String API_URL = "https://api.spotify.com/v1/";

    private static HttpURLConnection createConnection(String endpoint) throws Exception {
        String token = SpotifyAuthManager.getAccessToken(); 
        URI uri = URI.create(endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestMethod("GET");
        return conn;
    }

    // Search Album 
    public static List<JsonNode> searchAlbum(String albumName) {
        try {
            String query = API_URL + "search?q=" + URLEncoder.encode(albumName, "UTF-8") + "&type=album&limit=50";
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
            JsonNode albums = root.path("albums").path("items");

            if (!albums.isArray() || albums.size() == 0) {
                System.out.println("No albums found.");
                return Collections.emptyList();
            }

            
            List<JsonNode> albumList = new ArrayList<>();
            for (int i = 0; i < albums.size() && albumList.size() < 10; i++) {
                JsonNode a = albums.get(i);
                String type = a.path("album_type").asText("");
                // Ignore if single
                if ("single".equalsIgnoreCase(type)) continue;
                albumList.add(a);
            }

            if (albumList.isEmpty()) {
                System.out.println("No non-single albums found.");
                return Collections.emptyList();
            }
            
            
            System.out.println("\n🎵 Found " + albumList.size() + " albums:");
            for (int j = 0; j < albumList.size(); j++) {
                JsonNode a = albumList.get(j);
                String title = a.path("name").asText("-");
                String artist = a.path("artists").isArray() && a.path("artists").size() > 0
                        ? a.path("artists").get(0).path("name").asText("-")
                        : "-";
                String releaseDate = a.path("release_date").asText("");
                String year = releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : releaseDate;
                String albumType = a.path("album_type").asText("-");
                System.out.printf("%d. %s - %s [%s,%s]%n", j + 1, artist, title, albumType, year);
            }

            return albumList;

        } catch (Exception e) {
            System.err.println("Error searching album: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    // Get Album Details
    public static void getAlbumDetails(String albumId) {
        try {
            // Fetch album info 
            String query = API_URL + "albums/" + albumId;
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
            JsonNode album = mapper.readTree(response.toString());

            String title = album.path("name").asText();
            String album_type = album.path("album_type").asText();
            String artistId = album.path("artists").get(0).path("id").asText();
            String artistName = album.path("artists").get(0).path("name").asText();
            String label = album.path("label").asText();
            String release_date = album.path("release_date").asText();
            int total_tracks = album.path("total_tracks").asInt();

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

            // Fetch album tracks
            String tracksUrl = API_URL + "albums/" + albumId + "/tracks?limit=50";
            HttpURLConnection tracksConn = createConnection(tracksUrl);
            BufferedReader tracksIn = new BufferedReader(new InputStreamReader(tracksConn.getInputStream()));
            StringBuilder tracksResponse = new StringBuilder();
            String tracksLine;
            while ((tracksLine = tracksIn.readLine()) != null) tracksResponse.append(tracksLine);
            tracksIn.close();

            JsonNode tracksRoot = mapper.readTree(tracksResponse.toString());
            JsonNode tracks = tracksRoot.path("items");

            int totalDurationMs = 0;
            boolean hasRemix = false, hasLive = false, hasAcoustic = false;

            System.out.println("\n🎧 Tracklist:");
            for (int i = 0; i < tracks.size(); i++) {
                JsonNode track = tracks.get(i);
                String trackName = track.path("name").asText();
                int durationMs = track.path("duration_ms").asInt();
                totalDurationMs += durationMs;

                int minutes = durationMs / 60000;
                int seconds = (durationMs % 60000) / 1000;

                System.out.printf("%2d. %s (%d:%02d)%n", (i + 1), trackName, minutes, seconds);

                String lower = trackName.toLowerCase();
                if (title.toLowerCase().contains("remix") || lower.contains("remix")) hasRemix = true;
                if (title.toLowerCase().contains("live") || lower.contains("live")) hasLive = true;
                if (title.toLowerCase().contains("acoustic") || lower.contains("acoustic")) hasAcoustic = true;
            }

            // Total Duration
            int totalSeconds = totalDurationMs / 1000;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;

            // Adding hints
            List<String> hints = new ArrayList<>();
            if (hasRemix) hints.add("remix");
            if (hasLive) hints.add("live");
            if (hasAcoustic) hints.add("acoustic");

            if (!hints.isEmpty()) {
                album_type += "," + String.join(",", hints);
            }

            // Print details 
            System.out.println("\n");
            System.out.println("🎶 Album Details");
            System.out.println("Title: " + title);
            System.out.println("Album Type: " + album_type);
            System.out.println("Artist: " + artistName);
            System.out.println("Total Tracks: " + total_tracks);
            System.out.println("Genre: " + genreString);
            System.out.println("Release Date: " + release_date);
            if (hours > 0)
                System.out.println("Duration: " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds");
            else
                System.out.println("Duration: " + minutes + " minutes, " + seconds + " seconds");
            System.out.println("Label: " + label);
    

        } catch (Exception e) {
            System.err.println("Error fetching album details: " + e.getMessage());
        }
    }

 // Get only the Tracklist (for SearchSongs)
    public static List<JsonNode> getAlbumTracklist(String albumId) {
        List<JsonNode> tracksList = new ArrayList<>();
        try {
            String tracksUrl = API_URL + "albums/" + albumId + "/tracks?limit=50";
            HttpURLConnection tracksConn = createConnection(tracksUrl);

            if (tracksConn.getResponseCode() >= 400) {
                System.err.println("❌ Error fetching tracklist: " + tracksConn.getResponseCode() + " - " + tracksConn.getResponseMessage());
                return Collections.emptyList();
            }

            BufferedReader tracksIn = new BufferedReader(new InputStreamReader(tracksConn.getInputStream()));
            StringBuilder tracksResponse = new StringBuilder();
            String tracksLine;
            while ((tracksLine = tracksIn.readLine()) != null) tracksResponse.append(tracksLine);
            tracksIn.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode tracksRoot = mapper.readTree(tracksResponse.toString());
            JsonNode tracks = tracksRoot.path("items");

            if (!tracks.isArray() || tracks.size() == 0) {
                System.out.println("No tracks found for this album.");
                return Collections.emptyList();
            }

            System.out.println("\n🎧 Tracklist:");
            for (int i = 0; i < tracks.size(); i++) {
                JsonNode track = tracks.get(i);
                String trackName = track.path("name").asText();
                int durationMs = track.path("duration_ms").asInt();
                int minutes = durationMs / 60000;
                int seconds = (durationMs % 60000) / 1000;
                System.out.printf("%2d. %s (%d:%02d)%n", (i + 1), trackName, minutes, seconds);
                tracksList.add(track);
            }

            return tracksList;

        } catch (Exception e) {
            System.err.println("Error fetching album tracklist: " + e.getMessage());
            return Collections.emptyList();
        }
    }

 //  Get Album Details with Last.fm tags 
    public static void getAlbumDetailsWithTags(String albumId, List<String> tags) {
        try {
            String query = API_URL + "albums/" + albumId;
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
            JsonNode album = mapper.readTree(response.toString());

            String title = album.path("name").asText();
            String albumType = album.path("album_type").asText();
            String artistName = album.path("artists").get(0).path("name").asText();
            String label = album.path("label").asText();
            String releaseDate = album.path("release_date").asText();
            int totalTracks = album.path("total_tracks").asInt();

            // Fetch album tracks
            String tracksUrl = API_URL + "albums/" + albumId + "/tracks?limit=50";
            HttpURLConnection tracksConn = createConnection(tracksUrl);
            BufferedReader tracksIn = new BufferedReader(new InputStreamReader(tracksConn.getInputStream()));
            StringBuilder tracksResponse = new StringBuilder();
            while ((line = tracksIn.readLine()) != null) tracksResponse.append(line);
            tracksIn.close();

            JsonNode tracksRoot = mapper.readTree(tracksResponse.toString());
            JsonNode tracks = tracksRoot.path("items");

            int totalDurationMs = 0;
            System.out.println("\n🎧 Tracklist:");
            for (int i = 0; i < tracks.size(); i++) {
                JsonNode track = tracks.get(i);
                String trackName = track.path("name").asText();
                int durationMs = track.path("duration_ms").asInt();
                totalDurationMs += durationMs;
                int minutes = durationMs / 60000;
                int seconds = (durationMs % 60000) / 1000;
                System.out.printf("%2d. %s (%d:%02d)%n", (i + 1), trackName, minutes, seconds);
            }

            int totalSeconds = totalDurationMs / 1000;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            String genreString = tags.isEmpty() ? "-" : String.join(", ", tags);

            System.out.println("\n🎶 Album Details");
            System.out.println("Title: " + title);
            System.out.println("Album Type: " + albumType);
            System.out.println("Artist: " + artistName);
            System.out.println("Total Tracks: " + totalTracks);
            System.out.println("Genre: " + genreString);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Duration: " + minutes + " minutes, " + seconds + " seconds");
            System.out.println("Label: " + label);

        } catch (Exception e) {
            System.err.println("Error fetching album details with tags: " + e.getMessage());
        }
    }

 // Display album info by name along with Last.fm tags
    public static void displayAlbumWithLastFmTags(String albumName, List<String> lastFmTags) {
        try {
            String query = API_URL + "search?q=" + URLEncoder.encode(albumName, "UTF-8") + "&type=album&limit=1";
            HttpURLConnection conn = createConnection(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode album = root.path("albums").path("items").get(0);

            if (album == null || album.isMissingNode()) {
                System.out.println(" No matching album found for '" + albumName + "'.");
                return;
            }

            String title = album.path("name").asText("Unknown");
            String albumType = album.path("album_type").asText("Unknown");
            String artistName = album.path("artists").get(0).path("name").asText("Unknown");
            String releaseDate = album.path("release_date").asText("Unknown");
            String label = album.path("label").asText("Unknown");
            int totalTracks = album.path("total_tracks").asInt(0);

            String genreString = lastFmTags.isEmpty() ? "-" : String.join(", ", lastFmTags);

            System.out.println("\n🎶 Album Details");
            System.out.println("Title: " + title);
            System.out.println("Album Type: " + albumType);
            System.out.println("Artist: " + artistName);
            System.out.println("Total Tracks: " + totalTracks);
            System.out.println("Genres: " + genreString);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Label: " + label);

        } catch (Exception e) {
            System.err.println("Error displaying album info: " + e.getMessage());
        }
    }

 // Search albums by release decade
    public static List<JsonNode> searchByDecade(int startYear, int endYear) {
        List<JsonNode> results = new ArrayList<>();
        try {
            String query = API_URL + "search?q=year:" + startYear + "-" + endYear + "&type=album&limit=50";
            HttpURLConnection conn = createConnection(query);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode items = root.path("albums").path("items");

            if (!items.isArray() || items.size() == 0) return results;

            for (JsonNode album : items) {
                String type = album.path("album_type").asText("");
                if ("single".equalsIgnoreCase(type)) continue;
                results.add(album);
                if (results.size() >= 10) break;
            }

        } catch (Exception e) {
            System.err.println("Error in searchByDecade: " + e.getMessage());
        }
        return results;
    }
}
