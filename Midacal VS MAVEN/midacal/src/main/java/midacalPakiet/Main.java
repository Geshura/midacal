package midacalPakiet;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class Main {

    private static final String ZDARZENIA_FILE_PATH = "zdarzenia.xml";
    private static final String KONTAKTY_FILE_PATH = "kontakty.xml";
    private static int xmlSaveCounter = 0;
    private static final int BACKUP_INTERVAL = 10; 

    private static final XmlMapper XML_MAPPER;

    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.registerModule(new JavaTimeModule());
        XML_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @JacksonXmlRootElement(localName = "kontakty")
    public static class KontaktyWrapperInternal {
        @JacksonXmlElementWrapper(useWrapping = false) 
        @JsonProperty("kontakt")
        private List<Kontakt> kontakty;

        public List<Kontakt> getKontakty() { return kontakty; }
        public void setKontakty(List<Kontakt> kontakty) { this.kontakty = kontakty; }
        public KontaktyWrapperInternal() {}
    }

    @JacksonXmlRootElement(localName = "zdarzenia")
    public static class ZdarzeniaWrapperInternal {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JsonProperty("zdarzenie")
        private List<Zdarzenie> zdarzenia;

        public List<Zdarzenie> getZdarzenia() { return zdarzenia; }
        public void setZdarzenia(List<Zdarzenie> zdarzenia) { this.zdarzenia = zdarzenia; }
        public ZdarzeniaWrapperInternal() {}
    }

    // Klasa pomocnicza do wczytywania danych XML
    private static class DaneXML {
        List<Zdarzenie> zdarzenia;
        List<Kontakt> kontakty;
        public DaneXML(List<Zdarzenie> z, List<Kontakt> k) {
            this.zdarzenia = z;
            this.kontakty = k;
        }
        public List<Zdarzenie> getZdarzenia() { return zdarzenia; }
        public List<Kontakt> getKontakty() { return kontakty; }
    }


    public static void main(String[] args) {
        final List<Zdarzenie> listaZdarzen = new ArrayList<>();
        final List<Kontakt> listaKontaktow = new ArrayList<>();

        Kontakt.resetIdCounter();
        Zdarzenie.resetIdCounter();
        
        DBHelper.initDatabase();

        List<Kontakt> dbKontakty = DBHelper.getAllKontakty();
        List<Zdarzenie> dbZdarzenia = DBHelper.getAllZdarzenia();
        
        if ((dbKontakty != null && !dbKontakty.isEmpty()) || (dbZdarzenia != null && !dbZdarzenia.isEmpty())) {
            if (dbKontakty != null && !dbKontakty.isEmpty()) {
                listaKontaktow.addAll(dbKontakty);
            }
            if (dbZdarzenia != null && !dbZdarzenia.isEmpty()) {
                listaZdarzen.addAll(dbZdarzenia);
            }
            System.out.println("Wczytano dane z bazy danych (" + listaKontaktow.size() + " kontaktow, " + listaZdarzen.size() + " zdarzen)");
        } else {
            // Wczytaj dane z XML (dwa pliki)
            DaneXML daneZXML = wczytajDaneZXML();
            if (daneZXML != null) {
                if (daneZXML.getZdarzenia() != null) {
                    listaZdarzen.addAll(daneZXML.getZdarzenia());
                }
                if (daneZXML.getKontakty() != null) {
                    listaKontaktow.addAll(daneZXML.getKontakty());
                }
                if (!listaKontaktow.isEmpty() || !listaZdarzen.isEmpty()) {
                    System.out.println("Wczytano dane z plików " + ZDARZENIA_FILE_PATH + " i " + KONTAKTY_FILE_PATH + " (" + listaKontaktow.size() + " kontaktow, " + listaZdarzen.size() + " zdarzen)");
                    DBHelper.syncAllToDB(listaKontaktow, listaZdarzen);
                }
            }
        }

        // Hook wyjścia
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\nZapisywanie zmian do bazy danych...");
                DBHelper.syncAllToDB(listaKontaktow, listaZdarzen);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        // Interaktywne menu główne
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========== KALENDARZ ==========");
            System.out.println("1. Kontakty");
            System.out.println("2. Zdarzenia");
            System.out.println("3. Utwórz pliki testowe");
            System.out.println("4. Wyjdz");
            System.out.print("Wybierz opcje: ");

            int choice = 0;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Bledny wybor!");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    menuKontakty(listaKontaktow, listaZdarzen, scanner);
                    break;
                case 2:
                    menuZdarzenia(listaZdarzen, listaKontaktow, scanner);
                    break;
                case 3:
                    wczytajPlikTestowy(listaZdarzen, listaKontaktow);
                    break;
                case 4:
                    System.out.println("Do widzenia!");
                    running = false;
                    break;
                default:
                    System.out.println("Nieznana opcja!");
            }
        }

        scanner.close();
    }
    
    // --- ZMIANA METOD ZAPISU/ODCZYTU XML (Jackson, 2 pliki) ---

    public static void zapiszDaneDoXML(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow) {
        zapiszZdarzeniaDoXML(listaZdarzen);
        zapiszKontaktyDoXML(listaKontaktow);

        xmlSaveCounter++;
        if (xmlSaveCounter >= BACKUP_INTERVAL) {
            System.out.println("Automatyczny backup: synchronizacja danych XML do bazy danych...");
            try {
                DBHelper.syncAllToDB(listaKontaktow, listaZdarzen);
            } catch (Exception e) {
                System.err.println("Blad podczas backupu do DB: " + e.getMessage());
            }
            xmlSaveCounter = 0;
        }
    }

    public static void zapiszZdarzeniaDoXML(List<Zdarzenie> listaZdarzen) {
        try {
            ZdarzeniaWrapperInternal wrapper = new ZdarzeniaWrapperInternal();
            wrapper.setZdarzenia(listaZdarzen);

            XML_MAPPER.writer()
                .withRootName("zdarzenia")
                .writeValue(new File(ZDARZENIA_FILE_PATH), wrapper);
                
            System.out.println("Pomyslnie zapisano zdarzenia do pliku " + ZDARZENIA_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Blad podczas zapisu zdarzen do XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void zapiszKontaktyDoXML(List<Kontakt> listaKontaktow) {
        try {
            KontaktyWrapperInternal wrapper = new KontaktyWrapperInternal();
            wrapper.setKontakty(listaKontaktow);

            XML_MAPPER.writer()
                .withRootName("kontakty")
                .writeValue(new File(KONTAKTY_FILE_PATH), wrapper);
                
            System.out.println("Pomyslnie zapisano kontakty do pliku " + KONTAKTY_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Blad podczas zapisu kontaktow do XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DaneXML wczytajDaneZXML() {
        List<Zdarzenie> zdarzenia = new ArrayList<>();
        List<Kontakt> kontakty = new ArrayList<>();
        boolean loaded = false;

        try {
            File zdarzeniaFile = new File(ZDARZENIA_FILE_PATH);
            if (zdarzeniaFile.exists()) {
                ZdarzeniaWrapperInternal zdarzeniaWrapper = XML_MAPPER.readValue(zdarzeniaFile, ZdarzeniaWrapperInternal.class);
                
                if (zdarzeniaWrapper.getZdarzenia() != null) {
                    zdarzenia.addAll(zdarzeniaWrapper.getZdarzenia());
                    loaded = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Blad podczas wczytywania zdarzen z XML: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            File kontaktyFile = new File(KONTAKTY_FILE_PATH);
            if (kontaktyFile.exists()) {
                KontaktyWrapperInternal kontaktyWrapper = XML_MAPPER.readValue(kontaktyFile, KontaktyWrapperInternal.class);
                
                if (kontaktyWrapper.getKontakty() != null) {
                    kontakty.addAll(kontaktyWrapper.getKontakty());
                    loaded = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Blad podczas wczytywania kontaktow z XML: " + e.getMessage());
            e.printStackTrace();
        }

        if (loaded) {
            return new DaneXML(zdarzenia, kontakty);
        }

        return null;
    }
    
    // --- RESZTA METOD MENU (W CAŁOŚCI) ---

    private static void menuKontakty(List<Kontakt> listaKontaktow, List<Zdarzenie> listaZdarzen, Scanner scanner) { 
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n========== MENU KONTAKTY ==========");
            System.out.println("1. Wyswietl kontakty");
            System.out.println("2. Szczegoly kontaktu");
            System.out.println("3. Dodaj kontakt");
            System.out.println("4. Usun kontakt");
            System.out.println("5. Posortuj kontakty");
            System.out.println("6. Wstecz");
            System.out.print("Wybierz opcje: ");

            int choice = 0;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Bledny wybor!");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    wyswietlKontakty(listaKontaktow);
                    break;
                case 2:
                    wyswietlSzczegolKontaktu(listaKontaktow, listaZdarzen, scanner);
                    break;
                case 3:
                    dodajKontakt(listaKontaktow, listaZdarzen, scanner);
                    break;
                case 4:
                    usunKontakt(listaKontaktow, listaZdarzen, scanner);
                    break;
                case 5:
                    menuSortowaniaKontakty(listaKontaktow, scanner);
                    break;
                case 6:
                    inMenu = false;
                    break;
                default:
                    System.out.println("Nieznana opcja!");
            }
        }
    }
    private static void menuZdarzenia(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow, Scanner scanner) { 
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n========== MENU ZDARZENIA ==========");
            System.out.println("1. Wyswietl zdarzenia");
            System.out.println("2. Szczegoly zdarzenia");
            System.out.println("3. Dodaj zdarzenie");
            System.out.println("4. Usun zdarzenie");
            System.out.println("5. Posortuj zdarzenia");
            System.out.println("6. Wstecz");
            System.out.print("Wybierz opcje: ");

            int choice = 0;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Bledny wybor!");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    wyswietlZdarzenia(listaZdarzen);
                    break;
                case 2:
                    wyswietlSzczegolZdarzenia(listaZdarzen, listaKontaktow, scanner);
                    break;
                case 3:
                    dodajZdarzenie(listaZdarzen, listaKontaktow, scanner);
                    break;
                case 4:
                    usunZdarzenie(listaZdarzen, listaKontaktow, scanner);
                    break;
                case 5:
                    menuSortowaniaZdarzenia(listaZdarzen, scanner);
                    break;
                case 6:
                    inMenu = false;
                    break;
                default:
                    System.out.println("Nieznana opcja!");
            }
        }
    }
    private static void wczytajPlikTestowy(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow) { 
        File testFileZdarzenia = new File(ZDARZENIA_FILE_PATH);
        File testFileKontakty = new File(KONTAKTY_FILE_PATH);

        if (testFileZdarzenia.exists() || testFileKontakty.exists()) {
            System.out.println("Pliki testowe juz istnieja!");
            System.out.println("Aby stworzyc nowe pliki, najpierw usun: " + ZDARZENIA_FILE_PATH + " i " + KONTAKTY_FILE_PATH);
            return;
        }
        
        Kontakt.resetIdCounter();
        Zdarzenie.resetIdCounter();
        
        listaZdarzen.clear();
        listaKontaktow.clear();
        
        stworzDomyslneDane(listaZdarzen, listaKontaktow);
        
        zapiszDaneDoXML(listaZdarzen, listaKontaktow);
        
        System.out.println("Pliki testowe stworzone pomyslnie!");
        System.out.println("Stworzono " + listaKontaktow.size() + " kontaktow i " + listaZdarzen.size() + " zdarzen.");
        System.out.println("Dane zapisane do: " + ZDARZENIA_FILE_PATH + " i " + KONTAKTY_FILE_PATH);
    }
    private static void menuSortowaniaKontakty(List<Kontakt> listaKontaktow, Scanner scanner) {
        System.out.println("\n=== SORTOWANIE KONTAKTOW ===");
        System.out.println("1. Po nazwisku (domyslne)");
        System.out.println("2. Po imieniu");
        System.out.println("3. Po telefonie");
        System.out.println("4. Po emailu");
        System.out.print("Wybierz opcje sortowania: ");

        int choice = 0;
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Bledny wybor!");
            scanner.nextLine();
            return;
        }

        switch (choice) {
            case 1:
                Collections.sort(listaKontaktow);
                wyswietlKontakty(listaKontaktow);
                break;
            case 2:
                Collections.sort(listaKontaktow, Sortowanie.SORTUJ_KONTAKT_IMIE);
                wyswietlKontakty(listaKontaktow);
                break;
            case 3:
                Collections.sort(listaKontaktow, Sortowanie.SORTUJ_KONTAKT_TELEFON);
                wyswietlKontakty(listaKontaktow);
                break;
            case 4:
                Collections.sort(listaKontaktow, Sortowanie.SORTUJ_KONTAKT_EMAIL);
                wyswietlKontakty(listaKontaktow);
                break;
            default:
                System.out.println("Nieznana opcja!");
        }
    }
    private static void menuSortowaniaZdarzenia(List<Zdarzenie> listaZdarzen, Scanner scanner) {
        System.out.println("\n=== SORTOWANIE ZDARZEN ===");
        System.out.println("1. Po dacie (domyslne)");
        System.out.println("2. Po nazwie");
        System.out.println("3. Po miejscu");
        System.out.println("4. Po opisie");
        System.out.print("Wybierz opcje sortowania: ");

        int choice = 0;
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Bledny wybor!");
            scanner.nextLine();
            return;
        }

        switch (choice) {
            case 1:
                Collections.sort(listaZdarzen);
                wyswietlZdarzenia(listaZdarzen);
                break;
            case 2:
                Collections.sort(listaZdarzen, Sortowanie.SORTUJ_ZDARZENIE_NAZWA);
                wyswietlZdarzenia(listaZdarzen);
                break;
            case 3:
                Collections.sort(listaZdarzen, Sortowanie.SORTUJ_ZDARZENIE_MIEJSCE);
                wyswietlZdarzenia(listaZdarzen);
                break;
            case 4:
                Collections.sort(listaZdarzen, Sortowanie.SORTUJ_ZDARZENIE_OPIS);
                wyswietlZdarzenia(listaZdarzen);
                break;
            default:
                System.out.println("Nieznana opcja!");
        }
    }
    private static void wyswietlKontakty(List<Kontakt> listaKontaktow) {
        if (listaKontaktow.isEmpty()) {
            System.out.println("Brak kontaktow.");
            return;
        }
        System.out.println("\n=== KONTAKTY ===");
        for (int i = 0; i < listaKontaktow.size(); i++) {
            System.out.println(i + ". " + listaKontaktow.get(i));
        }
    }
    private static void wyswietlZdarzenia(List<Zdarzenie> listaZdarzen) {
        if (listaZdarzen.isEmpty()) {
            System.out.println("Brak zdarzen.");
            return;
        }
        System.out.println("\n=== ZDARZENIA ===");
        for (int i = 0; i < listaZdarzen.size(); i++) {
            System.out.println(i + ". " + listaZdarzen.get(i));
        }
    }
    private static void wyswietlSzczegolKontaktu(List<Kontakt> listaKontaktow, List<Zdarzenie> listaZdarzen, Scanner scanner) {
        if (listaKontaktow.isEmpty()) {
            System.out.println("Brak kontaktow.");
            return;
        }
        System.out.print("Podaj numer kontaktu: ");
        try {
            int idx = scanner.nextInt();
            scanner.nextLine();
            if (idx >= 0 && idx < listaKontaktow.size()) {
                Kontakt k = listaKontaktow.get(idx);
                System.out.println("\n=== SZCZEGOLY KONTAKTU ===");
                System.out.println("ID (Baza): " + k.getId());
                System.out.println("Imie: " + k.getImie());
                System.out.println("Nazwisko: " + k.getNazwisko());
                System.out.println("Telefon: " + (k.getTelefon() != null ? k.getTelefon().toString() : "brak"));
                System.out.println("Email: " + (k.getEmail() != null ? k.getEmail().toString() : "brak"));
                
                List<Zdarzenie> dbZdarzenia = DBHelper.getZdarzeniaForKontakt(k.getId()); 
                if (dbZdarzenia != null && !dbZdarzenia.isEmpty()) {
                    System.out.println("\n--- Zdarzenia w ktorych biora udzial (z bazy) ---");
                    for (int i = 0; i < dbZdarzenia.size(); i++) {
                        System.out.println(i + ". ID: " + dbZdarzenia.get(i).getId() + " - " + dbZdarzenia.get(i).getNazwa() + " (" + dbZdarzenia.get(i).getData() + ")"); 
                    }
                } else {
                    System.out.println("\n--- Brak udzialu w zdarzeniach ---");
                }
                
                System.out.println("\n1. Przypisz do zdarzenia (w bazie)");
                System.out.println("2. Usun z zdarzenia (w bazie)");
                System.out.println("3. Cofnij");
                System.out.print("Wybierz: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                if (choice == 1) {
                    przypisKontaktDoZdarzenia(k, listaZdarzen, listaKontaktow, scanner);
                } else if (choice == 2) {
                    usunKontaktZZdarzenia(k, listaZdarzen, listaKontaktow, scanner);
                }
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void przypisKontaktDoZdarzenia(Kontakt kontakt, List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow, Scanner scanner) {
        if (listaZdarzen.isEmpty()) {
            System.out.println("Brak zdarzen do przypisania.");
            return;
        }
        
        System.out.println("\n--- Dostepne zdarzenia ---");
        for (int i = 0; i < listaZdarzen.size(); i++) {
            System.out.println(i + ". ID: " + listaZdarzen.get(i).getId() + " - " + listaZdarzen.get(i).getNazwa() + " (" + listaZdarzen.get(i).getData() + ")");
        }
        
        System.out.print("Podaj numer zdarzenia (z listy 0, 1, 2...) aby przypisac: ");
        try {
            int zdarzenieIdx = scanner.nextInt();
            scanner.nextLine();
            if (zdarzenieIdx >= 0 && zdarzenieIdx < listaZdarzen.size()) {
                Zdarzenie zdarzenie = listaZdarzen.get(zdarzenieIdx);
                
                DBHelper.addZdarzenieToKontakt(kontakt.getId(), zdarzenie.getId()); 
                
                kontakt.addZdarzenie(zdarzenie);
                zdarzenie.addKontakt(kontakt);
                
                zapiszDaneDoXML(listaZdarzen, listaKontaktow); 
                System.out.println("Kontakt przypisany do zdarzenia (Baza i XML)!");
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void usunKontaktZZdarzenia(Kontakt kontakt, List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow, Scanner scanner) {
        List<Zdarzenie> dbZdarzenia = DBHelper.getZdarzeniaForKontakt(kontakt.getId()); 
        
        if (dbZdarzenia == null || dbZdarzenia.isEmpty()) {
            System.out.println("Kontakt nie ma przypisanych zdarzen.");
            return;
        }
        
        System.out.println("\n--- Przypisane zdarzenia ---");
        for (int i = 0; i < dbZdarzenia.size(); i++) {
            System.out.println(i + ". ID: " + dbZdarzenia.get(i).getId() + " - " + dbZdarzenia.get(i).getNazwa() + " (" + dbZdarzenia.get(i).getData() + ")");
        }
        
        System.out.print("Podaj numer zdarzenia (z listy 0, 1, 2...) aby usunac: ");
        try {
            int zdarzenieIdx = scanner.nextInt();
            scanner.nextLine();
            if (zdarzenieIdx >= 0 && zdarzenieIdx < dbZdarzenia.size()) {
                Zdarzenie zdarzenieDoUsuniecia = dbZdarzenia.get(zdarzenieIdx);
                
                DBHelper.removeZdarzenieFromKontakt(kontakt.getId(), zdarzenieDoUsuniecia.getId());
                
                Zdarzenie glownaListaZdarzenie = listaZdarzen.stream()
                        .filter(z -> z.getId() == zdarzenieDoUsuniecia.getId())
                        .findFirst().orElse(null);
                        
                if (glownaListaZdarzenie != null) {
                    kontakt.removeZdarzenie(glownaListaZdarzenie);
                    glownaListaZdarzenie.removeKontakt(kontakt);
                }
                
                zapiszDaneDoXML(listaZdarzen, listaKontaktow);
                System.out.println("Kontakt usuniety ze zdarzenia (Baza i XML)!");
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void wyswietlSzczegolZdarzenia(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow, Scanner scanner) {
        if (listaZdarzen.isEmpty()) {
            System.out.println("Brak zdarzen.");
            return;
        }
        System.out.print("Podaj numer zdarzenia: ");
        try {
            int idx = scanner.nextInt();
            scanner.nextLine();
            if (idx >= 0 && idx < listaZdarzen.size()) {
                Zdarzenie z = listaZdarzen.get(idx);
                System.out.println("\n=== SZCZEGOLY ZDARZENIA ===");
                System.out.println("ID (Baza): " + z.getId());
                System.out.println("Nazwa: " + z.getNazwa());
                System.out.println("Data: " + z.getData()); 
                System.out.println("Lokalizacja: " + z.getLokalizacja());
                System.out.println("Opis: " + z.getOpis());
                
                List<Kontakt> dbKontakty = DBHelper.getKontaktyForZdarzenie(z.getId()); 
                if (dbKontakty != null && !dbKontakty.isEmpty()) {
                    System.out.println("\n--- Kontakty biorace udzial w zdarzeniu (z bazy) ---");
                    for (int i = 0; i < dbKontakty.size(); i++) {
                        System.out.println(i + ". ID: " + dbKontakty.get(i).getId() + " - " + dbKontakty.get(i).getImie() + " " + dbKontakty.get(i).getNazwisko());
                    }
                } else {
                    System.out.println("\n--- Brak kontaktow w tym zdarzeniu ---");
                }
                
                System.out.println("\n1. Przypisz kontakt do zdarzenia (w bazie)");
                System.out.println("2. Usun kontakt ze zdarzenia (w bazie)");
                System.out.println("3. Cofnij");
                System.out.print("Wybierz: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                if (choice == 1) {
                    przypisZdarzenieDoKontaktu(z, listaKontaktow, listaZdarzen, scanner);
                } else if (choice == 2) {
                    usunKontaktZeZdarzenia(z, listaZdarzen, listaKontaktow, scanner);
                }
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void przypisZdarzenieDoKontaktu(Zdarzenie zdarzenie, List<Kontakt> listaKontaktow, List<Zdarzenie> listaZdarzen, Scanner scanner) {
        if (listaKontaktow.isEmpty()) {
            System.out.println("Brak kontaktow do przypisania.");
            return;
        }
        
        System.out.println("\n--- Dostepni kontakty ---");
        for (int i = 0; i < listaKontaktow.size(); i++) {
            System.out.println(i + ". ID: " + listaKontaktow.get(i).getId() + " - " + listaKontaktow.get(i).getImie() + " " + listaKontaktow.get(i).getNazwisko());
        }
        
        System.out.print("Podaj numer kontaktu (z listy 0, 1, 2...) aby przypisac: ");
        try {
            int kontaktIdx = scanner.nextInt();
            scanner.nextLine();
            if (kontaktIdx >= 0 && kontaktIdx < listaKontaktow.size()) {
                Kontakt kontakt = listaKontaktow.get(kontaktIdx);
                
                DBHelper.addZdarzenieToKontakt(kontakt.getId(), zdarzenie.getId());
                
                zdarzenie.addKontakt(kontakt);
                kontakt.addZdarzenie(zdarzenie);
                
                zapiszDaneDoXML(listaZdarzen, listaKontaktow);
                System.out.println("Kontakt przypisany do zdarzenia (Baza i XML)!");
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void usunKontaktZeZdarzenia(Zdarzenie zdarzenie, List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow, Scanner scanner) {
        List<Kontakt> dbKontakty = DBHelper.getKontaktyForZdarzenie(zdarzenie.getId()); 
        
        if (dbKontakty == null || dbKontakty.isEmpty()) {
            System.out.println("Zdarzenie nie ma przypisanych kontaktow.");
            return;
        }
        
        System.out.println("\n--- Przypisane kontakty ---");
        for (int i = 0; i < dbKontakty.size(); i++) {
            System.out.println(i + ". ID: " + dbKontakty.get(i).getId() + " - " + dbKontakty.get(i).getImie() + " " + dbKontakty.get(i).getNazwisko());
        }
        
        System.out.print("Podaj numer kontaktu (z listy 0, 1, 2...) aby usunac: ");
        try {
            int kontaktIdx = scanner.nextInt();
            scanner.nextLine();
            if (kontaktIdx >= 0 && kontaktIdx < dbKontakty.size()) {
                Kontakt kontaktDoUsuniecia = dbKontakty.get(kontaktIdx);
                
                DBHelper.removeZdarzenieFromKontakt(kontaktDoUsuniecia.getId(), zdarzenie.getId());
                
                Kontakt glownaListaKontakt = listaKontaktow.stream()
                        .filter(k -> k.getId() == kontaktDoUsuniecia.getId())
                        .findFirst().orElse(null);
                
                if (glownaListaKontakt != null) {
                    zdarzenie.removeKontakt(glownaListaKontakt);
                    glownaListaKontakt.removeZdarzenie(zdarzenie);
                }
                
                zapiszDaneDoXML(listaZdarzen, listaKontaktow);
                System.out.println("Kontakt usuniety ze zdarzenia (Baza i XML)!");
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void dodajKontakt(List<Kontakt> listaKontaktow, List<Zdarzenie> listaZdarzen, Scanner scanner) {
        try {
            System.out.print("Imie: ");
            String imie = scanner.nextLine();
            System.out.print("Nazwisko: ");
            String nazwisko = scanner.nextLine();
            System.out.print("Numer telefonu (np. 501234567): ");
            String telStr = scanner.nextLine();
            System.out.print("Email: ");
            String emailStr = scanner.nextLine();

            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            com.google.i18n.phonenumbers.Phonenumber.PhoneNumber telefon = null;
            if (!telStr.isEmpty()) {
                try {
                    telefon = phoneUtil.parse(telStr, "PL");
                } catch (NumberParseException e) {
                    System.err.println("Bledny numer telefonu: " + e.getMessage());
                    return;
                }
            }

            InternetAddress email = null;
            if (!emailStr.isEmpty()) {
                try {
                    email = new InternetAddress(emailStr);
                } catch (AddressException e) {
                    System.err.println("Bledny email: " + e.getMessage());
                    return;
                }
            }

            Kontakt k = new Kontakt(imie, nazwisko, telefon, email);
            listaKontaktow.add(k);
            zapiszDaneDoXML(listaZdarzen, listaKontaktow);
            System.out.println("Kontakt dodany!");
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine(); // Konsumowanie ewentualnych pozostałości
        }
    }
    private static void usunKontakt(List<Kontakt> listaKontaktow, List<Zdarzenie> listaZdarzen, Scanner scanner) {
        wyswietlKontakty(listaKontaktow);
        System.out.print("Podaj numer kontaktu do usunieccia: ");
        try {
            int idx = scanner.nextInt();
            scanner.nextLine();
            if (idx >= 0 && idx < listaKontaktow.size()) {
                listaKontaktow.remove(idx); 

                zapiszDaneDoXML(listaZdarzen, listaKontaktow);
                System.out.println("Kontakt usuniety!");
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void dodajZdarzenie(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow, Scanner scanner) {
        try {
            System.out.print("Nazwa zdarzenia: ");
            String nazwa = scanner.nextLine();
            System.out.print("Data (YYYY-MM-DD): ");
            String dataStr = scanner.nextLine();
            System.out.print("Lokalizacja (URL lub miejsce): ");
            String lokalizacja = scanner.nextLine();
            System.out.print("Opis: ");
            String opis = scanner.nextLine();

            LocalDate data = LocalDate.parse(dataStr);
            Zdarzenie z = new Zdarzenie(nazwa, data, lokalizacja, opis); 
            listaZdarzen.add(z);
            zapiszDaneDoXML(listaZdarzen, listaKontaktow);
            System.out.println("Zdarzenie dodane!");
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void usunZdarzenie(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow, Scanner scanner) {
        wyswietlZdarzenia(listaZdarzen);
        System.out.print("Podaj numer zdarzenia do usunieccia: ");
        try {
            int idx = scanner.nextInt();
            scanner.nextLine();
            if (idx >= 0 && idx < listaZdarzen.size()) {
                listaZdarzen.remove(idx);

                zapiszDaneDoXML(listaZdarzen, listaKontaktow);
                System.out.println("Zdarzenie usuniete!");
            } else {
                System.out.println("Bledny numer!");
            }
        } catch (Exception e) {
            System.err.println("Blad: " + e.getMessage());
            scanner.nextLine();
        }
    }
    private static void stworzDomyslneDane(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        listaZdarzen.add(new Zdarzenie("Wakacje w Londynie", LocalDate.of(2026, 8, 10), "https://www.google.com/maps/search/London,+UK/@51.5074,-0.1278,13z", "Lot o 10:30, zwiedzanie muzeow."));
        listaZdarzen.add(new Zdarzenie("Open'er Festival 2026", LocalDate.of(2026, 7, 1), "https://www.google.com/maps/search/Gdynia-Kosakowo/@54.4909,18.5841,13z", "Karnet 4-dniowy, spotkanie z ekipa."));
        listaZdarzen.add(new Zdarzenie("Roast Gimpera", LocalDate.of(2026, 1, 25), "https://www.google.com/maps/search/Warszawa,+Stodola/@52.2297,21.0122,13z", "Bilety kupione, rzad 10."));
        listaZdarzen.add(new Zdarzenie("Stand-up: Mateusz Socha", LocalDate.of(2026, 3, 14), "https://www.google.com/maps/search/Torun,+CKK+Jordanki/@53.0238,18.5976,13z", "Nowy program 'Masochista'."));
        listaZdarzen.add(new Zdarzenie("Splyw kajakowy (Rzeka Wda)", LocalDate.of(2026, 6, 13), "https://www.google.com/maps/search/Woryty,+Pomeranian/@53.7050,18.5500,13z", "Weekendowy splyw z namiotami."));
        listaZdarzen.add(new Zdarzenie("Jarmark Bozonarodzeniowy", LocalDate.of(2025, 12, 14), "https://www.google.com/maps/search/Wroclaw,+Rynek/@51.1079,17.0385,13z", "Spotkanie na grzane wino."));
        listaZdarzen.add(new Zdarzenie("Egzamin (Programowanie)", LocalDate.of(2026, 1, 29), "https://www.google.com/maps/search/Uniwersytet/@51.1079,17.0385,13z", "Sesja zimowa. Powtorzyc Jave!"));
        listaZdarzen.add(new Zdarzenie("Urodziny wujka Krzysia (50.)", LocalDate.of(2025, 11, 23), "https://www.google.com/maps/search/Restauracja+Pod+Debem/@52.2297,21.0122,13z", "Kupic prezent."));
        listaZdarzen.add(new Zdarzenie("Pyrkon - Festiwal Fantastyki", LocalDate.of(2026, 6, 12), "https://www.google.com/maps/search/Poznan,+MTP/@52.4064,16.9252,13z", "Caly weekend, kupic bilet 3-dniowy."));
        listaZdarzen.add(new Zdarzenie("Rezerwacja na tatuaz", LocalDate.of(2026, 2, 20), "https://www.google.com/maps/search/Studio+Czarny+Tusz/@52.2297,21.0122,13z", "Dokonczenie 'rekawa'. Sesja o 10:00."));

        try {
            listaKontaktow.add(new Kontakt("Cezary", "Pazura", phoneUtil.parse("501101101", "PL"), new InternetAddress("cezary.pazura@agencja.com")));
            listaKontaktow.add(new Kontakt("Monika", "Olejnik", phoneUtil.parse("602202202", "PL"), new InternetAddress("monika.olejnik@tvn.pl")));
            listaKontaktow.add(new Kontakt("Robert", "Maklowicz", phoneUtil.parse("603303303", "PL"), new InternetAddress("robert.maklowicz@podroze.pl")));
            listaKontaktow.add(new Kontakt("Magda", "Gessler", phoneUtil.parse("704404404", "PL"), new InternetAddress("magda.gessler@kuchenne.com")));
            listaKontaktow.add(new Kontakt("Kuba", "Wojewodzki", phoneUtil.parse("505505505", "PL"), new InternetAddress("kuba.wojewodzki@show.pl")));
            listaKontaktow.add(new Kontakt("Katarzyna", "Figura", phoneUtil.parse("606606606", "PL"), new InternetAddress("katarzyna.figura@film.pl")));
            listaKontaktow.add(new Kontakt("Andrzej", "Grabowski", phoneUtil.parse("707707707", "PL"), new InternetAddress("ferdek@kiepscy.pl")));
            listaKontaktow.add(new Kontakt("Dorota", "Wellman", phoneUtil.parse("808808808", "PL"), new InternetAddress("dorota.wellman@dziendobry.pl")));
            listaKontaktow.add(new Kontakt("Marcin", "Prokop", phoneUtil.parse("509909909", "PL"), new InternetAddress("marcin.prokop@dziendobry.pl")));
            listaKontaktow.add(new Kontakt("Szymon", "Holownia", phoneUtil.parse("600000001", "PL"), new InternetAddress("szymon.holownia@sejm.gov.pl")));
        
        } catch (AddressException e) {
            System.err.println("Blad w adresie e-mail: " + e.getMessage());
        } catch (NumberParseException e) {
            System.err.println("Blad w numerze telefonu: " + e.getMessage());
        }
    }
}