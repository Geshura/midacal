package midacalPakiet;

import java.util.Comparator;

public class SortZdarzenieNazwa implements Comparator<Zdarzenie> {
    public int compare(Zdarzenie zdarzenie1, Zdarzenie zdarzenie2) {
        return zdarzenie1.getNazwa().compareTo(zdarzenie2.getNazwa());
    }
}
