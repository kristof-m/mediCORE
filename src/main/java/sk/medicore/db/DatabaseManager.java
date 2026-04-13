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
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM pouzivatelia");
        rs.next();
        if (rs.getInt(1) > 0) { rs.close(); return; }
        rs.close();

        String h = PasswordUtil.hash("heslo123");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Pracoviska
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (1,'Kardiologická ambulancia','Budova A','1','101')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (2,'Dermatologická ambulancia','Budova B','2','204')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (3,'Všeobecná ambulancia','Budova C','1','110')");

        // Pacient
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (1,'Samuel','Thompson','pacient@medicore.sk','" + h + "','PACIENT')");

        // Lekári
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (2,'Sarah','Johnson','johnson@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (3,'Emily','Parker','parker@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (4,'Michael','Chen','chen@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (2,'Kardiológia',1)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (3,'Dermatológia',2)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (4,'Všeobecná prax',3)");

        // Procedúry
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (1,'Kardiologická prehliadka',45,'Kompletné kardiologické vyšetrenie')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (2,'Dermatologická prehliadka',30,'Vyšetrenie kože a kožných ochorení')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (3,'Všeobecná konzultácia',30,'Základná konzultácia so všeobecným lekárom')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (4,'EKG',20,'Elektrokardiogram — záznam srdcovej aktivity')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (5,'Kožné vyšetrenie',30,'Podrobné vyšetrenie kožných zmien a dermatoskopia')");

        // Lekar-Procedury
        stmt.execute("INSERT INTO lekar_procedury VALUES (2,1)"); // Johnson → Kardiologická
        stmt.execute("INSERT INTO lekar_procedury VALUES (2,4)"); // Johnson → EKG
        stmt.execute("INSERT INTO lekar_procedury VALUES (3,2)"); // Parker  → Dermatologická
        stmt.execute("INSERT INTO lekar_procedury VALUES (3,5)"); // Parker  → Kožné vyšetrenie
        stmt.execute("INSERT INTO lekar_procedury VALUES (4,3)"); // Chen    → Všeobecná konzultácia

        // Termíny —————————————————————————————————————————————————
        // terminId=1: past slot (yesterday) for ZRUSENA test reservation
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);
        stmt.execute("INSERT INTO terminy (id,lekar_id,datum_cas,trvanie_min,stav) VALUES (1,2,'"
            + yesterday.format(fmt) + "',45,'DOSTUPNY')");

        // Collect next 10 business days
        LocalDateTime[] days = new LocalDateTime[10];
        int d = 0;
        LocalDateTime cursor = LocalDateTime.now().plusDays(1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (d < 10) {
            if (cursor.getDayOfWeek().getValue() <= 5) days[d++] = cursor;
            cursor = cursor.plusDays(1);
        }

        // Hour patterns for variety (10 per doctor)
        int[] hJ = {8,  9, 10, 11, 13, 14,  8,  9, 10, 11};
        int[] hP = {9, 10, 11, 14, 15,  9, 10, 11, 14, 15};
        int[] hC = {8, 10, 13,  9, 14,  8, 11, 13, 10, 14};

        int id = 2;
        // Johnson (lekar=2, 45 min) — terminId 2-11, terminId=2 is REZERVOVANY
        for (int i = 0; i < 10; i++) {
            LocalDateTime slot = days[i].withHour(hJ[i]).withMinute(0);
            String stav = (i == 0) ? "REZERVOVANY" : "DOSTUPNY";
            stmt.execute("INSERT INTO terminy (id,lekar_id,datum_cas,trvanie_min,stav) VALUES ("
                + id++ + ",2,'" + slot.format(fmt) + "',45,'" + stav + "')");
        }
        // Parker (lekar=3, 30 min) — terminId 12-21, terminId=15 (i=3) is REZERVOVANY
        for (int i = 0; i < 10; i++) {
            LocalDateTime slot = days[i].withHour(hP[i]).withMinute(0);
            String stav = (i == 3) ? "REZERVOVANY" : "DOSTUPNY";
            stmt.execute("INSERT INTO terminy (id,lekar_id,datum_cas,trvanie_min,stav) VALUES ("
                + id++ + ",3,'" + slot.format(fmt) + "',30,'" + stav + "')");
        }
        // Chen (lekar=4, 30 min) — terminId 22-31, terminId=26 (i=4) is REZERVOVANY
        for (int i = 0; i < 10; i++) {
            LocalDateTime slot = days[i].withHour(hC[i]).withMinute(0);
            String stav = (i == 4) ? "REZERVOVANY" : "DOSTUPNY";
            stmt.execute("INSERT INTO terminy (id,lekar_id,datum_cas,trvanie_min,stav) VALUES ("
                + id++ + ",4,'" + slot.format(fmt) + "',30,'" + stav + "')");
        }

        // Test rezervácie
        // 1) POTVRDENA upcoming — Johnson's first slot (terminId=2, REZERVOVANY)
        stmt.execute("INSERT INTO rezervacie (pacient_id,lekar_id,termin_id,procedura_id,stav) VALUES (1,2,2,1,'POTVRDENA')");
        // 2) ZRUSENA past — yesterday's slot (terminId=1)
        stmt.execute("INSERT INTO rezervacie (pacient_id,lekar_id,termin_id,procedura_id,stav) VALUES (1,2,1,1,'ZRUSENA')");
    }
}
