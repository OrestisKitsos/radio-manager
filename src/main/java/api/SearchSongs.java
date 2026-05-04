package api;

import java.util.*;
import options.*;

public class SearchSongs {

	public static void start(Scanner scanner) {
		boolean backSong = false;
		while (!backSong) {
			System.out.println("\nSearch Songs by:");
			System.out.println("1. Name");
			System.out.println("2. Genre");
			System.out.println("3. Decade");
			System.out.println("4. Genre + Decade");
			System.out.println("5. Album");
			System.out.println("0. Back");
			System.out.print("Enter choice: ");
			String si = scanner.nextLine().trim();

			int sc;
			try {
				sc = Integer.parseInt(si);
				if (sc < 0 || sc > 5) {
					System.out.println("Error:Invalid choice. Please enter a number from 1-5 or 0 if you want to return to main menu.");
					continue; 
				}
			} catch (NumberFormatException e) {
				System.out.println("Error:Invalid choice. Please enter a number from 1-5 or 0 if you want to return to main menu.");
				continue;
			}

			if (si.equals("0")) {
				backSong=true;
				continue;
			}

			switch (sc) {

			case 1 : {
				SongOptions.searchByName(scanner);
				break;
			}
			case 2 : {
				SongOptions.searchByGenre(scanner);
				break;
			}
			case 3 : {
				SongOptions.searchByDecade(scanner);
				break;
			}
			case 4 : {
				SongOptions.searchByGenreAndDecade(scanner);
				break;
			}
			case 5 : {
				SongOptions.searchByAlbum(scanner);
				break;
			}
			case 0 : {
				System.out.println("Exiting...\n");
				System.out.println("Goodbye!");
				backSong = true;
			}
			}
		}    
	}
}
