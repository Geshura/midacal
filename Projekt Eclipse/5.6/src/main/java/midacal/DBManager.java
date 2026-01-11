package midacal;

import java.sql.*;
import java.util.List;

public class DBManager {
    private static final String URL = "jdbc:sqlite:midacalXML.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Tabela Kontakty
            stmt.execute("CREATE TABLE IF NOT EXISTS Kontakty (id INTEGER PRIMARY KEY AUTOINCREMENT, imie TEXT, nazwisko TEXT, telefon TEXT, email TEXT)");
            
            // Tabela Zdarzenia
            stmt.execute("CREATE TABLE IF NOT EXISTS Zdarzenia (id INTEGER PRIMARY KEY AUTOINCREMENT, tytul TEXT, opis TEXT, data TEXT, link TEXT)");
            
            // NOWA TABELA ŁĄCZĄCA (Relacja M:N)
            stmt.execute("CREATE TABLE IF NOT EXISTS Uczestnicy (" +
                         "kontakt_id INTEGER, zdarzenie_id INTEGER, " +
                         "FOREIGN KEY(kontakt_id) REFERENCES Kontakty(id), " +
                         "FOREIGN KEY(zdarzenie_id) REFERENCES Zdarzenia(id), " +
                         "PRIMARY KEY(kontakt_id, zdarzenie_id))");
            
            System.out.println("[OK] Struktura bazy SQL z relacjami M:N została zainicjalizowana.");
        } catch (SQLException e) {
            System.out.println("[X] Błąd SQL: " + e.getMessage());
        }
    }

    public static void saveAll(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia) {
        if (kontakty.isEmpty() && zdarzenia.isEmpty()) return;
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
            System.out.println("[OK] Synchronizacja SQL zakończona.");
        } catch (SQLException e) {
            System.out.println("[X] Błąd zapisu: " + e.getMessage());
        }
    }
    
    // Metoda loadInto pozostaje bez zmian (wczytywanie podstawowe)
}