# MediCORE — Demo Guide

**Všetky účty používajú heslo: `heslo123`**

> **Reset dát:** Vymaž `medicore.db` z koreňa projektu a spusti znova — všetko sa obnoví.

---

## Prehľad dát

| Entita | Počet |
|--------|-------|
| Pacienti | 30 |
| Lekári | 15 |
| Admin | 1 |
| Pracoviská | 15 |
| Procedúry | 20 |
| Termíny | ~1 200 (60 minulých + 30 dnešných + ~1 100 budúcich) |
| Rezervácie | ~150 (60 minulých + 30 dnešných + 60 budúcich) |

---

## Lekári

| # | Meno | E-mail | Špecializácia | Ambulancia |
|---|------|--------|---------------|------------|
| 1 | Dr. Mária Tóthová | `tothova.maria@medicore.sk` | Kardiológia | Budova A / 1. p. / 101 |
| 2 | Dr. Peter Kováč | `kovac.peter@medicore.sk` | Kardiológia | Budova A / 1. p. / 102 |
| 3 | Dr. Jana Némethová | `nemethova.jana@medicore.sk` | Dermatológia | Budova B / 2. p. / 204 |
| 4 | Dr. Michal Horváth | `horvath.michal@medicore.sk` | Všeobecná prax | Budova C / 1. p. / 110 |
| 5 | Dr. Zuzana Krajčíková | `krajcikova.zuzana@medicore.sk` | Všeobecná prax | Budova C / 1. p. / 112 |
| 6 | Dr. Tomáš Szabó | `szabo.tomas@medicore.sk` | Neurológia | Budova A / 2. p. / 212 |
| 7 | Dr. Andrej Baláž | `balaz.andrej@medicore.sk` | Ortopédia | Budova B / 1. p. / 105 |
| 8 | Dr. Eva Molnárová | `molnarova.eva@medicore.sk` | Oftalmológia | Budova C / 2. p. / 215 |
| 9 | Dr. Martin Varga | `varga.martin@medicore.sk` | Interná medicína | Budova D / 1. p. / 101 |
| 10 | Dr. Róbert Fekete | `fekete.robert@medicore.sk` | Chirurgia | Budova B / 2. p. / 210 |
| 11 | Dr. Katarína Hudáková | `hudakova.katarina@medicore.sk` | ORL | Budova A / 3. p. / 301 |
| 12 | Dr. Juraj Polák | `polak.juraj@medicore.sk` | Urológia | Budova D / 2. p. / 205 |
| 13 | Dr. Lenka Benčíková | `bencikova.lenka@medicore.sk` | Gynekológia | Budova C / 3. p. / 310 |
| 14 | Dr. Soňa Kučerová | `kucerova.sona@medicore.sk` | Pediatria | Budova D / 1. p. / 108 |
| 15 | Dr. Dušan Mazúr | `mazur.dusan@medicore.sk` | Pneumológia | Budova A / 3. p. / 305 |

Každý lekár má 2 pacientov dnes (09:00 a 14:00), 4 absolvované návštevy v histórii a ~70–80 budúcich termínov na nasledujúcich 10 pracovných dní.

---

## Pacienti

