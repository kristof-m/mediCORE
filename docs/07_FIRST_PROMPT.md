# First Prompt for Claude Code

Copy-paste this exactly as your first message to Claude Code:

---

I'm building MediCORE — a JavaFX + SQLite desktop reservation system for a university project. The project structure is already set up. Read all the markdown files in the docs/ folder first to understand the full context.

Then implement the following in order:

1. `src/main/java/sk/medicore/util/PasswordUtil.java` — SHA-256 hashing, method `hash(String password)` and `verify(String password, String hash)`

2. `src/main/java/sk/medicore/util/SessionManager.java` — singleton, holds current logged-in `Pouzivatel`, methods `setCurrentUser()`, `getCurrentUser()`, `logout()`

3. `src/main/java/sk/medicore/util/SceneManager.java` — helper to switch JavaFX scenes, method `switchTo(Stage stage, String fxmlPath)`

4. `src/main/java/sk/medicore/model/Pouzivatel.java` — abstract class with fields: id, meno, priezvisko, email, hesloHash, typ

5. `src/main/java/sk/medicore/model/Pacient.java` — extends Pouzivatel

6. `src/main/java/sk/medicore/model/Lekar.java` — extends Pouzivatel, extra fields: specializacia, pracoviskoId

7. `src/main/java/sk/medicore/model/AdministrativnyPracovnik.java` — extends Pouzivatel

8. `src/main/java/sk/medicore/model/Procedura.java` — fields: id, nazov, trvanieMin, popis

9. `src/main/java/sk/medicore/model/Termin.java` — fields: id, lekarId, datumCas (LocalDateTime), trvanieMin, stav (enum: DOSTUPNY/REZERVOVANY/ZRUSENY)

10. `src/main/java/sk/medicore/model/Rezervacia.java` — fields: id, pacientId, lekarId, terminId, proceduraId, stav (enum: POTVRDENA/ZRUSENA/PRESUNUTÁ), vytvorenaAt

11. `src/main/java/sk/medicore/db/DatabaseManager.java` — SQLite connection (file: medicore.db), creates all tables on init, seeds test data (2 doctors, test patient with email pacient@medicore.sk and password heslo123)

12. `src/main/java/sk/medicore/db/dao/PouzivatelDAO.java` — methods: `findByEmail(String email)`, `insert(Pouzivatel p)`, `emailExists(String email)`

13. `src/main/java/sk/medicore/db/dao/RezervaciaDAO.java` — methods: `findByPacientId(int id)`, `insert(Rezervacia r)`, `updateStav(int id, String stav)`, `findById(int id)`

14. `src/main/java/sk/medicore/db/dao/TerminDAO.java` — methods: `findByLekarId(int id)`, `findAvailable(int lekarId)`, `updateStav(int id, String stav)`

15. `src/main/java/sk/medicore/db/dao/ProceduraDAO.java` — methods: `findAll()`, `findByLekarId(int lekarId)`

16. `src/main/java/sk/medicore/db/dao/LekarDAO.java` — methods: `findAll()`, `findByProceduraId(int proceduraId)`, `findById(int id)`

17. `src/main/resources/view/prihlasenie.fxml` + `src/main/java/sk/medicore/controller/PrihlasenieController.java` — Login screen matching Figma (email + password fields, login button, link to register). On success load dashboard.fxml, on fail show error label.

18. `src/main/resources/view/registracia.fxml` + `src/main/java/sk/medicore/controller/RegistraciaController.java` — Registration screen matching Figma. Validate fields, check email uniqueness, hash password, insert into DB.

19. `src/main/java/sk/medicore/Main.java` — JavaFX Application, calls DatabaseManager.init(), loads prihlasenie.fxml as first scene, window title "MediCORE", size 900x650.

After all files are created, tell me how to run it and what to expect.

Style requirements:
- All user-facing text in Slovak with diacritics
- Primary color #1a9e8f (teal) matching Figma
- Clean, minimal UI — white cards on gray background
- No external libraries beyond what's in pom.xml (JavaFX 21 + sqlite-jdbc)

---

## After the first prompt works (app launches and login works), send this as second prompt:

Implement the patient dashboard and My Appointments screen:

1. `src/main/resources/view/dashboard.fxml` + `DashboardController.java` — Shows greeting with patient name, 3 stat cards (Upcoming/Completed/Cancelled counts from DB), list of upcoming reservations with Doctor name, specialty, procedure, date/time, location, Reschedule and Cancel buttons. Navigation sidebar with: Dashboard, Moje rezervácie, Rezervovať termín, Profil, Odhlásiť sa.

2. `src/main/resources/view/moje-rezervacie.fxml` + `MojeRezervacieController.java` — Tabbed view (Nadchádzajúce / Minulé / Zrušené). Each reservation card shows doctor, specialty, procedure, date, time, location, status badge. Cancel button on upcoming ones triggers UC7 confirmation dialog.

3. Implement UC7 cancel flow — confirmation dialog "Naozaj chcete zrušiť rezerváciu?", on confirm: update rezervacia.stav to ZRUSENA and termin.stav to DOSTUPNY in DB, refresh list.

## Third prompt (booking wizard UC5):

Implement the Book Appointment 4-step wizard (UC5):

`src/main/resources/view/rezervacia-wizard.fxml` + `RezervaciaWizardController.java`

Step 1: Grid of procedure cards (load from DB), each card shows name, duration, description, "Vybrať" button
Step 2: List of doctors for selected procedure (load from DB), each shows name, specialty, "Vybrať" button  
Step 3: Grid of available time slots for selected doctor (load from DB, only DOSTUPNY), each shows date and time, "Vybrať" button
Step 4: Confirmation summary — show selected procedure, doctor, date/time, "Potvrdiť rezerváciu" button

On confirm: insert into rezervacie table, update termin.stav to REZERVOVANY, navigate to dashboard.
