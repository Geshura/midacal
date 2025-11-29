package midacalPakiet;

import java.util.Comparator;

public class SortZdarzenieOpis implements Comparator<Zdarzenie> {
    public int compare(Zdarzenie zdarzenie1, Zdarzenie zdarzenie2) {
        return zdarzenie1.getOpis().compareTo(zdarzenie2.getOpis());
    }
}
