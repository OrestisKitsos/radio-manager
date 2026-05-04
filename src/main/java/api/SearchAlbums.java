package api;

import java.util.*;
import options.*;

public class SearchAlbums {

	public static void start(Scanner scanner) {
		boolean backAlbum = false;
		while (!backAlbum) {
			System.out.println("\nSearch Albums by:");
			System.out.println("1. Name");
			System.out.println("2. Genre");
			System.out.println("3. Decade");
			System.out.println("4. Genre + Decade");
			System.out.println("5. Song");
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
				backAlbum=true;
				continue;
			}

			switch (sc) {

			case 1 : {
				AlbumOptions.searchByName(scanner);
				break;
			}
			case 2 : {
				AlbumOptions.searchByGenre(scanner);
				break;
			}
			case 3 : {
				AlbumOptions.searchByDecade(scanner);
				break;
			}
			case 4 : {
				AlbumOptions.searchByGenreAndDecade(scanner);
				break;
			}
			case 5 : {
				AlbumOptions.searchBySong(scanner);
				break;
			}
			case 0 : {
				backAlbum = true;
			}
			}
		}
	}
}
