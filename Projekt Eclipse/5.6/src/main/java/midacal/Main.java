package midacal;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import jakarta.mail.internet.InternetAddress;

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
        if (new File(FILE_PATH).exists()) loadFromXml();
        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== " + APP_NAME + " ===");
            System.out.println("1. Wyświetl | 2. Szukaj | 3. Edytuj | 4. Relacje (M:N) | 5. Dane | X. Wyjście");
            System.out.print("Wybór: ");
            String choice = sc.nextLine().toUpperCase();
            switch (choice) {
                case "1" -> displayMenu();
                case "2" -> searchMenu();
                case "3" -> editRootMenu();
                case "4" -> manageRelations();
                case "5" -> dataManagementMenu();
                case "X" -> handleExit();
            }
        }
    }

    private static void handleExit() {
        if (!appMemory.kontakty.isEmpty() || !appMemory.zdarzenia.isEmpty()) {
            System.out.println("[...] Synchronizacja danych...");
            saveToXml();
            DBManager.saveAll(appMemory.kontakty, appMemory.zdarzenia);
        }
        System.out.println("[OK] Do widzenia!");
        System.exit(0);
    }

    private static void displayMenu() {
        if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
            System.out.println("[!] RAM pusty. 1. Seed (10+10) | X. Wstecz");
            if (sc.nextLine().equals("1")) seedData();
            return;
        }
        showKontakty(); showZdarzenia();
    }

    private static void manageRelations() {
        if (appMemory.kontakty.isEmpty() || appMemory.zdarzenia.isEmpty()) {
            System.out.println("[!] Brak danych do powiązania.");
            return;
        }
        try {
            showKontakty();
            System.out.print("Indeks kontaktu: ");
            int kid = Integer.parseInt(sc.nextLine());
            showZdarzenia();
            System.out.print("Indeks zdarzenia: ");
            int zid = Integer.parseInt(sc.nextLine());
            
            appMemory.zdarzenia.get(zid).dodajUczestnika(appMemory.kontakty.get(kid));
            System.out.println("[OK] Powiązano kontakt ze zdarzeniem.");
        } catch (Exception e) { System.out.println("[X] Błąd wyboru."); }
    }

    private static void searchMenu() {
        System.out.print("Szukaj (nazwisko/tytuł): ");
        String query = sc.nextLine().toLowerCase();
        appMemory.kontakty.stream().filter(k -> k.getNazwisko().toLowerCase().contains(query)).forEach(System.out::println);
        appMemory.zdarzenia.stream().filter(z -> z.getTytul().toLowerCase().contains(query)).forEach(System.out::println);
    }

    private static void dataManagementMenu() {
        while (true) {
            System.out.println("\n[DANE]: 1. RAM (Czyść) | 2. XML | 3. SQL | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> { appMemory.kontakty.clear(); appMemory.zdarzenia.clear(); System.out.println("[OK] RAM wyczyszczony."); }
                case "2" -> { 
                    System.out.print("1. Zapis | 2. Odczyt: ");
                    String s = sc.nextLine();
                    if (s.equals("1")) saveToXml(); else if (s.equals("2")) loadFromXml();
                }
                case "3" -> {
                    System.out.print("1. Eksport | 2. Import: ");
                    String s = sc.nextLine();
                    if (s.equals("1")) DBManager.saveAll(appMemory.kontakty, appMemory.zdarzenia);
                    else if (s.equals("2")) DBManager.loadInto(appMemory);
                }
            }
        }
    }

    private static void showKontakty() {
        System.out.println("\n--- KONTAKTY ---");
        for (int i = 0; i < appMemory.kontakty.size(); i++) System.out.println("[" + i + "] " + appMemory.kontakty.get(i));
    }

    private static void showZdarzenia() {
        System.out.println("\n--- ZDARZENIA ---");
        for (int i = 0; i < appMemory.zdarzenia.size(); i++) System.out.println("[" + i + "] " + appMemory.zdarzenia.get(i));
    }

    private static void saveToXml() {
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            m.enable(SerializationFeature.INDENT_OUTPUT);
            m.writeValue(new File(FILE_PATH), appMemory);
            System.out.println("[OK] XML zapisany.");
        } catch (Exception e) { System.out.println("[X] Błąd zapisu XML."); }
    }

    private static void loadFromXml() {
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            appMemory = m.readValue(new File(FILE_PATH), MemoryContainer.class);
            System.out.println("[OK] XML wczytany.");
        } catch (Exception e) { }
    }

    private static void editRootMenu() {
        System.out.println("[!] Edycja atrybutów dostępna przez index (logika T3-T4).");
    }

    private static void seedData() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            for(int i=0; i<10; i++) {
                Kontakt k = new Kontakt("Jan"+i, "Kowalski"+i, pu.parse("50010020"+i, "PL"), new InternetAddress("user"+i+"@test.pl"));
                Zdarzenie z = new Zdarzenie("Tytul"+i, "Opis", LocalDate.now().plusDays(i), URI.create("http://link"+i+".pl").toURL());
                appMemory.kontakty.add(k);
                appMemory.zdarzenia.add(z);
            }
            System.out.println("[OK] Załadowano dane testowe.");
        } catch (Exception e) {}
    }
}