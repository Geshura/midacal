package midacal;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import jakarta.mail.internet.InternetAddress;

public class Main {
    private static MemoryContainer appMemory = new MemoryContainer();
    private static final String FILE_PATH = "midacalXML.xml";
    private static final String APP_NAME = "KALENDARZ MIDACAL";
    private static final Scanner sc = new Scanner(System.in);
    private static final DBManager dbManager = new DBManager();
    private static int changeCounter = 0;
    private static final int CHANGE_INTERVAL = 10; // Co 10 zmian w RAM -> backup do DB

    public static class MemoryContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        System.out.println("=== URUCHAMIANIE KALENDARZA ===\n");
        
        // 1. Wczytaj z bazy danych (jeśli istnieje)
        System.out.println("[ETAP 1] Próba wczytania danych z bazy...");
        appMemory = dbManager.loadFromDatabase();
        
        // 2. Wczytaj z XML (jako backup/alternatywa)
        if (new File(FILE_PATH).exists()) {
            System.out.println("[ETAP 2] Znaleziono plik XML...");
            if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
                System.out.println("[INFO] Baza pusta, wczytywanie z XML...");
                loadFromXml();
            } else {
                System.out.println("[INFO] Dane już wczytane z bazy, pomijanie XML.");
            }
        }
        
        System.out.println("[GOTOWE] Aplikacja uruchomiona.\n");
        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== " + APP_NAME + " ===");
            System.out.println("1. Wyświetl\n2. Sortuj\n3. Edytuj rekord\n4. Dane (RAM/XML)\nX. Wyjście");
            System.out.print("Wybór: ");
            String choice = sc.nextLine().toUpperCase();
            switch (choice) {
                case "1" -> displayMenu();
                case "2" -> sortMenu();
                case "3" -> editRootMenu();
                case "4" -> dataManagementMenu();
                case "X" -> handleExit();
            }
        }
    }

    private static void handleExit() {
        if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
            System.out.println("[!] Pamięć jest pusta - nic nie zostało zapisane.");
        } else {
            System.out.println("[...] Zapisywanie danych przed zamknięciem...");
            saveToXml();
            
            // Zapis do bazy danych przed wyjściem
            System.out.println("[...] Synchronizacja z bazą danych...");
            dbManager.saveToDatabase(appMemory);
        }
        System.out.println("[OK] Aplikacja zakończona. Do widzenia!");
        System.exit(0);
    }

    // --- 1. WYŚWIETL ---
    private static void displayMenu() {
        if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
            System.out.println("\n[!] Pamięć operacyjna jest obecnie pusta.");
            System.out.println("1. Przejdź do: Utwórz Kontakt");
            System.out.println("2. Przejdź do: Utwórz Zdarzenie");
            System.out.println("3. Załaduj 10+10 (Hard-coding)");
            System.out.println("X. Wstecz");
            System.out.print("Wybór: ");
            String choice = sc.nextLine().toUpperCase();
            switch (choice) {
                case "1" -> addManualKontakt();
                case "2" -> addManualZdarzenie();
                case "3" -> seedData();
            }
            return;
        }
        while (true) {
            System.out.println("\n[WYŚWIETL]: 1. Wszystko | 2. Kontakty | 3. Zdarzenia | 4. Kontakty > Zdarzenia | 5. Zdarzenia > Kontakty | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> { showKontakty(); showZdarzenia(); }
                case "2" -> showKontakty();
                case "3" -> showZdarzenia();
                case "4" -> showKontaktyWithZdarzenia();
                case "5" -> showZdarzeniaWithKontakty();
            }
        }
    }

    // --- 2. SORTUJ ---
    private static void sortMenu() {
        while (true) {
            System.out.println("\n[SORTUJ]: 1. Domyślnie (Comparable) | 2. Po wybranym (Comparator) | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> {
                    System.out.println("1. Kontakty (Nazwisko) | 2. Zdarzenia (Data) | X. Wstecz");
                    String s = sc.nextLine().toUpperCase();
                    if (s.equals("1")) { Collections.sort(appMemory.kontakty); System.out.println("[OK] Posortowano kontakty."); showKontakty(); }
                    if (s.equals("2")) { Collections.sort(appMemory.zdarzenia); System.out.println("[OK] Posortowano zdarzenia."); showZdarzenia(); }
                }
                case "2" -> comparatorMenu();
            }
        }
    }

    private static void comparatorMenu() {
        while (true) {
            System.out.println("\n[SORTUJ PRZEZ COMPARATOR]: 1. Kontakty | 2. Zdarzenia | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            if (c.equals("1")) {
                System.out.println("1. Imię | 2. Numer | 3. E-mail | X. Wstecz");
                String s = sc.nextLine().toUpperCase();
                if (s.equals("1")) appMemory.kontakty.sort(new Kontakt.ImieComparator());
                else if (s.equals("2")) appMemory.kontakty.sort(new Kontakt.TelComparator());
                else if (s.equals("3")) appMemory.kontakty.sort(new Kontakt.EmailComparator());
                else continue;
                System.out.println("[OK] Posortowano."); showKontakty();
            } else if (c.equals("2")) {
                System.out.println("1. Tytuł | 2. Opis | 3. Link | X. Wstecz");
                String s = sc.nextLine().toUpperCase();
                if (s.equals("1")) appMemory.zdarzenia.sort(new Zdarzenie.TytulComparator());
                else if (s.equals("2")) appMemory.zdarzenia.sort(new Zdarzenie.OpisComparator());
                else if (s.equals("3")) appMemory.zdarzenia.sort(new Zdarzenie.LinkComparator());
                else continue;
                System.out.println("[OK] Posortowano."); showZdarzenia();
            }
        }
    }

    // --- 3. EDYTUJ REKORD ---
    private static void editRootMenu() {
        while (true) {
            System.out.println("\n[EDYTUJ]: 1. Kontakty | 2. Zdarzenia | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> manageKontakty();
                case "2" -> manageZdarzenia();
            }
        }
    }

    private static void manageKontakty() {
        while (true) {
            System.out.println("\n[MENU KONTAKTY]: 1. Utwórz | 2. Wybierz (Edycja) | 3. Usuń | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> addManualKontakt();
                case "2" -> editKontaktAtrybuty();
                case "3" -> performDelete("1");
            }
        }
    }

    private static void editKontaktAtrybuty() {
        if (appMemory.kontakty.isEmpty()) { System.out.println("[!] Brak rekordów do edycji."); return; }
        showKontakty(); System.out.print("Wybierz indeks kontaktu: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            Kontakt k = appMemory.kontakty.get(idx);
            while (true) {
                System.out.println("\n[EDYTUJ KONTAKT]: 1. Wszystko | 2. Imię | 3. Nazwisko | 4. Numer | 5. E-mail | 6. Zdarzenia | X. Wstecz");
                String a = sc.nextLine().toUpperCase();
                if (a.equals("X")) return;
                if (a.equals("1") || a.equals("2")) { System.out.print("Nowe imię: "); k.setImie(sc.nextLine()); }
                if (a.equals("1") || a.equals("3")) { System.out.print("Nowe nazwisko: "); k.setNazwisko(sc.nextLine()); }
                if (a.equals("1") || a.equals("4")) { System.out.print("Nowy numer: "); k.setTelStr(sc.nextLine()); }
                if (a.equals("1") || a.equals("5")) { System.out.print("Nowy e-mail: "); k.setEmailStr(sc.nextLine()); }
                if (a.equals("6")) { manageKontaktZdarzenia(k); }
                if (!a.equals("6")) System.out.println("[OK] Zaktualizowano pomyślnie.");
                if (!a.equals("6")) markChanged();
            }
        } catch (Exception e) { System.out.println("[X] Błąd indeksu."); }
    }

    private static void manageKontaktZdarzenia(Kontakt k) {
        while (true) {
            System.out.println("\n[ZDARZENIA KONTAKTU]: " + k.getNazwisko() + " " + k.getImie());
            System.out.println("1. Pokaż zdarzenia | 2. Dodaj zdarzenie | 3. Usuń zdarzenie | X. Wstecz");
            String choice = sc.nextLine().toUpperCase();
            if (choice.equals("X")) return;
            
            switch (choice) {
                case "1" -> {
                    List<Zdarzenie> zdarzenia = k.getZdarzenia();
                    if (zdarzenia == null || zdarzenia.isEmpty()) {
                        System.out.println("   — brak zdarzeń");
                    } else {
                        System.out.println("   Zdarzenia:");
                        for (int i = 0; i < zdarzenia.size(); i++) {
                            Zdarzenie z = zdarzenia.get(i);
                            System.out.println("   [" + i + "] [" + z.getData() + "] " + z.getTytul());
                        }
                    }
                }
                case "2" -> {
                    if (appMemory.zdarzenia.isEmpty()) {
                        System.out.println("[!] Brak zdarzeń w systemie.");
                    } else {
                        showZdarzenia();
                        System.out.print("Wybierz indeks zdarzenia do dodania: ");
                        try {
                            int idx = Integer.parseInt(sc.nextLine());
                            Zdarzenie z = appMemory.zdarzenia.get(idx);
                            k.dodajZdarzenie(z);
                            z.dodajKontakt(k);
                            System.out.println("[OK] " + z.getTytul() + " dodane do kontaktu.");
                            markChanged();
                        } catch (Exception e) {
                            System.out.println("[X] Błąd indeksu.");
                        }
                    }
                }
                case "3" -> {
                    List<Zdarzenie> zdarzenia = k.getZdarzenia();
                    if (zdarzenia == null || zdarzenia.isEmpty()) {
                        System.out.println("   — brak zdarzeń do usunięcia");
                    } else {
                        System.out.println("   Obecne zdarzenia:");
                        for (int i = 0; i < zdarzenia.size(); i++) {
                            Zdarzenie z = zdarzenia.get(i);
                            System.out.println("   [" + i + "] [" + z.getData() + "] " + z.getTytul());
                        }
                        System.out.print("Wybierz indeks zdarzenia do usunięcia: ");
                        try {
                            int idx = Integer.parseInt(sc.nextLine());
                            Zdarzenie z = zdarzenia.get(idx);
                            k.usunZdarzenie(z);
                            z.usunKontakt(k);
                            System.out.println("[OK] " + z.getTytul() + " usunięte z kontaktu.");
                            markChanged();
                        } catch (Exception e) {
                            System.out.println("[X] Błąd indeksu.");
                        }
                    }
                }
            }
        }
    }

    private static void manageZdarzenia() {
        while (true) {
            System.out.println("\n[MENU ZDARZENIA]: 1. Utwórz | 2. Wybierz (Edycja) | 3. Usuń | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> addManualZdarzenie();
                case "2" -> editZdarzenieAtrybuty();
                case "3" -> performDelete("2");
            }
        }
    }

    private static void editZdarzenieAtrybuty() {
        if (appMemory.zdarzenia.isEmpty()) { System.out.println("[!] Brak rekordów do edycji."); return; }
        showZdarzenia(); System.out.print("Wybierz indeks zdarzenia: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            Zdarzenie z = appMemory.zdarzenia.get(idx);
            while (true) {
                System.out.println("\n[EDYTUJ ZDARZENIE]: 1. Wszystko | 2. Tytuł | 3. Opis | 4. Data | 5. Miejsce | 6. Uczestnicy | X. Wstecz");
                String a = sc.nextLine().toUpperCase();
                if (a.equals("X")) return;
                if (a.equals("1") || a.equals("2")) { System.out.print("Nowy tytuł: "); z.setTytul(sc.nextLine()); }
                if (a.equals("1") || a.equals("3")) { System.out.print("Nowy opis: "); z.setOpis(sc.nextLine()); }
                if (a.equals("1") || a.equals("4")) { System.out.print("Nowa data (RRRR-MM-DD): "); z.setData(LocalDate.parse(sc.nextLine())); }
                if (a.equals("1") || a.equals("5")) { System.out.print("Nowy link: "); try { z.setMiejsce(URI.create(sc.nextLine()).toURL()); } catch(Exception e){} }
                if (a.equals("6")) { manageZdarzenieUczestnicy(z); }
                if (!a.equals("6")) System.out.println("[OK] Zaktualizowano pomyślnie.");
                if (!a.equals("6")) markChanged();
            }
        } catch (Exception e) { System.out.println("[X] Błąd danych."); }
    }

    // --- 4. DANE (RAM/XML) ---
    private static void manageZdarzenieUczestnicy(Zdarzenie z) {
        while (true) {
            System.out.println("\n[UCZESTNICY ZDARZENIA]: " + z.getTytul());
            System.out.println("1. Pokaż uczestników | 2. Dodaj kontakt | 3. Usuń kontakt | X. Wstecz");
            String choice = sc.nextLine().toUpperCase();
            if (choice.equals("X")) return;
            
            switch (choice) {
                case "1" -> {
                    List<Kontakt> uczestnicy = z.getKontakty();
                    if (uczestnicy == null || uczestnicy.isEmpty()) {
                        System.out.println("   — brak uczestników");
                    } else {
                        System.out.println("   Uczestnicy:");
                        for (int i = 0; i < uczestnicy.size(); i++) {
                            Kontakt k = uczestnicy.get(i);
                            System.out.println("   [" + i + "] " + k.getNazwisko() + " " + k.getImie());
                        }
                    }
                }
                case "2" -> {
                    if (appMemory.kontakty.isEmpty()) {
                        System.out.println("[!] Brak kontaktów w systemie.");
                    } else {
                        showKontakty();
                        System.out.print("Wybierz indeks kontaktu do dodania: ");
                        try {
                            int idx = Integer.parseInt(sc.nextLine());
                            Kontakt k = appMemory.kontakty.get(idx);
                            z.dodajKontakt(k);
                            k.dodajZdarzenie(z);
                            System.out.println("[OK] " + k.getNazwisko() + " dodany do zdarzenia.");
                            markChanged();
                        } catch (Exception e) {
                            System.out.println("[X] Błąd indeksu.");
                        }
                    }
                }
                case "3" -> {
                    List<Kontakt> uczestnicy = z.getKontakty();
                    if (uczestnicy == null || uczestnicy.isEmpty()) {
                        System.out.println("   — brak uczestników do usunięcia");
                    } else {
                        System.out.println("   Obecni uczestnicy:");
                        for (int i = 0; i < uczestnicy.size(); i++) {
                            Kontakt k = uczestnicy.get(i);
                            System.out.println("   [" + i + "] " + k.getNazwisko() + " " + k.getImie());
                        }
                        System.out.print("Wybierz indeks uczestnika do usunięcia: ");
                        try {
                            int idx = Integer.parseInt(sc.nextLine());
                            Kontakt k = uczestnicy.get(idx);
                            z.usunKontakt(k);
                            k.usunZdarzenie(z);
                            System.out.println("[OK] " + k.getNazwisko() + " usunięty ze zdarzenia.");
                            markChanged();
                        } catch (Exception e) {
                            System.out.println("[X] Błąd indeksu.");
                        }
                    }
                }
            }
        }
    }

    // --- 4. DANE (RAM/XML) ---
    private static void dataManagementMenu() {
        while (true) {
            System.out.println("\n[DANE]: 1. RAM | 2. XML | 3. BAZA DANYCH | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            if (c.equals("1")) {
                System.out.println("1. Usuń dane z pamięci | X. Wstecz");
                if (sc.nextLine().equals("1")) {
                    if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
                        System.out.println("[!] Pamięć RAM jest już pusta.");
                    } else {
                        appMemory.kontakty.clear(); appMemory.zdarzenia.clear();
                        System.out.println("[OK] Pamięć RAM została wyczyszczona.");
                    }
                }
            } else if (c.equals("2")) {
                System.out.println("1. Zapisz | 2. Wczytaj | 3. Usuń plik XML | X. Wstecz");
                String x = sc.nextLine().toUpperCase();
                switch (x) {
                    case "1" -> saveToXml();
                    case "2" -> loadFromXml();
                    case "3" -> {
                        File f = new File(FILE_PATH);
                        System.gc();
                        if (f.exists() && f.delete()) System.out.println("[OK] Plik " + FILE_PATH + " został usunięty.");
                        else System.out.println("[X] Błąd: Plik nie istnieje lub jest zablokowany.");
                    }
                }
            } else if (c.equals("3")) {
                System.out.println("\n[BAZA DANYCH]");
                System.out.println("1. Zapisz do bazy | 2. Wczytaj z bazy | X. Wstecz");
                String db = sc.nextLine().toUpperCase();
                switch (db) {
                    case "1" -> {
                        System.out.println("[...] Zapisywanie do bazy danych...");
                        dbManager.saveToDatabase(appMemory);
                    }
                    case "2" -> {
                        System.out.println("[...] Wczytywanie z bazy danych...");
                        appMemory = dbManager.loadFromDatabase();
                    }
                }
            }
        }
    }

    // --- POMOCNICZE ---
    private static void showKontakty() {
        System.out.println("\n--- KONTAKTY ---");
        if (appMemory.kontakty.isEmpty()) System.out.println("(lista pusta)");
        else for (int i = 0; i < appMemory.kontakty.size(); i++) System.out.println("[" + i + "] " + appMemory.kontakty.get(i));
    }

    private static void showZdarzenia() {
        System.out.println("\n--- ZDARZENIA ---");
        if (appMemory.zdarzenia.isEmpty()) System.out.println("(lista pusta)");
        else for (int i = 0; i < appMemory.zdarzenia.size(); i++) System.out.println("[" + i + "] " + appMemory.zdarzenia.get(i));
    }

    private static void showKontaktyWithZdarzenia() {
        System.out.println("\n--- KONTAKTY → ZDARZENIA ---");
        if (appMemory.kontakty.isEmpty()) { System.out.println("(lista kontaktów pusta)"); return; }
        for (int i = 0; i < appMemory.kontakty.size(); i++) {
            Kontakt k = appMemory.kontakty.get(i);
            System.out.println("[" + i + "] " + k);
            List<Zdarzenie> list = k.getZdarzenia();
            if (list == null || list.isEmpty()) {
                System.out.println("   — brak zdarzeń");
            } else {
                for (Zdarzenie z : list) {
                    System.out.println("   • [" + z.getData() + "] " + z.getTytul());
                }
            }
        }
    }

    private static void showZdarzeniaWithKontakty() {
        System.out.println("\n--- ZDARZENIA → KONTAKTY ---");
        if (appMemory.zdarzenia.isEmpty()) { System.out.println("(lista zdarzeń pusta)"); return; }
        for (int i = 0; i < appMemory.zdarzenia.size(); i++) {
            Zdarzenie z = appMemory.zdarzenia.get(i);
            System.out.println("[" + i + "] " + z);
            List<Kontakt> list = z.getKontakty();
            if (list == null || list.isEmpty()) {
                System.out.println("   — brak uczestników");
            } else {
                for (Kontakt k : list) {
                    System.out.println("   • " + k.getNazwisko() + " " + k.getImie());
                }
            }
        }
    }

    private static void performDelete(String type) {
        if (type.equals("1")) showKontakty(); else showZdarzenia();
        System.out.print("Podaj indeks do usunięcia: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            System.out.print("Potwierdzasz usunięcie? (T/N): ");
            if (sc.nextLine().equalsIgnoreCase("T")) {
                if (type.equals("1")) appMemory.kontakty.remove(idx);
                else appMemory.zdarzenia.remove(idx);
                System.out.println("[OK] Rekord został usunięty.");
                markChanged();
            }
        } catch (Exception e) { System.out.println("[X] Błąd indeksu."); }
    }

    private static void saveToXml() {
        if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
            System.out.println("[!] Brak danych w RAM - zapis przerwany.");
            return;
        }
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            m.enable(SerializationFeature.INDENT_OUTPUT);
            m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            m.writeValue(new File(FILE_PATH), appMemory);
            System.out.println("[OK] Dane zostały zapisane do pliku " + FILE_PATH);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadFromXml() {
        File f = new File(FILE_PATH);
        if (!f.exists()) {
            System.out.println("[!] Plik XML nie istnieje - nie można wczytać danych.");
            return;
        }
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            appMemory = m.readValue(f, MemoryContainer.class);
            System.out.println("[OK] Dane zostały wczytane pomyślnie.");
        } catch (Exception e) { System.out.println("[X] Błąd podczas wczytywania pliku XML."); }
    }

    private static void addManualKontakt() {
        System.out.println("\n--- NOWY KONTAKT ---");
        try {
            System.out.print("Imię: "); String i = sc.nextLine();
            System.out.print("Nazwisko: "); String n = sc.nextLine();
            System.out.print("Telefon: "); String t = sc.nextLine();
            System.out.print("E-mail: "); String e = sc.nextLine();
            appMemory.kontakty.add(new Kontakt(i, n, PhoneNumberUtil.getInstance().parse(t, "PL"), new InternetAddress(e)));
            System.out.println("[OK] Kontakt dodany.");
            markChanged();
        } catch (Exception ex) { System.out.println("[X] Błąd formatu danych."); }
    }

    private static void addManualZdarzenie() {
        System.out.println("\n--- NOWE ZDARZENIE ---");
        try {
            System.out.print("Tytuł: "); String t = sc.nextLine();
            System.out.print("Opis: "); String o = sc.nextLine();
            System.out.print("Data (RRRR-MM-DD): "); LocalDate d = LocalDate.parse(sc.nextLine());
            System.out.print("Link: "); URL u = URI.create(sc.nextLine()).toURL();
            appMemory.zdarzenia.add(new Zdarzenie(t, o, d, u));
            System.out.println("[OK] Zdarzenie dodane.");
            markChanged();
        } catch (Exception ex) { System.out.println("[X] Błąd formatu danych."); }
    }

    private static void seedData() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            appMemory.kontakty.add(new Kontakt("Adam", "Mickiewicz", pu.parse("501111222", "PL"), new InternetAddress("adam@wieszcz.pl")));
            appMemory.kontakty.add(new Kontakt("Juliusz", "Słowacki", pu.parse("602222333", "PL"), new InternetAddress("julek@balladyna.pl")));
            appMemory.kontakty.add(new Kontakt("Iga", "Świątek", pu.parse("703333444", "PL"), new InternetAddress("iga@sport.pl")));
            appMemory.kontakty.add(new Kontakt("Robert", "Lewandowski", pu.parse("804444555", "PL"), new InternetAddress("rl9@lewy.pl")));
            appMemory.kontakty.add(new Kontakt("Janusz", "Gajos", pu.parse("505555666", "PL"), new InternetAddress("janusz@aktor.pl")));
            appMemory.kontakty.add(new Kontakt("Beata", "Kozidrak", pu.parse("606666777", "PL"), new InternetAddress("beata@bajm.pl")));
            appMemory.kontakty.add(new Kontakt("Fryderyk", "Chopin", pu.parse("707777888", "PL"), new InternetAddress("frycek@piano.pl")));
            appMemory.kontakty.add(new Kontakt("Maria", "Skłodowska", pu.parse("808888999", "PL"), new InternetAddress("maria@rad.pl")));
            appMemory.kontakty.add(new Kontakt("Wisława", "Szymborska", pu.parse("509999000", "PL"), new InternetAddress("wisla@poezja.pl")));
            appMemory.kontakty.add(new Kontakt("Mikołaj", "Kopernik", pu.parse("610123456", "PL"), new InternetAddress("mikolaj@niebo.pl")));

            appMemory.zdarzenia.add(new Zdarzenie("Finał RG", "Mecz tenisowy", LocalDate.of(2026, 6, 8), URI.create("https://rg.fr").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Premiera", "Nowy kryminał", LocalDate.of(2026, 3, 15), URI.create("https://kino.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Zjazd", "Spotkanie klasowe", LocalDate.of(2026, 9, 20), URI.create("https://u.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Bajm", "Koncert jubileuszowy", LocalDate.of(2026, 11, 5), URI.create("https://ticket.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Lekarz", "Kontrola roczna", LocalDate.of(2026, 2, 10), URI.create("https://med.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Urlop", "Narty w Alpach", LocalDate.of(2026, 1, 15), URI.create("https://travel.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Egzamin", "Java JDK 25", LocalDate.of(2026, 1, 30), URI.create("https://oracle.com").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Warsztaty", "Kuchnia azjatycka", LocalDate.of(2026, 5, 12), URI.create("https://food.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Serwis", "Auto - olej", LocalDate.of(2026, 4, 1), URI.create("https://aso.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Urodziny", "Impreza domowa", LocalDate.of(2026, 8, 25), URI.create("https://fb.com").toURL()));
            System.out.println("[OK] Pomyślnie załadowano 20 rekordów testowych.");
        } catch (Exception e) {}
    }

    private static void markChanged() {
        incrementChanges(1);
    }

    private static void incrementChanges(int n) {
        changeCounter += Math.max(0, n);
        if (changeCounter >= CHANGE_INTERVAL) {
            System.out.println("[AUTO] Backup do bazy po " + changeCounter + " zmianach w RAM...");
            dbManager.saveToDatabase(appMemory);
            changeCounter = 0;
        }
    }
}