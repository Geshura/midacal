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
        createTablesIfNotExist();
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
            
            System.out.println("[DB] Wczytano: " + memory.kontakty.size() + " kontaktów, " + 
                             memory.zdarzenia.size() + " zdarzeń.");
            
            disconnect();
            System.out.println("[DB] Połączenie zamknięte.");
            
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Błąd wczytywania: " + e.getMessage());
        }
        
        return memory;
    }
    
    /**
     * Wczytuje kontakty z bazy do mapy (id -> Kontakt)
     */
    private Map<Long, Kontakt> loadKontakty() throws SQLException {
        Map<Long, Kontakt> map = new HashMap<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM kontakty");
        
        while (rs.next()) {
            Kontakt k = new Kontakt();
            k.setId(rs.getLong("id"));
            k.setImie(rs.getString("imie"));
            k.setNazwisko(rs.getString("nazwisko"));
            k.setTelStr(rs.getString("telefon"));
            k.setEmailStr(rs.getString("email"));
            
            map.put(k.getId(), k);
        }
        
        rs.close();
        stmt.close();
        return map;
    }
    
    /**
     * Wczytuje zdarzenia z bazy do mapy (id -> Zdarzenie)
     */
    private Map<Long, Zdarzenie> loadZdarzenia() throws SQLException {
        Map<Long, Zdarzenie> map = new HashMap<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM zdarzenia");
        
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
        }
        
        rs.close();
        stmt.close();
        return map;
    }
    
    /**
     * Wczytuje relacje z tabeli asocjacyjnej i uzupełnia listy w obiektach
     */
    private void loadRelacje(Map<Long, Kontakt> kontakty, Map<Long, Zdarzenie> zdarzenia) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM kontakty_zdarzenia");
        
        while (rs.next()) {
            Long kontaktId = rs.getLong("kontakt_id");
            Long zdarzenieId = rs.getLong("zdarzenie_id");
            
            Kontakt k = kontakty.get(kontaktId);
            Zdarzenie z = zdarzenia.get(zdarzenieId);
            
            if (k != null && z != null) {
                k.dodajZdarzenie(z);
                z.dodajKontakt(k);
            }
        }
        
        rs.close();
        stmt.close();
    }
    
    // === ZAPISYWANIE DANYCH DO BAZY ===
    
    /**
     * Zapisuje wszystkie dane z pamięci RAM do bazy
     * Otwiera połączenie -> czyści tabele -> zapisuje -> zamyka połączenie
     */
    public void saveToDatabase(Main.MemoryContainer memory) {
        try {
            System.out.println("[DB] Otwieranie połączenia z bazą danych...");
            connect();
            
            // Wyczyść istniejące dane
            clearAllData();
            
            // Zapisz kontakty
            Map<Kontakt, Long> kontaktyIdMap = saveKontakty(memory.kontakty);
            
            // Zapisz zdarzenia
            Map<Zdarzenie, Long> zdarzeniaIdMap = saveZdarzenia(memory.zdarzenia);
            
            // Zapisz relacje
            saveRelacje(memory.kontakty, memory.zdarzenia, kontaktyIdMap, zdarzeniaIdMap);
            
            System.out.println("[DB] Zapisano: " + memory.kontakty.size() + " kontaktów, " + 
                             memory.zdarzenia.size() + " zdarzeń.");
            
            disconnect();
            System.out.println("[DB] Połączenie zamknięte.");
            
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Błąd zapisywania: " + e.getMessage());
        }
    }
    
    /**
     * Czyści wszystkie dane z tabel (przed pełnym zapisem)
     */
    private void clearAllData() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM kontakty_zdarzenia");
        stmt.execute("DELETE FROM kontakty");
        stmt.execute("DELETE FROM zdarzenia");
        stmt.close();
    }
    
    /**
     * Zapisuje kontakty do bazy - UPDATE jeśli istnieje ID, INSERT jeśli nowy
     */
    private Map<Kontakt, Long> saveKontakty(List<Kontakt> kontakty) throws SQLException {
        Map<Kontakt, Long> idMap = new HashMap<>();
        
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
                pstmt.executeUpdate();
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
                }
                rs.close();
                pstmt.close();
            }
        }
        
        return idMap;
    }
    
    /**
     * Zapisuje zdarzenia do bazy - UPDATE jeśli istnieje ID, INSERT jeśli nowe
     */
    private Map<Zdarzenie, Long> saveZdarzenia(List<Zdarzenie> zdarzenia) throws SQLException {
        Map<Zdarzenie, Long> idMap = new HashMap<>();
        
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
                pstmt.executeUpdate();
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
                }
                rs.close();
                pstmt.close();
            }
        }
        
        return idMap;
    }
    
    /**
     * Zapisuje relacje do tabeli asocjacyjnej - najpierw czyści stare, potem dodaje nowe
     */
    private void saveRelacje(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia,
                            Map<Kontakt, Long> kontaktyIdMap, Map<Zdarzenie, Long> zdarzeniaIdMap) throws SQLException {
        // Najpierw wyczyść stare relacje (ale NIE całą tabelę, bo mogą być inne rekordy)
        // Usuwamy tylko relacje dla zdarzeń które mamy w pamięci
        Statement stmt = connection.createStatement();
        StringBuilder deleteIds = new StringBuilder();
        for (Zdarzenie z : zdarzenia) {
            Long zId = zdarzeniaIdMap.get(z);
            if (zId != null) {
                if (deleteIds.length() > 0) deleteIds.append(",");
                deleteIds.append(zId);
            }
        }
        if (deleteIds.length() > 0) {
            stmt.execute("DELETE FROM kontakty_zdarzenia WHERE zdarzenie_id IN (" + deleteIds + ")");
        }
        stmt.close();
        
        // Teraz dodaj nowe relacje
        String sql = "INSERT INTO kontakty_zdarzenia (kontakt_id, zdarzenie_id) VALUES (?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        
        // Przechodząc przez kontakty, zapisz ich relacje do zdarzeń
        for (Kontakt k : kontakty) {
            Long kontaktId = kontaktyIdMap.get(k);
            if (kontaktId == null) continue;
            
            for (Zdarzenie z : k.getZdarzenia()) {
                Long zdarzenieId = zdarzeniaIdMap.get(z);
                if (zdarzenieId != null) {
                    pstmt.setLong(1, kontaktId);
                    pstmt.setLong(2, zdarzenieId);
                    pstmt.executeUpdate();
                }
            }
        }
        
        pstmt.close();
    }
}