package com.midacalprojekt;

import java.net.URI;
import java.util.*;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.mail.internet.InternetAddress;

public class Main {

    // Scanner jako pole statyczne, żeby był dostępny w każdej metodzie
    private static final Scanner skaner = new Scanner(System.in);
    private static final BazaXML baza = new BazaXML();
    
    // Listy przechowujące dane w pamięci
    private static List<Kontakt> listaKontaktow = new ArrayList<>();
    private static List<Zdarzenie> listaZdarzen = new ArrayList<>();

    public static void main(String[] args) {
        // Na starcie próbujemy wczytać dane, żeby nie zaczynać od zera
        zaladujDane();

        boolean czyDalej = true;

        while (czyDalej) {
            wyswietlMenu();
            String wybor = skaner.nextLine();

            try {
                switch (wybor) {
                    case "1" -> pokazKontakty();
                    case "2" -> dodajKontaktInteraktywnie();
                    case "3" -> usunKontakt();
                    case "4" -> edytujKontakt();
                    case "5" -> sortujKontakty();
                    
                    case "6" -> pokazZdarzenia();
                    case "7" -> dodajZdarzenieInteraktywnie();
                    case "8" -> usunZdarzenie();
                    case "9" -> sortujZdarzenia();
                    
                    case "s" -> zapiszDane();
                    case "l" -> zaladujDane();
                    case "x" -> {
                        zapiszDane(); // Auto-zapis przy wyjściu
                        czyDalej = false;
                        System.out.println("Do widzenia!");
                    }
                    default -> System.out.println("Nieznana opcja.");
                }
            } catch (Exception e) {
                System.out.println("!!! Wystąpił błąd: " + e.getMessage());
                e.printStackTrace(); // Dla celów debugowania
            }
        }
    }

    // --- MENU I OBSŁUGA ---

    private static void wyswietlMenu() {
        System.out.println("\n=== MENU GŁÓWNE ===");
        System.out.println("1. Pokaż kontakty");
        System.out.println("2. Dodaj kontakt");
        System.out.println("3. Usuń kontakt");
        System.out.println("4. Edytuj kontakt");
        System.out.println("5. Sortuj kontakty");
        System.out.println("-------------------");
        System.out.println("6. Pokaż zdarzenia");
        System.out.println("7. Dodaj zdarzenie");
        System.out.println("8. Usuń zdarzenie");
        System.out.println("9. Sortuj zdarzenia");
        System.out.println("-------------------");
        System.out.println("s. Zapisz do XML (baza.xml)");
        System.out.println("l. Wczytaj z XML (baza.xml)");
        System.out.println("x. Wyjście");
        System.out.print("Wybór > ");
    }

    // --- METODY DLA KONTAKTÓW ---

    private static void pokazKontakty() {
        if (listaKontaktow.isEmpty()) {
            System.out.println("Lista kontaktów jest pusta.");
        } else {
            System.out.println("\n--- LISTA KONTAKTÓW ---");
            for (int i = 0; i < listaKontaktow.size(); i++) {
                System.out.println("[" + i + "] " + listaKontaktow.get(i));
            }
        }
    }

    private static void dodajKontaktInteraktywnie() throws Exception {
        System.out.println("\n--- DODAWANIE KONTAKTU ---");
        System.out.print("Imię: ");
        String imie = skaner.nextLine();
        System.out.print("Nazwisko: ");
        String nazwisko = skaner.nextLine();
        
        System.out.print("Telefon (np. 500-123-456): ");
        String tel = skaner.nextLine();
        
        System.out.print("Email (np. jan@wp.pl): ");
        String mail = skaner.nextLine();

        // Używamy naszej metody parsującej
        Kontakt k = stworzKontakt(imie, nazwisko, tel, mail);
        listaKontaktow.add(k);
        System.out.println("Dodano kontakt.");
    }

    private static void usunKontakt() {
        pokazKontakty();
        System.out.print("Podaj numer ID do usunięcia: ");
        int id = Integer.parseInt(skaner.nextLine());
        
        if (id >= 0 && id < listaKontaktow.size()) {
            listaKontaktow.remove(id);
            System.out.println("Usunięto.");
        } else {
            System.out.println("Nieprawidłowy numer.");
        }
    }
    
    private static void edytujKontakt() {
        pokazKontakty();
        System.out.print("Podaj numer ID do edycji: ");
        int id = Integer.parseInt(skaner.nextLine());
        
        if (id >= 0 && id < listaKontaktow.size()) {
            Kontakt k = listaKontaktow.get(id);
            System.out.println("Edytujesz: " + k);
            System.out.print("Nowe imię (Enter by pominąć): ");
            String noweImie = skaner.nextLine();
            if(!noweImie.isEmpty()) k.setImie(noweImie);
            
            System.out.print("Nowe nazwisko (Enter by pominąć): ");
            String noweNazwisko = skaner.nextLine();
            if(!noweNazwisko.isEmpty()) k.setNazwisko(noweNazwisko);
            
            // Edycja komponentów PhoneNumber i Email wymagałaby ponownego parsowania, 
            // dla uproszczenia w tym przykładzie edytujemy tylko Stringi
            System.out.println("Zaktualizowano dane tekstowe.");
        } else {
            System.out.println("Błąd indeksu.");
        }
    }
    
