package midacal;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Zdarzenie implements Serializable, Comparable<Zdarzenie> {
    private static final long serialVersionUID = 1L;
    @JsonProperty("tytul") private String tytul;
    @JsonProperty("opis") private String opis;
    @JsonProperty("data") private LocalDate data;
    @JsonProperty("link") private URL miejsce;
    
    // NOWE POLE: Lista kontaktów biorących udział w zdarzeniu
    @JsonIgnore private List<Kontakt> listaUczestnikow = new ArrayList<>();

    public Zdarzenie() {}
    public Zdarzenie(String tytul, String opis, LocalDate data, URL miejsce) {
        this.tytul = tytul;
        this.opis = opis;
        this.data = data;
        this.miejsce = miejsce;
    }

    public String getTytul() { return tytul; }
    public void setTytul(String t) { this.tytul = t; }
    public String getOpis() { return opis; }
    public void setOpis(String o) { this.opis = o; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate d) { this.data = d; }
    public URL getMiejsce() { return miejsce; }
    public void setMiejsce(URL m) { this.miejsce = m; }
    
    public List<Kontakt> getListaUczestnikow() { return listaUczestnikow; }
    public void dodajUczestnika(Kontakt k) { 
        if (!listaUczestnikow.contains(k)) {
            listaUczestnikow.add(k);
            k.dodajZdarzenie(this); // Obustronne powiązanie (ORM logic)
        }
    }

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
        return String.format("[%s] %-20s | Uczestnicy: %d | Link: %s", data, tytul, listaUczestnikow.size(), miejsce);
    }
}