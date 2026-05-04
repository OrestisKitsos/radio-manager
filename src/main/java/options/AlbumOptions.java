package options;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

import last_fm.*;
import spotify.*;

public class AlbumOptions {

	// ===================== OPTION 1: BY NAME =====================
		public static void searchByName(Scanner scanner) {
			System.out.print("\nEnter album name: ");
			String name = scanner.nextLine().trim();
			if (name.isEmpty()) {
				System.out.println("Error:Empty input.Please try again.");
				return;
			}

			List<JsonNode> albums = SpotifyAlbum.searchAlbum(name);
			if (albums.isEmpty()) return;

			chooseAlbumFromList(scanner,albums);
		}

		// ===================== OPTION 2: BY GENRE =====================
		public static void searchByGenre(Scanner scanner) {
			System.out.print("\nEnter genres or country (comma separated) or 0 to go back: ");
			String genreInput = scanner.nextLine().trim();
			if (genreInput.equals("0")) return;

			// Find top artists by genre using Last.fm
			List<JsonNode> artists = LastFmArtist.searchByGenre(genreInput, false);
			if (artists.isEmpty()) {
				System.out.println("No artists found for these genres.");
				return;
			}

			System.out.printf("\nShowing top albums for %s artists:\n", genreInput);

			List<JsonNode> allResults = new ArrayList<>();
			List<String> allArtists = new ArrayList<>();
			boolean foundAny = false;

			// For each artist, fetch top albums from Spotify
			for (JsonNode artist : artists) {
				String artistName = artist.path("name").asText();
				String artistId = SpotifyArtist.getArtistIdByName(artistName);
				if (artistId == null) continue;

				List<JsonNode> topAlbums = SpotifyArtist.fetchTopAlbums(artistId, false);
				if (topAlbums == null || topAlbums.isEmpty()) continue;

				foundAny = true;
				System.out.println("\n🎤 " + artistName);

				for (JsonNode album : topAlbums.subList(0, Math.min(topAlbums.size(), 3))) {
					String albumName = album.path("name").asText();
					String releaseDate = album.path("release_date").asText("-");

					allResults.add(album);
					allArtists.add(artistName);

					System.out.printf("%2d. %s (%s)%n", allResults.size(), albumName, releaseDate);
				}
			}

			// Handle case where nothing was found
			if (!foundAny) {
				System.out.printf("Error:No albums found for genres '%s'.%n", genreInput);
				return;
			}

			// Let the user choose a album for more details
			System.out.print("\n➡️ Enter album number (1–" + allResults.size() + ") or 0 to go back: ");
			String idx = scanner.nextLine().trim();
			if (idx.equals("0")) return;

			try {
				int selection = Integer.parseInt(idx);
				if (selection < 1 || selection > allResults.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosenAlbum = allResults.get(selection - 1);
				String albumName = chosenAlbum.path("name").asText();
				String artistName = allArtists.get(selection - 1);

				// Fetch Last.fm tags and display detailed album info
				List<String> tags = LastFmAlbum.getAlbumTags(albumName, artistName);
				SpotifyAlbum.getAlbumDetailsWithTags(chosenAlbum.path("id").asText(), tags);

			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a valid number.");
			} catch (Exception e) {
				System.err.println("Error displaying album details: " + e.getMessage());
			}
		}


		// ===================== OPTION 3: BY DECADE =====================
		public static void searchByDecade(Scanner scanner) {
			Integer startYear = DecadeMenu.chooseDecade(scanner);
			if (startYear == null) return;

			int endYear = (startYear == 1946) ? 1950 : startYear + 9;

			System.out.println("\n🔎 Searching for popular albums released between " + startYear + "–" + endYear + "...");

			try {
				List<JsonNode> albums = SpotifyAlbum.searchByDecade(startYear, endYear);
				if (albums.isEmpty()) {
					System.out.println("No albums found for that decade.");
					return;
				}

				System.out.println("\n🎵 Found " + albums.size() + " albums from " + startYear + "–" + endYear + ":");
				for (int i = 0; i < albums.size(); i++) {
					JsonNode a = albums.get(i);
					String title = a.path("name").asText("-");
					String artist = a.path("artists").get(0).path("name").asText("-");
					String release = a.path("release_date").asText("-");
					System.out.printf("%d. %s - %s (%s)%n", (i + 1), artist, title, release);
				}

				// Choosing album
				System.out.print("\n➡️ Enter album number (1–" + albums.size() + ") or 0 to go back: ");
				String idx = scanner.nextLine().trim();
				if (idx.equals("0")) return;

				int selection = Integer.parseInt(idx);
				if (selection < 1 || selection > albums.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosen = albums.get(selection - 1);
				String albumId = chosen.path("id").asText();
				String albumName = chosen.path("name").asText();
				String artistName = chosen.path("artists").get(0).path("name").asText();

				// Get Last.fm tags
				List<String> tags = LastFmAlbum.getAlbumTags(albumName, artistName);

				// Show full details with tags
				SpotifyAlbum.getAlbumDetailsWithTags(albumId, tags);

			} catch (Exception e) {
				System.err.println("Error searching albums by decade: " + e.getMessage());
			}
		}

		// ===================== OPTION 4: BY GENRE + DECADE =====================
		public static void searchByGenreAndDecade(Scanner scanner) {
			System.out.print("\nEnter genres (comma separated): ");
			String genreInput = scanner.nextLine().trim();

			// Find top artists for given genres from Last.fm
			List<JsonNode> artists = LastFmArtist.searchByGenre(genreInput, false);
			if (artists.isEmpty()) {
				System.out.println("No artists found for these genres.");
				return;
			}

			// Choose decade
			Integer decadeStart = DecadeMenu.chooseDecade(scanner);
			if (decadeStart == null) return;
			int decadeEnd = (decadeStart == 1946) ? 1950 : decadeStart + 9;

			System.out.printf("\n🎯 Showing top albums for %s artists from %d–%d:\n",
					genreInput, decadeStart, decadeEnd);

			List<JsonNode> allResults = new ArrayList<>();
			List<String> allArtists = new ArrayList<>();
			boolean foundAny = false;

			// For each artist, fetch Spotify albums
			for (JsonNode artist : artists) {
				String artistName = artist.path("name").asText();
				String artistId = SpotifyArtist.getArtistIdByName(artistName);
				if (artistId == null) continue;

				// Fetch albums without printing
				List<JsonNode> albums = SpotifyArtist.fetchTopAlbums(artistId, false);
				if (albums == null || albums.isEmpty()) continue;

				// Filter by decade
				List<JsonNode> decadeAlbums = new ArrayList<>();
				for (JsonNode album : albums) {
					String releaseDate = album.path("release_date").asText("");
					if (releaseDate.length() >= 4) {
						try {
							int year = Integer.parseInt(releaseDate.substring(0, 4));
							if (year >= decadeStart && year <= decadeEnd) {
								decadeAlbums.add(album);
							}
						} catch (NumberFormatException ignored) {}
					}
				}

				// Display up to 3 albums per artist
				if (!decadeAlbums.isEmpty()) {
					foundAny = true;
					System.out.println("\n🎤 " + artistName);
					for (JsonNode album : decadeAlbums.subList(0, Math.min(decadeAlbums.size(), 3))) {
						String albumName = album.path("name").asText();
						String release = album.path("release_date").asText("-");
						allResults.add(album);
						allArtists.add(artistName);
						System.out.printf("%2d. %s (%s)%n", allResults.size(), albumName, release);
					}
				}
			}

			// Handle empty case
			if (!foundAny) {
				System.out.printf("Error: No albums found for genres '%s' in the %d–%d decade.%n",
						genreInput, decadeStart, decadeEnd);
				return;
			}

			// Let user choose an album
			System.out.print("\n➡️ Enter album number (1–" + allResults.size() + ") or 0 to go back: ");
			String idx = scanner.nextLine().trim();
			if (idx.equals("0")) return;

			try {
				int selection = Integer.parseInt(idx);
				if (selection < 1 || selection > allResults.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosenAlbum = allResults.get(selection - 1);
				String albumId = chosenAlbum.path("id").asText();
				SpotifyAlbum.getAlbumDetails(albumId);

			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a valid number.");
			} catch (Exception e) {
				System.err.println("Error displaying album details: " + e.getMessage());
			}
		}



		// ===================== OPTION 5: BY SONG =====================
		public static void searchBySong(Scanner scanner) {
			System.out.print("\nEnter song title: ");
			String songTitle = scanner.nextLine().trim();
			if (songTitle.isEmpty()) {
				System.out.println("Error: Empty input. Please try again.");
				return;
			}

			try {
				// Search for songs by title on Spotify
				List<JsonNode> songs = SpotifySong.searchSong(songTitle);
				if (songs.isEmpty()) {
					System.out.println("No songs found with that title.");
					return;
				}
				// Let user pick one
				System.out.print("\n➡️ Enter song number (1–" + songs.size() + ") or 0 to go back: ");
				String idx = scanner.nextLine().trim();
				if (idx.equals("0")) return;

				int selection = Integer.parseInt(idx);
				if (selection < 1 || selection > songs.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosenSong = songs.get(selection - 1);
				String albumId = chosenSong.path("album").path("id").asText();
				String albumName = chosenSong.path("album").path("name").asText();
				String artistName = chosenSong.path("artists").get(0).path("name").asText();

				// Get Last.fm tags
				List<String> tags = LastFmAlbum.getAlbumTags(albumName, artistName);

				// Show album info (SpotifyAlbum)
				System.out.println("\n📀 Album details for \"" + albumName + "\" by " + artistName + ":");
				SpotifyAlbum.getAlbumDetailsWithTags(albumId, tags);

			} catch (Exception e) {
				System.err.println("Error searching album by song: " + e.getMessage());
			}
		}

		// ===================== Album Selection Helper =====================
		private static void chooseAlbumFromList(Scanner scanner,List<JsonNode> albums) {
			System.out.print("\n➡️ Enter album number (1–" + albums.size() + ") or 0 to go back: ");
			String choice = scanner.nextLine().trim();

			try {
				int index = Integer.parseInt(choice);
				if (index == 0) return;
				if (index < 1 || index > albums.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode selected = albums.get(index - 1);
				String albumId = selected.path("id").asText();
				String albumName = selected.path("name").asText();
				String artistName = selected.path("artists").get(0).path("name").asText();

				// Get Last.fm tags
				List<String> tags = LastFmAlbum.getAlbumTags(albumName, artistName);

				// Show full details with tags
				SpotifyAlbum.getAlbumDetailsWithTags(albumId, tags);


			} catch (NumberFormatException e) {
				System.out.println("Invalid input.");
			}
		}
}
