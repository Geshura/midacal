package midacal;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Zdarzenie implements Serializable, Comparable<Zdarzenie> {
    private static final long serialVersionUID = 1L;

    private String tytul;
    private String opis;
    private LocalDateTime data;
    private URL miejsce;

    public Zdarzenie() {}

    public Zdarzenie(String tytul, String opis, LocalDateTime data, URL miejsce) {
        this.tytul = tytul;
        this.opis = opis;
        this.data = data;
        this.miejsce = miejsce;
    }

    public String getTytul() { return tytul; }
    public void setTytul(String tytul) { this.tytul = tytul; }
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public URL getMiejsce() { return miejsce; }
    public void setMiejsce(URL miejsce) { this.miejsce = miejsce; }

    @Override
    public int compareTo(Zdarzenie inne) {
        return (this.data == null || inne.data == null) ? 0 : this.data.compareTo(inne.data);
    }

    public static class TytulComparator implements Comparator<Zdarzenie> {
        @Override
        public int compare(Zdarzenie z1, Zdarzenie z2) {
            return z1.getTytul().compareToIgnoreCase(z2.getTytul());
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %-15s | Opis: %-15s | Link: %s", data, tytul, opis, miejsce);
    }
}