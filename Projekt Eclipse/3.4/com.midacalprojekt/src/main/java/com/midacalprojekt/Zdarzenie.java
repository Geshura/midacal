package com.midacalprojekt;

import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Zdarzenie implements Serializable, Comparable<Zdarzenie> {
    private static final long serialVersionUID = 1L;

    private String tytul;
    private Date data; // Komponent standardowy
    private URL url;   // Komponent standardowy
    private Kontakt kontakt;

    public Zdarzenie() {}

    public Zdarzenie(String tytul, Date data, URL url, Kontakt kontakt) {
        this.tytul = tytul;
        this.data = data;
        this.url = url;
        this.kontakt = kontakt;
    }

    // --- Comparable: Domyślne sortowanie chronologiczne ---
    @Override
    public int compareTo(Zdarzenie inne) {
        if (this.data == null) return -1;
        if (inne.data == null) return 1;
        return this.data.compareTo(inne.data);
    }

    // --- Comparator: Alternatywne sortowanie po Tytule ---
    public static class KomparatorTytul implements Comparator<Zdarzenie> {
        @Override
        public int compare(Zdarzenie z1, Zdarzenie z2) {
            String t1 = (z1.getTytul() != null) ? z1.getTytul() : "";
            String t2 = (z2.getTytul() != null) ? z2.getTytul() : "";
            return t1.compareToIgnoreCase(t2);
        }
    }

    // Gettery i Settery
    public String getTytul() { return tytul; }
    public void setTytul(String tytul) { this.tytul = tytul; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public URL getUrl() { return url; }
    public void setUrl(URL url) { this.url = url; }

    public Kontakt getKontakt() { return kontakt; }
    public void setKontakt(Kontakt kontakt) { this.kontakt = kontakt; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dataStr = (data != null) ? sdf.format(data) : "---";
        String urlStr = (url != null) ? url.toString() : "Brak";
        
        return String.format("[%s] %-25s (Link: %s) - Z: %s", 
                dataStr, tytul, urlStr, (kontakt != null ? kontakt.getNazwisko() : "Brak"));
    }
}