🎧 Radio Manager

## 📌 Overview

Radio Manager is a Java-based console application designed to assist radio producers in quickly searching and retrieving information about artists, songs, and albums.

The system integrates with the Spotify API and Last.fm API to provide real-time music data, including track details, album information, artist metadata, and genre tags.

It is built as a Maven project in Eclipse and focuses on simplicity, speed, and practical usage in a live radio production environment.

---

## ⚙️ Technologies Used

- Java (Maven Project)
- Spotify Web API – Music data (songs, albums, artists)
- Last.fm API – Genre tags and artist-based search
- Jackson (JSON processing)

---

## 🔧 Requirements

- Java 11 or later
- Maven 3.6+
- Internet connection (required for API calls)
- Spotify API credentials (Client ID & Secret)
- Last.fm API key

---

## 🧩 System Functionality

### 🎤 Artist Search
- Search artists by name
- Retrieve artist details and related metadata

### 🎵 Song Search
- Search songs by:
  - Name
  - Genre
  - Decade
  - Genre + Decade
  - Album
- Display top tracks per artist
- Show release dates and metadata
- Retrieve genre tags from Last.fm

### 💿 Album Search
- Search albums by:
  - Name
  - Genre
  - Decade
  - Genre + Decade
  - Song
- Display album details and release information

### 🔗 API Integration
- Spotify → primary source for music data
- Last.fm → genre-based discovery and tagging

---

## 🖥️ User Interaction

The application operates entirely through a command-line interface.

Users navigate through menus to:
- Search for artists, songs, and albums
- Filter results by genre and/or decade
- Select specific entries to view detailed information

---

## 📂 Project Structure
