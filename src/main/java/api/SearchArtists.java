package api;

import java.util.*;
import options.*;



public class SearchArtists {

	public static void start(Scanner scanner) {
		boolean backArtist = false;
		while (!backArtist) {
			System.out.println("\nSearch Artists by:");
			System.out.println("1. Name");
			System.out.println("2. Genre");
			System.out.println("3. Decade");
			System.out.println("4. Genre + Decade");
			System.out.println("0. Back");
			System.out.print("Enter choice: ");
			String si = scanner.nextLine().trim();

			int sc;
			try {
				sc = Integer.parseInt(si);
				if (sc < 0 || sc > 4) {
					System.out.println("Error:Invalid choice. Please enter a number from 1-4 or 0 if you want to return to main menu.");
					continue; 
				}
			} catch (NumberFormatException e) {
				System.out.println("Error:Invalid choice. Please enter a number from 1-4 or 0 if you want to return to main menu.");
				continue;
			}

			if (si.equals("0")) {
				backArtist=true;
				continue;
			}

			switch (sc) {

			case 1 : {
				ArtistOptions.searchByName(scanner);
				break;
			}
			case 2 : {
				ArtistOptions.searchByGenre(scanner);
				break;
			}
			case 3 : {
				ArtistOptions.searchByDecade(scanner);
				break;
			}
			case 4 : {
				ArtistOptions.searchByGenreAndDecade(scanner);
				break;
			}
			case 0 : {
				backArtist = true;
			}
			}
		}
	}
}
