package midacal;

import java.io.File;
import java.net.URI;
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
    private static DataContainer db = new DataContainer();
    private static final String FILE_PATH = "midacal_kalendarz.xml";
    private static final Scanner sc = new Scanner(System.in);

    public static class DataContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (!loadFromXml()) {
            seedData();
        }

        while (true) {
            System.out.println("\n--- PROFESJONALNY MIDACAL (JDK 25) ---");
            System.out.println("1. Lista | 2. Sortuj | 3. Zapisz XML | 4. Wczytaj XML | 5. USUŃ | 0. Wyjście");
            System.out.print("Wybierz: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> showAll();
                case "2" -> sortMenu();
                case "3" -> saveToXml();
                case "4" -> loadFromXml();
                case "5" -> deleteMenu();
                case "0" -> { saveToXml(); System.exit(0); }
                default -> System.out.println("Nieznana opcja.");
            }
        }
    }

    private static void sortMenu() {
        System.out.println("Sortuj: 1. Domyślne | 2. Imię | 3. Email | 4. Tel | 5. Tytuł | 6. Opis | 7. Link");
        String s = sc.nextLine();
        switch (s) {
            case "1" -> { Collections.sort(db.kontakty); Collections.sort(db.zdarzenia); }
            case "2" -> db.kontakty.sort(new Kontakt.ImieComparator());
            case "3" -> db.kontakty.sort(new Kontakt.EmailComparator());
            case "4" -> db.kontakty.sort(new Kontakt.TelComparator());
            case "5" -> db.zdarzenia.sort(new Zdarzenie.TytulComparator());
            case "6" -> db.zdarzenia.sort(new Zdarzenie.OpisComparator());
            case "7" -> db.zdarzenia.sort(new Zdarzenie.LinkComparator());
        }
        showAll();
    }

    private static void deleteMenu() {
        System.out.println("Usuń: 1. Kontakt | 2. Zdarzenie | Inny: Anuluj");
        String type = sc.nextLine();
        if (!type.equals("1") && !type.equals("2")) return;

        showAll();
        System.out.print("Podaj indeks: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            System.out.print("CZY NA PEWNO USUNĄĆ REKORD #" + idx + "? (T/N): ");
            if (sc.nextLine().equalsIgnoreCase("T")) {
                if (type.equals("1")) db.kontakty.remove(idx);
                else db.zdarzenia.remove(idx);
                System.out.println("Usunięto.");
                saveToXml();
            }
        } catch (Exception e) { System.out.println("Błąd indeksu."); }
        showAll();
    }

    private static void saveToXml() {
        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(FILE_PATH), db);
            System.out.println(">>> XML Zapisany.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static boolean loadFromXml() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return false;
        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule());
            db = mapper.readValue(f, DataContainer.class);
            System.out.println(">>> Dane wczytane z XML.");
            return true;
        } catch (Exception e) { return false; }
    }

    private static void showAll() {
        System.out.println("\n--- LISTA KONTAKTÓW ---");
        for (int i = 0; i < db.kontakty.size(); i++) System.out.println("[" + i + "] " + db.kontakty.get(i));
        System.out.println("\n--- LISTA ZDARZEŃ ---");
        for (int i = 0; i < db.zdarzenia.size(); i++) System.out.println("[" + i + "] " + db.zdarzenia.get(i));
    }

    private static void seedData() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            // --- HARD-CODED LISTA KONTAKTÓW ---
            db.kontakty.add(new Kontakt("Adam", "Nowak", pu.parse("501234567", "PL"), new InternetAddress("adam.nowak@gmail.com")));
            db.kontakty.add(new Kontakt("Barbara", "Kowalska", pu.parse("602345678", "PL"), new InternetAddress("basia.k@wp.pl")));
            db.kontakty.add(new Kontakt("Cezary", "Pazura", pu.parse("703456789", "PL"), new InternetAddress("czarek@aktor.pl")));
            db.kontakty.add(new Kontakt("Dorota", "Rabczewska", pu.parse("804567890", "PL"), new InternetAddress("doda@muzyka.pl")));
            db.kontakty.add(new Kontakt("Edward", "Nożycoręki", pu.parse("505678901", "PL"), new InternetAddress("edzio@scissors.com")));
            db.kontakty.add(new Kontakt("Filip", "Chajzer", pu.parse("606789012", "PL"), new InternetAddress("filip@tvn.pl")));
            db.kontakty.add(new Kontakt("Grażyna", "Torbicka", pu.parse("707890123", "PL"), new InternetAddress("grazyna@kultura.pl")));
            db.kontakty.add(new Kontakt("Henryk", "Sienkiewicz", pu.parse("808901234", "PL"), new InternetAddress("henio@trylogia.pl")));
            db.kontakty.add(new Kontakt("Iga", "Świątek", pu.parse("509012345", "PL"), new InternetAddress("iga.tenis@sport.pl")));
            db.kontakty.add(new Kontakt("Janusz", "Gajos", pu.parse("610123456", "PL"), new InternetAddress("janusz@teatr.pl")));

            // --- HARD-CODED LISTA ZDARZEŃ ---
            db.zdarzenia.add(new Zdarzenie("Finał Roland Garros", "Oglądanie meczu Igi", LocalDate.of(2026, 6, 8), URI.create("https://eurosport.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Wizyta u dentysty", "Przegląd kontrolny", LocalDate.of(2026, 2, 15), URI.create("https://znanylekarz.pl/stomatolog").toURL()));
            db.zdarzenia.add(new Zdarzenie("Premiera filmu", "Nowy Bond w kinach", LocalDate.of(2026, 11, 20), URI.create("https://multikino.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Warsztaty Java", "JDK 25 Nowości", LocalDate.of(2026, 3, 10), URI.create("https://oracle.com/java").toURL()));
            db.zdarzenia.add(new Zdarzenie("Urlop w górach", "Zakopane - ferie", LocalDate.of(2026, 1, 25), URI.create("https://booking.com/zakopane").toURL()));
            db.zdarzenia.add(new Zdarzenie("Konferencja IT", "Wystąpienie o XML", LocalDate.of(2026, 5, 5), URI.create("https://it-conf.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Urodziny Mamy", "Przyjęcie niespodzianka", LocalDate.of(2026, 8, 12), URI.create("https://fb.com/events/1").toURL()));
            db.zdarzenia.add(new Zdarzenie("Zjazd Absolwentów", "Spotkanie po latach", LocalDate.of(2026, 9, 30), URI.create("https://nasza-klasa.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Zakupy świąteczne", "Prezenty dla rodziny", LocalDate.of(2026, 12, 20), URI.create("https://allegro.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Trening personalny", "Siłownia - klatka i plecy", LocalDate.of(2026, 1, 15), URI.create("https://gym-pro.pl").toURL()));

            System.out.println(">>> Zainicjowano 10+10 unikalnych rekordów.");
        } catch (Exception e) { e.printStackTrace(); }
    }
}