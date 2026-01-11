package midacal;

import java.sql.*;
import java.util.List;
import java.net.URI;
import java.time.LocalDate;

public class DBManager {
    private static final String URL = "jdbc:sqlite:midacalXML.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Kontakty (id INTEGER PRIMARY KEY AUTOINCREMENT, imie TEXT, nazwisko TEXT, telefon TEXT, email TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Zdarzenia (id INTEGER PRIMARY KEY AUTOINCREMENT, tytul TEXT, opis TEXT, data TEXT, link TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Uczestnicy (kontakt_id INTEGER, zdarzenie_id INTEGER, " +
                         "FOREIGN KEY(kontakt_id) REFERENCES Kontakty(id), " +
                         "FOREIGN KEY(zdarzenie_id) REFERENCES Zdarzenia(id), " +
                         "PRIMARY KEY(kontakt_id, zdarzenie_id))");
            System.out.println("[OK] Baza danych SQLite została zainicjalizowana.");
        } catch (SQLException e) {
            System.out.println("[X] Błąd SQL: " + e.getMessage());
        }
    }

    public static void saveAll(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia) {
        if (kontakty.isEmpty() && zdarzenia.isEmpty()) {
            System.out.println("[!] Brak danych w RAM - zapis SQL przerwany.");
            return;
        }
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Uczestnicy");
            stmt.executeUpdate("DELETE FROM Zdarzenia");
            stmt.executeUpdate("DELETE FROM Kontakty");

            for (Kontakt k : kontakty) {
                stmt.executeUpdate(String.format("INSERT INTO Kontakty (imie, nazwisko, telefon, email) VALUES ('%s', '%s', '%s', '%s')",
                        k.getImie(), k.getNazwisko(), k.getTelStr(), k.getEmailStr()));
            }
            for (Zdarzenie z : zdarzenia) {
                stmt.executeUpdate(String.format("INSERT INTO Zdarzenia (tytul, opis, data, link) VALUES ('%s', '%s', '%s', '%s')",
                        z.getTytul(), z.getOpis(), z.getData().toString(), z.getMiejsce().toString()));
            }
            System.out.println("[OK] Dane wyeksportowane do bazy SQL.");
        } catch (SQLException e) {
            System.out.println("[X] Błąd zapisu SQL: " + e.getMessage());
        }
    }

    public static void loadInto(Main.MemoryContainer memory) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            memory.kontakty.clear();
            memory.zdarzenia.clear();

            ResultSet rsK = stmt.executeQuery("SELECT * FROM Kontakty");
            while (rsK.next()) {
                Kontakt k = new Kontakt();
                k.setImie(rsK.getString("imie"));
                k.setNazwisko(rsK.getString("nazwisko"));
                k.setTelStr(rsK.getString("telefon"));
                k.setEmailStr(rsK.getString("email"));
                memory.kontakty.add(k);
            }

            ResultSet rsZ = stmt.executeQuery("SELECT * FROM Zdarzenia");
            while (rsZ.next()) {
                Zdarzenie z = new Zdarzenie(rsZ.getString("tytul"), rsZ.getString("opis"), 
                                           LocalDate.parse(rsZ.getString("data")), URI.create(rsZ.getString("link")).toURL());
                memory.zdarzenia.add(z);
            }
            System.out.println("[OK] Dane zaimportowane z bazy SQL.");
        } catch (Exception e) {
            System.out.println("[X] Błąd odczytu SQL: " + e.getMessage());
        }
    }
}