package midacal;

import java.io.Serializable;
import java.util.Comparator;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.mail.internet.InternetAddress;

public class Kontakt implements Serializable, Comparable<Kontakt> {
    private static final long serialVersionUID = 1L;
    private String imie;
    private String nazwisko;
    private PhoneNumber numerTelefonu;
    private InternetAddress email;

    public Kontakt() {}
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
        int wynik = this.nazwisko.compareToIgnoreCase(inny.nazwisko);
        return (wynik == 0) ? this.imie.compareToIgnoreCase(inny.imie) : wynik;
    }

    public static class EmailComparator implements Comparator<Kontakt> {
        @Override
        public int compare(Kontakt k1, Kontakt k2) {
            String e1 = (k1.getEmail() != null) ? k1.getEmail().getAddress() : "";
            String e2 = (k2.getEmail() != null) ? k2.getEmail().getAddress() : "";
            return e1.compareToIgnoreCase(e2);
        }
    }

    @Override
    public String toString() {
        String telStr = (numerTelefonu != null) ? String.valueOf(numerTelefonu.getNationalNumber()) : "brak";
        String emailStr = (email != null) ? email.getAddress() : "brak";
        return String.format("%-15s %-15s | Tel: %-12s | Email: %s", nazwisko, imie, telStr, emailStr);
    }
}