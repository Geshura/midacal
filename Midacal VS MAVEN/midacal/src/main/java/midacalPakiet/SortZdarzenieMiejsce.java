package midacalPakiet;

import java.util.Comparator;
import java.net.URI;

public class SortZdarzenieMiejsce implements Comparator<Zdarzenie> {
    public int compare(Zdarzenie zdarzenie1, Zdarzenie zdarzenie2) {
        URI a = zdarzenie1.getLokalizacja();
        URI b = zdarzenie2.getLokalizacja();
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.toString().compareTo(b.toString());
    }
}
