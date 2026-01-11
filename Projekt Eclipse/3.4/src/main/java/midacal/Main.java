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
    private static DataContainer db = new DataContainer();
    private static final String FILE_PATH = "midacalXML.xml";
    private static final Scanner sc = new Scanner(System.in);

    public static class DataContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (new File(FILE_PATH).exists()) loadFromXml();
        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== KALENDARZ MIDACAL ===");
            System.out.println("1. Wyświetl\n2. Sortuj\n3. Edytuj rekord\n4. Dane (RAM/XML)\nX. Wyjście");
            System.out.print("Wybór: ");
            String choice = sc.nextLine().toLowerCase();
            switch (choice) {
                case "1" -> displayMenu();
                case "2" -> sortMenu();
                case "3" -> editRootMenu();
                case "4" -> dataManagementMenu();
                case "x" -> handleExit();
            }
        }
    }

    private static void handleExit() {
        if (db.kontakty.isEmpty() && db.zdarzenia.isEmpty()) {
            System.out.println("[!] RAM pusty - nic nie zapisano. Do widzenia!");
        } else {
            saveToXml();
            System.out.println("Dane zapisane. Do widzenia!");
        }
        System.exit(0);
    }

    // --- 1. WYŚWIETL ---
    private static void displayMenu() {
        if (db.kontakty.isEmpty() && db.zdarzenia.isEmpty()) {
            System.out.println("\n[!] Pamięć pusta. 1. Dodaj ręcznie | 2. Załaduj 10+10 (Hard-coding) | x. Wstecz");
            String choice = sc.nextLine().toLowerCase();
            if (choice.equals("1")) addManualKontakt(); 
            else if (choice.equals("2")) seedData(); 
            return;
        }
        while (true) {
            System.out.println("\n[Wyświetl]: 1. Wszystko | 2. Kontakty | 3. Zdarzenia | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            switch (c) {
                case "1" -> { showKontakty(); showZdarzenia(); }
                case "2" -> showKontakty();
                case "3" -> showZdarzenia();
            }
        }
    }

    // --- 2. SORTUJ ---
    private static void sortMenu() {
        while (true) {
            System.out.println("\n[Sortuj]: 1. Domyślnie (Comparable) | 2. Po wybranym (Comparator) | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            switch (c) {
                case "1" -> {
                    System.out.println("1. Kontakty | 2. Zdarzenia | x. Wstecz");
                    String s = sc.nextLine().toLowerCase();
                    if (s.equals("1")) { Collections.sort(db.kontakty); showKontakty(); }
                    if (s.equals("2")) { Collections.sort(db.zdarzenia); showZdarzenia(); }
                }
                case "2" -> comparatorMenu();
            }
        }
    }

    private static void comparatorMenu() {
        while (true) {
            System.out.println("\n[Comparator]: 1. Kontakty | 2. Zdarzenia | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            if (c.equals("1")) {
                System.out.println("1. imie | 2. numer | 3. e-mail | x. Wstecz");
                String s = sc.nextLine().toLowerCase();
                if (s.equals("1")) db.kontakty.sort(new Kontakt.ImieComparator());
                else if (s.equals("2")) db.kontakty.sort(new Kontakt.TelComparator());
                else if (s.equals("3")) db.kontakty.sort(new Kontakt.EmailComparator());
                showKontakty();
            } else if (c.equals("2")) {
                System.out.println("1. tytul | 2. opis | 3. link | x. Wstecz");
                String s = sc.nextLine().toLowerCase();
                if (s.equals("1")) db.zdarzenia.sort(new Zdarzenie.TytulComparator());
                else if (s.equals("2")) db.zdarzenia.sort(new Zdarzenie.OpisComparator());
                else if (s.equals("3")) db.zdarzenia.sort(new Zdarzenie.LinkComparator());
                showZdarzenia();
            }
        }
    }

    // --- 3. EDYTUJ REKORD ---
    private static void editRootMenu() {
        while (true) {
            System.out.println("\n[Edytuj]: 1. Kontakty | 2. Zdarzenia | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            switch (c) {
                case "1" -> manageKontakty();
                case "2" -> manageZdarzenia();
            }
        }
    }

    private static void manageKontakty() {
        while (true) {
            System.out.println("\n[Menu Kontakty]: 1. Utwórz | 2. Wybierz (Edycja) | 3. Usuń | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            switch (c) {
                case "1" -> addManualKontakt();
                case "2" -> editKontaktAtrybuty();
                case "3" -> { showKontakty(); System.out.print("Indeks: "); try { db.kontakty.remove(Integer.parseInt(sc.nextLine())); } catch(Exception e){} }
            }
        }
    }

    private static void editKontaktAtrybuty() {
        showKontakty(); System.out.print("Wybierz indeks: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            Kontakt k = db.kontakty.get(idx);
            while (true) {
                System.out.println("\n[Atrybuty]: 1. wszystko | 2. imie | 3. nazwisko | 4. numer | 5. e-mail | x. Wstecz");
                String a = sc.nextLine().toLowerCase();
                if (a.equals("x")) return;
                if (a.equals("1") || a.equals("2")) { System.out.print("Imie: "); k.setImie(sc.nextLine()); }
                if (a.equals("1") || a.equals("3")) { System.out.print("Nazwisko: "); k.setNazwisko(sc.nextLine()); }
                if (a.equals("1") || a.equals("4")) { System.out.print("Numer: "); k.setTelStr(sc.nextLine()); }
                if (a.equals("1") || a.equals("5")) { System.out.print("E-mail: "); k.setEmailStr(sc.nextLine()); }
            }
        } catch (Exception e) {}
    }

    private static void manageZdarzenia() {
        while (true) {
            System.out.println("\n[Menu Zdarzenia]: 1. Utwórz | 2. Wybierz (Edycja) | 3. Usuń | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            switch (c) {
                case "1" -> addManualZdarzenie();
                case "2" -> editZdarzenieAtrybuty();
                case "3" -> { showZdarzenia(); System.out.print("Indeks: "); try { db.zdarzenia.remove(Integer.parseInt(sc.nextLine())); } catch(Exception e){} }
            }
        }
    }

    private static void editZdarzenieAtrybuty() {
        showZdarzenia(); System.out.print("Wybierz indeks: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            Zdarzenie z = db.zdarzenia.get(idx);
            while (true) {
                System.out.println("\n[Atrybuty]: 1. wszystko | 2. tytul | 3. opis | 4. data | 5. miejsce | x. Wstecz");
                String a = sc.nextLine().toLowerCase();
                if (a.equals("x")) return;
                if (a.equals("1") || a.equals("2")) { System.out.print("Tytul: "); z.setTytul(sc.nextLine()); }
                if (a.equals("1") || a.equals("3")) { System.out.print("Opis: "); z.setOpis(sc.nextLine()); }
                if (a.equals("1") || a.equals("4")) { System.out.print("Data (RRRR-MM-DD): "); z.setData(LocalDate.parse(sc.nextLine())); }
                if (a.equals("1") || a.equals("5")) { System.out.print("Link: "); try { z.setMiejsce(URI.create(sc.nextLine()).toURL()); } catch(Exception e){} }
            }
        } catch (Exception e) {}
    }

    // --- 4. DANE (RAM/XML) ---
    private static void dataManagementMenu() {
        while (true) {
            System.out.println("\n[Dane]: 1. RAM | 2. XML | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            if (c.equals("1")) {
                System.out.println("1. Usuń dane z pamięci | x. Wstecz");
                if (sc.nextLine().equals("1")) { db.kontakty.clear(); db.zdarzenia.clear(); System.out.println("RAM wyczyszczony."); }
            } else if (c.equals("2")) {
                System.out.println("1. Zapisz | 2. Wczytaj | 3. Usuń plik XML | x. Wstecz");
                String x = sc.nextLine().toLowerCase();
                switch (x) {
                    case "1" -> saveToXml();
                    case "2" -> loadFromXml();
                    case "3" -> { System.gc(); if(new File(FILE_PATH).delete()) System.out.println("Plik usunięty."); }
                }
            }
        }
    }

    // --- POMOCNICZE ---
    private static void showKontakty() {
        System.out.println("\n--- LISTA KONTAKTÓW ---");
        if (db.kontakty.isEmpty()) System.out.println("(brak)");
        else for (int i = 0; i < db.kontakty.size(); i++) System.out.println("[" + i + "] " + db.kontakty.get(i));
    }

    private static void showZdarzenia() {
        System.out.println("\n--- LISTA ZDARZEŃ ---");
        if (db.zdarzenia.isEmpty()) System.out.println("(brak)");
        else for (int i = 0; i < db.zdarzenia.size(); i++) System.out.println("[" + i + "] " + db.zdarzenia.get(i));
    }

    private static void saveToXml() {
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            m.enable(SerializationFeature.INDENT_OUTPUT);
            m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            m.writeValue(new File(FILE_PATH), db);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadFromXml() {
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            db = m.readValue(new File(FILE_PATH), DataContainer.class);
            System.out.println("XML wczytany.");
        } catch (Exception e) { System.out.println("Błąd XML."); }
    }

    private static void addManualKontakt() {
        try {
            System.out.print("Imie: "); String i = sc.nextLine();
            System.out.print("Nazwisko: "); String n = sc.nextLine();
            System.out.print("Tel: "); String t = sc.nextLine();
            System.out.print("Email: "); String e = sc.nextLine();
            db.kontakty.add(new Kontakt(i, n, PhoneNumberUtil.getInstance().parse(t, "PL"), new InternetAddress(e)));
        } catch (Exception ex) {}
    }

    private static void addManualZdarzenie() {
        try {
            System.out.print("Tytul: "); String t = sc.nextLine();
            db.zdarzenia.add(new Zdarzenie(t, "Opis", LocalDate.now(), URI.create("http://link.pl").toURL()));
        } catch (Exception ex) {}
    }

    private static void seedData() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            // --- 10 KONTAKTÓW (HARD-CODED) ---
            db.kontakty.add(new Kontakt("Adam", "Mickiewicz", pu.parse("501111222", "PL"), new InternetAddress("adam@pan-tadeusz.pl")));
            db.kontakty.add(new Kontakt("Juliusz", "Słowacki", pu.parse("602222333", "PL"), new InternetAddress("julek@balladyna.pl")));
            db.kontakty.add(new Kontakt("Iga", "Świątek", pu.parse("703333444", "PL"), new InternetAddress("iga@tennis-champ.pl")));
            db.kontakty.add(new Kontakt("Robert", "Lewandowski", pu.parse("804444555", "PL"), new InternetAddress("rl9@lewy.pl")));
            db.kontakty.add(new Kontakt("Janusz", "Gajos", pu.parse("505555666", "PL"), new InternetAddress("janusz@aktor.pl")));
            db.kontakty.add(new Kontakt("Beata", "Kozidrak", pu.parse("606666777", "PL"), new InternetAddress("beata@bajm.pl")));
            db.kontakty.add(new Kontakt("Fryderyk", "Chopin", pu.parse("707777888", "PL"), new InternetAddress("frycek@piano.pl")));
            db.kontakty.add(new Kontakt("Maria", "Skłodowska", pu.parse("808888999", "PL"), new InternetAddress("maria@rad.pl")));
            db.kontakty.add(new Kontakt("Wisława", "Szymborska", pu.parse("509999000", "PL"), new InternetAddress("wisla@poezja.pl")));
            db.kontakty.add(new Kontakt("Mikołaj", "Kopernik", pu.parse("610123456", "PL"), new InternetAddress("mikolaj@niebo.pl")));

            // --- 10 ZDARZEŃ (HARD-CODED) ---
            db.zdarzenia.add(new Zdarzenie("Finał Roland Garros", "Mecz o puchar", LocalDate.of(2026, 6, 8), URI.create("https://rolandgarros.fr").toURL()));
            db.zdarzenia.add(new Zdarzenie("Premiera Filmowa", "Nowy polski kryminał", LocalDate.of(2026, 3, 15), URI.create("https://multikino.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Zjazd Absolwentów", "Spotkanie po latach", LocalDate.of(2026, 9, 20), URI.create("https://naszaklasa.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Koncert Bajm", "Trasa 45-lecie", LocalDate.of(2026, 11, 5), URI.create("https://ticketmaster.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Wizyta u lekarza", "Kontrola roczna", LocalDate.of(2026, 2, 10), URI.create("https://znanylekarz.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Urlop Zima", "Narty w Alpach", LocalDate.of(2026, 1, 15), URI.create("https://booking.com").toURL()));
            db.zdarzenia.add(new Zdarzenie("Egzamin Java", "Zaliczenie JDK 25", LocalDate.of(2026, 1, 30), URI.create("https://oracle.com").toURL()));
            db.zdarzenia.add(new Zdarzenie("Warsztaty Kulinarne", "Kuchnia tajska", LocalDate.of(2026, 5, 12), URI.create("https://cookandtalk.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Serwis Samochodu", "Wymiana oleju", LocalDate.of(2026, 4, 1), URI.create("https://aso.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Urodziny", "Przyjęcie niespodzianka", LocalDate.of(2026, 8, 25), URI.create("https://facebook.com/events").toURL()));

            System.out.println(">>> Pomyślnie załadowano 10 kontaktów i 10 zdarzeń (Hard-coding).");
        } catch (Exception e) {}
    }
}