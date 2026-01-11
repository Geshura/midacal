package midacal;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Zdarzenie implements Serializable, Comparable<Zdarzenie> {
    private static final long serialVersionUID = 1L;

    @JsonProperty("tytul")
    private String tytul;
    
    @JsonProperty("opis")
    private String opis;
    
    @JsonProperty("data")
    private LocalDate data;
    
    @JsonProperty("link")
    private URL miejsce;

    public Zdarzenie() {}

    public Zdarzenie(String tytul, String opis, LocalDate data, URL miejsce) {
        this.tytul = tytul;
        this.opis = opis;
        this.data = data;
        this.miejsce = miejsce;
    }

    public String getTytul() { return tytul; }
    public String getOpis() { return opis; }
    public LocalDate getData() { return data; }
    public URL getMiejsce() { return miejsce; }

    @Override
    public int compareTo(Zdarzenie inne) { return this.data.compareTo(inne.data); }

    // Komparator alternatywny (wg Tytułu)
    public static class TytulComparator implements Comparator<Zdarzenie> {
        @Override public int compare(Zdarzenie z1, Zdarzenie z2) { return z1.tytul.compareToIgnoreCase(z2.tytul); }
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Opis: %-25s | Link: %s", data, tytul, opis, miejsce);
    }
}