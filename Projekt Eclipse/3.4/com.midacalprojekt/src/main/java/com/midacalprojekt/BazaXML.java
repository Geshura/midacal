package com.midacalprojekt;

import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import jakarta.mail.internet.InternetAddress;

public class BazaXML {

    public void zapiszWszystko(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia, String nazwaPliku) {
        try (XMLEncoder koder = new XMLEncoder(
                new BufferedOutputStream(
                        new FileOutputStream(nazwaPliku)))) {

            // Konfiguracja PersistenceDelegate dla komponentów bez pustego konstruktora
            
            // 1. InternetAddress tworzymy przez new InternetAddress(address)
            koder.setPersistenceDelegate(InternetAddress.class,
                    new DefaultPersistenceDelegate(new String[]{"address"}));

            // 2. URL tworzymy przez new URL(protocol, host, port, file)
            koder.setPersistenceDelegate(URL.class,
                    new DefaultPersistenceDelegate(new String[]{"protocol", "host", "port", "file"}));

            koder.writeObject(kontakty);
            koder.writeObject(zdarzenia);
            
            System.out.println("-> Zapisano dane do XML.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Object> wczytajWszystko(String nazwaPliku) {
        List<Object> wyniki = new ArrayList<>();
        try (XMLDecoder dekoder = new XMLDecoder(
                new BufferedInputStream(
                        new FileInputStream(nazwaPliku)))) {

            wyniki.add(dekoder.readObject()); // Kontakty
            wyniki.add(dekoder.readObject()); // Zdarzenia
            
            System.out.println("-> Wczytano dane z XML.");

        } catch (FileNotFoundException e) {
            System.out.println("-> Brak pliku bazy (pierwsze uruchomienie).");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wyniki;
    }
}