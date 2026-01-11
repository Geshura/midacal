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
    private static MemoryContainer appMemory = new MemoryContainer();
    private static final String FILE_PATH = "midacal_v2.xml";
    private static final Scanner sc = new Scanner(System.in);

    public static class MemoryContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (new File(FILE_PATH).exists()) loadFromXml();
        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== SYSTEM MIDACAL (JDK 25) ===");
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
        if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
            System.out.println("[!] RAM pusty - nic nie zapisano. Do widzenia!");
        } else {
            saveToXml();
            System.out.println("Dane zapisane. Do widzenia!");
        }
        System.exit(0);
    }

    private static void displayMenu() {
        if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
            System.out.println("\n[!] Pamięć operacyjna jest pusta.");
            System.out.println("1. Przejdź do: Utwórz Kontakt\n2. Przejdź do: Utwórz Zdarzenie\n3. Załaduj 10+10 (Hard-coding)\nx. Wstecz");
            String choice = sc.nextLine().toLowerCase();
            switch (choice) {
                case "1" -> addManualKontakt();
                case "2" -> addManualZdarzenie();
                case "3" -> seedData();
            }
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

    private static void sortMenu() {
        while (true) {
            System.out.println("\n[Sortuj]: 1. Domyślnie (Comparable) | 2. Po wybranym (Comparator) | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            switch (c) {
                case "1" -> {
                    System.out.println("1. Kontakty | 2. Zdarzenia");
                    String s = sc.nextLine();
                    if (s.equals("1")) { Collections.sort(appMemory.kontakty); showKontakty(); }
                    if (s.equals("2")) { Collections.sort(appMemory.zdarzenia); showZdarzenia(); }
                }
                case "2" -> comparatorMenu();
            }
        }
    }

    private static void comparatorMenu() {
        while (true) {
            System.out.println("\n[Comparator]: 1. imie | 2. numer | 3. e-mail | 4. tytul | 5. opis | 6. link | x. Wstecz");
            String s = sc.nextLine().toLowerCase();
            if (s.equals("x")) return;
            switch(s) {
                case "1" -> appMemory.kontakty.sort(new Kontakt.ImieComparator());
                case "2" -> appMemory.kontakty.sort(new Kontakt.TelComparator());
                case "3" -> appMemory.kontakty.sort(new Kontakt.EmailComparator());
                case "4" -> appMemory.zdarzenia.sort(new Zdarzenie.TytulComparator());
                case "5" -> appMemory.zdarzenia.sort(new Zdarzenie.OpisComparator());
                case "6" -> appMemory.zdarzenia.sort(new Zdarzenie.LinkComparator());
            }
            showKontakty(); showZdarzenia();
        }
    }

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
                case "3" -> { showKontakty(); System.out.print("Indeks: "); try { appMemory.kontakty.remove(Integer.parseInt(sc.nextLine())); } catch(Exception e){} }
            }
        }
    }

    private static void editKontaktAtrybuty() {
        if (appMemory.kontakty.isEmpty()) return;
        showKontakty(); System.out.print("Wybierz indeks: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            Kontakt k = appMemory.kontakty.get(idx);
            System.out.println("1. wszystko | 2. imie | 3. nazwisko | 4. numer | 5. e-mail");
            String a = sc.nextLine();
            if (a.equals("1") || a.equals("2")) { System.out.print("Imie: "); k.setImie(sc.nextLine()); }
            if (a.equals("1") || a.equals("3")) { System.out.print("Nazwisko: "); k.setNazwisko(sc.nextLine()); }
            if (a.equals("1") || a.equals("4")) { System.out.print("Numer: "); k.setTelStr(sc.nextLine()); }
            if (a.equals("1") || a.equals("5")) { System.out.print("E-mail: "); k.setEmailStr(sc.nextLine()); }
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
                case "3" -> { showZdarzenia(); System.out.print("Indeks: "); try { appMemory.zdarzenia.remove(Integer.parseInt(sc.nextLine())); } catch(Exception e){} }
            }
        }
    }

    private static void editZdarzenieAtrybuty() {
        if (appMemory.zdarzenia.isEmpty()) return;
        showZdarzenia(); System.out.print("Wybierz indeks: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            Zdarzenie z = appMemory.zdarzenia.get(idx);
            System.out.println("1. wszystko | 2. tytul | 3. opis | 4. data | 5. miejsce");
            String a = sc.nextLine();
            if (a.equals("1") || a.equals("2")) { System.out.print("Tytul: "); z.setTytul(sc.nextLine()); }
            if (a.equals("1") || a.equals("3")) { System.out.print("Opis: "); z.setOpis(sc.nextLine()); }
            if (a.equals("1") || a.equals("4")) { System.out.print("Data (RRRR-MM-DD): "); z.setData(LocalDate.parse(sc.nextLine())); }
            if (a.equals("1") || a.equals("5")) { System.out.print("Link: "); z.setMiejsce(URI.create(sc.nextLine()).toURL()); }
        } catch (Exception e) {}
    }

    private static void dataManagementMenu() {
        while (true) {
            System.out.println("\n[Dane]: 1. RAM | 2. XML | x. Wstecz");
            String c = sc.nextLine().toLowerCase();
            if (c.equals("x")) return;
            if (c.equals("1")) {
                System.out.println("1. Usuń dane z pamięci | x. Wstecz");
                if (sc.nextLine().equals("1")) {
                    if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
                        System.out.println("[!] Pamięć RAM jest już pusta.");
                    } else {
                        appMemory.kontakty.clear(); appMemory.zdarzenia.clear();
                        System.out.println("RAM wyczyszczony.");
                    }
                }
            } else if (c.equals("2")) {
                System.out.println("1. Zapisz | 2. Wczytaj | 3. Usuń plik XML");
                String x = sc.nextLine();
                if (x.equals("1")) saveToXml();
                if (x.equals("2")) loadFromXml();
                if (x.equals("3")) { System.gc(); new File(FILE_PATH).delete(); System.out.println("Plik usunięty."); }
            }
        }
    }

    private static void showKontakty() {
        for (int i = 0; i < appMemory.kontakty.size(); i++) System.out.println("[" + i + "] " + appMemory.kontakty.get(i));
    }

    private static void showZdarzenia() {
        for (int i = 0; i < appMemory.zdarzenia.size(); i++) System.out.println("[" + i + "] " + appMemory.zdarzenia.get(i));
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
            System.out.println("XML zapisany.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadFromXml() {
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            appMemory = m.readValue(new File(FILE_PATH), MemoryContainer.class);
            System.out.println("XML wczytany.");
        } catch (Exception e) { System.out.println("Plik XML nie istnieje lub błąd formatu."); }
    }

    private static void addManualKontakt() {
        try {
            System.out.print("Imie: "); String i = sc.nextLine();
            System.out.print("Nazwisko: "); String n = sc.nextLine();
            System.out.print("Tel: "); String t = sc.nextLine();
            System.out.print("Email: "); String e = sc.nextLine();
            appMemory.kontakty.add(new Kontakt(i, n, PhoneNumberUtil.getInstance().parse(t, "PL"), new InternetAddress(e)));
            System.out.println("Dodano.");
        } catch (Exception ex) {}
    }

    private static void addManualZdarzenie() {
        try {
            System.out.print("Tytul: "); String t = sc.nextLine();
            appMemory.zdarzenia.add(new Zdarzenie(t, "Opis", LocalDate.now(), URI.create("http://man.pl").toURL()));
            System.out.println("Dodano.");
        } catch (Exception ex) {}
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

            appMemory.zdarzenia.add(new Zdarzenie("Finał RG", "Mecz", LocalDate.of(2026, 6, 8), URI.create("https://rg.fr").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Premiera", "Kryminał", LocalDate.of(2026, 3, 15), URI.create("https://kino.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Zjazd", "Spotkanie", LocalDate.of(2026, 9, 20), URI.create("https://nasza.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Bajm", "Koncert", LocalDate.of(2026, 11, 5), URI.create("https://ticket.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Lekarz", "Kontrola", LocalDate.of(2026, 2, 10), URI.create("https://med.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Urlop", "Narty", LocalDate.of(2026, 1, 15), URI.create("https://booking.com").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Java", "Zaliczenie", LocalDate.of(2026, 1, 30), URI.create("https://oracle.com").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Warsztaty", "Kuchnia", LocalDate.of(2026, 5, 12), URI.create("https://food.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Serwis", "Auto", LocalDate.of(2026, 4, 1), URI.create("https://aso.pl").toURL()));
            appMemory.zdarzenia.add(new Zdarzenie("Urodziny", "Impreza", LocalDate.of(2026, 8, 25), URI.create("https://fb.com").toURL()));
            System.out.println("Załadowano 20 rekordów testowych.");
        } catch (Exception e) {}
    }
}