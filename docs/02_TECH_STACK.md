# MediCORE — Tech Stack & Project Setup

## Stack
- **Language:** Java 21 (project uses OpenJDK 26, compatible)
- **UI:** JavaFX 21.0.2
- **Database:** SQLite via `org.xerial:sqlite-jdbc:3.45.1.0`
- **Build:** Maven
- **IDE:** IntelliJ IDEA 2025.3.2

## pom.xml Dependencies (already configured)
```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.45.1.0</version>
    </dependency>
</dependencies>
```

## Project Structure
```
src/main/java/sk/medicore/
├── Main.java                        # JavaFX Application entry point
├── model/
│   ├── Pouzivatel.java              # Abstract base class
│   ├── Pacient.java
│   ├── Lekar.java
│   ├── AdministrativnyPracovnik.java
│   ├── Rezervacia.java
│   ├── Termin.java
│   ├── Procedura.java
│   ├── Kalendar.java
│   └── Pracovisko.java
├── db/
│   ├── DatabaseManager.java         # SQLite connection + schema init + seed data
│   └── dao/
│       ├── PouzivatelDAO.java
│       ├── PacientDAO.java
│       ├── LekarDAO.java
│       ├── RezervaciaDAO.java
│       ├── TerminDAO.java
│       └── ProceduraDAO.java
├── controller/
│   ├── PrihlasenieController.java
│   ├── RegistraciaController.java
│   ├── DashboardController.java
│   ├── MojeRezervacieController.java
│   ├── RezervaciaWizardController.java
│   └── LekarKalendarController.java
└── util/
    ├── PasswordUtil.java            # SHA-256 hashing
    ├── SessionManager.java          # Current logged-in user singleton
    └── ValidationUtil.java

src/main/resources/
└── view/
    ├── prihlasenie.fxml
    ├── registracia.fxml
    ├── dashboard.fxml
    ├── moje-rezervacie.fxml
    ├── rezervacia-wizard.fxml
    └── lekar-kalendar.fxml
```

## Database File
SQLite creates `medicore.db` in the project root on first run. No setup needed.

## Running the App
Main class: `sk.medicore.Main`
In IntelliJ: Run → Edit Configurations → Application → Main class: `sk.medicore.Main`

## Test Credentials (seeded on first run)
| Role | Email | Password |
|------|-------|----------|
| Pacient | pacient@medicore.sk | heslo123 |
| Lekár | johnson@medicore.sk | heslo123 |
| Lekár | parker@medicore.sk | heslo123 |
