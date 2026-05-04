package options;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

import last_fm.*;
import spotify.*;

public class SongOptions {

	// ===================== OPTION 1: BY NAME =====================
		public static void searchByName(Scanner scanner) {
			System.out.print("\nEnter song name: ");
			String name = scanner.nextLine().trim();
			if (name.isEmpty()) {
				System.out.println("Error:Empty input.Please try again.");
				return;
			}

			List<JsonNode> songs = SpotifySong.searchSong(name);
			if (songs.isEmpty()) return;

			chooseSongFromList(scanner,songs);
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

			System.out.printf("\nShowing top tracks for %s artists:\n", genreInput);

			List<JsonNode> allResults = new ArrayList<>();
			List<String> allArtists = new ArrayList<>();
			boolean foundAny = false;

			// For each artist, fetch top tracks from Spotify
			for (JsonNode artist : artists) {
				String artistName = artist.path("name").asText();
				String artistId = SpotifyArtist.getArtistIdByName(artistName);
				if (artistId == null) continue;

				List<JsonNode> topTracks = SpotifyArtist.fetchTopTracks(artistId, false);
				if (topTracks == null || topTracks.isEmpty()) continue;

				foundAny = true;
				System.out.println("\n🎤 " + artistName);

				for (JsonNode track : topTracks.subList(0, Math.min(topTracks.size(), 3))) {
					String songName = track.path("name").asText();
					String releaseDate = track.path("album").path("release_date").asText("-");

					allResults.add(track);
					allArtists.add(artistName);

					System.out.printf("%2d. %s (%s)%n", allResults.size(), songName, releaseDate);
				}
			}

			// Handle case where nothing was found
			if (!foundAny) {
				System.out.printf("Error:No songs found for genres '%s'.%n", genreInput);
				return;
			}

			// Let the user choose a song for more details
			System.out.print("\n➡️ Enter song number (1–" + allResults.size() + ") or 0 to go back: ");
			String idx = scanner.nextLine().trim();
			if (idx.equals("0")) return;

			try {
				int selection = Integer.parseInt(idx);
				if (selection < 1 || selection > allResults.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosenTrack = allResults.get(selection - 1);
				String songName = chosenTrack.path("name").asText();
				String artistName = allArtists.get(selection - 1);

				// Fetch Last.fm tags and display detailed song info
				List<String> tags = LastFmSong.getSongTags(songName, artistName);
				SpotifySong.getSongDetailsWithTags(chosenTrack.path("id").asText(), tags);

			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a valid number.");
			} catch (Exception e) {
				System.err.println("Error displaying song details: " + e.getMessage());
			}
		}


		// ===================== OPTION 3: BY DECADE =====================
		public static void searchByDecade(Scanner scanner) {
			Integer startYear = DecadeMenu.chooseDecade(scanner);
			if (startYear == null) return;

			int endYear = (startYear == 1946) ? 1950 : startYear + 9;

			System.out.println("\n🔎 Searching for popular songs released between " + startYear + "–" + endYear + "...");

			try {
				List<JsonNode> songs = SpotifySong.searchByDecade(startYear, endYear);
				if (songs.isEmpty()) {
					System.out.println("No songs found for that decade.");
					return;
				}

				System.out.println("\n🎵 Top " + songs.size() + " songs from " + startYear + "–" + endYear + ":");
				for (int i = 0; i < songs.size(); i++) {
					JsonNode s = songs.get(i);
					String title = s.path("name").asText("Unknown");
					String artist = s.path("artists").get(0).path("name").asText("Unknown");
					String album = s.path("album").path("name").asText("Unknown");
					String releaseDate = s.path("album").path("release_date").asText("-");
					System.out.printf("%d. %s - %s (%s) [%s]%n", (i + 1), artist, title, album, releaseDate);
				}

				// Choosing song
				System.out.print("\n➡️ Enter song number (1–" + songs.size() + ") or 0 to go back: ");
				String idx = scanner.nextLine().trim();
				if (idx.equals("0")) return;

				int selection = Integer.parseInt(idx);
				if (selection < 1 || selection > songs.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosen = songs.get(selection - 1);
				String songId = chosen.path("id").asText();
				String songName = chosen.path("name").asText();
				String artistName = chosen.path("artists").get(0).path("name").asText();

				// Get Last.fm tags
				List<String> tags = LastFmSong.getSongTags(songName, artistName);

				// Show full details with tags
				SpotifySong.getSongDetailsWithTags(songId, tags);

			} catch (Exception e) {
				System.err.println("Error searching songs by decade: " + e.getMessage());
			}
		}

		// ===================== OPTION 4: BY GENRE + DECADE =====================
		public static void searchByGenreAndDecade(Scanner scanner) {
			System.out.print("\nEnter genres (comma separated): ");
			String genreInput = scanner.nextLine().trim();

			// Find top artists by genre from Last.fm
			List<JsonNode> artists = LastFmArtist.searchByGenre(genreInput, false);
			if (artists.isEmpty()) {
				System.out.println("No artists found for these genres.");
				return;
			}

			// Choose decade using existing menu
			Integer decadeStart = DecadeMenu.chooseDecade(scanner);
			if (decadeStart == null) return; // user chose "0"
			int decadeEnd = (decadeStart == 1946) ? 1950 : decadeStart + 9;

			System.out.printf("\n🎯 Showing top tracks for %s artists from %d–%d:\n",
					genreInput, decadeStart, decadeEnd);

			// Track list for user selection
			List<JsonNode> allResults = new ArrayList<>();
			List<String> allArtists = new ArrayList<>();
			boolean foundAny = false;

			// For each artist, fetch their Spotify top tracks
			for (JsonNode artist : artists) {
				String artistName = artist.path("name").asText();
				String artistId = SpotifyArtist.getArtistIdByName(artistName);
				if (artistId == null) continue;

				List<JsonNode> topTracks = SpotifyArtist.fetchTopTracks(artistId, false);
				if (topTracks == null || topTracks.isEmpty()) continue;

				// Filter by decade
				List<JsonNode> decadeTracks = new ArrayList<>();
				for (JsonNode track : topTracks) {
					String releaseDate = track.path("album").path("release_date").asText("");
					if (releaseDate.length() >= 4) {
						int year = Integer.parseInt(releaseDate.substring(0, 4));
						if (year >= decadeStart && year <= decadeEnd) {
							decadeTracks.add(track);
						}
					}
				}

				// Display and number tracks per artist
				if (!decadeTracks.isEmpty()) {
					foundAny = true;
					System.out.println("\n🎤 " + artistName);
					for (JsonNode track : decadeTracks.subList(0, Math.min(decadeTracks.size(), 3))) {
						String songName = track.path("name").asText();
						String release = track.path("album").path("release_date").asText("-");
						allResults.add(track);
						allArtists.add(artistName);
						System.out.printf("%2d. %s (%s)%n", allResults.size(), songName, release);
					}
				}
			}

			// Handle case where nothing was found
			if (!foundAny) {
				System.out.printf("Error: No songs found for genres '%s' in the %d–%d decade.%n",
						genreInput, decadeStart, decadeEnd);
				return;
			}

			// Let user choose a song
			System.out.print("\n➡️ Enter song number (1–" + allResults.size() + ") or 0 to go back: ");
			String idx = scanner.nextLine().trim();
			if (idx.equals("0")) return;

			try {
				int selection = Integer.parseInt(idx);
				if (selection < 1 || selection > allResults.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosenTrack = allResults.get(selection - 1);
				String songName = chosenTrack.path("name").asText();
				String artistName = allArtists.get(selection - 1);

				// Fetch tags and display detailed info
				List<String> tags = LastFmSong.getSongTags(songName, artistName);
				SpotifySong.getSongDetailsWithTags(chosenTrack.path("id").asText(), tags);

			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a valid number.");
			} catch (Exception e) {
				System.err.println("Error displaying song details: " + e.getMessage());
			}
		}



		// ===================== OPTION 5: BY ALBUM =====================
		public static void searchByAlbum(Scanner scanner) {
			System.out.print("\nEnter album name: ");
			String albumName = scanner.nextLine().trim();
			if (albumName.isEmpty()) {
				System.out.println("Error: Empty input. Please try again.");
				return;
			}

			try {
				// Search albums
				List<JsonNode> albums = SpotifyAlbum.searchAlbum(albumName);
				if (albums.isEmpty()) return;

				// Choose album
				System.out.print("\n➡️ Enter album number (1–" + albums.size() + ") or 0 to go back: ");
				String input = scanner.nextLine().trim();
				if (input.equals("0")) return;

				int albumChoice = Integer.parseInt(input);
				if (albumChoice < 1 || albumChoice > albums.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosenAlbum = albums.get(albumChoice - 1);
				String albumId = chosenAlbum.path("id").asText();
				String artistName = chosenAlbum.path("artists").get(0).path("name").asText();

				// Display tracklist
				List<JsonNode> tracks = SpotifyAlbum.getAlbumTracklist(albumId);
				if (tracks.isEmpty()) {
					System.out.println("No tracks found in this album.");
					return;
				}

				// Choose track from album
				System.out.print("\n➡️ Enter track number (1–" + tracks.size() + ") or 0 to go back: ");
				String trackInput = scanner.nextLine().trim();
				if (trackInput.equals("0")) return;

				int trackChoice = Integer.parseInt(trackInput);
				if (trackChoice < 1 || trackChoice > tracks.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode chosenTrack = tracks.get(trackChoice - 1);
				String trackName = chosenTrack.path("name").asText();

				// Get tags and show details
				List<String> tags = LastFmSong.getSongTags(trackName, artistName);
				SpotifySong.displaySongWithLastFmTags(trackName, tags);

			} catch (Exception e) {
				System.err.println("Error searching by album: " + e.getMessage());
			}
		}

		// ===================== Song Selection Helper =====================
		private static void chooseSongFromList(Scanner scanner,List<JsonNode> songs) {
			System.out.print("\n➡️ Enter song number (1–" + songs.size() + ")" + " or 0 to go back: ");
			String choice = scanner.nextLine().trim();

			try {
				int index = Integer.parseInt(choice);
				if (index == 0) return;
				if (index < 1 || index > songs.size()) {
					System.out.println("Invalid choice.");
					return;
				}

				JsonNode selected = songs.get(index - 1);
				String songId = selected.path("id").asText();
				String songName = selected.path("name").asText();
				String artistName = selected.path("artists").get(0).path("name").asText();

				// Get Last.fm tags
				List<String> tags = LastFmSong.getSongTags(songName, artistName);

				// Show full details with tags
				SpotifySong.getSongDetailsWithTags(songId, tags);


			} catch (NumberFormatException e) {
				System.out.println("Invalid input.");
			}
		}
}
