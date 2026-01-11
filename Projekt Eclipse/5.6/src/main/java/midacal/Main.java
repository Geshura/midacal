package midacal;

import java.io.File;
import java.util.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Main {
    public static MemoryContainer appMemory = new MemoryContainer();
    private static final String XML_PATH = "midacalXML.xml";
    private static final String APP_NAME = "KALENDARZ MIDACAL";
    private static final Scanner sc = new Scanner(System.in);

    public static class MemoryContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        DBManager.initDatabase();
        // 1. POBRANIE DANYCH NA POCZĄTKU (Otwiera i zamyka bazę)
        DBManager.loadFromDatabase(appMemory);
        
        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== " + APP_NAME + " ===");
            System.out.println("1. Wyświetl | 2. Szukaj | 3. Edytuj | 4. Relacje (Uczestnicy) | 5. Dane (XML/Baza) | X. Wyjście");
            System.out.print("Wybór: ");
            String choice = sc.nextLine().toUpperCase();
            switch (choice) {
                case "1" -> displayMenu();
                case "4" -> manageRelations();
                case "5" -> dataManagementMenu();
                case "X" -> handleExit();
            }
        }
    }

    private static void handleExit() {
        // 2. AUTOMATYCZNY ZAPIS NA KONIEC (Otwiera i zamyka bazę)
        if (!appMemory.kontakty.isEmpty() || !appMemory.zdarzenia.isEmpty()) {
            DBManager.saveToDatabase(appMemory.kontakty, appMemory.zdarzenia);
        }
        System.out.println("[OK] Aplikacja zakończona.");
        System.exit(0);
    }

    private static void dataManagementMenu() {
        System.out.println("\n[DANE]: 1. Zapisz tymczasowy XML | 2. Wyczyść RAM | X. Wstecz");
        String c = sc.nextLine().toUpperCase();
        if (c.equals("1")) saveToXml();
        else if (c.equals("2")) { 
            appMemory.kontakty.clear(); appMemory.zdarzenia.clear(); 
            System.out.println("[OK] Pamięć RAM wyczyszczona.");
        }
    }

    private static void saveToXml() {
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            m.enable(SerializationFeature.INDENT_OUTPUT);
            m.writeValue(new File(XML_PATH), appMemory);
            System.out.println("[OK] Tymczasowa kopia XML utworzona.");
        } catch (Exception e) { System.out.println("[X] Błąd XML."); }
    }

    private static void manageRelations() {
        try {
            showKontakty();
            System.out.print("Index kontaktu: ");
            int kid = Integer.parseInt(sc.nextLine());
            showZdarzenia();
            System.out.print("Index zdarzenia: ");
            int zid = Integer.parseInt(sc.nextLine());
            appMemory.zdarzenia.get(zid).dodajUczestnika(appMemory.kontakty.get(kid));
            System.out.println("[OK] Powiązano uczestnika.");
        } catch (Exception e) { System.out.println("[X] Błąd wyboru."); }
    }

    private static void displayMenu() {
        showKontakty();
        showZdarzenia();
    }

    private static void showKontakty() {
        System.out.println("\n--- KONTAKTY ---");
        for (int i=0; i<appMemory.kontakty.size(); i++) System.out.println("["+i+"] " + appMemory.kontakty.get(i));
    }

    private static void showZdarzenia() {
        System.out.println("\n--- ZDARZENIA ---");
        for (int i=0; i<appMemory.zdarzenia.size(); i++) System.out.println("["+i+"] " + appMemory.zdarzenia.get(i));
    }
}