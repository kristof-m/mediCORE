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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifikacie (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pacient_id INTEGER NOT NULL REFERENCES pouzivatelia(id),
                    typ TEXT NOT NULL,
                    sprava TEXT NOT NULL,
                    precitana INTEGER NOT NULL DEFAULT 0,
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
        LocalDateTime now = LocalDateTime.now();

        // ── Pracoviska ─────────────────────────────────────────────
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (1,'Kardiologická ambulancia','Budova A','1','101')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (2,'Dermatologická ambulancia','Budova B','2','204')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (3,'Všeobecná ambulancia','Budova C','1','110')");

        // ── Pacienti (IDs 1, 5–8) ─────────────────────────────────
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (1,'Samuel','Thompson','pacient@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (5,'Lukáš','Novák','novak@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (6,'Eva','Kozárová','kozarova@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (7,'Tomáš','Blaho','blaho@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (8,'Nina','Horváthová','horvath@medicore.sk','" + h + "','PACIENT')");

        // ── Lekári (IDs 2–4) ───────────────────────────────────────
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (2,'Sarah','Johnson','johnson@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (3,'Emily','Parker','parker@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (4,'Michael','Chen','chen@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (2,'Kardiológia',1)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (3,'Dermatológia',2)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (4,'Všeobecná prax',3)");

        // ── Procedúry ──────────────────────────────────────────────
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (1,'Kardiologická prehliadka',45,'Kompletné kardiologické vyšetrenie vrátane EKG a echokardiografie')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (2,'Dermatologická prehliadka',30,'Vyšetrenie kože, nechtov a kožných ochorení')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (3,'Všeobecná konzultácia',30,'Základná konzultácia so všeobecným lekárom')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (4,'EKG',20,'Elektrokardiogram — záznam elektrickej aktivity srdca')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis) VALUES (5,'Kožné vyšetrenie',30,'Podrobné vyšetrenie kožných zmien a dermatoskopia')");

        // ── Lekár ↔ Procedúry ──────────────────────────────────────
        stmt.execute("INSERT INTO lekar_procedury VALUES (2,1)"); // Johnson → Kardiologická prehliadka
        stmt.execute("INSERT INTO lekar_procedury VALUES (2,4)"); // Johnson → EKG
        stmt.execute("INSERT INTO lekar_procedury VALUES (3,2)"); // Parker  → Dermatologická prehliadka
        stmt.execute("INSERT INTO lekar_procedury VALUES (3,5)"); // Parker  → Kožné vyšetrenie
        stmt.execute("INSERT INTO lekar_procedury VALUES (4,3)"); // Chen    → Všeobecná konzultácia

        // ── Termíny ────────────────────────────────────────────────
        // IDs 1–9:  past slots (history for absolvované/zrušené)
        // IDs 10–13: today's slots (for doctor dashboard workload)
        // IDs 14–43: future slots (10 per doctor × 3 doctors)

        // -- Past terminy (ids 1-9, 3 per doctor) ------------------
        ins(stmt, fmt, 1, 2, now.minusDays(14).withHour(9).withMinute(0).withSecond(0).withNano(0),  45, "DOSTUPNY");
        ins(stmt, fmt, 2, 2, now.minusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0), 45, "DOSTUPNY");
        ins(stmt, fmt, 3, 2, now.minusDays(7).withHour(9).withMinute(0).withSecond(0).withNano(0),   45, "DOSTUPNY");
        ins(stmt, fmt, 4, 3, now.minusDays(12).withHour(10).withMinute(0).withSecond(0).withNano(0), 30, "DOSTUPNY");
        ins(stmt, fmt, 5, 3, now.minusDays(8).withHour(11).withMinute(0).withSecond(0).withNano(0),  30, "DOSTUPNY");
        ins(stmt, fmt, 6, 3, now.minusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0),  30, "DOSTUPNY");
        ins(stmt, fmt, 7, 4, now.minusDays(11).withHour(11).withMinute(0).withSecond(0).withNano(0), 30, "DOSTUPNY");
        ins(stmt, fmt, 8, 4, now.minusDays(6).withHour(10).withMinute(0).withSecond(0).withNano(0),  30, "DOSTUPNY");
        ins(stmt, fmt, 9, 4, now.minusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0),   30, "DOSTUPNY");

        // -- Today's terminy (ids 10-13) ----------------------------
        ins(stmt, fmt, 10, 2, now.withHour(10).withMinute(0).withSecond(0).withNano(0), 45, "REZERVOVANY"); // Johnson 10:00 → Samuel
        ins(stmt, fmt, 11, 2, now.withHour(14).withMinute(0).withSecond(0).withNano(0), 45, "REZERVOVANY"); // Johnson 14:00 → Nina
        ins(stmt, fmt, 12, 3, now.withHour(10).withMinute(0).withSecond(0).withNano(0), 30, "REZERVOVANY"); // Parker  10:00 → Tomáš
        ins(stmt, fmt, 13, 4, now.withHour(9).withMinute(0).withSecond(0).withNano(0),  30, "REZERVOVANY"); // Chen    09:00 → Nina

        // -- Future terminy (ids 14-43): next 10 business days ------
        LocalDateTime[] days = new LocalDateTime[10];
        int d = 0;
        LocalDateTime cursor = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (d < 10) {
            if (cursor.getDayOfWeek().getValue() <= 5) days[d++] = cursor;
            cursor = cursor.plusDays(1);
        }
        int[] hJ = {8,  9, 10, 11, 13, 14,  8,  9, 10, 11};
        int[] hP = {9, 10, 11, 14, 15,  9, 10, 11, 14, 15};
        int[] hC = {8, 10, 13,  9, 14,  8, 11, 13, 10, 14};

        int id = 14;
        // Johnson (ids 14-23): id=14 (i=0) REZERVOVANY → Samuel upcoming
        for (int i = 0; i < 10; i++) {
            String stav = (i == 0) ? "REZERVOVANY" : "DOSTUPNY";
            ins(stmt, fmt, id++, 2, days[i].withHour(hJ[i]).withMinute(0), 45, stav);
        }
        // Parker (ids 24-33): id=24 (i=0) → Lukáš, id=25 (i=1) → Eva
        for (int i = 0; i < 10; i++) {
            String stav = (i == 0 || i == 1) ? "REZERVOVANY" : "DOSTUPNY";
            ins(stmt, fmt, id++, 3, days[i].withHour(hP[i]).withMinute(0), 30, stav);
        }
        // Chen (ids 34-43): id=34 (i=0) → Nina upcoming, id=37 (i=3) → Samuel 2nd upcoming
        for (int i = 0; i < 10; i++) {
            String stav = (i == 0 || i == 3) ? "REZERVOVANY" : "DOSTUPNY";
            ins(stmt, fmt, id++, 4, days[i].withHour(hC[i]).withMinute(0), 30, stav);
        }

        // ── Rezervácie ─────────────────────────────────────────────
        // Samuel Thompson (pacient=1): 3 absolvované + 1 zrušená + 1 dnes + 2 nadchádzajúce
        rez(stmt, 1, 2,  1, 1, "POTVRDENA"); // -14d Johnson — Kardiologická (absolvovaná)
        rez(stmt, 1, 3,  5, 2, "POTVRDENA"); // -8d  Parker  — Dermatologická (absolvovaná)
        rez(stmt, 1, 4,  7, 3, "POTVRDENA"); // -11d Chen    — Všeobecná (absolvovaná)
        rez(stmt, 1, 3,  4, 5, "ZRUSENA");   // -12d Parker  — Kožné (zrušená)
        rez(stmt, 1, 2, 10, 4, "POTVRDENA"); // dnes 10:00 Johnson — EKG
        rez(stmt, 1, 2, 14, 1, "POTVRDENA"); // day+1 Johnson — Kardiologická (nadchádzajúca)
        rez(stmt, 1, 4, 37, 3, "POTVRDENA"); // day+4 Chen   — Všeobecná (nadchádzajúca)

        // Lukáš Novák (pacient=5): 1 absolvovaná + 1 zrušená + 1 nadchádzajúca
        rez(stmt, 5, 4,  8, 3, "POTVRDENA"); // -6d Chen   — Všeobecná (absolvovaná)
        rez(stmt, 5, 2,  2, 4, "ZRUSENA");   // -10d Johnson — EKG (zrušená)
        rez(stmt, 5, 3, 24, 2, "POTVRDENA"); // day+1 Parker — Dermatologická (nadchádzajúca)

        // Eva Kozárová (pacient=6): 1 absolvovaná + 1 nadchádzajúca
        rez(stmt, 6, 2,  3, 1, "POTVRDENA"); // -7d Johnson — Kardiologická (absolvovaná)
        rez(stmt, 6, 3, 25, 5, "POTVRDENA"); // day+2 Parker — Kožné (nadchádzajúca)

        // Tomáš Blaho (pacient=7): dnes + 1 zrušená
        rez(stmt, 7, 3, 12, 2, "POTVRDENA"); // dnes 10:00 Parker — Dermatologická
        rez(stmt, 7, 4,  9, 3, "ZRUSENA");   // -2d Chen — Všeobecná (zrušená)

        // Nina Horváthová (pacient=8): 2 dnes + 1 nadchádzajúca
        rez(stmt, 8, 4, 13, 3, "POTVRDENA"); // dnes 09:00 Chen — Všeobecná
        rez(stmt, 8, 2, 11, 1, "POTVRDENA"); // dnes 14:00 Johnson — Kardiologická
        rez(stmt, 8, 4, 34, 3, "POTVRDENA"); // day+1 Chen — Všeobecná (nadchádzajúca)
    }

    private static void ins(Statement stmt, DateTimeFormatter fmt,
                             int id, int lekarId, LocalDateTime dt,
                             int trvanie, String stav) throws SQLException {
        stmt.execute("INSERT INTO terminy (id,lekar_id,datum_cas,trvanie_min,stav) VALUES ("
            + id + "," + lekarId + ",'" + dt.format(fmt) + "'," + trvanie + ",'" + stav + "')");
    }

    private static void rez(Statement stmt,
                             int pacientId, int lekarId, int terminId,
                             int proceduraId, String stav) throws SQLException {
        stmt.execute("INSERT INTO rezervacie (pacient_id,lekar_id,termin_id,procedura_id,stav) VALUES ("
            + pacientId + "," + lekarId + "," + terminId + "," + proceduraId + ",'" + stav + "')");
    }
}