    private static void sortujKontakty() {
        System.out.println("a - Po Nazwisku (Domyślne)");
        System.out.println("b - Po Emailu");
        String wybor = skaner.nextLine();
        
        if (wybor.equalsIgnoreCase("a")) {
            Collections.sort(listaKontaktow);
        } else if (wybor.equalsIgnoreCase("b")) {
            Collections.sort(listaKontaktow, new Kontakt.KomparatorEmail());
        }
        System.out.println("Posortowano.");
    }

    // --- METODY DLA ZDARZEŃ ---

    private static void pokazZdarzenia() {
        if (listaZdarzen.isEmpty()) {
            System.out.println("Lista zdarzeń jest pusta.");
        } else {
            System.out.println("\n--- LISTA ZDARZEŃ ---");
            for (int i = 0; i < listaZdarzen.size(); i++) {
                System.out.println("[" + i + "] " + listaZdarzen.get(i));
            }
        }
    }

    private static void dodajZdarzenieInteraktywnie() throws Exception {
        if (listaKontaktow.isEmpty()) {
            System.out.println("Najpierw dodaj chociaż jeden kontakt!");
            return;
        }

        System.out.println("\n--- DODAWANIE ZDARZENIA ---");
        System.out.print("Tytuł: ");
        String tytul = skaner.nextLine();
        
        System.out.print("Data (RRRR-MM-DD): ");
        String dataStr = skaner.nextLine();
        // Prosty parser daty
        String[] parts = dataStr.split("-");
        Date data = stworzDate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

        System.out.print("Link URL (np. http://google.com): ");
        String urlStr = skaner.nextLine();
        
        System.out.println("Wybierz kontakt do zdarzenia:");
        pokazKontakty();
        System.out.print("Numer ID kontaktu: ");
        int idKontaktu = Integer.parseInt(skaner.nextLine());
        
        if (idKontaktu >= 0 && idKontaktu < listaKontaktow.size()) {
            Zdarzenie z = new Zdarzenie(
                tytul, 
                data, 
                URI.create(urlStr).toURL(), 
                listaKontaktow.get(idKontaktu)
            );
            listaZdarzen.add(z);
            System.out.println("Dodano zdarzenie.");
        } else {
            System.out.println("Nieprawidłowy kontakt.");
        }
    }
    
    private static void usunZdarzenie() {
        pokazZdarzenia();
        System.out.print("Podaj numer ID do usunięcia: ");
        int id = Integer.parseInt(skaner.nextLine());
        if (id >= 0 && id < listaZdarzen.size()) {
            listaZdarzen.remove(id);
            System.out.println("Usunięto.");
        }
    }
    
    private static void sortujZdarzenia() {
        System.out.println("a - Chronologicznie (Domyślne)");
        System.out.println("b - Po Tytule");
        String wybor = skaner.nextLine();
        
        if (wybor.equalsIgnoreCase("a")) {
            Collections.sort(listaZdarzen);
        } else if (wybor.equalsIgnoreCase("b")) {
            Collections.sort(listaZdarzen, new Zdarzenie.KomparatorTytul());
        }
        System.out.println("Posortowano.");
    }

    // --- ZAPIS I ODCZYT ---

    private static void zapiszDane() {
        baza.zapiszWszystko(listaKontaktow, listaZdarzen, "baza.xml");
    }

    @SuppressWarnings("unchecked")
    private static void zaladujDane() {
        List<Object> dane = baza.wczytajWszystko("baza.xml");
        if (dane.size() >= 2) {
            listaKontaktow = (List<Kontakt>) dane.get(0);
            listaZdarzen = (List<Zdarzenie>) dane.get(1);
            System.out.println("Załadowano dane: " + listaKontaktow.size() + " kontaktów, " + listaZdarzen.size() + " zdarzeń.");
        } else {
            System.out.println("Brak danych lub nowa baza.");
        }
    }

    // --- POMOCNICZE ---

    private static Kontakt stworzKontakt(String imie, String nazwisko, String tel, String mail) throws Exception {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumber numer = phoneUtil.parse(tel, "PL");
        InternetAddress email = new InternetAddress(mail);
        return new Kontakt(imie, nazwisko, numer, email);
    }

    private static Date stworzDate(int r, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.set(r, m - 1, d);
        return c.getTime();
    }
}