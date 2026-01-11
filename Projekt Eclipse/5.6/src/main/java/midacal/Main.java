package midacal;

import java.io.File;
import java.util.*;

public class Main {
    public static MemoryContainer appMemory = new MemoryContainer();
    private static final String FILE_PATH = "midacalXML.xml";
    private static final String APP_NAME = "KALENDARZ MIDACAL";
    private static final Scanner sc = new Scanner(System.in);

    public static class MemoryContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        DBManager.initDatabase();
        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== " + APP_NAME + " ===");
            System.out.println("1. Wyświetl | 2. Szukaj | 3. Edytuj | 4. Zarządzaj Relacjami | 5. Dane | X. Wyjście");
            System.out.print("Wybór: ");
            String choice = sc.nextLine().toUpperCase();
            switch (choice) {
                case "1" -> displayMenu();
                case "4" -> manageRelations();
                case "X" -> handleExit();
            }
        }
    }

    private static void manageRelations() {
        if (appMemory.kontakty.isEmpty() || appMemory.zdarzenia.isEmpty()) {
            System.out.println("[!] Potrzebujesz min. 1 kontaktu i 1 zdarzenia.");
            return;
        }
        try {
            showKontakty();
            System.out.print("Wybierz indeks kontaktu: ");
            int kid = Integer.parseInt(sc.nextLine());
            
            showZdarzenia();
            System.out.print("Wybierz indeks zdarzenia: ");
            int zid = Integer.parseInt(sc.nextLine());
            
            appMemory.zdarzenia.get(zid).dodajUczestnika(appMemory.kontakty.get(kid));
            System.out.println("[OK] Kontakt został przypisany do zdarzenia.");
        } catch (Exception e) { System.out.println("[X] Błąd wyboru."); }
    }

    private static void handleExit() {
        DBManager.saveAll(appMemory.kontakty, appMemory.zdarzenia);
        System.out.println("[OK] Aplikacja zamknięta.");
        System.exit(0);
    }

    private static void displayMenu() {
        if (appMemory.kontakty.isEmpty()) {
            System.out.println("[!] RAM pusty. 1. Seed (10+10): ");
            if (sc.nextLine().equals("1")) seedData();
        }
        showKontakty(); showZdarzenia();
    }

    private static void showKontakty() {
        System.out.println("\n--- KONTAKTY ---");
        for (int i = 0; i < appMemory.kontakty.size(); i++) System.out.println("[" + i + "] " + appMemory.kontakty.get(i));
    }

    private static void showZdarzenia() {
        System.out.println("\n--- ZDARZENIA ---");
        for (int i = 0; i < appMemory.zdarzenia.size(); i++) System.out.println("[" + i + "] " + appMemory.zdarzenia.get(i));
    }

    private static void seedData() { /* Metoda z poprzednich kroków */ }
}