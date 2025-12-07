package midacalPakiet;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class Main {

    private static final String DATA_FILE_PATH = "kalendarz.xml";
    private static int xmlSaveCounter = 0;
    private static final int BACKUP_INTERVAL = 10; // liczba zapisow XML przed backupem do DB

    public static void main(String[] args) {
        final List<Zdarzenie> listaZdarzen = new ArrayList<>();
        final List<Kontakt> listaKontaktow = new ArrayList<>();

        // ✅ START: Inicjalizuj bazę danych
        DBHelper.initDatabase();

        // ✅ Jeśli baza ma dane → użyj je (przerywamy połączenie)
        List<Kontakt> dbKontakty = DBHelper.getAllKontakty();
        List<Zdarzenie> dbZdarzenia = DBHelper.getAllZdarzenia();
        
        if ((dbKontakty != null && !dbKontakty.isEmpty()) || (dbZdarzenia != null && !dbZdarzenia.isEmpty())) {
            // Baza ma dane - załaduj je
            if (dbKontakty != null && !dbKontakty.isEmpty()) {
                listaKontaktow.addAll(dbKontakty);
            }
            if (dbZdarzenia != null && !dbZdarzenia.isEmpty()) {
                listaZdarzen.addAll(dbZdarzenia);
            }

            // Wczytaj relacje między kontaktami a zdarzeniami
            for (Kontakt k : listaKontaktow) {
                if (k.getId() > 0) {
                    List<Zdarzenie> rel = DBHelper.getZdarzeniaForKontakt(k.getId());
                    k.setZdarzenia(rel);
                }
            }
            for (Zdarzenie z : listaZdarzen) {
                if (z.getId() > 0) {
                    List<Kontakt> relk = DBHelper.getKontaktyForZdarzenie(z.getId());
                    z.setKontakty(relk);
                }
            }
            System.out.println("Wczytano dane z bazy danych");
        } else {
            // Jeśli baza pusta -> wczytaj XML
            KalendarzDane wrapper = wczytajDaneZXML();

            if (wrapper != null && wrapper.getZdarzenia() != null && wrapper.getKontakty() != null) {
                listaZdarzen.addAll(wrapper.getZdarzenia());
                listaKontaktow.addAll(wrapper.getKontakty());
                System.out.println("Pomyslnie wczytano dane z pliku " + DATA_FILE_PATH);
                // Wstaw wczytane dane z XML do bazy
                DBHelper.syncAllToDB(listaKontaktow, listaZdarzen);
            } else {
                // Jeśli XML pusty -> stwórz domyślne dane i zapisz do bazy
                System.out.println("Nie udalo sie wczytac danych z pliku. Tworzenie nowych danych...");
                stworzDomyslneDane(listaZdarzen, listaKontaktow);
                zapiszDaneDoXML(listaZdarzen, listaKontaktow);
                // Wstaw domyślne dane do bazy
                DBHelper.syncAllToDB(listaKontaktow, listaZdarzen);
            }
        }

        DBHelper.printAllKontakty();
        DBHelper.printAllZdarzenia();

        // Dodaj hook wyjscia, aby zapisac aktualne dane do bazy przy zamknieciu aplikacji
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\nZapisywanie zmian do bazy danych...");
                DBHelper.syncAllToDB(listaKontaktow, listaZdarzen);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));


        //wyswietlenie utworzonych zdarzen
        System.out.println("\nUtworzone Zdarzenia (" + listaZdarzen.size() + ")");
        for (Zdarzenie z : listaZdarzen) {
            System.out.println(z);
        }

        //wyswietlenie utworzonych kontaktow
        System.out.println("\nUtworzone Kontakty (" + listaKontaktow.size() + ")");
        for (Kontakt k : listaKontaktow) {
            System.out.println(k);
        }

        //sortowanie wedlug domyslnego atrybutu w klasie Zdarzenie (data) (Comparable/metoda compareTo)
        Collections.sort(listaZdarzen);
        System.out.println("\nPosortowane Zdarzenia (po dacie)");
        for (Zdarzenie z : listaZdarzen) {
            System.out.println(z);
        }

        //sortowanie wedlug domyslnego atrybutu w klasie Kontakt (nazwisko) (Comparable/metoda compareTo)
        Collections.sort(listaKontaktow);
        System.out.println("\nPosortowane Kontakty (po nazwisku)");
        for (Kontakt k : listaKontaktow) {
            System.out.println(k);
        }

        //sortowanie wedlug pozostalych atrybutow w klasach Zdarzenie (nazwa, miejsce, opis) (Comparator/metoda compare)
        Collections.sort(listaZdarzen, new SortZdarzenieNazwa());
        System.out.println("\nPosortowane Zdarzenia (po nazwie)");
        for (Zdarzenie z : listaZdarzen) {
            System.out.println(z);
        }

        Collections.sort(listaZdarzen, new SortZdarzenieMiejsce());
        System.out.println("\nPosortowane Zdarzenia (po miejscu)");
        for (Zdarzenie z : listaZdarzen) {
            System.out.println(z);
        }

        Collections.sort(listaZdarzen, new SortZdarzenieOpis());
        System.out.println("\nPosortowane Zdarzenia (po opisie)");
        for (Zdarzenie z : listaZdarzen) {
            System.out.println(z);
        }        
        
        //sortowanie wedlug pozostalych atrybutow w klasie Kontakt (imie, telefon, email) (Comparator/metoda compare)
        Collections.sort(listaKontaktow, new SortKontaktImie());
        System.out.println("\nPosortowane Kontakty (po imieniu)");
        for (Kontakt k : listaKontaktow) {
            System.out.println(k);
        }

        Collections.sort(listaKontaktow, new SortKontaktTelefon());
        System.out.println("\nPosortowane Kontakty (po telefonie)");
        for (Kontakt k : listaKontaktow) {
            System.out.println(k);
        }

        Collections.sort(listaKontaktow, new SortKontaktEmail());
        System.out.println("\nPosortowane Kontakty (po emailu)");
        for (Kontakt k : listaKontaktow) {
            System.out.println(k);
        }

    }

    private static void stworzDomyslneDane(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow) {
        //tworzenie obiektu PhoneNumberUtil do walidacji numerow telefonow
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        //dodanie 10 zdarzen do listy zdarzen z URL-ami na Google Maps
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

        //dodanie 10 kontaktow do listy kontaktow
        //sprawdzenie poprawnosci numerow telefonow i adresow e-mail
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

    public static void zapiszDaneDoXML(List<Zdarzenie> listaZdarzen, List<Kontakt> listaKontaktow) {
        try {
            JAXBContext context = JAXBContext.newInstance(KalendarzDane.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            KalendarzDane wrapper = new KalendarzDane();
            wrapper.setZdarzenia(listaZdarzen);
            wrapper.setKontakty(listaKontaktow);

            marshaller.marshal(wrapper, new File(DATA_FILE_PATH));
            System.out.println("Pomyslnie zapisano dane do pliku " + DATA_FILE_PATH);
            
            // Inkremencja licznika zapisu XML i opcjonalny backup do bazy co BACKUP_INTERVAL zapisow
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
        } catch (JAXBException e) {
            System.err.println("Blad podczas zapisu danych do XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static KalendarzDane wczytajDaneZXML() {
        try {
            File file = new File(DATA_FILE_PATH);
            if (!file.exists()) {
                return null;
            }

            JAXBContext context = JAXBContext.newInstance(KalendarzDane.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            return (KalendarzDane) unmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            System.err.println("Blad podczas wczytywania danych z XML: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}