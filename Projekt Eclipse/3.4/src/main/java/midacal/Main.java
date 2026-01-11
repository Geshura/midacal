package midacal;

import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
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
    private static KalendarzData db = new KalendarzData();
    private static final String PLIK_XML = "kalendarz_data.xml";
    private static Scanner scanner = new Scanner(System.in);

    // Root container dla XML
    public static class KalendarzData {
        public List<Kontakt> kontakty = new ArrayList<>();
        public List<Zdarzenie> zdarzenia = new ArrayList<>();
    }

    public static void main(String[] args) {
        // Próba wczytania danych z XML, jeśli plik nie istnieje - generuj 10 rekordów
        if (!wczytajZXML()) {
            generujDaneHardCoded();
        }

        boolean run = true;
        while (run) {
            System.out.println("\n=== MIDACAL JDK 25 (Jackson XML) ===");
            System.out.println("1. Wyświetl wszystko");
            System.out.println("2. Sortuj domyślnie");
            System.out.println("3. Sortuj alternatywnie (Email/Tytuł)");
            System.out.println("4. Zapisz do XML");
            System.out.println("0. Wyjście");
            System.out.print("Wybór: ");

            String w = scanner.nextLine();
            switch (w) {
                case "1" -> wyswietl();
                case "2" -> {
                    Collections.sort(db.kontakty);
                    Collections.sort(db.zdarzenia);
                    System.out.println("Posortowano domyślnie (Comparable).");
                }
                case "3" -> {
                    Collections.sort(db.kontakty, new Kontakt.EmailComparator());
                    Collections.sort(db.zdarzenia, new Zdarzenie.TytulComparator());
                    System.out.println("Posortowano alternatywnie (Comparator).");
                }
                case "4" -> zapiszDoXML();
                case "0" -> {
                    zapiszDoXML();
                    run = false;
                }
                default -> System.out.println("Niepoprawny wybór.");
            }
        }
    }

    private static void zapiszDoXML() {
        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule()); // Obsługa LocalDateTime
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(new File(PLIK_XML), db);
            System.out.println(">>> Pomyślnie zapisano do pliku: " + PLIK_XML);
        } catch (Exception e) {
            System.err.println("Błąd zapisu XML: " + e.getMessage());
        }
    }

    private static boolean wczytajZXML() {
        File file = new File(PLIK_XML);
        if (!file.exists()) return false;
        try {
            XmlMapper mapper = new XmlMapper();
            mapper.registerModule(new JavaTimeModule());
            db = mapper.readValue(file, KalendarzData.class);
            System.out.println(">>> Wczytano dane z pliku XML.");
            return true;
        } catch (Exception e) {
            System.err.println("Błąd odczytu XML: " + e.getMessage());
            return false;
        }
    }

    private static void wyswietl() {
        System.out.println("\n--- KONTAKTY (ArrayList) ---");
        db.kontakty.forEach(System.out::println);
        System.out.println("\n--- ZDARZENIA (ArrayList) ---");
        db.zdarzenia.forEach(System.out::println);
    }

    private static void generujDaneHardCoded() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            for (int i = 1; i <= 10; i++) {
                db.kontakty.add(new Kontakt("Jan"+i, "Kowalski"+(11-i), 
                        pu.parse("50000000" + i, "PL"), 
                        new InternetAddress("test"+i+"@midacal.pl")));
                
                // JDK 25: URI.create().toURL() zamiast new URL()
                db.zdarzenia.add(new Zdarzenie("Spotkanie "+(11-i), "Opis "+i, 
                        LocalDateTime.now().plusDays(i), 
                        URI.create("http://link"+i+".pl").toURL()));
            }
            System.out.println(">>> Wygenerowano 10 kontaktów i 10 zdarzeń (Hard-coding).");
        } catch (Exception e) {
            System.err.println("Błąd generowania danych: " + e.getMessage());
        }
    }
}