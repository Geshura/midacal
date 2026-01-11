package midacal;

import java.io.Serializable;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.mail.internet.InternetAddress;

public class Kontakt implements Serializable, Comparable<Kontakt> {
    private static final long serialVersionUID = 1L;
    private String imie;
    private String nazwisko;
    @JsonIgnore private PhoneNumber numerTelefonu;
    @JsonIgnore private InternetAddress email;
    
    // NOWE POLE: Lista zdarzeń, w których kontakt bierze udział
    @JsonIgnore private List<Zdarzenie> udzialWZdarzeniach = new ArrayList<>();

    public Kontakt() {}
    public Kontakt(String imie, String nazwisko, PhoneNumber numer, InternetAddress email) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.numerTelefonu = numer;
        this.email = email;
    }

    @JsonProperty("imie") public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }
    @JsonProperty("nazwisko") public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }
    
    public List<Zdarzenie> getUdzialWZdarzeniach() { return udzialWZdarzeniach; }
    public void dodajZdarzenie(Zdarzenie z) { if (!udzialWZdarzeniach.contains(z)) udzialWZdarzeniach.add(z); }

    @JsonProperty("telefon")
    public String getTelStr() { return numerTelefonu != null ? String.valueOf(numerTelefonu.getNationalNumber()) : ""; }
    public void setTelStr(String tel) {
        try { this.numerTelefonu = PhoneNumberUtil.getInstance().parse(tel, "PL"); } catch (Exception e) {}
    }

    @JsonProperty("email")
    public String getEmailStr() { return email != null ? email.getAddress() : ""; }
    public void setEmailStr(String mail) {
        try { this.email = new InternetAddress(mail); } catch (Exception e) {}
    }

    @Override
    public int compareTo(Kontakt inny) {
        int res = this.nazwisko.compareToIgnoreCase(inny.nazwisko);
        return (res == 0) ? this.imie.compareToIgnoreCase(inny.imie) : res;
    }

    public static class ImieComparator implements Comparator<Kontakt> {
        @Override public int compare(Kontakt k1, Kontakt k2) { return k1.imie.compareToIgnoreCase(k2.imie); }
    }
    public static class TelComparator implements Comparator<Kontakt> {
        @Override public int compare(Kontakt k1, Kontakt k2) {
            return Long.compare(k1.numerTelefonu.getNationalNumber(), k2.numerTelefonu.getNationalNumber());
        }
    }
    public static class EmailComparator implements Comparator<Kontakt> {
        @Override public int compare(Kontakt k1, Kontakt k2) { return k1.getEmailStr().compareToIgnoreCase(k2.getEmailStr()); }
    }

    @Override
    public String toString() {
        return String.format("%-15s %-15s | Tel: %-10s | Email: %s | Zdarzenia: %d", 
                nazwisko, imie, getTelStr(), getEmailStr(), udzialWZdarzeniach.size());
    }
}