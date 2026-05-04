import java.util.Scanner;
import api.*;

public class Main {
	public static void main(String[] args) {
		
		// === Main Menu ===
		Scanner scanner = new Scanner(System.in);
		boolean running = true;
		System.out.println("=== Welcome Radio Producer! ===");

		while (running) {
			System.out.println("\nMain menu:");
			System.out.println("1. Search Artists");
			System.out.println("2. Search Songs");
			System.out.println("3. Search Albums");
			System.out.println("0. Exit");
			System.out.print("Enter choice: ");

			String input = scanner.nextLine().trim();
			int choice;
			try {
				choice = Integer.parseInt(input);
				if (choice < 0 || choice > 3) {
					System.out.println("Error:Invalid choice. Please enter 1-3 or 0 to quit.");
					continue;
				}
			} catch (NumberFormatException e) {
				System.out.println("Error:Invalid choice. Please enter 1-3 or 0 to quit.");
				continue;
			}

			switch (choice) {
			case 1 : {
				SearchArtists.start(scanner);
				break;
			}
			case 2 : {
				SearchSongs.start(scanner);
				break;
			}
			case 3 : {
				SearchAlbums.start(scanner);
				break;
			}
			case 0 : {
				System.out.println("Exiting...\n");
                System.out.println("Goodbye!");
				running = false;
			}
			}
		}
		scanner.close();
	}
}
