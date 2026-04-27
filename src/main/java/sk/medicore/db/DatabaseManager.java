package sk.medicore.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import sk.medicore.util.PasswordUtil;

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
                    stav TEXT NOT NULL DEFAULT 'PUBLIKOVANY'
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
        if (version >= 3) return;

        for (String t : new String[]{"rezervacie","notifikacie","terminy","lekar_procedury","procedury","lekari","pouzivatelia","pracoviska"})
            stmt.execute("DELETE FROM " + t);
        try { stmt.execute("DELETE FROM sqlite_sequence"); } catch (Exception ignored) {}

        String h = PasswordUtil.hash("heslo123");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        // ── Pracoviska (15) ─────────────────────────────────────────────
        String[][] prac = {
            {"1","Kardiologická ambulancia","Budova A","1","101"},
            {"2","Kardiologická ambulancia II","Budova A","1","102"},
            {"3","Dermatologická ambulancia","Budova B","2","204"},
            {"4","Všeobecná ambulancia I","Budova C","1","110"},
            {"5","Všeobecná ambulancia II","Budova C","1","112"},
            {"6","Neurologická ambulancia","Budova A","2","212"},
            {"7","Ortopedická ambulancia","Budova B","1","105"},
            {"8","Oftalmologická ambulancia","Budova C","2","215"},
            {"9","Interná ambulancia","Budova D","1","101"},
            {"10","Chirurgická ambulancia","Budova B","2","210"},
            {"11","ORL ambulancia","Budova A","3","301"},
            {"12","Urologická ambulancia","Budova D","2","205"},
            {"13","Gynekologická ambulancia","Budova C","3","310"},
            {"14","Pediatrická ambulancia","Budova D","1","108"},
            {"15","Pneumologická ambulancia","Budova A","3","305"},
        };
        for (String[] p : prac)
            stmt.execute("INSERT INTO pracoviska (id,nazov,budova,poschodie,miestnost) VALUES ("
                + p[0] + ",'" + p[1] + "','" + p[2] + "','" + p[3] + "','" + p[4] + "')");

        // ── Pacienti (IDs 1–30) ─────────────────────────────────────────
        String[][] pat = {
            {"1","Ján","Novák","novak.jan"},
            {"2","Mária","Horváthová","horvathova.maria"},
            {"3","Peter","Sloboda","sloboda.peter"},
            {"4","Eva","Štefanková","stefankova.eva"},
            {"5","Tomáš","Holub","holub.tomas"},
            {"6","Zuzana","Vlčková","vlckova.zuzana"},
            {"7","Martin","Rusnák","rusnak.martin"},
            {"8","Jana","Ondrejčíková","ondrejcikova.jana"},
            {"9","Lukáš","Kamenický","kamenicky.lukas"},
            {"10","Katarína","Hrušovská","hrusovska.katarina"},
            {"11","Andrej","Sedlák","sedlak.andrej"},
            {"12","Lenka","Záborská","zaborska.lenka"},
            {"13","Michal","Straka","straka.michal"},
            {"14","Nina","Bartoňová","bartonova.nina"},
            {"15","Dušan","Mikuš","mikus.dusan"},
            {"16","Soňa","Kráľovičová","kralovicova.sona"},
            {"17","Róbert","Jurčík","jurcik.robert"},
            {"18","Monika","Šimková","simkova.monika"},
            {"19","Štefan","Dolinský","dolinsky.stefan"},
            {"20","Iveta","Pálková","palkova.iveta"},
            {"21","Richard","Kučera","kucera.richard"},
            {"22","Alena","Blahová","blahova.alena"},
            {"23","Vladimír","Gašpar","gaspar.vladimir"},
            {"24","Daniela","Ráczová","raczova.daniela"},
            {"25","Marek","Kupčík","kupcik.marek"},
            {"26","Barbora","Tkáčová","tkacova.barbora"},
            {"27","Filip","Zelinka","zelinka.filip"},
            {"28","Lucia","Havlíčková","havlickova.lucia"},
            {"29","Pavol","Šoltés","soltes.pavol"},
            {"30","Simona","Ďurišová","durisova.simona"},
        };
        for (String[] p : pat)
            stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES ("
                + p[0] + ",'" + p[1] + "','" + p[2] + "','" + p[3] + "@medicore.sk','" + h + "','PACIENT')");

        // ── Lekári (IDs 31–45) ──────────────────────────────────────────
        String[][] doc = {
            {"31","Mária","Tóthová","tothova.maria","Kardiológia","1"},
            {"32","Peter","Kováč","kovac.peter","Kardiológia","2"},
            {"33","Jana","Némethová","nemethova.jana","Dermatológia","3"},
            {"34","Michal","Horváth","horvath.michal","Všeobecná prax","4"},
            {"35","Zuzana","Krajčíková","krajcikova.zuzana","Všeobecná prax","5"},
            {"36","Tomáš","Szabó","szabo.tomas","Neurológia","6"},
            {"37","Andrej","Baláž","balaz.andrej","Ortopédia","7"},
            {"38","Eva","Molnárová","molnarova.eva","Oftalmológia","8"},
            {"39","Martin","Varga","varga.martin","Interná medicína","9"},
            {"40","Róbert","Fekete","fekete.robert","Chirurgia","10"},
            {"41","Katarína","Hudáková","hudakova.katarina","ORL","11"},
            {"42","Juraj","Polák","polak.juraj","Urológia","12"},
            {"43","Lenka","Benčíková","bencikova.lenka","Gynekológia","13"},
            {"44","Soňa","Kučerová","kucerova.sona","Pediatria","14"},
            {"45","Dušan","Mazúr","mazur.dusan","Pneumológia","15"},
        };
        for (String[] d : doc) {
            stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES ("
                + d[0] + ",'" + d[1] + "','" + d[2] + "','" + d[3] + "@medicore.sk','" + h + "','LEKAR')");
            stmt.execute("INSERT INTO lekari (id,specializacia,pracovisko_id) VALUES ("
                + d[0] + ",'" + d[4] + "'," + d[5] + ")");
        }

        // ── Admin (ID 46)───────────────────────────────────────────────
        stmt.execute("INSERT INTO pouzivatelia (id,meno,priezvisko,email,heslo_hash,typ) VALUES "
            + "(46,'Admin','Polyklinika','admin@medicore.sk','" + h + "','ADMIN')");

        // ── Procedúry (20)──────────────────────────────────────────────
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (1,'Kardiologická prehliadka',45,'Kompletné kardiologické vyšetrenie vrátane EKG a echokardiografie','Kardiológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (2,'EKG',20,'Elektrokardiogram — záznam elektrickej aktivity srdca','Kardiológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (3,'Echokardiografia',30,'Ultrazvukové vyšetrenie srdca','Kardiológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (4,'Dermatologická prehliadka',30,'Vyšetrenie kože, nechtov a kožných ochorení','Dermatológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (5,'Dermatoskopia',20,'Vyšetrenie kožných útvarov dermatoskopom','Dermatológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (6,'Všeobecná konzultácia',30,'Základná konzultácia so všeobecným lekárom','Všeobecná prax')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (7,'Preventívna prehliadka',45,'Komplexná preventívna zdravotná prehliadka','Všeobecná prax')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (8,'Neurologické vyšetrenie',45,'Komplexné neurologické vyšetrenie reflexov a neurologických funkcií','Neurológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (9,'EEG',30,'Elektroencefalogram — záznam elektrickej aktivity mozgu','Neurológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (10,'Ortopedická konzultácia',30,'Vyšetrenie pohybového aparátu, kĺbov a chrbtice','Ortopédia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (11,'Meranie zraku',20,'Vyšetrenie zrakovej ostrosti a refrakčných chýb','Oftalmológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (12,'Očné vyšetrenie',30,'Komplexné oftalmologické vyšetrenie','Oftalmológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (13,'Interné vyšetrenie',45,'Komplexné internistické vyšetrenie','Interná medicína')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (14,'Chirurgická konzultácia',30,'Konzultácia a vyšetrenie pred chirurgickým zákrokom','Chirurgia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (15,'ORL vyšetrenie',30,'Vyšetrenie ucha, nosa a hrdla','ORL')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (16,'Urologické vyšetrenie',30,'Vyšetrenie močových ciest a reprodukčného systému','Urológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (17,'Gynekologická prehliadka',30,'Preventívna gynekologická prehliadka','Gynekológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (18,'Pediatrické vyšetrenie',30,'Vyšetrenie detí a dorastu','Pediatria')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (19,'Pneumologické vyšetrenie',30,'Vyšetrenie dýchacích ciest a pľúc','Pneumológia')");
        stmt.execute("INSERT INTO procedury (id,nazov,trvanie_min,popis,kategoria) VALUES (20,'Krvné testy',15,'Odber krvi a základný krvný rozbor','Laboratórium')");

        // ── Lekár ↔ Procedúry ───────────────────────────────────────────
        int[][] lp = {
            {31,1},{31,2},{31,20},
            {32,1},{32,3},{32,20},
            {33,4},{33,5},{33,20},
            {34,6},{34,7},{34,20},
            {35,6},{35,7},{35,20},
            {36,8},{36,9},{36,20},
            {37,10},{37,20},
            {38,11},{38,12},{38,20},
            {39,13},{39,20},
            {40,14},{40,20},
            {41,15},{41,20},
            {42,16},{42,20},
            {43,17},
            {44,18},{44,20},
            {45,19},{45,20},
        };
        for (int[] pair : lp)
            stmt.execute("INSERT INTO lekar_procedury VALUES (" + pair[0] + "," + pair[1] + ")");

        // ── Termíny & Rezervácie ────────────────────────────────────────
        int[] lekarIds  = {31,32,33,34,35,36,37,38,39,40,41,42,43,44,45};
        int[] durations = {45,45,30,30,30,45,30,30,45,30,30,30,30,30,30};
        int[] defProc   = { 1, 1, 4, 6, 6, 8,10,11,13,14,15,16,17,18,19};

        int tid = 1;
        int patIdx = 0;

        // Past termíny: 4 per doctor = 60 (UKONCENY + UKONCENA/ZRUSENA rez.)
        int[] pastDaysAgo = {20, 14, 8, 3};
        int[] pastHours   = { 9, 10,14,15};
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 4; j++) {
                ins(stmt, fmt, tid, lekarIds[i], past(now, pastDaysAgo[j], pastHours[j]), durations[i], "UKONCENY");
                String rezStav = (patIdx % 7 == 6) ? "ZRUSENA" : "UKONCENA";
                rez(stmt, (patIdx % 30) + 1, lekarIds[i], tid, defProc[i], rezStav);
                tid++;
                patIdx++;
            }
        }

        // Today termíny: 2 per doctor = 30 (REZERVOVANY + POTVRDENA rez.)
        for (int i = 0; i < 15; i++) {
            ins(stmt, fmt, tid, lekarIds[i], today(now, 9, 0), durations[i], "REZERVOVANY");
            rez(stmt, (patIdx % 30) + 1, lekarIds[i], tid, defProc[i], "POTVRDENA");
            tid++; patIdx++;
            ins(stmt, fmt, tid, lekarIds[i], today(now, 14, 0), durations[i], "REZERVOVANY");
            rez(stmt, (patIdx % 30) + 1, lekarIds[i], tid, defProc[i], "POTVRDENA");
            tid++; patIdx++;
        }

        // Build next 10 business days
        LocalDateTime[] futureDays = new LocalDateTime[10];
        int dIdx = 0;
        LocalDateTime dayCursor = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (dIdx < 10) {
            if (dayCursor.getDayOfWeek().getValue() <= 5) futureDays[dIdx++] = dayCursor;
            dayCursor = dayCursor.plusDays(1);
        }

        // Future termíny: first 2 days have 2 booked slots each, rest PUBLIKOVANY
        for (int i = 0; i < 15; i++) {
            int dur = durations[i];
            int[] hours = dur >= 45
                ? new int[]{8, 9, 10, 11, 13, 14}
                : new int[]{8, 9, 10, 11, 13, 14, 15, 16};
            for (int di = 0; di < 10; di++) {
                for (int si = 0; si < hours.length; si++) {
                    boolean booked = di < 2 && si < 2;
                    ins(stmt, fmt, tid, lekarIds[i], futureDays[di].withHour(hours[si]), dur,
                        booked ? "REZERVOVANY" : "PUBLIKOVANY");
                    if (booked) {
                        rez(stmt, (patIdx % 30) + 1, lekarIds[i], tid, defProc[i], "POTVRDENA");
                        patIdx++;
                    }
                    tid++;
                }
            }
        }

        stmt.execute("INSERT OR REPLACE INTO meta (key,value) VALUES ('seed_version','3')");
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
