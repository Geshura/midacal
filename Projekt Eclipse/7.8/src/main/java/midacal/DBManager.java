package midacal;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DBManager - zarządza połączeniem z bazą danych SQLite
 * Implementuje ORM (Object-Relational Mapping) dla Kontaktów i Zdarzeń
 * Relacja wiele-do-wielu poprzez tabelę asocjacyjną kontakty_zdarzenia
 */
public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:midacalDB.db";
    private Connection connection;
    
    // === KONSTRUKTOR I POŁĄCZENIE ===
    
    public DBManager() {
        // Połączenie zostanie otwarte tylko przy load() i save()
    }
    
    /**
     * Otwiera połączenie z bazą danych i tworzy tabele jeśli nie istnieją
     */
    private void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(false); // Wyłącz autocommit dla transakcji
        createTablesIfNotExist();
        connection.commit(); // Commit po utworzeniu tabel
    }
    
    /**
     * Zamyka połączenie z bazą danych
     */
    private void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    // === TWORZENIE TABEL ===
    
    /**
     * Tworzy strukturę tabel w bazie danych jeśli nie istnieją
     * Tabele: kontakty, zdarzenia, kontakty_zdarzenia (asocjacyjna)
     */
    private void createTablesIfNotExist() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Tabela Kontakty
        String createKontakty = """
            CREATE TABLE IF NOT EXISTS kontakty (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                imie TEXT NOT NULL,
                nazwisko TEXT NOT NULL,
                telefon TEXT,
                email TEXT
            )
        """;
        
        // Tabela Zdarzenia
        String createZdarzenia = """
            CREATE TABLE IF NOT EXISTS zdarzenia (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tytul TEXT NOT NULL,
                opis TEXT,
                data TEXT NOT NULL,
                link TEXT
            )
        """;
        
        // Tabela asocjacyjna - relacja wiele-do-wielu
        String createAsocjacyjna = """
            CREATE TABLE IF NOT EXISTS kontakty_zdarzenia (
                kontakt_id INTEGER NOT NULL,
                zdarzenie_id INTEGER NOT NULL,
                PRIMARY KEY (kontakt_id, zdarzenie_id),
                FOREIGN KEY (kontakt_id) REFERENCES kontakty(id) ON DELETE CASCADE,
                FOREIGN KEY (zdarzenie_id) REFERENCES zdarzenia(id) ON DELETE CASCADE
            )
        """;
        
        stmt.execute(createKontakty);
        stmt.execute(createZdarzenia);
        stmt.execute(createAsocjacyjna);
        stmt.close();
        
        System.out.println("[DB] Tabele zweryfikowane/utworzone.");
    }
    
    // === WCZYTYWANIE DANYCH Z BAZY ===
    
    /**
     * Wczytuje wszystkie dane z bazy do pamięci RAM
     * Otwiera połączenie -> pobiera dane -> zamyka połączenie
     */
    public Main.MemoryContainer loadFromDatabase() {
        Main.MemoryContainer memory = new Main.MemoryContainer();
        
        try {
            System.out.println("[DB] Otwieranie połączenia z bazą danych...");
            connect();
            
            // Najpierw wczytaj kontakty
            Map<Long, Kontakt> kontaktyMap = loadKontakty();
            memory.kontakty = new ArrayList<>(kontaktyMap.values());
            
            // Potem wczytaj zdarzenia
            Map<Long, Zdarzenie> zdarzeniaMap = loadZdarzenia();
            memory.zdarzenia = new ArrayList<>(zdarzeniaMap.values());
            
            // Na końcu wczytaj relacje (kto w jakim zdarzeniu uczestniczy)
            loadRelacje(kontaktyMap, zdarzeniaMap);
            
            // Commit po odczytaniu (dla pewności, choć SELECT nie wymaga)
            connection.commit();
            
            System.out.println("[DB] Wczytano: " + memory.kontakty.size() + " kontaktów, " + 
                             memory.zdarzenia.size() + " zdarzeń.");
            
            disconnect();
            System.out.println("[DB] Połączenie zamknięte.");
            
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Błąd wczytywania: " + e.getMessage());
            e.printStackTrace();
        }
        
        return memory;
    }
    
    /**
     * Wczytuje kontakty z bazy do mapy (id -> Kontakt)
     */
    private Map<Long, Kontakt> loadKontakty() throws SQLException {
        Map<Long, Kontakt> map = new HashMap<>();
        System.out.println("[DB] Wczytywanie kontaktów...");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM kontakty");
        
        int count = 0;
        while (rs.next()) {
            Kontakt k = new Kontakt();
            k.setId(rs.getLong("id"));
            k.setImie(rs.getString("imie"));
            k.setNazwisko(rs.getString("nazwisko"));
            k.setTelStr(rs.getString("telefon"));
            k.setEmailStr(rs.getString("email"));
            
            map.put(k.getId(), k);
            count++;
            System.out.println("[DB]   ID=" + k.getId() + ": " + k.getNazwisko() + " " + k.getImie());
        }
        
        System.out.println("[DB] Wczytano " + count + " kontaktów.");
        rs.close();
        stmt.close();
        return map;
    }
    
    /**
     * Wczytuje zdarzenia z bazy do mapy (id -> Zdarzenie)
     */
    private Map<Long, Zdarzenie> loadZdarzenia() throws SQLException {
        Map<Long, Zdarzenie> map = new HashMap<>();
        System.out.println("[DB] Wczytywanie zdarzeń...");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM zdarzenia");
        
        int count = 0;
        while (rs.next()) {
            Zdarzenie z = new Zdarzenie();
            z.setId(rs.getLong("id"));
            z.setTytul(rs.getString("tytul"));
            z.setOpis(rs.getString("opis"));
            z.setData(LocalDate.parse(rs.getString("data")));
            
            String link = rs.getString("link");
            if (link != null && !link.isEmpty()) {
                try {
                    z.setMiejsce(URI.create(link).toURL());
                } catch (Exception e) {
                    System.err.println("[DB WARN] Nieprawidłowy URL: " + link);
                }
            }
            
            map.put(z.getId(), z);
            count++;
            System.out.println("[DB]   ID=" + z.getId() + ": " + z.getTytul() + " (" + z.getData() + ")");
        }
        
        System.out.println("[DB] Wczytano " + count + " zdarzeń.");
        rs.close();
        stmt.close();
        return map;
    }
    
    /**
     * Wczytuje relacje z tabeli asocjacyjnej i uzupełnia listy w obiektach
     */
    private void loadRelacje(Map<Long, Kontakt> kontakty, Map<Long, Zdarzenie> zdarzenia) throws SQLException {
        System.out.println("[DB] Wczytywanie relacji kontakty_zdarzenia...");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM kontakty_zdarzenia");
        
        int count = 0;
        while (rs.next()) {
            Long kontaktId = rs.getLong("kontakt_id");
            Long zdarzenieId = rs.getLong("zdarzenie_id");
            
            Kontakt k = kontakty.get(kontaktId);
            Zdarzenie z = zdarzenia.get(zdarzenieId);
            
            if (k != null && z != null) {
                k.dodajZdarzenie(z);
                z.dodajKontakt(k);
                count++;
                System.out.println("[DB]   Relacja: kontakt_id=" + kontaktId + " <-> zdarzenie_id=" + zdarzenieId);
            } else {
                System.err.println("[DB WARN] Nie znaleziono kontaktu=" + kontaktId + " lub zdarzenia=" + zdarzenieId);
            }
        }
        
        System.out.println("[DB] Wczytano " + count + " relacji.");
        rs.close();
        stmt.close();
    }
    
    // === ZAPISYWANIE DANYCH DO BAZY ===
    
    /**
     * Zapisuje wszystkie dane z pamięci RAM do bazy
     * Otwiera połączenie -> UPDATE istniejące -> INSERT nowe -> zamyka połączenie
     */
    public void saveToDatabase(Main.MemoryContainer memory) {
        try {
            System.out.println("[DB] Otwieranie połączenia z bazą danych...");
            connect();
            
            try {
                // Wyczyść tylko relacje (nie kontakty/zdarzenia - będą UPDATE'owane)
                clearRelations();
                
                // Zapisz kontakty
                Map<Kontakt, Long> kontaktyIdMap = saveKontakty(memory.kontakty);
                
                // Zapisz zdarzenia
                Map<Zdarzenie, Long> zdarzeniaIdMap = saveZdarzenia(memory.zdarzenia);
                
                // Zapisz relacje
                saveRelacje(memory.kontakty, memory.zdarzenia, kontaktyIdMap, zdarzeniaIdMap);
                
                // COMMIT - zapisz zmiany do pliku bazy danych
                connection.commit();
                
                System.out.println("[DB] Zapisano: " + memory.kontakty.size() + " kontaktów, " + 
                                 memory.zdarzenia.size() + " zdarzeń.");
                
            } catch (SQLException e) {
                System.err.println("[DB ERROR] Błąd podczas zapisywania: " + e.getMessage());
                connection.rollback(); // Wycofaj wszystkie zmiany w transakcji
                throw e; // Przerzuć wyjątek wyżej
            }
            
            disconnect();
            System.out.println("[DB] Połączenie zamknięte.");
            
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Błąd zapisu do bazy danych: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Czyści tylko relacje (przed zapisem nowych)
     * Kontakty i zdarzenia będą UPDATE'owane/INSERT'owane
     */
    private void clearRelations() throws SQLException {
        System.out.println("[DB] Czyszczenie relacji kontakty_zdarzenia...");
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM kontakty_zdarzenia");
        System.out.println("[DB]   Usunięto relacje kontakty_zdarzenia");
        stmt.close();
    }
    
    /**
     * Zapisuje kontakty do bazy - UPDATE jeśli istnieje ID, INSERT jeśli nowy
     */
    private Map<Kontakt, Long> saveKontakty(List<Kontakt> kontakty) throws SQLException {
        Map<Kontakt, Long> idMap = new HashMap<>();
        System.out.println("[DB] Zapisywanie " + kontakty.size() + " kontaktów...");
        
        // Debugowanie - sprawdz czy obiekty mają ID
        for (Kontakt k : kontakty) {
            System.out.println("[DB DEBUG] Kontakt " + k.getNazwisko() + ": ID=" + k.getId());
        }
        
        for (Kontakt k : kontakty) {
            if (k.getId() != null && k.getId() > 0) {
                // UPDATE dla istniejącego kontaktu
                String sql = "UPDATE kontakty SET imie=?, nazwisko=?, telefon=?, email=? WHERE id=?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, k.getImie());
                pstmt.setString(2, k.getNazwisko());
                pstmt.setString(3, k.getTelStr());
                pstmt.setString(4, k.getEmailStr());
                pstmt.setLong(5, k.getId());
                int updated = pstmt.executeUpdate();
                System.out.println("[DB]   UPDATE ID=" + k.getId() + ": " + k.getNazwisko() + " (" + updated + " wiersz)");
                pstmt.close();
                idMap.put(k, k.getId());
            } else {
                // INSERT dla nowego kontaktu
                String sql = "INSERT INTO kontakty (imie, nazwisko, telefon, email) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, k.getImie());
                pstmt.setString(2, k.getNazwisko());
                pstmt.setString(3, k.getTelStr());
                pstmt.setString(4, k.getEmailStr());
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    Long newId = rs.getLong(1);
                    k.setId(newId);
                    idMap.put(k, newId);
                    System.out.println("[DB]   INSERT: " + k.getNazwisko() + " -> ID=" + newId);
                } else {
                    System.err.println("[DB ERROR] Nie udało się pobrać ID dla kontaktu: " + k.getNazwisko());
                }
                rs.close();
                pstmt.close();
            }
        }
        System.out.println("[DB] Zapisano " + idMap.size() + " kontaktów.");
        return idMap;
    }
    
    /**
     * Zapisuje zdarzenia do bazy - UPDATE jeśli istnieje ID, INSERT jeśli nowe
     */
    private Map<Zdarzenie, Long> saveZdarzenia(List<Zdarzenie> zdarzenia) throws SQLException {
        Map<Zdarzenie, Long> idMap = new HashMap<>();
        System.out.println("[DB] Zapisywanie " + zdarzenia.size() + " zdarzeń...");
        
        // Debugowanie - sprawdz czy obiekty mają ID
        for (Zdarzenie z : zdarzenia) {
            System.out.println("[DB DEBUG] Zdarzenie " + z.getTytul() + ": ID=" + z.getId());
        }
        
        for (Zdarzenie z : zdarzenia) {
            if (z.getId() != null && z.getId() > 0) {
                // UPDATE dla istniejącego zdarzenia
                String sql = "UPDATE zdarzenia SET tytul=?, opis=?, data=?, link=? WHERE id=?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, z.getTytul());
                pstmt.setString(2, z.getOpis());
                pstmt.setString(3, z.getData().toString());
                pstmt.setString(4, z.getMiejsce() != null ? z.getMiejsce().toString() : null);
                pstmt.setLong(5, z.getId());
                int updated = pstmt.executeUpdate();
                System.out.println("[DB]   UPDATE ID=" + z.getId() + ": " + z.getTytul() + " (" + updated + " wiersz)");
                pstmt.close();
                idMap.put(z, z.getId());
            } else {
                // INSERT dla nowego zdarzenia
                String sql = "INSERT INTO zdarzenia (tytul, opis, data, link) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, z.getTytul());
                pstmt.setString(2, z.getOpis());
                pstmt.setString(3, z.getData().toString());
                pstmt.setString(4, z.getMiejsce() != null ? z.getMiejsce().toString() : null);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    Long newId = rs.getLong(1);
                    z.setId(newId);
                    idMap.put(z, newId);
                    System.out.println("[DB]   INSERT: " + z.getTytul() + " (" + z.getData() + ") -> ID=" + newId);
                } else {
                    System.err.println("[DB ERROR] Nie udało się pobrać ID dla zdarzenia: " + z.getTytul());
                }
                rs.close();
                pstmt.close();
            }
        }
        System.out.println("[DB] Zapisano " + idMap.size() + " zdarzeń.");
        return idMap;
    }
    
    /**
     * Zapisuje relacje do tabeli asocjacyjnej - zapisuje wszystkie relacje z pamięci
     * Tabela kontakty_zdarzenia została już wyczyszczona w clearAllData()
     */
    private void saveRelacje(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia,
                            Map<Kontakt, Long> kontaktyIdMap, Map<Zdarzenie, Long> zdarzeniaIdMap) throws SQLException {
        String sql = "INSERT INTO kontakty_zdarzenia (kontakt_id, zdarzenie_id) VALUES (?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        
        int relacjeCount = 0;
        System.out.println("[DB] Zapisywanie relacji kontakty_zdarzenia...");
        
        // Przechodź przez zdarzenia i zapisz wszystkich uczestników
        for (Zdarzenie z : zdarzenia) {
            Long zdarzenieId = zdarzeniaIdMap.get(z);
            if (zdarzenieId == null || z.getKontakty() == null) continue;
            
            for (Kontakt k : z.getKontakty()) {
                Long kontaktId = kontaktyIdMap.get(k);
                if (kontaktId != null) {
                    try {
                        pstmt.setLong(1, kontaktId);
                        pstmt.setLong(2, zdarzenieId);
                        pstmt.executeUpdate();
                        relacjeCount++;
                        System.out.println("[DB]   Relacja: kontakt_id=" + kontaktId + " <-> zdarzenie_id=" + zdarzenieId);
                    } catch (SQLException e) {
                        // Ignoruj duplikaty (jeśli relacja już istnieje)
                        if (!e.getMessage().contains("UNIQUE constraint")) {
                            throw e;
                        }
                    }
                }
            }
        }
        
        System.out.println("[DB] Zapisano " + relacjeCount + " relacji kontakty_zdarzenia.");
        pstmt.close();
    }
}