 package spotify;

 import java.io.*;
 import java.net.*;
 import java.util.Base64;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.JsonNode;
 import io.github.cdimascio.dotenv.Dotenv;

 public class SpotifyAuthManager {
	 private static final Dotenv dotenv = Dotenv.load();
	 private static final String clientId = dotenv.get("SPOTIFY_CLIENT_ID");
	 private static final String clientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");

     private static String accessToken;
     private static long tokenExpirationTime;

     public static String getAccessToken() {
         if (accessToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
             refreshAccessToken();
         }
         return accessToken;
     }

     private static void refreshAccessToken() {
         try {
             if (clientId == null || clientSecret == null) {
                 throw new RuntimeException("❗ Missing Spotify credentials.");
             }

             String query = "https://accounts.spotify.com/api/token";
             URI uri = URI.create(query);
             HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
             conn.setRequestMethod("POST");
             conn.setDoOutput(true);

             String encodedCredentials = Base64.getEncoder()
                     .encodeToString((clientId + ":" + clientSecret).getBytes());
             conn.setRequestProperty("Authorization", "Basic " + encodedCredentials);
             conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

             try (OutputStream os = conn.getOutputStream()) {
                 os.write("grant_type=client_credentials".getBytes());
             }

             int responseCode = conn.getResponseCode();
             if (responseCode != 200) {
                 System.err.println("❌ Spotify Auth failed! HTTP " + responseCode);
                 try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                     err.lines().forEach(System.err::println);
                 }
                 return;
             }

             BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuilder response = new StringBuilder();
             String line;
             while ((line = in.readLine()) != null) response.append(line);
             in.close();

             ObjectMapper mapper = new ObjectMapper();
             JsonNode json = mapper.readTree(response.toString());

             accessToken = json.get("access_token").asText();
             int expiresIn = json.get("expires_in").asInt();
             tokenExpirationTime = System.currentTimeMillis() + (expiresIn - 60) * 1000;

         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