| # | Meno | E-mail |
|---|------|--------|
| 1 | Ján Novák | `novak.jan@medicore.sk` |
| 2 | Mária Horváthová | `horvathova.maria@medicore.sk` |
| 3 | Peter Sloboda | `sloboda.peter@medicore.sk` |
| 4 | Eva Štefanková | `stefankova.eva@medicore.sk` |
| 5 | Tomáš Holub | `holub.tomas@medicore.sk` |
| 6 | Zuzana Vlčková | `vlckova.zuzana@medicore.sk` |
| 7 | Martin Rusnák | `rusnak.martin@medicore.sk` |
| 8 | Jana Ondrejčíková | `ondrejcikova.jana@medicore.sk` |
| 9 | Lukáš Kamenický | `kamenicky.lukas@medicore.sk` |
| 10 | Katarína Hrušovská | `hrusovska.katarina@medicore.sk` |
| 11 | Andrej Sedlák | `sedlak.andrej@medicore.sk` |
| 12 | Lenka Záborská | `zaborska.lenka@medicore.sk` |
| 13 | Michal Straka | `straka.michal@medicore.sk` |
| 14 | Nina Bartoňová | `bartonova.nina@medicore.sk` |
| 15 | Dušan Mikuš | `mikus.dusan@medicore.sk` |
| 16 | Soňa Kráľovičová | `kralovicova.sona@medicore.sk` |
| 17 | Róbert Jurčík | `jurcik.robert@medicore.sk` |
| 18 | Monika Šimková | `simkova.monika@medicore.sk` |
| 19 | Štefan Dolinský | `dolinsky.stefan@medicore.sk` |
| 20 | Iveta Pálková | `palkova.iveta@medicore.sk` |
| 21 | Richard Kučera | `kucera.richard@medicore.sk` |
| 22 | Alena Blahová | `blahova.alena@medicore.sk` |
| 23 | Vladimír Gašpar | `gaspar.vladimir@medicore.sk` |
| 24 | Daniela Ráczová | `raczova.daniela@medicore.sk` |
| 25 | Marek Kupčík | `kupcik.marek@medicore.sk` |
| 26 | Barbora Tkáčová | `tkacova.barbora@medicore.sk` |
| 27 | Filip Zelinka | `zelinka.filip@medicore.sk` |
| 28 | Lucia Havlíčková | `havlickova.lucia@medicore.sk` |
| 29 | Pavol Šoltés | `soltes.pavol@medicore.sk` |
| 30 | Simona Ďurišová | `durisova.simona@medicore.sk` |

Každý pacient má 1 dnešnú rezerváciu, niekoľko minulých (absolvované aj zrušené) a niekoľko budúcich.

### Odporúčané testovacie účty

| Účet | E-mail | Prečo |
|------|--------|-------|
| Ján Novák | `novak.jan@medicore.sk` | Bohaté dáta — minulé, dnešné aj budúce rezervácie u kardiológa |
| Zuzana Vlčková | `vlckova.zuzana@medicore.sk` | Minulé + dnešné u Němethov./Kováč |
| Martin Rusnák | `rusnak.martin@medicore.sk` | Zrušená rezervácia v histórii (demo UC7) |

---

## Admin

| Meno | E-mail |
|------|--------|
| Admin Polyklinika | `admin@medicore.sk` |

Admin vidí:
- Dashboard s **celoklinickými** štatistikami (30 rezervácií dnes naprieč 15 lekármi)
- Kalendár s prepínačom lekára — môže prechádzať rozvrh ľubovoľného lekára

---

## Procedúry (20)

| # | Procedúra | Trvanie | Kategória | Lekár(i) |
|---|-----------|---------|-----------|----------|
| 1 | Kardiologická prehliadka | 45 min | Kardiológia | Tóthová, Kováč |
| 2 | EKG | 20 min | Kardiológia | Tóthová |
| 3 | Echokardiografia | 30 min | Kardiológia | Kováč |
| 4 | Dermatologická prehliadka | 30 min | Dermatológia | Némethová |
| 5 | Dermatoskopia | 20 min | Dermatológia | Némethová |
| 6 | Všeobecná konzultácia | 30 min | Všeobecná prax | Horváth, Krajčíková |
| 7 | Preventívna prehliadka | 45 min | Všeobecná prax | Horváth, Krajčíková |
| 8 | Neurologické vyšetrenie | 45 min | Neurológia | Szabó |
| 9 | EEG | 30 min | Neurológia | Szabó |
| 10 | Ortopedická konzultácia | 30 min | Ortopédia | Baláž |
| 11 | Meranie zraku | 20 min | Oftalmológia | Molnárová |
| 12 | Očné vyšetrenie | 30 min | Oftalmológia | Molnárová |
| 13 | Interné vyšetrenie | 45 min | Interná medicína | Varga |
| 14 | Chirurgická konzultácia | 30 min | Chirurgia | Fekete |
| 15 | ORL vyšetrenie | 30 min | ORL | Hudáková |
| 16 | Urologické vyšetrenie | 30 min | Urológia | Polák |
| 17 | Gynekologická prehliadka | 30 min | Gynekológia | Benčíková |
| 18 | Pediatrické vyšetrenie | 30 min | Pediatria | Kučerová |
| 19 | Pneumologické vyšetrenie | 30 min | Pneumológia | Mazúr |
| 20 | Krvné testy | 15 min | Laboratórium | všetci okrem Benčíkovej |

