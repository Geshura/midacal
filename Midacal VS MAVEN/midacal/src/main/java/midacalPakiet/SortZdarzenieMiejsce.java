package midacalPakiet;

import java.util.Comparator;

public class SortZdarzenieMiejsce implements Comparator<Zdarzenie> {
    public int compare(Zdarzenie zdarzenie1, Zdarzenie zdarzenie2) {
        return zdarzenie1.getMiejsce().compareTo(zdarzenie2.getMiejsce());
    }
}
