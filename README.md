# MediCORE

Rezervačný systém pre ambulantnú polykliniku — školský projekt PSI, FIIT STU Bratislava, 2025/26.

## Tím

| Meno | UC |
|------|----|
| Marek Kujan-Privrel | UC2, UC8 |
| Samuel Krechňák | UC4, UC5, kódová implementácia |
| Michael Krištof | UC1, UC3, UC7 |
| Peter Martišovič | UC6, UC9 |

---

## Požiadavky

| Nástroj | Verzia |
|---------|--------|
| Java (JDK) | 21 alebo novší |
| IntelliJ IDEA | 2024+ (Community alebo Ultimate) |
| Maven | zabudovaný v IntelliJ — **nie je potrebný samostatný** |

> **Java 21 stiahnutie:** https://adoptium.net/  
> Vyber „Temurin 21 (LTS)" → tvoj operačný systém.

---

## Inštalácia a spustenie

### 1. Klonovanie repozitára

```bash
git clone https://github.com/kristof-m/mediCORE.git
cd mediCORE
```

### 2. Otvorenie v IntelliJ IDEA

1. Spusti IntelliJ IDEA
2. **File → Open** → vyber priečinok `mediCORE`
3. IntelliJ automaticky rozpozná Maven projekt a stiahne závislosti (JavaFX, SQLite)
4. Počkaj kým sa stiahnu (progress bar v spodnej lište)

### 3. Spustenie aplikácie

**Odporúčaná metóda — Maven Tool Window:**

1. Otvor Maven panel: `View → Tool Windows → Maven`
2. Rozbaliť: `mediCORE → Plugins → javafx`
3. Dvojklik na **`javafx:run`**

Alternatívne — ak máš Maven nainštalovaný samostatne:
```bash
mvn javafx:run
```

> **Konfigurácia `MediCORE` je už pripravená** — klikni na zelený ▶ v pravom hornom rohu (IntelliJ ju načíta z `.idea/runConfigurations/MediCORE.xml`).

### 4. Prvé spustenie

Pri prvom spustení sa automaticky vytvorí súbor `medicore.db` v koreňovom priečinku projektu so všetkými tabuľkami a testnými dátami. Nie je potrebná žiadna inštalácia databázy.

> **Reset databázy:** Ak chceš dáta obnoviť do pôvodného stavu, vymaž `medicore.db` z koreňa projektu a spusti aplikáciu znovu.

---

## Testovacie prihlasovacie údaje

Všetky účty, demo scenáre a prehľad predpripravených dát nájdeš v **[DEMO.md](DEMO.md)**.

Skrátene: heslo je `heslo123` pre všetky účty. Systém obsahuje **30 pacientov**, **15 lekárov** a **1 admin** účet.

---

## Tok aplikácie

### Pacient

```
Prihlásenie ──► Dashboard
                   │
       ┌───────────┼──────────────┬────────────┐
       ▼           ▼              ▼            ▼
 Moje rezerv.  Rezervovať    Kalendár      Profil
       │        termín
 Zrušiť rez.  (4-krokový wizard:
               Lekár → Procedúra → Termín → Potvrdenie)
```

### Lekár

```
Prihlásenie ──► Dashboard (dnešné rezervácie, štatistiky)
                   │
         ┌─────────┼─────────────┐
         ▼         ▼             ▼
    Môj kalendár  Definovať    Moji pacienti
    (týždenný)    termíny      (pripravujeme)
```

### Administrátor

```
Prihlásenie ──► Dashboard (celoklinické štatistiky, všetci lekári)
                   │
              ┌────┘
              ▼
           Kalendár (výber lekára → týždenný pohľad)
```

---

## Štruktúra projektu

```
src/main/java/sk/medicore/
├── Main.java                             # Vstupný bod aplikácie
├── model/                                # Dátové triedy
│   ├── Pouzivatel.java                   # Abstraktná základná trieda
│   ├── Pacient.java
│   ├── Lekar.java
│   ├── Rezervacia.java
│   ├── Termin.java
│   └── Procedura.java
├── db/
│   ├── DatabaseManager.java              # SQLite pripojenie, schéma, seed dáta
│   └── dao/                              # Data Access Objects
│       ├── PouzivatelDAO.java
│       ├── LekarDAO.java
│       ├── RezervaciaDAO.java
│       ├── TerminDAO.java
│       └── ProceduraDAO.java
├── controller/                           # JavaFX kontroléry (MVC)
│   ├── PrihlasenieController.java
│   ├── RegistraciaController.java
│   ├── SidebarPacientController.java     # Zdieľaný sidebar — pacient
│   ├── SidebarLekarController.java       # Zdieľaný sidebar — lekár
│   ├── SidebarAdminController.java       # Zdieľaný sidebar — admin
│   ├── DashboardController.java
│   ├── MojeRezervacieController.java
│   ├── PatientKalendarController.java
│   ├── ProfilController.java
│   ├── RezervaciaWizardController.java
│   ├── LekarDashboardController.java
│   ├── LekarKalendarController.java
│   ├── LekarTerminyController.java
│   ├── AdminDashboardController.java
│   └── AdminKalendarController.java
└── util/
    ├── PasswordUtil.java                 # SHA-256 hashovanie hesiel
    ├── SessionManager.java               # Singleton — prihlásený používateľ
    ├── SceneManager.java                 # Prepínanie JavaFX scén
    └── DateUtil.java                     # Slovenské formáty dátumov

src/main/resources/view/                 # FXML obrazovky
├── prihlasenie.fxml
├── registracia.fxml
├── sidebar-pacient.fxml                  # Zdieľaný sidebar komponent — pacient
├── sidebar-lekar.fxml                    # Zdieľaný sidebar komponent — lekár
├── sidebar-admin.fxml                    # Zdieľaný sidebar komponent — admin
├── dashboard.fxml
├── moje-rezervacie.fxml
├── patient-kalendar.fxml
├── profil.fxml
├── rezervacia-wizard.fxml
├── lekar-dashboard.fxml
├── lekar-kalendar.fxml
├── lekar-terminy.fxml
├── admin-dashboard.fxml
└── admin-kalendar.fxml
```

---

## Časté problémy

### `Module javafx.controls not found`

IntelliJ vygeneroval zlý `--module-path` v konfigurácii spúšťania. Použi namiesto toho Maven:

1. `View → Tool Windows → Maven`
2. `mediCORE → Plugins → javafx → javafx:run`

### `medicore.db` je poškodená / chybné dáta

Vymaž súbor `medicore.db` z koreňa projektu a znovu spusti — databáza sa nanovo vytvorí so seed dátami.

### Java verzia nižšia ako 21

`File → Project Structure → Project → SDK` — nastav JDK 21+.

---

## Stack

- **Java 21** + **JavaFX 21.0.2** (FXML, Controls)
- **SQLite** cez `sqlite-jdbc 3.45.1.0`
- **Maven** (build + dependency management)
- Bez ORM — čistý JDBC + DAO vzor
- Bez externých UI knižníc
