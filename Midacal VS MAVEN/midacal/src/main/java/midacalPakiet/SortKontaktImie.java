package midacalPakiet;

import java.util.Comparator;
public class SortKontaktImie implements Comparator<Kontakt> {
    public int compare(Kontakt kontakt1, Kontakt kontakt2) {
        return kontakt1.getImie().compareTo(kontakt2.getImie());
    }
}
