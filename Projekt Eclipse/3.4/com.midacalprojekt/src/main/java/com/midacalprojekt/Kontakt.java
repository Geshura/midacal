package com.midacalprojekt;

import java.io.Serializable;
import java.util.Comparator;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.mail.internet.InternetAddress;

public class Kontakt implements Serializable, Comparable<Kontakt> {
    private static final long serialVersionUID = 1L;

    private String imie;
    private String nazwisko;
    private PhoneNumber phoneNumber; // Komponent zewnętrzny
    private InternetAddress email;   // Komponent zewnętrzny

    public Kontakt() {}

    public Kontakt(String imie, String nazwisko, PhoneNumber phoneNumber, InternetAddress email) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // --- Comparable: Domyślne sortowanie po Nazwisku ---
    @Override
    public int compareTo(Kontakt inny) {
        int wynik = this.nazwisko.compareToIgnoreCase(inny.nazwisko);
        if (wynik == 0) {
            return this.imie.compareToIgnoreCase(inny.imie);
        }
        return wynik;
    }

    // --- Comparator: Alternatywne sortowanie po Emailu ---
    public static class KomparatorEmail implements Comparator<Kontakt> {
        @Override
        public int compare(Kontakt k1, Kontakt k2) {
            String mail1 = (k1.getEmail() != null) ? k1.getEmail().getAddress() : "";
            String mail2 = (k2.getEmail() != null) ? k2.getEmail().getAddress() : "";
            return mail1.compareToIgnoreCase(mail2);
        }
    }

    // Gettery i Settery
    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(PhoneNumber phoneNumber) { this.phoneNumber = phoneNumber; }

    public InternetAddress getEmail() { return email; }
    public void setEmail(InternetAddress email) { this.email = email; }

    @Override
    public String toString() {
        String tel = (phoneNumber != null) ? String.valueOf(phoneNumber.getNationalNumber()) : "Brak";
        String mail = (email != null) ? email.getAddress() : "Brak";
        return String.format("%-15s %-15s | Tel: %-12s | Email: %s", nazwisko, imie, tel, mail);
    }
}