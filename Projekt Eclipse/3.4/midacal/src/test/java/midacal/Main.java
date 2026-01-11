package midacal;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import jakarta.mail.internet.InternetAddress;

public class Main {
    private static KalendarzManager manager = new KalendarzManager();
    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static class KalendarzManager {
        private List<Kontakt> kontakty = new ArrayList<>();
        private List<Zdarzenie> zdarzenia = new ArrayList<>();
        public void dodajKontakt(Kontakt k) { kontakty.add(k); }
        public void dodajZdarzenie(Zdarzenie z) { zdarzenia.add(z); }
        public List<Kontakt> getKontakty() { return kontakty; }
        public List<Zdarzenie> getZdarzenia() { return zdarzenia; }
    }

    public static void main(String[] args) {
        generujDane();
        boolean run = true;
        while (run) {
            System.out.println("\n1. Lista | 2. Dodaj | 3. Sortuj | 0. Wyjście");
            String w = scanner.nextLine();
            switch (w) {
                case "1" -> wyswietl();
                case "2" -> dodajKontaktInterfejs();
                case "3" -> {
                    Collections.sort(manager.getKontakty());
                    Collections.sort(manager.getZdarzenia());
                    System.out.println("Posortowano domyślnie.");
                }
                case "0" -> run = false;
            }
        }
    }

    private static void wyswietl() {
        System.out.println("\n--- KONTAKTY ---");
        manager.getKontakty().forEach(System.out::println);
        System.out.println("\n--- ZDARZENIA ---");
        manager.getZdarzenia().forEach(System.out::println);
    }

    private static void dodajKontaktInterfejs() {
        try {
            System.out.print("Imię: "); String i = scanner.nextLine();
            System.out.print("Nazwisko: "); String n = scanner.nextLine();
            System.out.print("Tel: "); String t = scanner.nextLine();
            System.out.print("Email: "); String e = scanner.nextLine();
            
            var pu = PhoneNumberUtil.getInstance();
            var mail = new InternetAddress(e);
            mail.validate();
            
            manager.dodajKontakt(new Kontakt(i, n, pu.parse(t, "PL"), mail));
            System.out.println("Dodano.");
        } catch (Exception ex) { System.out.println("Błąd danych!"); }
    }

    private static void generujDane() {
        try {
            var pu = PhoneNumberUtil.getInstance();
            for (int i = 1; i <= 10; i++) {
                manager.dodajKontakt(new Kontakt("Jan"+i, "Kowalski"+i, pu.parse("50000000"+i, "PL"), new InternetAddress("test"+i+"@midacal.pl")));
                // Użycie URI.create().toURL() naprawia ostrzeżenie z Twojego zdjęcia
                manager.dodajZdarzenie(new Zdarzenie("Spotkanie "+i, "Opis", LocalDateTime.now().plusDays(i), 
                        URI.create("http://link"+i+".pl").toURL()));
            }
        } catch (Exception e) {}
    }
}