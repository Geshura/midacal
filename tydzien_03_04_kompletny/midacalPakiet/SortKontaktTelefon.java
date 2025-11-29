package midacalPakiet;

import java.util.Comparator;

public class SortKontaktTelefon implements Comparator<Kontakt> {
    public int compare(Kontakt kontakt1, Kontakt kontakt2) {
        return Long.compare(kontakt1.getTelefon().getNationalNumber(), kontakt2.getTelefon().getNationalNumber());
    }
}
