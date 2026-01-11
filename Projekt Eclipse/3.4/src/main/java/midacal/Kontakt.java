package midacal;

import java.io.Serializable;
import java.util.Comparator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.mail.internet.InternetAddress;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Kontakt implements Serializable, Comparable<Kontakt> {
    private static final long serialVersionUID = 1L;
    private String imie;
    private String nazwisko;
    private PhoneNumber numerTelefonu;
    private InternetAddress email;

    public Kontakt() {} // Wymagany dla Jacksona

    public Kontakt(String imie, String nazwisko, PhoneNumber numerTelefonu, InternetAddress email) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.numerTelefonu = numerTelefonu;
        this.email = email;
    }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }
    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }
    public PhoneNumber getNumerTelefonu() { return numerTelefonu; }
    public void setNumerTelefonu(PhoneNumber numerTelefonu) { this.numerTelefonu = numerTelefonu; }
    public InternetAddress getEmail() { return email; }
    public void setEmail(InternetAddress email) { this.email = email; }

    @Override
    public int compareTo(Kontakt inny) {
        int res = this.nazwisko.compareToIgnoreCase(inny.nazwisko);
        return (res == 0) ? this.imie.compareToIgnoreCase(inny.imie) : res;
    }

    public static class ImieComparator implements Comparator<Kontakt> {
        @Override public int compare(Kontakt k1, Kontakt k2) { return k1.imie.compareToIgnoreCase(k2.imie); }
    }
    public static class EmailComparator implements Comparator<Kontakt> {
        @Override public int compare(Kontakt k1, Kontakt k2) {
            return k1.email.getAddress().compareToIgnoreCase(k2.email.getAddress());
        }
    }
    public static class TelComparator implements Comparator<Kontakt> {
        @Override public int compare(Kontakt k1, Kontakt k2) {
            return Long.compare(k1.numerTelefonu.getNationalNumber(), k2.numerTelefonu.getNationalNumber());
        }
    }

    @Override
    public String toString() {
        return String.format("%-15s %-15s | Tel: %-10d | Email: %s", 
            nazwisko, imie, numerTelefonu.getNationalNumber(), email.getAddress());
    }
}