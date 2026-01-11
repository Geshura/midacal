package midacal;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Zdarzenie implements Serializable, Comparable<Zdarzenie> {
    private static final long serialVersionUID = 1L;
    private String tytul;
    private String opis;
    private LocalDate data;
    private URL miejsce;

    public Zdarzenie() {}

    public Zdarzenie(String tytul, String opis, LocalDate data, URL miejsce) {
        this.tytul = tytul;
        this.opis = opis;
        this.data = data;
        this.miejsce = miejsce;
    }

    public String getTytul() { return tytul; }
    public void setTytul(String tytul) { this.tytul = tytul; }
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public URL getMiejsce() { return miejsce; }
    public void setMiejsce(URL miejsce) { this.miejsce = miejsce; }

    @Override
    public int compareTo(Zdarzenie inne) { return this.data.compareTo(inne.data); }

    public static class TytulComparator implements Comparator<Zdarzenie> {
        @Override public int compare(Zdarzenie z1, Zdarzenie z2) { return z1.tytul.compareToIgnoreCase(z2.tytul); }
    }
    public static class OpisComparator implements Comparator<Zdarzenie> {
        @Override public int compare(Zdarzenie z1, Zdarzenie z2) { return z1.opis.compareToIgnoreCase(z2.opis); }
    }
    public static class LinkComparator implements Comparator<Zdarzenie> {
        @Override public int compare(Zdarzenie z1, Zdarzenie z2) {
            return z1.miejsce.toString().compareToIgnoreCase(z2.miejsce.toString());
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %-15s | Opis: %-15s | Miejsce: %s", data, tytul, opis, miejsce);
    }
}