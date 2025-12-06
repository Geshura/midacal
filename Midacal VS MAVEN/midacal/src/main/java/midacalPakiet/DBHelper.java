package midacalPakiet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.NumberParseException;

public class DBHelper {
    private static final String DB_URL = "jdbc:sqlite:midacal.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS kontakty ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "imie TEXT,"
                + "nazwisko TEXT,"
                + "telefon TEXT,"
                + "email TEXT"
                + ")";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Baza danych zainicjalizowana (" + DB_URL + ")");
            // utworzenie tabeli zdarzenia
                String sql2 = "CREATE TABLE IF NOT EXISTS zdarzenia ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nazwa TEXT,"
                    + "data TEXT,"
                    + "lokalizacja TEXT,"
                    + "opis TEXT"
                    + ")";
            stmt.execute(sql2);
            System.out.println("Tabela zdarzenia utworzona (jeśli nie istniala)");
            // Sprawdzamy, czy kolumna 'lokalizacja' istnieje w tabeli zdarzenia; jeśli nie, dodajemy ją.
            try (ResultSet cols = stmt.executeQuery("PRAGMA table_info('zdarzenia')")) {
                boolean hasLokalizacja = false;
                while (cols.next()) {
                    String colName = cols.getString("name");
                    if ("lokalizacja".equalsIgnoreCase(colName)) {
                        hasLokalizacja = true;
                        break;
                    }
                }
                if (!hasLokalizacja) {
                    System.out.println("Kolumna 'lokalizacja' nie istnieje - dodaje kolumne...");
                    stmt.execute("ALTER TABLE zdarzenia ADD COLUMN lokalizacja TEXT");
                    System.out.println("Kolumna 'lokalizacja' dodana do tabeli zdarzenia");
                }
            } catch (SQLException e) {
                System.err.println("Blad sprawdzania kolumn tabeli zdarzenia: " + e.getMessage());
            }
             // utworzenie tabeli łącznej dla relacji wiele-do-wielu
            String sql3 = "CREATE TABLE IF NOT EXISTS kontakt_zdarzenie ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "kontakt_id INTEGER NOT NULL,"
                    + "zdarzenie_id INTEGER NOT NULL,"
                    + "FOREIGN KEY(kontakt_id) REFERENCES kontakty(id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(zdarzenie_id) REFERENCES zdarzenia(id) ON DELETE CASCADE,"
                    + "UNIQUE(kontakt_id, zdarzenie_id)"
                    + ")";
            stmt.execute(sql3);
            System.out.println("Tabela kontakt_zdarzenie utworzona (jeśli nie istniala)");

        } catch (SQLException e) {
            System.err.println("Blad inicjalizacji bazy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertKontakt(String imie, String nazwisko, String telefon, String email) {
        String sql = "INSERT INTO kontakty(imie,nazwisko,telefon,email) VALUES(?,?,?,?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, imie);
            pstmt.setString(2, nazwisko);
            pstmt.setString(3, telefon);
            pstmt.setString(4, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Blad podczas wstawiania kontaktu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertZdarzenie(String nazwa, String data, java.net.URI lokalizacja, String opis) {
        String sql = "INSERT INTO zdarzenia(nazwa,data,lokalizacja,opis) VALUES(?,?,?,?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nazwa);
            pstmt.setString(2, data);
            pstmt.setString(3, lokalizacja != null ? lokalizacja.toString() : null);
            pstmt.setString(4, opis);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Blad podczas wstawiania zdarzenia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int insertKontaktReturnId(String imie, String nazwisko, String telefon, String email) {
        String sql = "INSERT INTO kontakty(imie,nazwisko,telefon,email) VALUES(?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, imie);
            pstmt.setString(2, nazwisko);
            pstmt.setString(3, telefon);
            pstmt.setString(4, email);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas wstawiania kontaktu (z ID): " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static int insertZdarzenieReturnId(String nazwa, String data, java.net.URI lokalizacja, String opis) {
        String sql = "INSERT INTO zdarzenia(nazwa,data,lokalizacja,opis) VALUES(?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nazwa);
            pstmt.setString(2, data);
            pstmt.setString(3, lokalizacja != null ? lokalizacja.toString() : null);
            pstmt.setString(4, opis);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas wstawiania zdarzenia (z ID): " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static void syncAllToDB(List<Kontakt> kontakty, List<Zdarzenie> zdarzenia) {
        // Clear existing data
        clearZdarzeniaKontakty();
        clearZdarzenia();
        clearKontakty();

        // Insert kontakty and remember new IDs
        for (Kontakt k : kontakty) {
            String tel = null;
            if (k.getTelefon() != null) {
                tel = String.valueOf(k.getTelefon().getNationalNumber());
            }
            String email = null;
            if (k.getEmail() != null) {
                email = k.getEmail().getAddress();
            }
            int newId = insertKontaktReturnId(k.getImie(), k.getNazwisko(), tel, email);
            k.setId(newId);
        }

        // Insert zdarzenia and remember new IDs
        for (Zdarzenie z : zdarzenia) {
            String data = (z.getData() != null) ? z.getData().toString() : null;
            int newId = insertZdarzenieReturnId(z.getNazwa(), data, z.getLokalizacja(), z.getOpis());
            z.setId(newId);
        }

        // Recreate relations based on matching fields (email or name)
        for (Zdarzenie z : zdarzenia) {
            int zId = z.getId();
            for (Kontakt k : z.getKontakty()) {
                // find matching kontakt in provided kontakty list
                Integer kId = findKontaktId(k, kontakty);
                if (kId != null && zId > 0) {
                    addZdarzenieToKontakt(kId, zId);
                }
            }
        }

        System.out.println("Wszystkie dane zsynchronizowane z baza (kontakty, zdarzenia, relacje)");
    }

    private static Integer findKontaktId(Kontakt needle, List<Kontakt> haystack) {
        if (needle == null || haystack == null) return null;
        // Prefer matching by email if present
        String e = (needle.getEmail() != null) ? needle.getEmail().getAddress() : null;
        for (Kontakt k : haystack) {
            if (e != null && k.getEmail() != null && e.equals(k.getEmail().getAddress())) {
                return k.getId();
            }
        }
        // Fallback to matching by name
        for (Kontakt k : haystack) {
            if (k.getImie() != null && k.getNazwisko() != null && k.getImie().equals(needle.getImie()) && k.getNazwisko().equals(needle.getNazwisko())) {
                return k.getId();
            }
        }
        return null;
    }

    public static void printAllZdarzenia() {
        String sql = "SELECT id, nazwa, data, lokalizacja, opis FROM zdarzenia";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nZdarzenia w bazie:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nazwa = rs.getString("nazwa");
                String data = rs.getString("data");
                String lokalizacja = rs.getString("lokalizacja");
                String opis = rs.getString("opis");
                System.out.println("#" + id + " - " + nazwa + " | data=" + data + " | lokalizacja=" + lokalizacja + " | opis=" + opis);
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas odczytu zdarzen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void printAllKontakty() {
        String sql = "SELECT id, imie, nazwisko, telefon, email FROM kontakty";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nKontakty w bazie:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String imie = rs.getString("imie");
                String nazwisko = rs.getString("nazwisko");
                String telefon = rs.getString("telefon");
                String email = rs.getString("email");
                System.out.println("#" + id + " - " + imie + " " + nazwisko + " | tel=" + telefon + " | email=" + email);
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas odczytu kontaktow: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Kontakt> getAllKontakty() {
        List<Kontakt> kontakty = new ArrayList<>();
        String sql = "SELECT id, imie, nazwisko, telefon, email FROM kontakty";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            while (rs.next()) {
                int id = rs.getInt("id");
                String imie = rs.getString("imie");
                String nazwisko = rs.getString("nazwisko");
                String telefonStr = rs.getString("telefon");
                String emailStr = rs.getString("email");

                try {
                    com.google.i18n.phonenumbers.Phonenumber.PhoneNumber telefon = null;
                    if (telefonStr != null && !telefonStr.isEmpty()) {
                        telefon = phoneUtil.parse(telefonStr, "PL");
                    }
                    
                    InternetAddress email = null;
                    if (emailStr != null && !emailStr.isEmpty()) {
                        email = new InternetAddress(emailStr);
                    }

                    Kontakt k = new Kontakt(imie, nazwisko, telefon, email);
                    k.setId(id);
                    kontakty.add(k);
                } catch (AddressException | NumberParseException e) {
                    System.err.println("Blad podczas parsowania danych kontaktu: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas odczytu kontaktow z bazy: " + e.getMessage());
            e.printStackTrace();
        }
        return kontakty;
    }

    public static List<Zdarzenie> getAllZdarzenia() {
        List<Zdarzenie> zdarzenia = new ArrayList<>();
        String sql = "SELECT id, nazwa, data, lokalizacja, opis FROM zdarzenia";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String nazwa = rs.getString("nazwa");
                String dataStr = rs.getString("data");
                String lokalizacjaStr = rs.getString("lokalizacja");
                String opis = rs.getString("opis");

                try {
                    java.time.LocalDate data = null;
                    if (dataStr != null && !dataStr.isEmpty()) {
                        data = java.time.LocalDate.parse(dataStr);
                    }

                    Zdarzenie z = new Zdarzenie(nazwa, data, lokalizacjaStr, opis);
                    z.setId(id);
                    zdarzenia.add(z);
                } catch (Exception e) {
                    System.err.println("Blad podczas parsowania danych zdarzenia: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas odczytu zdarzen z bazy: " + e.getMessage());
            e.printStackTrace();
        }
        return zdarzenia;
    }

    public static void clearKontakty() {
        String sql = "DELETE FROM kontakty";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Kontakty usuniete z bazy");
        } catch (SQLException e) {
            System.err.println("Blad podczas czyszczenia tabeli kontakty: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void clearZdarzenia() {
        String sql = "DELETE FROM zdarzenia";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Zdarzenia usuniete z bazy");
        } catch (SQLException e) {
            System.err.println("Blad podczas czyszczenia tabeli zdarzenia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void syncKontaktyToDB(List<Kontakt> kontakty) {
        clearKontakty();
        for (Kontakt k : kontakty) {
            String tel = null;
            if (k.getTelefon() != null) {
                tel = String.valueOf(k.getTelefon().getNationalNumber());
            }
            String email = null;
            if (k.getEmail() != null) {
                email = k.getEmail().getAddress();
            }
            insertKontakt(k.getImie(), k.getNazwisko(), tel, email);
        }
        System.out.println("Kontakty zsynchronizowane z baza");
    }

    public static void syncZdarzeniaToDB(List<Zdarzenie> zdarzenia) {
        clearZdarzenia();
        for (Zdarzenie z : zdarzenia) {
            String data = (z.getData() != null) ? z.getData().toString() : null;
            insertZdarzenie(z.getNazwa(), data, z.getLokalizacja(), z.getOpis());
        }
        System.out.println("Kontakty zsynchronizowane z baza");
    }

    public static void clearZdarzeniaKontakty() {
        String sql = "DELETE FROM kontakt_zdarzenie";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Relacje kontakt-zdarzenie usuniete z bazy");
        } catch (SQLException e) {
            System.err.println("Blad podczas czyszczenia tabeli kontakt_zdarzenie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addZdarzenieToKontakt(int kontaktId, int zdarzenieId) {
        String sql = "INSERT OR IGNORE INTO kontakt_zdarzenie(kontakt_id, zdarzenie_id) VALUES(?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kontaktId);
            pstmt.setInt(2, zdarzenieId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Blad podczas dodawania relacji kontakt-zdarzenie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeZdarzenieFromKontakt(int kontaktId, int zdarzenieId) {
        String sql = "DELETE FROM kontakt_zdarzenie WHERE kontakt_id = ? AND zdarzenie_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kontaktId);
            pstmt.setInt(2, zdarzenieId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Blad podczas usuwania relacji kontakt-zdarzenie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Zdarzenie> getZdarzeniaForKontakt(int kontaktId) {
        List<Zdarzenie> zdarzenia = new ArrayList<>();
        String sql = "SELECT z.nazwa, z.data, z.lokalizacja, z.opis FROM zdarzenia z "
                   + "INNER JOIN kontakt_zdarzenie kz ON z.id = kz.zdarzenie_id "
                   + "WHERE kz.kontakt_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kontaktId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String nazwa = rs.getString("nazwa");
                String dataStr = rs.getString("data");
                String lokalizacjaStr = rs.getString("lokalizacja");
                String opis = rs.getString("opis");

                try {
                    java.time.LocalDate data = null;
                    if (dataStr != null && !dataStr.isEmpty()) {
                        data = java.time.LocalDate.parse(dataStr);
                    }
                    Zdarzenie z = new Zdarzenie(nazwa, data, lokalizacjaStr, opis);
                    zdarzenia.add(z);
                } catch (Exception e) {
                    System.err.println("Blad podczas parsowania zdarzenia dla kontaktu: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas odczytu zdarzen dla kontaktu: " + e.getMessage());
            e.printStackTrace();
        }
        return zdarzenia;
    }

    public static List<Kontakt> getKontaktyForZdarzenie(int zdarzenieId) {
        List<Kontakt> kontakty = new ArrayList<>();
        String sql = "SELECT k.imie, k.nazwisko, k.telefon, k.email FROM kontakty k "
                   + "INNER JOIN kontakt_zdarzenie kz ON k.id = kz.kontakt_id "
                   + "WHERE kz.zdarzenie_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, zdarzenieId);
            ResultSet rs = pstmt.executeQuery();

            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            while (rs.next()) {
                String imie = rs.getString("imie");
                String nazwisko = rs.getString("nazwisko");
                String telefonStr = rs.getString("telefon");
                String emailStr = rs.getString("email");

                try {
                    com.google.i18n.phonenumbers.Phonenumber.PhoneNumber telefon = null;
                    if (telefonStr != null && !telefonStr.isEmpty()) {
                        telefon = phoneUtil.parse(telefonStr, "PL");
                    }

                    InternetAddress email = null;
                    if (emailStr != null && !emailStr.isEmpty()) {
                        email = new InternetAddress(emailStr);
                    }

                    Kontakt k = new Kontakt(imie, nazwisko, telefon, email);
                    kontakty.add(k);
                } catch (AddressException | NumberParseException e) {
                    System.err.println("Blad podczas parsowania kontaktu dla zdarzenia: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Blad podczas odczytu kontaktow dla zdarzenia: " + e.getMessage());
            e.printStackTrace();
        }
        return kontakty;
    }
}