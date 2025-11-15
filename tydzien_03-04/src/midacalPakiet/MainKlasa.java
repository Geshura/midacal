package midacalPakiet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class MainKlasa {

    public static void main(String[] args) {
        System.out.println("Start programu..");
        
        List<Zdarzenie> ListaZdarzenia = new ArrayList();
        List<Kontakt> ListaKontakty = new ArrayList();

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        // --- Tworzenie 10 zdarzeñ ---
        ListaZdarzenia.add(new Zdarzenie("Wakacje w Londynie", LocalDate.of(2026, 8, 10), "Londyn, UK", "Lot o 10:30, zwiedzanie muzeów."));
        ListaZdarzenia.add(new Zdarzenie("Open'er Festival 2026", LocalDate.of(2026, 7, 1), "Gdynia-Kosakowo", "Karnet 4-dniowy, spotkanie z ekip¹."));
        ListaZdarzenia.add(new Zdarzenie("Roast Gimpera", LocalDate.of(2026, 1, 25), "Warszawa, Stodo³a", "Bilety kupione, rz¹d 10."));
        ListaZdarzenia.add(new Zdarzenie("Stand-up: Mateusz Socha", LocalDate.of(2026, 3, 14), "Toruñ, CKK Jordanki", "Nowy program 'Masochista'."));
        ListaZdarzenia.add(new Zdarzenie("Sp³yw kajakowy (Rzeka Wda)", LocalDate.of(2026, 6, 13), "Woryty (Start)", "Weekendowy sp³yw z namiotami."));
        ListaZdarzenia.add(new Zdarzenie("Jarmark Bo¿onarodzeniowy", LocalDate.of(2025, 12, 14), "Wroc³aw, Rynek", "Spotkanie na grzane wino."));
        ListaZdarzenia.add(new Zdarzenie("Egzamin (Programowanie)", LocalDate.of(2026, 1, 29), "Uniwersytet, Sala 301", "Sesja zimowa. Powtórzyæ Javê!"));
        ListaZdarzenia.add(new Zdarzenie("Urodziny wujka Krzysia (50.)", LocalDate.of(2025, 11, 23), "Restauracja 'Pod Dêbem'", "Kupiæ prezent."));
        ListaZdarzenia.add(new Zdarzenie("Pyrkon - Festiwal Fantastyki", LocalDate.of(2026, 6, 12), "Poznañ, MTP", "Ca³y weekend, kupiæ bilet 3-dniowy."));
        ListaZdarzenia.add(new Zdarzenie("Rezerwacja na tatua¿", LocalDate.of(2026, 2, 20), "Studio 'Czarny Tusz'", "Dokoñczenie 'rêkawa'. Sesja o 10:00."));

        // --- Tworzenie 10 kontaktów ---
        try {
            ListaKontakty.add(new Kontakt("Cezary", "Pazura", phoneUtil.parse("501101101", "PL"), new InternetAddress("cezary.pazura@agencja.com")));
            ListaKontakty.add(new Kontakt("Monika", "Olejnik", phoneUtil.parse("602202202", "PL"), new InternetAddress("monika.olejnik@tvn.pl")));
            ListaKontakty.add(new Kontakt("Robert", "Mak³owicz", phoneUtil.parse("603303303", "PL"), new InternetAddress("robert.maklowicz@podroze.pl")));
            ListaKontakty.add(new Kontakt("Magda", "Gessler", phoneUtil.parse("704404404", "PL"), new InternetAddress("magda.gessler@kuchenne.com")));
            ListaKontakty.add(new Kontakt("Kuba", "Wojewódzki", phoneUtil.parse("505505505", "PL"), new InternetAddress("kuba.wojewodzki@show.pl")));
            ListaKontakty.add(new Kontakt("Katarzyna", "Figura", phoneUtil.parse("606606606", "PL"), new InternetAddress("katarzyna.figura@film.pl")));
            ListaKontakty.add(new Kontakt("Andrzej", "Grabowski", phoneUtil.parse("707707707", "PL"), new InternetAddress("ferdek@kiepscy.pl")));
            ListaKontakty.add(new Kontakt("Dorota", "Wellman", phoneUtil.parse("808808808", "PL"), new InternetAddress("dorota.wellman@dziendobry.pl")));
            ListaKontakty.add(new Kontakt("Marcin", "Prokop", phoneUtil.parse("509909909", "PL"), new InternetAddress("marcin.prokop@dziendobry.pl")));
            ListaKontakty.add(new Kontakt("Szymon", "Ho³ownia", phoneUtil.parse("600000001", "PL"), new InternetAddress("szymon.holownia@sejm.gov.pl")));
        
        } catch (AddressException e) {
            System.err.println("B³¹d w adresie e-mail: " + e.getMessage());
        } catch (NumberParseException e) {
            System.err.println("B³¹d w numerze telefonu: " + e.getMessage());
        }

        System.out.println("\n--- Utworzone Zdarzenia (" + ListaZdarzenia.size() + ") ---");
        for (Zdarzenie z : ListaZdarzenia) {
            System.out.println(z);
        }

        System.out.println("\n--- Utworzone Kontakty (" + ListaKontakty.size() + ") ---");
        for (Kontakt k : ListaKontakty) {
            System.out.println(k);
        }
    }
}