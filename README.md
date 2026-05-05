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
- Search artists by:
  - Name
  - Genre
  - Decade
  - Genre + Decade
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
- The application operates entirely through a command-line interface.

Users navigate through menus to:
- Search for artists, songs, and albums
- Filter results by genre and/or decade
- Select specific entries to view detailed information

📂 Project Structure
```
src/main/java
│
├── Main.java # Application entry point (main menu)
│
├── api/
│ ├── SearchArtists.java # Artist search workflows
│ ├── SearchSongs.java # Song search workflows
│ └── SearchAlbums.java # Album search workflows
│
├── spotify/
│ ├── SpotifyArtist.java # Spotify artist API calls
│ ├── SpotifySong.java # Spotify track API calls
│ ├── SpotifyAlbum.java # Spotify album API calls
│ └── SpotifyAuthManager.java # Spotify authentication handler
│
├── last_fm/
│ ├── LastFmArtist.java # Genre-based Artist search
│ ├── LastFmSong.java # Genre-based Song search and Song metadata
│ └── LastFmAlbum.java # Genre-based Album search and Album metadata
│
├── options/
│ ├── ArtistOptions.java # Artist menu options
│ ├── SongOptions.java # Song menu options
│ ├── AlbumOptions.java # Album menu options
│ └── DecadeMenu.java # Decade selection utility
│
└── excel/ # Planned feature: export favourites to Excel

pom.xml # Maven configuration
```

▶️ How to Run

1. Clone the repository

   - git clone https://github.com/OrestisKitsos/radio-manager.git
   - cd radio-manager

2. Create a .env file in the root directory

3. Create your API credentials

  - Spotify: https://developer.spotify.com/dashboard/

  - Last.fm: https://www.last.fm/api/account/create

4. Add your credentials to the .env file

  - SPOTIFY_CLIENT_ID=your_client_id

  - SPOTIFY_CLIENT_SECRET=your_client_secret

  - LASTFM_API_KEY=your_api_key

5. Build the project

   - mvn clean install

6. Run the application

   - mvn exec:java

   (or run Main.java directly from your IDE)


▶️ Example Usage
```
Search Artists by:
1. Name
2. Genre
3. Decade
4. Genre + Decade
0. Back
Enter choice: 1

Enter artist name: The Clash

🎤 Artist: The Clash
🌍 Genres: [punk, punk rock, british, rock, classic rock, 70s, 80s, reggae, new wave, ska]

🎶 Top Tracks:
- Should I Stay or Should I Go - Remastered (1982)
- Rock the Casbah - Remastered (1982)
- London Calling - Remastered (1979)
- Train in Vain (Stand by Me) - Remastered (1979)
- I Fought the Law (2013)

🔥 Top Albums:
- Combat Rock + The People's Hall (2022-05-20)
- Live at Shea Stadium (Remastered) (2008)
- From Here to Eternity (Live) [Remastered] (1999)
- Raw and Unfiltered (1988-06-14)
- Cut The Crap (1985-11-04)
```

⚠️ Limitations

- Search results depend on external APIs (Spotify & Last.fm), which may not always return complete or expected data.
- Some searches (e.g., specific artist/song names) may not yield results due to API matching limitations.
- Genre and decade combinations may occasionally return limited or no results, depending on available data.
- Release dates may be missing for certain tracks due to incomplete metadata from the APIs.
- The application requires a stable internet connection.

🚀 Future Improvements

- Export favourite artists/songs/albums to Excel (playlist-like functionality)
- Add user “favourites” system
- Improve search accuracy and result matching
- Enhance genre and decade filtering reliability
- Implement fallback mechanisms when API results are missing
- Add caching to reduce API calls and improve performance
- Develop a graphical user interface (GUI)
- Support additional music data sources

👤 Target Users

- Radio producers
- DJs
- Music enthusiasts
- Content creators

📜 License

This project is licensed under the MIT License. You are free to use, modify, and distribute it with proper attribution.
