package sk.medicore.db;

import sk.medicore.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:medicore.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void init() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pracoviska (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nazov TEXT NOT NULL,
                    budova TEXT,
                    poschodie TEXT,
                    miestnost TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pouzivatelia (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    meno TEXT NOT NULL,
                    priezvisko TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    heslo_hash TEXT NOT NULL,
                    typ TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lekari (
                    id INTEGER PRIMARY KEY,
                    specializacia TEXT NOT NULL,
                    pracovisko_id INTEGER REFERENCES pracoviska(id),
                    FOREIGN KEY(id) REFERENCES pouzivatelia(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS procedury (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nazov TEXT NOT NULL,
                    trvanie_min INTEGER NOT NULL,
                    popis TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lekar_procedury (
                    lekar_id INTEGER REFERENCES lekari(id),
                    procedura_id INTEGER REFERENCES procedury(id),
                    PRIMARY KEY (lekar_id, procedura_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS terminy (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    lekar_id INTEGER NOT NULL REFERENCES lekari(id),
                    datum_cas DATETIME NOT NULL,
                    trvanie_min INTEGER NOT NULL,
                    stav TEXT NOT NULL DEFAULT 'DOSTUPNY'
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rezervacie (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pacient_id INTEGER NOT NULL REFERENCES pouzivatelia(id),
                    lekar_id INTEGER NOT NULL REFERENCES lekari(id),
                    termin_id INTEGER NOT NULL REFERENCES terminy(id),
                    procedura_id INTEGER NOT NULL REFERENCES procedury(id),
                    stav TEXT NOT NULL DEFAULT 'POTVRDENA',
                    vytvorena_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            seedData(stmt);
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException("Chyba pri inicializácii databázy", e);
        }
    }

    private static void seedData(Statement stmt) throws SQLException {
        // Check if data already exists
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM pouzivatelia");
        rs.next();
        if (rs.getInt(1) > 0) {
            rs.close();
            return;
        }
        rs.close();

        String hesloHash = PasswordUtil.hash("heslo123");

        // Pracoviska
        stmt.execute("INSERT INTO pracoviska (id, nazov, budova, poschodie, miestnost) VALUES (1, 'Všeobecná ambulancia', 'Budova A', '3', '301')");
        stmt.execute("INSERT INTO pracoviska (id, nazov, budova, poschodie, miestnost) VALUES (2, 'Kardiologická ambulancia', 'Budova B', '1', '105')");
        stmt.execute("INSERT INTO pracoviska (id, nazov, budova, poschodie, miestnost) VALUES (3, 'Fyzioterapia', 'Budova A', '2', '210')");

        // Pacient
        stmt.execute("INSERT INTO pouzivatelia (id, meno, priezvisko, email, heslo_hash, typ) VALUES (1, 'Samuel', 'Thompson', 'pacient@medicore.sk', '" + hesloHash + "', 'PACIENT')");

        // Lekári
        stmt.execute("INSERT INTO pouzivatelia (id, meno, priezvisko, email, heslo_hash, typ) VALUES (2, 'James', 'Johnson', 'johnson@medicore.sk', '" + hesloHash + "', 'LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id, meno, priezvisko, email, heslo_hash, typ) VALUES (3, 'Emily', 'Parker', 'parker@medicore.sk', '" + hesloHash + "', 'LEKAR')");

        stmt.execute("INSERT INTO lekari (id, specializacia, pracovisko_id) VALUES (2, 'Všeobecné lekárstvo', 1)");
        stmt.execute("INSERT INTO lekari (id, specializacia, pracovisko_id) VALUES (3, 'Kardiológia', 2)");

        // Procedúry
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (1, 'Všeobecná konzultácia', 30, 'Základné vyšetrenie a konzultácia so všeobecným lekárom')");
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (2, 'Kardiologické vyšetrenie', 45, 'Kompletné kardiologické vyšetrenie vrátane EKG')");
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (3, 'Fyzioterapia', 60, 'Rehabilitačné cvičenia a fyzioterapeutické procedúry')");
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (4, 'Očné vyšetrenie', 30, 'Komplexné vyšetrenie zraku a očného pozadia')");
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (5, 'Psychologická konzultácia', 60, 'Konzultácia s klinickým psychológom')");
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (6, 'Konzultácia pred operáciou', 45, 'Predoperačné vyšetrenie a konzultácia s chirurgom')");
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (7, 'Odber krvi a laboratórne testy', 20, 'Odber krvi a základné laboratórne vyšetrenia')");
        stmt.execute("INSERT INTO procedury (id, nazov, trvanie_min, popis) VALUES (8, 'Predĺženie receptu', 15, 'Kontrola a predĺženie existujúceho receptu')");

        // Lekar-Procedury mapping
        stmt.execute("INSERT INTO lekar_procedury (lekar_id, procedura_id) VALUES (2, 1)");
        stmt.execute("INSERT INTO lekar_procedury (lekar_id, procedura_id) VALUES (2, 7)");
        stmt.execute("INSERT INTO lekar_procedury (lekar_id, procedura_id) VALUES (2, 8)");
        stmt.execute("INSERT INTO lekar_procedury (lekar_id, procedura_id) VALUES (3, 2)");
        stmt.execute("INSERT INTO lekar_procedury (lekar_id, procedura_id) VALUES (3, 7)");

        // Termíny — generate available slots for next 2 weeks
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        // If 8am today has already passed, start slots from tomorrow
        if (LocalDateTime.now().isAfter(now)) {
            now = now.plusDays(1);
        }

        int terminId = 1;
        for (int day = 0; day < 14; day++) {
            LocalDateTime date = now.plusDays(day);
            if (date.getDayOfWeek().getValue() > 5) continue; // skip weekends

            // Dr. Johnson — slots at 8:00, 9:00, 10:00, 11:00, 13:00, 14:00
            for (int hour : new int[]{8, 9, 10, 11, 13, 14}) {
                LocalDateTime slot = date.withHour(hour).withMinute(0);
                stmt.execute("INSERT INTO terminy (id, lekar_id, datum_cas, trvanie_min, stav) VALUES ("
                        + terminId++ + ", 2, '" + slot.format(fmt) + "', 30, 'DOSTUPNY')");
            }

            // Dr. Parker — slots at 9:00, 10:00, 11:00, 14:00, 15:00
            for (int hour : new int[]{9, 10, 11, 14, 15}) {
                LocalDateTime slot = date.withHour(hour).withMinute(0);
                stmt.execute("INSERT INTO terminy (id, lekar_id, datum_cas, trvanie_min, stav) VALUES ("
                        + terminId++ + ", 3, '" + slot.format(fmt) + "', 45, 'DOSTUPNY')");
            }
        }
    }
}
