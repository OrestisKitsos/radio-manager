package options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

import last_fm.*;
import spotify.*;

public class ArtistOptions {

	// ===================== OPTION 1: BY NAME =====================
		public static void searchByName(Scanner scanner) {
			System.out.print("\nEnter artist name: ");
			String name = scanner.nextLine().trim();
			if (name.isEmpty()) {
				System.out.println("Error:Empty input. Please try again.");
				return;
			}

			List<JsonNode> artists = SpotifyArtist.searchArtist(name);
			if (artists.isEmpty()) return;

			chooseArtistFromList(scanner,artists);
		}

		// ===================== OPTION 2: BY GENRE =====================
		public static void searchByGenre(Scanner scanner) {
			while (true) {
				System.out.print("\nEnter genres or country (comma separated) or 0 to go back: ");
				String input = scanner.nextLine().trim();

				if (input.equals("0")) return;
				if (input.isEmpty()) {
					System.out.println("Error:Empty input.Please try again.");
					continue;
				}

				// Last.fm API call
				List<JsonNode> results = LastFmArtist.searchByGenre(input,true);
				if (results.isEmpty()) {
					System.out.println("Please try again.");
					continue;
				}

				// Artist selection
				System.out.print("\n➡️ Enter artist number (1–" + results.size() + ")" + " or 0 to go back: ");
				String idx = scanner.nextLine().trim();
				if (idx.equals("0")) return;

				try {
					int selection = Integer.parseInt(idx);
					if (selection < 1 || selection > results.size()) {
						System.out.println("Invalid choice.");
						continue;
					}

					JsonNode chosen = results.get(selection - 1);
					String artistName = chosen.path("name").asText();

					// Get tags (genres) from Last.fm
					List<String> tags = LastFmArtist.getArtistTags(artistName);

					// Get remaining details from Spotify
					System.out.println("\nFetching details for " + artistName + "...");
					SpotifyArtist.displayArtistWithLastFmTags(artistName, tags);

				} catch (NumberFormatException e) {
					System.out.println("Invalid choice.");
				}
				break;
			}
		}

		// ===================== OPTION 3: BY DECADE =====================
		public static void searchByDecade(Scanner scanner) {
			Integer startYear = DecadeMenu.chooseDecade(scanner);
			if (startYear == null) return;

			int endYear = (startYear == 1946) ? 1950 : startYear + 9;
			String tag;

			// Convert decade start year into Last.fm tag format
			if (startYear == 1946 || startYear == 1950) tag = "50s";
			else if (startYear == 1960) tag = "60s";
			else if (startYear == 1970) tag = "70s";
			else if (startYear == 1980) tag = "80s";
			else if (startYear == 1990) tag = "90s";
			else if (startYear == 2000) tag = "2000s";
			else if (startYear == 2010) tag = "2010s";
			else tag = "2020s";

			System.out.println("\n🔎 Searching for artists tagged with \"" + tag + "\"...");

			try {
				List<JsonNode> artists = LastFmArtist.searchByGenre(tag, false);
				if (artists.isEmpty()) {
					System.out.println("No artists found for decade: " + startYear + "–" + endYear);
					return;
				}

				System.out.println("\n🎵 Found " + artists.size() + " artists active during " + startYear + "–" + endYear + ":");
				for (int i = 0; i < artists.size(); i++) {
					String name = artists.get(i).path("name").asText();
					List<String> tags = LastFmArtist.getArtistTags(name);
					tags.removeIf(t -> t.equalsIgnoreCase(name));
					System.out.printf("%d. %s | Genres: %s%n", (i + 1), name,
							tags.isEmpty() ? "-" : String.join(", ", tags));
				}

				chooseArtistFromList(scanner,artists);

			} catch (Exception e) {
				System.err.println("Error searching artists by decade: " + e.getMessage());
			}
		}

		//  ===================== OPTION 4: BY GENRE + DECADE =====================
		public static void searchByGenreAndDecade(Scanner scanner) {
			while (true) {
				System.out.print("\nEnter genres (comma separated) or 0 to go back: ");
				String input = scanner.nextLine().trim();
				if (input.equals("0")) return;
				if (input.isEmpty()) {
					System.out.println("Error:Empty input. Please try again.");
					continue;
				}

				// validate genre input via Last.fm (no printing)
				List<JsonNode> genreArtists = LastFmArtist.searchByGenre(input, false);
				if (genreArtists.isEmpty()) {
					System.out.println("Error:Genre not found or no matching artists. Please try again.");
					continue;
				}

				// choose decade
				Integer startYear = DecadeMenu.chooseDecade(scanner);
				if (startYear == null) return;

				// map decade to tag
				String tag;
				if (startYear == 1946 || startYear == 1950) tag = "50s";
				else if (startYear == 1960) tag = "60s";
				else if (startYear == 1970) tag = "70s";
				else if (startYear == 1980) tag = "80s";
				else if (startYear == 1990) tag = "90s";
				else if (startYear == 2000) tag = "2000s";
				else if (startYear == 2010) tag = "2010s";
				else tag = "2020s";

				// get decade artists (no printing)
				List<JsonNode> decadeArtists = LastFmArtist.searchByGenre(tag, false);
				if (decadeArtists.isEmpty()) {
					System.out.println("Error:No artists found for selected decade. Please try again.");
					continue;
				}

				// build maps of artist names for exact match (case-insensitive)
				Map<String, JsonNode> byGenreMap = new HashMap<>();
				for (JsonNode a : genreArtists) {
					String name = a.path("name").asText();
					byGenreMap.put(name.toLowerCase(), a);
				}

				Map<String, JsonNode> byDecadeMap = new HashMap<>();
				for (JsonNode a : decadeArtists) {
					String name = a.path("name").asText();
					byDecadeMap.put(name.toLowerCase(), a);
				}

				// intersection (exact name match)
				List<JsonNode> intersection = new ArrayList<>();
				for (String nameLower : byGenreMap.keySet()) {
					if (byDecadeMap.containsKey(nameLower)) {
						intersection.add(byGenreMap.get(nameLower));
						if (intersection.size() >= 10) break;
					}
				}

				if (intersection.isEmpty()) {
					System.out.println("No artists found that match both your genres and the selected decade. Please try again.");
					continue;
				}

				System.out.println("\n🎵 Found " + intersection.size() + " artists:");
				for (int i = 0; i < intersection.size(); i++) {
					String name = intersection.get(i).path("name").asText();
					List<String> tags = LastFmArtist.getArtistTags(name);
					tags.removeIf(t -> t.equalsIgnoreCase(name));
					System.out.printf("%d. %s | Genres: %s%n", (i + 1), name,
							tags.isEmpty() ? "-" : String.join(", ", tags));
				}

				chooseArtistFromList(scanner,intersection);
				break;
			}
		}

		// ===================== Artist Selection Helper =====================
		private static void chooseArtistFromList(Scanner scanner,List<JsonNode> artists) {
			System.out.print("\n➡️ Enter artist number (1–" + artists.size() + ")" + " or 0 to go back: ");
			String choice = scanner.nextLine().trim();

			try {
				int index = Integer.parseInt(choice);
				if (index == 0) return;
				if (index < 1 || index > artists.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode selected = artists.get(index - 1);
				String artistName = selected.path("name").asText();

				// Get Last.fm tags
				List<String> tags = LastFmArtist.getArtistTags(artistName);

				// Display full artist info using Last.fm tags
				SpotifyArtist.displayArtistWithLastFmTags(artistName, tags);

			} catch (NumberFormatException e) {
				System.out.println("Invalid input.");
			}
		}
}
