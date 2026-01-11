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
    private static final String FILE_PATH = "midacal_v2.xml";
    private static final Scanner sc = new Scanner(System.in);

    public static class DataContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (new File(FILE_PATH).exists()) {
            loadFromXml();
        }
        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n=== SYSTEM MIDACAL (JDK 25) ===");
            System.out.println("1. Wyświetl\n2. Sortuj\n3. Zarządzaj (Edycja/Usuwanie)\n4. XML\nX. Wyjście (zapisuje pamięć)");
            System.out.print("Wybór: ");
            String choice = sc.nextLine().toUpperCase();
            switch (choice) {
                case "1" -> displayMenu();
                case "2" -> sortMenu();
                case "3" -> manageMenu();
                case "4" -> xmlMenu();
                case "X" -> { 
                    System.out.println("Automatyczny zapis danych...");
                    saveToXml(); 
                    System.out.println("Zamknięto aplikację.");
                    System.exit(0); 
                }
            }
        }
    }

    private static void displayMenu() {
        if (db.kontakty.isEmpty() && db.zdarzenia.isEmpty()) {
            System.out.println("\n[!] Pamięć operacyjna jest pusta.");
            System.out.println("1. Dodaj pojedynczy rekord ręcznie\n2. Załaduj 10+10 (Hard-coding)\nX. Wstecz");
            String emptyChoice = sc.nextLine().toUpperCase();
            if (emptyChoice.equals("1")) { addManualEntry(); return; } 
            else if (emptyChoice.equals("2")) { seedData(); } 
            else return;
        }

        while (true) {
            System.out.println("\n[Wyświetl]: 1. Wszystko | 2. Kontakty | 3. Zdarzenia | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> { showKontakty(); showZdarzenia(); }
                case "2" -> showKontakty();
                case "3" -> showZdarzenia();
            }
        }
    }

    private static void sortMenu() {
        if (db.kontakty.isEmpty() && db.zdarzenia.isEmpty()) {
            System.out.println("Brak danych do sortowania.");
            return;
        }
        while (true) {
            System.out.println("\n[Sortuj]: 1. Domyślnie | 2. Wybrane (Comparator) | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            if (c.equals("1")) {
                System.out.println("1. Kontakty (Nazwisko) | 2. Zdarzenia (Data)");
                String c2 = sc.nextLine();
                if (c2.equals("1")) { Collections.sort(db.kontakty); showKontakty(); }
                if (c2.equals("2")) { Collections.sort(db.zdarzenia); showZdarzenia(); }
            } else if (c.equals("2")) {
                comparatorSubMenu();
            }
        }
    }

    private static void comparatorSubMenu() {
        System.out.println("\n[Sortuj przez Comparator]:");
        System.out.println("1. Kontakty: Imię | 2. Kontakty: Telefon | 3. Kontakty: E-mail");
        System.out.println("4. Zdarzenia: Tytuł | 5. Zdarzenia: Opis | 6. Zdarzenia: Link | X. Wstecz");
        String c = sc.nextLine().toUpperCase();
        switch(c) {
            case "1" -> db.kontakty.sort(new Kontakt.ImieComparator());
            case "2" -> db.kontakty.sort(new Kontakt.TelComparator());
            case "3" -> db.kontakty.sort(new Kontakt.EmailComparator());
            case "4" -> db.zdarzenia.sort(new Zdarzenie.TytulComparator());
            case "5" -> db.zdarzenia.sort(new Zdarzenie.OpisComparator());
            case "6" -> db.zdarzenia.sort(new Zdarzenie.LinkComparator());
        }
        if(!c.equals("X")) { System.out.println("Posortowano."); showKontakty(); showZdarzenia(); }
    }

    private static void manageMenu() {
        while (true) {
            System.out.println("\n[Zarządzaj]: 1. Edytuj | 2. Usuń wybrany | 3. WYCZYŚĆ CAŁĄ PAMIĘĆ | X. Wstecz");
            String choice = sc.nextLine().toUpperCase();
            if (choice.equals("X")) return;
            
            if (choice.equals("3")) {
                System.out.print("CZY NA PEWNO WYCZYŚCIĆ WSZYSTKO Z RAM? (T/N): ");
                if (sc.nextLine().equalsIgnoreCase("T")) {
                    db.kontakty.clear();
                    db.zdarzenia.clear();
                    System.out.println("Pamięć została wyczyszczona (plik XML pozostaje bez zmian do momentu zapisu/wyjścia).");
                }
                return;
            }

            if (db.kontakty.isEmpty() && db.zdarzenia.isEmpty()) {
                System.out.println("Brak rekordów do modyfikacji.");
                return;
            }

            System.out.println("Dotyczy: 1. Kontakt | 2. Zdarzenie");
            String target = sc.nextLine();
            if (choice.equals("1")) performEdit(target);
            else if (choice.equals("2")) performDelete(target);
        }
    }

    private static void performEdit(String target) {
        if (target.equals("1")) showKontakty(); else showZdarzenia();
        System.out.print("Podaj indeks: ");
        try {
            int i = Integer.parseInt(sc.nextLine());
            if (target.equals("1")) {
                System.out.print("Nowe imię: "); db.kontakty.get(i).setImie(sc.nextLine());
                System.out.print("Nowe nazwisko: "); db.kontakty.get(i).setNazwisko(sc.nextLine());
            } else {
                System.out.print("Nowy tytuł: "); db.zdarzenia.get(i).setTytul(sc.nextLine());
            }
            System.out.println("Zaktualizowano.");
        } catch (Exception e) { System.out.println("Błąd indeksu."); }
    }

    private static void performDelete(String target) {
        if (target.equals("1")) showKontakty(); else showZdarzenia();
        System.out.print("Indeks do usunięcia: ");
        try {
            int i = Integer.parseInt(sc.nextLine());
            System.out.print("Potwierdź (T/N): ");
            if (sc.nextLine().equalsIgnoreCase("T")) {
                if (target.equals("1")) db.kontakty.remove(i);
                else db.zdarzenia.remove(i);
                System.out.println("Usunięto rekord.");
            }
        } catch (Exception e) { System.out.println("Błąd."); }
    }

    private static void xmlMenu() {
        while (true) {
            System.out.println("\n[XML]: 1. Zapisz | 2. Wczytaj | 3. Usuń plik XML z dysku | X. Wstecz");
            String c = sc.nextLine().toUpperCase();
            if (c.equals("X")) return;
            switch (c) {
                case "1" -> saveToXml();
                case "2" -> loadFromXml();
                case "3" -> {
                    File f = new File(FILE_PATH);
                    if (f.exists()) {
                        System.gc(); 
                        if (f.delete()) System.out.println("Plik fizyczny został usunięty.");
                        else System.out.println("Błąd: Plik jest zablokowany.");
                    } else System.out.println("Plik nie istnieje.");
                }
            }
        }
    }

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
            System.out.println("Zapisano dane do pliku.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadFromXml() {
        try {
            XmlMapper m = new XmlMapper();
            m.registerModule(new JavaTimeModule());
            db = m.readValue(new File(FILE_PATH), DataContainer.class);
            System.out.println("Wczytano dane z pliku.");
        } catch (Exception e) { System.out.println("Nie można wczytać pliku."); }
    }

    private static void addManualEntry() {
        try {
            System.out.print("1. Kontakt | 2. Zdarzenie: ");
            String choice = sc.nextLine();
            if (choice.equals("1")) {
                System.out.print("Imię: "); String i = sc.nextLine();
                System.out.print("Nazwisko: "); String n = sc.nextLine();
                System.out.print("Tel: "); String t = sc.nextLine();
                System.out.print("Email: "); String e = sc.nextLine();
                db.kontakty.add(new Kontakt(i, n, PhoneNumberUtil.getInstance().parse(t, "PL"), new InternetAddress(e)));
            } else {
                System.out.print("Tytuł: "); String t = sc.nextLine();
                db.zdarzenia.add(new Zdarzenie(t, "Opis", LocalDate.now(), URI.create("http://manual.pl").toURL()));
            }
            System.out.println("Dodano.");
        } catch (Exception ex) { System.out.println("Błąd danych."); }
    }

    private static void seedData() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            db.kontakty.add(new Kontakt("Adam", "Mickiewicz", pu.parse("501100200", "PL"), new InternetAddress("adam@wieszcz.pl")));
            db.kontakty.add(new Kontakt("Juliusz", "Słowacki", pu.parse("602200300", "PL"), new InternetAddress("julek@kordian.pl")));
            db.kontakty.add(new Kontakt("Iga", "Świątek", pu.parse("703300400", "PL"), new InternetAddress("iga@sport.pl")));
            db.kontakty.add(new Kontakt("Robert", "Lewandowski", pu.parse("804400500", "PL"), new InternetAddress("rl9@lewy.pl")));
            db.kontakty.add(new Kontakt("Janusz", "Gajos", pu.parse("505100100", "PL"), new InternetAddress("janusz@aktor.pl")));
            db.kontakty.add(new Kontakt("Beata", "Kozidrak", pu.parse("606200200", "PL"), new InternetAddress("beata@bajm.pl")));
            db.kontakty.add(new Kontakt("Fryderyk", "Chopin", pu.parse("707300300", "PL"), new InternetAddress("frycek@piano.pl")));
            db.kontakty.add(new Kontakt("Maria", "Skłodowska", pu.parse("808400400", "PL"), new InternetAddress("maria@rad.pl")));
            db.kontakty.add(new Kontakt("Wisława", "Szymborska", pu.parse("509500500", "PL"), new InternetAddress("wisla@nobel.pl")));
            db.kontakty.add(new Kontakt("Mikołaj", "Kopernik", pu.parse("610600600", "PL"), new InternetAddress("mikolaj@niebo.pl")));

            db.zdarzenia.add(new Zdarzenie("Finał Roland Garros", "Mecz Igi", LocalDate.of(2026, 6, 8), URI.create("http://rg.fr").toURL()));
            db.zdarzenia.add(new Zdarzenie("Koncert Bajm", "Trasa", LocalDate.of(2026, 5, 20), URI.create("http://bajm.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Egzamin Java", "Zaliczenie", LocalDate.of(2026, 1, 30), URI.create("http://u.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Premiera", "Film", LocalDate.of(2026, 3, 12), URI.create("http://kino.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Mecz", "Kadra", LocalDate.of(2026, 9, 15), URI.create("http://pzpn.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Lekarz", "Kontrola", LocalDate.of(2026, 2, 10), URI.create("http://med.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Urlop", "Góry", LocalDate.of(2026, 1, 15), URI.create("http://travel.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Nobel", "Gala", LocalDate.of(2026, 11, 5), URI.create("http://nobel.org").toURL()));
            db.zdarzenia.add(new Zdarzenie("Serwis", "Auto", LocalDate.of(2026, 4, 1), URI.create("http://aso.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Wielkanoc", "Święta", LocalDate.of(2026, 4, 5), URI.create("http://dom.pl").toURL()));
            System.out.println(">>> Pamięć RAM załadowana danymi testowymi.");
        } catch (Exception e) {}
    }
}