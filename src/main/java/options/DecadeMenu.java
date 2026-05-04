package options;

import java.time.LocalDate;
import java.util.Scanner;

public class DecadeMenu {

	// ===================== Decade chooser menu =====================
			public static Integer chooseDecade(Scanner scanner) {
				int currentYear = LocalDate.now().getYear();
				int lastDecade = currentYear - (currentYear % 10);

				while (true) {
					System.out.println("\nChoose a decade or enter 0 to return back:");
					System.out.println("1. 1946–1950 (early LP era)");
					int option = 2;
					for (int year = 1950; year <= lastDecade; year += 10) {
						System.out.println(option + ". " + year + "–" + (year + 9));
						option++;
					}
					System.out.print("Enter choice: ");
					String input = scanner.nextLine().trim();

					try {
						int choice = Integer.parseInt(input);
						if (choice == 0) return null;
						if (choice == 1) return 1946;
						if (choice >= 2 && choice < option) return 1950 + (choice - 2) * 10;
					} catch (NumberFormatException ignored) {}

					System.out.println("Error:Invalid choice. Please enter a number from 0–" + (option - 1));
				}
			}
}
