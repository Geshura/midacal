package midacal;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import jakarta.mail.internet.InternetAddress;

public class Main {
    // Kontener danych - ArrayList
    private static DataContainer db = new DataContainer();
    private static final String PLIK_XML = "baza_danych.xml";
    private static final Scanner sc = new Scanner(System.in);

    public static class DataContainer {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (!wczytajXML()) {
            inicjalizujDaneHardCoded();
        }

        while (true) {
            System.out.println("\n--- MIDACAL PROFESIONAL (JDK 25) ---");
            System.out.println("1. Wyświetl | 2. Sortuj | 3. Zapisz | 4. Wczytaj | 5. USUŃ | 0. Wyjście");
            System.out.print("Wybór: ");
            String w = sc.nextLine();

            switch (w) {
                case "1" -> wyswietlWszystko();
                case "2" -> menuSortowania();
                case "3" -> zapiszXML();
                case "4" -> wczytajXML();
                case "5" -> menuUsuwania();
                case "0" -> { zapiszXML(); System.exit(0); }
                default -> System.out.println("Niepoprawna opcja.");
            }
        }
    }

    private static void menuSortowania() {
        System.out.println("Sortuj: 1. Nazwisko | 2. Imię | 3. Data | 4. Tytuł");
        String s = sc.nextLine();
        switch (s) {
            case "1" -> Collections.sort(db.kontakty);
            case "2" -> db.kontakty.sort(new Kontakt.ImieComparator());
            case "3" -> Collections.sort(db.zdarzenia);
            case "4" -> db.zdarzenia.sort(new Zdarzenie.TytulComparator());
        }
        wyswietlWszystko();
    }

    private static void menuUsuwania() {
        wyswietlWszystko();
        System.out.print("Usuń z: 1. Kontakty | 2. Zdarzenia: ");
        String t = sc.nextLine();
        System.out.print("Podaj indeks: ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            System.out.print("CZY NA PEWNO USUNĄĆ? (T/N): ");
            if (sc.nextLine().equalsIgnoreCase("T")) {
                if (t.equals("1")) db.kontakty.remove(idx);
                else db.zdarzenia.remove(idx);
                zapiszXML();
            }
        } catch (Exception e) { System.out.println("Błąd indeksu."); }
        wyswietlWszystko();
    }

    private static void zapiszXML() {
        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            
            mapper.writeValue(new File(PLIK_XML), db);
            System.out.println(">>> Zapisano do XML.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static boolean wczytajXML() {
        File f = new File(PLIK_XML);
        if (!f.exists()) return false;
        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule());
            db = mapper.readValue(f, DataContainer.class);
            System.out.println(">>> Dane wczytane z XML.");
            return true;
        } catch (Exception e) { return false; }
    }

    private static void wyswietlWszystko() {
        System.out.println("\n--- KONTAKTY ---");
        for (int i = 0; i < db.kontakty.size(); i++) System.out.println("[" + i + "] " + db.kontakty.get(i));
        System.out.println("\n--- ZDARZENIA ---");
        for (int i = 0; i < db.zdarzenia.size(); i++) System.out.println("[" + i + "] " + db.zdarzenia.get(i));
    }

    private static void inicjalizujDaneHardCoded() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            // 10 KONTAKTÓW (HARD-CODED)
            db.kontakty.add(new Kontakt("Jan", "Kowalski", pu.parse("500100200", "PL"), new InternetAddress("jan.k@wp.pl")));
            db.kontakty.add(new Kontakt("Anna", "Nowak", pu.parse("600200300", "PL"), new InternetAddress("ania.n@gmail.com")));
            db.kontakty.add(new Kontakt("Piotr", "Zieliński", pu.parse("700300400", "PL"), new InternetAddress("piotrek@o2.pl")));
            db.kontakty.add(new Kontakt("Maria", "Wiśniewska", pu.parse("800400500", "PL"), new InternetAddress("maria.w@onet.pl")));
            db.kontakty.add(new Kontakt("Tomasz", "Mazur", pu.parse("501501501", "PL"), new InternetAddress("tomek.m@interia.pl")));
            db.kontakty.add(new Kontakt("Ewa", "Wójcik", pu.parse("602602602", "PL"), new InternetAddress("ewcia@poczta.pl")));
            db.kontakty.add(new Kontakt("Krzysztof", "Krawczyk", pu.parse("703703703", "PL"), new InternetAddress("krzysiu@parostatek.pl")));
            db.kontakty.add(new Kontakt("Beata", "Kozidrak", pu.parse("804804804", "PL"), new InternetAddress("beata@bajm.pl")));
            db.kontakty.add(new Kontakt("Robert", "Lewandowski", pu.parse("900100200", "PL"), new InternetAddress("rl9@bmw.de")));
            db.kontakty.add(new Kontakt("Iga", "Świątek", pu.parse("555444333", "PL"), new InternetAddress("iga@tenis.pl")));

            // 10 ZDARZEŃ (HARD-CODED)
            db.zdarzenia.add(new Zdarzenie("Finał Tenisa", "Mecz Igi Świątek", LocalDate.of(2026, 6, 10), URI.create("http://rolandgarros.com").toURL()));
            db.zdarzenia.add(new Zdarzenie("Dentysta", "Przegląd okresowy", LocalDate.of(2026, 2, 15), URI.create("http://znanylekarz.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Premiera Filmu", "Nowy film akcji", LocalDate.of(2026, 3, 20), URI.create("http://multikino.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Egzamin Java", "Zaliczenie projektu", LocalDate.of(2026, 1, 30), URI.create("http://uczelnia.edu.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Urodziny", "Impreza u Adama", LocalDate.of(2026, 5, 5), URI.create("http://facebook.com/events").toURL()));
            db.zdarzenia.add(new Zdarzenie("Konferencja IT", "Nowości w JDK 25", LocalDate.of(2026, 4, 12), URI.create("http://devconf.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Zakupy", "Prezent dla mamy", LocalDate.of(2026, 12, 22), URI.create("http://allegro.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Urlop", "Wyjazd w Alpy", LocalDate.of(2026, 1, 10), URI.create("http://booking.com").toURL()));
            db.zdarzenia.add(new Zdarzenie("Mecz kadry", "Polska - Niemcy", LocalDate.of(2026, 9, 15), URI.create("http://laczynaspilka.pl").toURL()));
            db.zdarzenia.add(new Zdarzenie("Serwis auta", "Wymiana oleju", LocalDate.of(2026, 3, 5), URI.create("http://aso.pl").toURL()));

            System.out.println(">>> Pomyślnie wczytano 10 kontaktów i 10 zdarzeń (Hard-coded).");
        } catch (Exception e) { e.printStackTrace(); }
    }
}