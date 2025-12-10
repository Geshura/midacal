package midacalPakiet;

import java.util.Comparator;
import java.net.URI;

public class Sortowanie {

    // --- Sortowanie Zdarzeń (Zdarzenie) ---

    /**
     * Komparator sortujący Zdarzenia alfabetycznie według nazwy.
     */
    public static final Comparator<Zdarzenie> SORTUJ_ZDARZENIE_NAZWA = (zdarzenie1, zdarzenie2) -> 
        zdarzenie1.getNazwa().compareTo(zdarzenie2.getNazwa());

    /**
     * Komparator sortujący Zdarzenia alfabetycznie według opisu.
     */
    public static final Comparator<Zdarzenie> SORTUJ_ZDARZENIE_OPIS = (zdarzenie1, zdarzenie2) -> 
        zdarzenie1.getOpis().compareTo(zdarzenie2.getOpis());

    /**
     * Komparator sortujący Zdarzenia według lokalizacji (URI).
     * Zakłada, że nullowa lokalizacja jest "mniejsza" (ustawiana wcześniej) niż nienull.
     */
    public static final Comparator<Zdarzenie> SORTUJ_ZDARZENIE_MIEJSCE = (zdarzenie1, zdarzenie2) -> {
        URI a = zdarzenie1.getLokalizacja();
        URI b = zdarzenie2.getLokalizacja();
        
        // Obsługa wartości null (zgodnie z logiką z SortZdarzenieMiejsce.java)
        if (a == null && b == null) return 0;
        if (a == null) return -1; // zdarzenie1 bez lokalizacji jest mniejsze
        if (b == null) return 1;  // zdarzenie2 bez lokalizacji jest mniejsze
        
        return a.toString().compareTo(b.toString());
    };


    // --- Sortowanie Kontaktów (Kontakt) ---

    /**
     * Komparator sortujący Kontakty alfabetycznie według imienia.
     */
    public static final Comparator<Kontakt> SORTUJ_KONTAKT_IMIE = (kontakt1, kontakt2) -> 
        kontakt1.getImie().compareTo(kontakt2.getImie());

    /**
     * Komparator sortujący Kontakty numerycznie według numeru telefonu.
     */
    public static final Comparator<Kontakt> SORTUJ_KONTAKT_TELEFON = (kontakt1, kontakt2) -> 
        kontakt1.getTelefon() != null && kontakt2.getTelefon() != null
            ? Long.compare(kontakt1.getTelefon().getNationalNumber(), kontakt2.getTelefon().getNationalNumber())
            : (kontakt1.getTelefon() == null ? (kontakt2.getTelefon() == null ? 0 : -1) : 1);


    /**
     * Komparator sortujący Kontakty alfabetycznie według adresu email.
     */
    public static final Comparator<Kontakt> SORTUJ_KONTAKT_EMAIL = (kontakt1, kontakt2) -> 
        kontakt1.getEmail() != null && kontakt2.getEmail() != null
            ? kontakt1.getEmail().getAddress().compareTo(kontakt2.getEmail().getAddress())
            : (kontakt1.getEmail() == null ? (kontakt2.getEmail() == null ? 0 : -1) : 1);
}