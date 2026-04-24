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
                    popis TEXT,
                    kategoria TEXT
                )
            """);
            try { stmt.execute("ALTER TABLE procedury ADD COLUMN kategoria TEXT"); } catch (Exception ignored) {}

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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS meta (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
            """);

            seedData(stmt);
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException("Chyba pri inicializácii databázy", e);
        }
    }

    private static void seedData(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)");

        var rv = stmt.executeQuery("SELECT value FROM meta WHERE key='seed_version'");
        int version = rv.next() ? Integer.parseInt(rv.getString("value")) : 0;
        rv.close();
        if (version >= 2) return;

        // ── Wipe and re-seed ────────────────────────────────────────────────
        stmt.execute("DELETE FROM rezervacie");
        stmt.execute("DELETE FROM notifikacie");
        stmt.execute("DELETE FROM terminy");
        stmt.execute("DELETE FROM lekar_procedury");
        stmt.execute("DELETE FROM procedury");
        stmt.execute("DELETE FROM lekari");
        stmt.execute("DELETE FROM pouzivatelia");
        stmt.execute("DELETE FROM pracoviska");
        try { stmt.execute("DELETE FROM sqlite_sequence"); } catch (Exception ignored) {}

        String h = PasswordUtil.hash("heslo123");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        // ── Pracoviska ───────────────────────────────────────────────────────
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (1,'Kardiologická ambulancia','Budova A','1','101')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (2,'Dermatologická ambulancia','Budova B','2','204')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (3,'Všeobecná ambulancia','Budova C','1','110')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (4,'Neurologická ambulancia','Budova A','2','212')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (5,'Ortopedická ambulancia','Budova B','1','105')");
        stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES (6,'Oftalmologická ambulancia','Budova C','2','215')");

        // ── Pacienti ─────────────────────────────────────────────────────────
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (1,'Samuel','Thompson','pacient@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (2,'Lukáš','Novák','novak@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (3,'Eva','Kozárová','kozarova@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (4,'Tomáš','Blaho','blaho@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (5,'Nina','Horváthová','horvath@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (6,'Mária','Kováčová','kovacova@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (7,'Peter','Horník','hornik@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (8,'Jana','Procházková','prochazka@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (9,'Martin','Oravec','oravec@medicore.sk','" + h + "','PACIENT')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (10,'Zuzana','Malá','mala@medicore.sk','" + h + "','PACIENT')");

        // ── Lekári ───────────────────────────────────────────────────────────
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (11,'Sarah','Johnson','johnson@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (12,'Emily','Parker','parker@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (13,'Michael','Chen','chen@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (14,'Jakub','Novotný','novotny@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (15,'Andrea','Horvat','horvat@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (16,'Petra','Žilková','zilkova@medicore.sk','" + h + "','LEKAR')");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (11,'Kardiológia',1)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (12,'Dermatológia',2)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (13,'Všeobecná prax',3)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (14,'Neurológia',4)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (15,'Ortopédia',5)");
        stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES (16,'Oftalmológia',6)");

        // ── Admin ─────────────────────────────────────────────────────────────
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES (17,'Admin','Polyklinika','admin@medicore.sk','" + h + "','ADMIN')");

        // ── Procedúry ─────────────────────────────────────────────────────────
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (1,'Kardiologická prehliadka',45,'Kompletné kardiologické vyšetrenie vrátane EKG a echokardiografie','Kardiológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (2,'Dermatologická prehliadka',30,'Vyšetrenie kože, nechtov a kožných ochorení','Dermatológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (3,'Všeobecná konzultácia',30,'Základná konzultácia so všeobecným lekárom','Všeobecná prax')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (4,'EKG',20,'Elektrokardiogram — záznam elektrickej aktivity srdca','Kardiológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (5,'Kožné vyšetrenie',30,'Podrobné vyšetrenie kožných zmien a dermatoskopia','Dermatológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (6,'Neurologické vyšetrenie',45,'Komplexné neurologické vyšetrenie reflexov a neurologických funkcií','Neurológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (7,'Ortopedická konzultácia',30,'Vyšetrenie pohybového aparátu, kĺbov a chrbtice','Ortopédia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (8,'Meranie zraku',20,'Vyšetrenie zrakovej ostrosti a refrakčných chýb','Oftalmológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (9,'Krvné testy',15,'Odber krvi a základný krvný rozbor','Laboratórium')");

        // ── Lekár ↔ Procedúry ─────────────────────────────────────────────────
        stmt.execute("INSERT INTO lekar_procedury VALUES (11,1)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (11,4)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (11,9)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (12,2)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (12,5)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (12,9)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (13,3)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (13,9)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (14,6)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (14,9)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (15,7)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (15,9)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (16,8)");
        stmt.execute("INSERT INTO lekar_procedury VALUES (16,9)");

        // ── Build next 15 business days ────────────────────────────────────────
        LocalDateTime[] days = new LocalDateTime[15];
        int d = 0;
        LocalDateTime cursor = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (d < 15) {
            if (cursor.getDayOfWeek().getValue() <= 5) days[d++] = cursor;
            cursor = cursor.plusDays(1);
        }

        // ── Past termíny (IDs 1–30) ────────────────────────────────────────────
        ins(stmt,fmt, 1,11,past(now,20,9),45,"DOSTUPNY"); ins(stmt,fmt, 2,11,past(now,15,9),45,"DOSTUPNY");
        ins(stmt,fmt, 3,11,past(now,12,9),45,"DOSTUPNY"); ins(stmt,fmt, 4,11,past(now, 8,9),45,"DOSTUPNY");
        ins(stmt,fmt, 5,11,past(now, 5,9),45,"DOSTUPNY");
        ins(stmt,fmt, 6,12,past(now,18,10),30,"DOSTUPNY"); ins(stmt,fmt, 7,12,past(now,14,10),30,"DOSTUPNY");
        ins(stmt,fmt, 8,12,past(now,10,10),30,"DOSTUPNY"); ins(stmt,fmt, 9,12,past(now, 7,10),30,"DOSTUPNY");
        ins(stmt,fmt,10,12,past(now, 4,10),30,"DOSTUPNY");
        ins(stmt,fmt,11,13,past(now,16,9),30,"DOSTUPNY"); ins(stmt,fmt,12,13,past(now,12,9),30,"DOSTUPNY");
        ins(stmt,fmt,13,13,past(now, 9,9),30,"DOSTUPNY"); ins(stmt,fmt,14,13,past(now, 6,9),30,"DOSTUPNY");
        ins(stmt,fmt,15,13,past(now, 3,9),30,"DOSTUPNY");
        ins(stmt,fmt,16,14,past(now,19,10),45,"DOSTUPNY"); ins(stmt,fmt,17,14,past(now,13,10),45,"DOSTUPNY");
        ins(stmt,fmt,18,14,past(now,10,10),45,"DOSTUPNY"); ins(stmt,fmt,19,14,past(now, 7,10),45,"DOSTUPNY");
        ins(stmt,fmt,20,14,past(now, 5,10),45,"DOSTUPNY");
        ins(stmt,fmt,21,15,past(now,17,9),30,"DOSTUPNY"); ins(stmt,fmt,22,15,past(now,11,9),30,"DOSTUPNY");
        ins(stmt,fmt,23,15,past(now, 8,9),30,"DOSTUPNY"); ins(stmt,fmt,24,15,past(now, 5,9),30,"DOSTUPNY");
        ins(stmt,fmt,25,15,past(now, 3,9),30,"DOSTUPNY");
        ins(stmt,fmt,26,16,past(now,20,9),20,"DOSTUPNY"); ins(stmt,fmt,27,16,past(now,14,9),20,"DOSTUPNY");
        ins(stmt,fmt,28,16,past(now, 9,9),20,"DOSTUPNY"); ins(stmt,fmt,29,16,past(now, 6,9),20,"DOSTUPNY");
        ins(stmt,fmt,30,16,past(now, 3,9),20,"DOSTUPNY");

        // ── Today termíny (IDs 31–42) ──────────────────────────────────────────
        ins(stmt,fmt,31,11,today(now, 9,0),45,"REZERVOVANY");
        ins(stmt,fmt,32,11,today(now,11,0),45,"REZERVOVANY");
        ins(stmt,fmt,33,12,today(now,10,0),30,"REZERVOVANY");
        ins(stmt,fmt,34,12,today(now,14,0),30,"REZERVOVANY");
        ins(stmt,fmt,35,13,today(now, 9,0),30,"REZERVOVANY");
        ins(stmt,fmt,36,13,today(now,11,0),30,"REZERVOVANY");
        ins(stmt,fmt,37,14,today(now,10,0),45,"REZERVOVANY");
        ins(stmt,fmt,38,14,today(now,15,0),45,"REZERVOVANY");
        ins(stmt,fmt,39,15,today(now, 9,0),30,"REZERVOVANY");
        ins(stmt,fmt,40,15,today(now,14,0),30,"DOSTUPNY");
        ins(stmt,fmt,41,16,today(now, 9,0),20,"REZERVOVANY");
        ins(stmt,fmt,42,16,today(now,11,0),20,"DOSTUPNY");

        // ── Future termíny — days[0]–days[1] with some bookings (IDs 43–90) ────
        ins(stmt,fmt,43,11,days[0].withHour( 8),45,"REZERVOVANY");
        ins(stmt,fmt,44,11,days[0].withHour(10),45,"REZERVOVANY");
        ins(stmt,fmt,45,11,days[1].withHour( 9),45,"REZERVOVANY");
        ins(stmt,fmt,46,11,days[1].withHour(11),45,"REZERVOVANY");
        ins(stmt,fmt,47,11,days[2].withHour( 8),45,"DOSTUPNY");
        ins(stmt,fmt,48,11,days[2].withHour(14),45,"DOSTUPNY");
        ins(stmt,fmt,49,11,days[3].withHour( 9),45,"DOSTUPNY");
        ins(stmt,fmt,50,11,days[4].withHour(10),45,"DOSTUPNY");
        ins(stmt,fmt,51,12,days[0].withHour( 9),30,"REZERVOVANY");
        ins(stmt,fmt,52,12,days[1].withHour(10),30,"REZERVOVANY");
        ins(stmt,fmt,53,12,days[2].withHour(11),30,"DOSTUPNY");
        ins(stmt,fmt,54,12,days[2].withHour(14),30,"DOSTUPNY");
        ins(stmt,fmt,55,12,days[3].withHour( 9),30,"DOSTUPNY");
        ins(stmt,fmt,56,12,days[4].withHour(10),30,"DOSTUPNY");
        ins(stmt,fmt,57,12,days[5].withHour(11),30,"DOSTUPNY");
        ins(stmt,fmt,58,12,days[5].withHour(14),30,"DOSTUPNY");
        ins(stmt,fmt,59,13,days[0].withHour( 8),30,"REZERVOVANY");
        ins(stmt,fmt,60,13,days[0].withHour(13),30,"REZERVOVANY");
        ins(stmt,fmt,61,13,days[1].withHour( 9),30,"DOSTUPNY");
        ins(stmt,fmt,62,13,days[2].withHour(10),30,"REZERVOVANY");
        ins(stmt,fmt,63,13,days[3].withHour( 8),30,"DOSTUPNY");
        ins(stmt,fmt,64,13,days[3].withHour(13),30,"DOSTUPNY");
        ins(stmt,fmt,65,13,days[4].withHour( 9),30,"DOSTUPNY");
        ins(stmt,fmt,66,13,days[5].withHour(10),30,"DOSTUPNY");
        ins(stmt,fmt,67,14,days[0].withHour(10),45,"REZERVOVANY");
        ins(stmt,fmt,68,14,days[0].withHour(14),45,"REZERVOVANY");
        ins(stmt,fmt,69,14,days[1].withHour( 9),45,"REZERVOVANY");
        ins(stmt,fmt,70,14,days[2].withHour(10),45,"DOSTUPNY");
        ins(stmt,fmt,71,14,days[3].withHour(11),45,"DOSTUPNY");
        ins(stmt,fmt,72,14,days[4].withHour( 9),45,"DOSTUPNY");
        ins(stmt,fmt,73,14,days[5].withHour(10),45,"DOSTUPNY");
        ins(stmt,fmt,74,14,days[6].withHour(11),45,"DOSTUPNY");
        ins(stmt,fmt,75,15,days[0].withHour( 8),30,"REZERVOVANY");
        ins(stmt,fmt,76,15,days[1].withHour( 9),30,"DOSTUPNY");
        ins(stmt,fmt,77,15,days[2].withHour(10),30,"DOSTUPNY");
        ins(stmt,fmt,78,15,days[3].withHour( 8),30,"DOSTUPNY");
        ins(stmt,fmt,79,15,days[4].withHour( 9),30,"DOSTUPNY");
        ins(stmt,fmt,80,15,days[5].withHour(10),30,"DOSTUPNY");
        ins(stmt,fmt,81,15,days[6].withHour(11),30,"DOSTUPNY");
        ins(stmt,fmt,82,15,days[7].withHour( 8),30,"DOSTUPNY");
        ins(stmt,fmt,83,16,days[0].withHour( 9),20,"REZERVOVANY");
        ins(stmt,fmt,84,16,days[1].withHour(10),20,"REZERVOVANY");
        ins(stmt,fmt,85,16,days[2].withHour( 9),20,"REZERVOVANY");
        ins(stmt,fmt,86,16,days[3].withHour(10),20,"DOSTUPNY");
        ins(stmt,fmt,87,16,days[4].withHour(11),20,"DOSTUPNY");
        ins(stmt,fmt,88,16,days[5].withHour( 9),20,"DOSTUPNY");
        ins(stmt,fmt,89,16,days[6].withHour(10),20,"DOSTUPNY");
        ins(stmt,fmt,90,16,days[7].withHour(11),20,"DOSTUPNY");

        // ── Additional future termíny — days[2]–days[14] (IDs 91+) ─────────────
        // Johnson (lekarId=11, 45 min): 6 slots/day
        int[] jhHours  = {8, 9, 10, 11, 13, 14};
        // Parker (lekarId=12, 30 min): 8 slots/day
        int[] pkHours  = {8, 9, 10, 11, 13, 14, 15, 16};
        // Chen (lekarId=13, 30 min): 8 slots/day
        int[] cnHours  = {8, 9, 10, 11, 13, 14, 15, 16};
        // Novotný (lekarId=14, 45 min): 6 slots/day
        int[] nvHours  = {8, 9, 10, 11, 13, 14};
        // Horvat (lekarId=15, 30 min): 8 slots/day
        int[] hvHours  = {8, 9, 10, 11, 13, 14, 15, 16};
        // Žilková (lekarId=16, 20 min): 9 slots/day
        int[] zlHours  = {8, 9, 10, 11, 12, 13, 14, 15, 16};

        int nextId = 91;
        for (int di = 2; di < 15; di++) {
            for (int hh : jhHours) ins(stmt,fmt,nextId++,11,days[di].withHour(hh),45,"DOSTUPNY");
            for (int hh : pkHours) ins(stmt,fmt,nextId++,12,days[di].withHour(hh),30,"DOSTUPNY");
            for (int hh : cnHours) ins(stmt,fmt,nextId++,13,days[di].withHour(hh),30,"DOSTUPNY");
            for (int hh : nvHours) ins(stmt,fmt,nextId++,14,days[di].withHour(hh),45,"DOSTUPNY");
            for (int hh : hvHours) ins(stmt,fmt,nextId++,15,days[di].withHour(hh),30,"DOSTUPNY");
            for (int hh : zlHours) ins(stmt,fmt,nextId++,16,days[di].withHour(hh),20,"DOSTUPNY");
        }

        // ── Rezervácie ────────────────────────────────────────────────────────
        rez(stmt, 1,11, 1,1,"POTVRDENA"); rez(stmt, 1,12, 8,2,"POTVRDENA");
        rez(stmt, 1,13,11,3,"POTVRDENA"); rez(stmt, 1,14,16,6,"POTVRDENA");
        rez(stmt, 1,15,25,7,"ZRUSENA");
        rez(stmt, 1,11,31,1,"POTVRDENA");
        rez(stmt, 1,11,43,1,"POTVRDENA"); rez(stmt, 1,13,62,3,"POTVRDENA");

        rez(stmt, 2,13,12,3,"POTVRDENA"); rez(stmt, 2,16,27,8,"POTVRDENA");
        rez(stmt, 2,11, 2,4,"ZRUSENA");
        rez(stmt, 2,16,41,8,"POTVRDENA");
        rez(stmt, 2,12,51,2,"POTVRDENA");

        rez(stmt, 3,11, 3,1,"POTVRDENA"); rez(stmt, 3,15,21,7,"POTVRDENA");
        rez(stmt, 3,12,33,2,"POTVRDENA");
        rez(stmt, 3,12,52,5,"POTVRDENA"); rez(stmt, 3,16,84,8,"POTVRDENA");

        rez(stmt, 4,15,22,7,"POTVRDENA"); rez(stmt, 4,13,15,3,"ZRUSENA");
        rez(stmt, 4,13,35,3,"POTVRDENA");
        rez(stmt, 4,11,45,4,"POTVRDENA");

        rez(stmt, 5,12, 9,5,"POTVRDENA"); rez(stmt, 5,13,14,3,"POTVRDENA");
        rez(stmt, 5,11,32,1,"POTVRDENA");
        rez(stmt, 5,13,59,3,"POTVRDENA");

        rez(stmt, 6,11, 4,4,"POTVRDENA"); rez(stmt, 6,12, 7,5,"POTVRDENA");
        rez(stmt, 6,14,18,6,"ZRUSENA");
        rez(stmt, 6,13,36,3,"POTVRDENA");
        rez(stmt, 6,11,44,1,"POTVRDENA"); rez(stmt, 6,14,69,6,"POTVRDENA");

        rez(stmt, 7,14,17,6,"POTVRDENA"); rez(stmt, 7,16,26,8,"POTVRDENA");
        rez(stmt, 7,13,13,3,"ZRUSENA");
        rez(stmt, 7,14,37,6,"POTVRDENA");
        rez(stmt, 7,14,67,6,"POTVRDENA"); rez(stmt, 7,16,85,8,"POTVRDENA");

        rez(stmt, 8,12, 6,2,"POTVRDENA"); rez(stmt, 8,14,19,6,"POTVRDENA");
        rez(stmt, 8,15,23,7,"POTVRDENA"); rez(stmt, 8,16,29,8,"ZRUSENA");
        rez(stmt, 8,15,39,7,"POTVRDENA");
        rez(stmt, 8,14,68,6,"POTVRDENA");

        rez(stmt, 9,12,10,5,"POTVRDENA"); rez(stmt, 9,15,24,7,"POTVRDENA");
        rez(stmt, 9,16,30,8,"ZRUSENA");
        rez(stmt, 9,14,38,6,"POTVRDENA");
        rez(stmt, 9,13,60,3,"POTVRDENA"); rez(stmt, 9,15,75,7,"POTVRDENA");

        rez(stmt,10,11, 5,4,"POTVRDENA"); rez(stmt,10,14,20,6,"POTVRDENA");
        rez(stmt,10,16,28,8,"POTVRDENA");
        rez(stmt,10,12,34,5,"POTVRDENA");
        rez(stmt,10,11,46,1,"POTVRDENA"); rez(stmt,10,16,83,8,"POTVRDENA");

        stmt.execute("INSERT OR REPLACE INTO meta (key,value) VALUES ('seed_version','2')");
    }

    private static LocalDateTime past(LocalDateTime now, int daysAgo, int hour) {
        return now.minusDays(daysAgo).withHour(hour).withMinute(0).withSecond(0).withNano(0);
    }

    private static LocalDateTime today(LocalDateTime now, int hour, int minute) {
        return now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
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