---

## Pracoviská (15)

| # | Ambulancia | Budova | Poschodie | Miestnosť |
|---|-----------|--------|-----------|------------|
| 1 | Kardiologická ambulancia | Budova A | 1 | 101 |
| 2 | Kardiologická ambulancia II | Budova A | 1 | 102 |
| 3 | Dermatologická ambulancia | Budova B | 2 | 204 |
| 4 | Všeobecná ambulancia I | Budova C | 1 | 110 |
| 5 | Všeobecná ambulancia II | Budova C | 1 | 112 |
| 6 | Neurologická ambulancia | Budova A | 2 | 212 |
| 7 | Ortopedická ambulancia | Budova B | 1 | 105 |
| 8 | Oftalmologická ambulancia | Budova C | 2 | 215 |
| 9 | Interná ambulancia | Budova D | 1 | 101 |
| 10 | Chirurgická ambulancia | Budova B | 2 | 210 |
| 11 | ORL ambulancia | Budova A | 3 | 301 |
| 12 | Urologická ambulancia | Budova D | 2 | 205 |
| 13 | Gynekologická ambulancia | Budova C | 3 | 310 |
| 14 | Pediatrická ambulancia | Budova D | 1 | 108 |
| 15 | Pneumologická ambulancia | Budova A | 3 | 305 |

---

## Demo scenáre

### UC1 – Registrácia nového pacienta
Klikni **Registrovať sa** na prihlasovacej obrazovke. Vytvor nový účet — systém automaticky prihlási a presmeruje na dashboard.

### UC2 – Zabudnuté heslo
Na prihlasovacej obrazovke klikni **Zabudli ste heslo?** Zadaj e-mail existujúceho pacienta (napr. `novak.jan@medicore.sk`).

### UC3 – Rezervovať termín
Prihlás sa ako ľubovoľný pacient → **Rezervovať termín** (4-krokový wizard: procedúra → lekár → termín → potvrdenie). Dostupných je ~1 100 budúcich termínov u 15 lekárov.

### UC4 – Zobraziť moje rezervácie
Prihlás sa ako `novak.jan@medicore.sk` → **Moje rezervácie** — vidíš 3 záložky: Nadchádzajúce / Minulé / Zrušené.

### UC5 – Zobraziť kalendár pacienta
Sekcia **Kalendár** v pacientskom rozhraní — mesačný pohľad s vyznačenými dňami.

### UC6 – Presunúť rezerváciu
V **Moje rezervácie** alebo **Dashboard** klikni **Presunúť** pri nadchádzajúcej rezervácii.

### UC7 – Zrušiť rezerváciu
Klikni **Zrušiť** pri rezervácii — systém blokuje zrušenie menej ako 24 hodín pred termínom.

### UC8 – Lekár: pridanie termínu
Prihlás sa ako `tothova.maria@medicore.sk` → **Definovať termíny** → pridaj nový termín.

### UC9 – Lekár: úprava termínu
V **Definovať termíny** klikni **Upraviť** pri existujúcom termíne.

### UC10 – Admin: správa kalendára
Prihlás sa ako `admin@medicore.sk` → **Kalendár** → vyber lekára z rozbaľovacieho menu — vidíš rozvrh všetkých 15 lekárov.

### Notifikácie
Po každej akcii (rezervácia, presun, zrušenie) sa pacientovi zobrazí notifikácia v hornej časti dashboardu.
