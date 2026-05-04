🎧 Radio Manager

📌 Overview

Radio Manager is a Java-based console application designed to assist radio producers in quickly searching and retrieving information about artists, songs, and albums.

The system integrates with the Spotify API and Last.fm API to provide real-time music data, including track details, album information, artist metadata, and genre tags.

It is built as a Maven project in Eclipse and focuses on simplicity, speed, and practical usage in a live radio production environment.

⚙️ Technologies Used

- Java (Maven Project)
- Spotify Web API – Music data (songs, albums, artists)
- Last.fm API – Genre tags and artist-based search
- Jackson (JSON processing)

🔧 Requirements

- Java 11 or later
- Maven 3.6+
- Internet connection (required for API calls)
- Spotify API credentials (Client ID & Secret)
- Last.fm API key

🧩 System Functionality

🎤 Artist Search
- Search artists by name
- Retrieve artist details and related metadata

🎵 Song Search
- Search songs by:
  - Name
  - Genre
  - Decade
  - Genre + Decade
  - Album
- Display top tracks per artist
- Show release dates and metadata
- Retrieve genre tags from Last.fm

💿 Album Search
- Search albums by:
  - Name
  - Genre
  - Decade
  - Genre + Decade
  - Song
- Display album details and release information

🔗 API Integration
- Spotify → primary source for music data
- Last.fm → genre-based discovery and tagging

🖥️ User Interaction
The application operates entirely through a command-line interface.

Users navigate through menus to:
- Search for artists, songs, and albums
- Filter results by genre and/or decade
- Select specific entries to view detailed information

📂 Project Structure
```
src/main/java
│
├── Main.java # Entry point (main menu)
│
├── api/ # Core application logic
│ ├── SearchArtists.java
│ ├── SearchSongs.java
│ └── SearchAlbums.java
│
├── spotify/ # Spotify API integration
│ ├── SpotifyArtist.java
│ ├── SearchSong.java
│ ├── SearchAlbum.java
│ └── SpotifyAuthManager.java
├── last_fm/ # Last.fm API integration
│ ├── LastFmArtist.java
│ ├── LastFmSong.java
│ └── LastFmAlbum.java
├── options/ # Menu utilities & helpers
│ ├── ArtistOptions.java
│ ├── SongOptions.java
│ ├── AlbumOptions.java
│ └── DecadeMenu.java
│ ├── excel/ # (Planned feature – not yet implemented)
│ └── pom.xml # Maven configuration
```
▶️ Example Usage


Search Songs by:

Name
Genre
Decade
Genre + Decade
Album
Back

Enter choice: 4
Enter genres: metal
Choose decade: 2000–2009

🎯 Showing top tracks for metal artists from 2000–2009:

🎤 System of a Down

Chop Suey! (2001-09-04)
Toxicity (2001-09-04)
Aerials (2001-09-04)

---

🚀 Future Improvements

- Export favorite artists/songs/albums to Excel (playlist-like functionality)
- Add user “favorites” system
- Improve search accuracy and filtering
- Develop a graphical user interface (GUI)
- Add caching to reduce API calls
- Support more music data sources

---

👤 Target Users

- Radio producers
- DJs
- Music enthusiasts
- Content creators

---

📜 License

This project is licensed under the MIT License. You are free to use, modify, and distribute it with proper attribution.
