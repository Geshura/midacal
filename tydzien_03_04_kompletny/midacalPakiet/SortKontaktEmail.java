package midacalPakiet;

import java.util.Comparator;

public class SortKontaktEmail implements Comparator<Kontakt> {
    public int compare(Kontakt kontakt1, Kontakt kontakt2) {
        return kontakt1.getEmail().getAddress().compareTo(kontakt2.getEmail().getAddress());
    }
}
