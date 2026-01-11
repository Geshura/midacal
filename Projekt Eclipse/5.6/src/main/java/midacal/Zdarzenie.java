package midacal;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Zdarzenie implements Serializable, Comparable<Zdarzenie> {
    private static final long serialVersionUID = 1L;
    private Long id;
    @JsonProperty("tytul") private String tytul;
    @JsonProperty("opis") private String opis;
    @JsonProperty("data") private LocalDate data;
    @JsonProperty("link") private URL miejsce;
    @JsonIgnore private List<Kontakt> kontakty = new ArrayList<>();

    public Zdarzenie() {}
    public Zdarzenie(String tytul, String opis, LocalDate data, URL miejsce) {
        this.tytul = tytul;
        this.opis = opis;
        this.data = data;
        this.miejsce = miejsce;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public List<Kontakt> getKontakty() { return kontakty; }
    public void setKontakty(List<Kontakt> kontakty) { this.kontakty = kontakty; }
    public void dodajKontakt(Kontakt k) { if (!kontakty.contains(k)) kontakty.add(k); }
    public void usunKontakt(Kontakt k) { kontakty.remove(k); }

    public String getTytul() { return tytul; }
    public void setTytul(String t) { this.tytul = t; }
    public String getOpis() { return opis; }
    public void setOpis(String o) { this.opis = o; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate d) { this.data = d; }
    public URL getMiejsce() { return miejsce; }
    public void setMiejsce(URL m) { this.miejsce = m; }

    @Override
    public int compareTo(Zdarzenie inne) { return this.data.compareTo(inne.data); }

    public static class TytulComparator implements Comparator<Zdarzenie> {
        @Override public int compare(Zdarzenie z1, Zdarzenie z2) { return z1.tytul.compareToIgnoreCase(z2.tytul); }
    }
    public static class OpisComparator implements Comparator<Zdarzenie> {
        @Override public int compare(Zdarzenie z1, Zdarzenie z2) { return z1.opis.compareToIgnoreCase(z2.opis); }
    }
    public static class LinkComparator implements Comparator<Zdarzenie> {
        @Override public int compare(Zdarzenie z1, Zdarzenie z2) { return z1.miejsce.toString().compareToIgnoreCase(z2.miejsce.toString()); }
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Opis: %-25s | Link: %s", data, tytul, opis, miejsce);
    }
}