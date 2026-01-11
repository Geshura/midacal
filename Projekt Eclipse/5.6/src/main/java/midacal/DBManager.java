package midacal;

import java.sql.*;
import java.util.List;
import java.net.URI;
import java.time.LocalDate;

public class DBManager {
    private static final String URL = "jdbc:sqlite:midacalXML.db";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Kontakty (id INTEGER PRIMARY KEY AUTOINCREMENT, imie TEXT, nazwisko TEXT, telefon TEXT, email TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Zdarzenia (id INTEGER PRIMARY KEY AUTOINCREMENT, tytul TEXT, opis TEXT, data TEXT, link TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Uczestnicy (kontakt_id INTEGER, zdarzenie_id INTEGER, PRIMARY KEY(kontakt_id, zdarzenie_id))");
            System.out.println("[OK] Baza danych gotowa.");
        } catch (SQLException e) {
            System.out.println("[X] Błąd inicjalizacji SQL: " + e.getMessage());
        }
    }

    public static void saveToDatabase(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia) {
        System.out.println("[...] Otwieranie połączenia z bazą celem zapisu...");
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            stmt.executeUpdate("DELETE FROM Uczestnicy");
            stmt.executeUpdate("DELETE FROM Zdarzenia");
            stmt.executeUpdate("DELETE FROM Kontakty");

            for (Kontakt k : kontakty) {
                String sql = String.format("INSERT INTO Kontakty (imie, nazwisko, telefon, email) VALUES ('%s', '%s', '%s', '%s')",
                        k.getImie(), k.getNazwisko(), k.getTelStr(), k.getEmailStr());
                stmt.executeUpdate(sql);
            }
            for (Zdarzenie z : zdarzenia) {
                String sql = String.format("INSERT INTO Zdarzenia (tytul, opis, data, link) VALUES ('%s', '%s', '%s', '%s')",
                        z.getTytul(), z.getOpis(), z.getData().toString(), z.getMiejsce().toString());
                stmt.executeUpdate(sql);
            }
            conn.commit();
            System.out.println("[OK] Pamięć RAM została zsynchronizowana z bazą danych. Połączenie zamknięte.");
        } catch (SQLException e) {
            System.out.println("[X] Błąd zapisu SQL: " + e.getMessage());
        }
    }

    public static void loadFromDatabase(Main.MemoryContainer memory) {
        System.out.println("[...] Otwieranie połączenia z bazą celem pobrania danych...");
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
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
            System.out.println("[OK] Dane pobrane do RAM. Połączenie zamknięte.");
        } catch (Exception e) {
            System.out.println("[X] Błąd pobierania danych: " + e.getMessage());
        }
    }
}