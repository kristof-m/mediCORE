# MediCORE — Demo Guide

**Všetky účty používajú heslo: `heslo123`**

> **Reset dát:** Vymaž `medicore.db` z koreňa projektu a spusti znova — všetko sa obnoví.

---

## Pacienti

| Meno | E-mail | Predpripravené dáta |
|------|--------|---------------------|
| Samuel Thompson | `pacient@medicore.sk` | 4 absolvované, 1 zrušená, **1 dnes** (Johnson 09:00), 2 nadchádzajúce |
| Lukáš Novák | `novak@medicore.sk` | 2 absolvované, 1 zrušená, **1 dnes** (Žilková 09:00), 1 nadchádzajúca |
| Eva Kozárová | `kozarova@medicore.sk` | 2 absolvované, **1 dnes** (Parker 10:00), 2 nadchádzajúce |
| Tomáš Blaho | `blaho@medicore.sk` | 1 absolvovaná, 1 zrušená, **1 dnes** (Chen 09:00), 1 nadchádzajúca |
| Nina Horváthová | `horvath@medicore.sk` | 2 absolvované, **1 dnes** (Johnson 11:00), 1 nadchádzajúca |
| Mária Kováčová | `kovacova@medicore.sk` | 2 absolvované, 1 zrušená, **1 dnes** (Chen 11:00), 2 nadchádzajúce |
| Peter Horník | `hornik@medicore.sk` | 2 absolvované, 1 zrušená, **1 dnes** (Novotný 10:00), 2 nadchádzajúce |
| Jana Procházková | `prochazka@medicore.sk` | 3 absolvované, 1 zrušená, **1 dnes** (Horvat 09:00), 1 nadchádzajúca |
| Martin Oravec | `oravec@medicore.sk` | 2 absolvované, 1 zrušená, **1 dnes** (Novotný 15:00), 2 nadchádzajúce |
| Zuzana Malá | `mala@medicore.sk` | 3 absolvované, **1 dnes** (Parker 14:00), 2 nadchádzajúce |

---

## Lekári

| Meno | E-mail | Špecializácia | Ambulancia |
|------|--------|---------------|------------|
| Dr. Sarah Johnson | `johnson@medicore.sk` | Kardiológia | Budova A / 1. posch. / Izba 101 |
| Dr. Emily Parker | `parker@medicore.sk` | Dermatológia | Budova B / 2. posch. / Izba 204 |
| Dr. Michael Chen | `chen@medicore.sk` | Všeobecná prax | Budova C / 1. posch. / Izba 110 |
| Dr. Jakub Novotný | `novotny@medicore.sk` | Neurológia | Budova A / 2. posch. / Izba 212 |
| Dr. Andrea Horvat | `horvat@medicore.sk` | Ortopédia | Budova B / 1. posch. / Izba 105 |
| Dr. Petra Žilková | `zilkova@medicore.sk` | Oftalmológia | Budova C / 2. posch. / Izba 215 |

**Dnešné zaťaženie kliniky (dnes):**

| Lekár | Pacienti dnes |
|-------|--------------|
| Dr. Johnson | Samuel Thompson 09:00, Nina Horváthová 11:00 |
| Dr. Parker | Eva Kozárová 10:00, Zuzana Malá 14:00 |
| Dr. Chen | Tomáš Blaho 09:00, Mária Kováčová 11:00 |
| Dr. Novotný | Peter Horník 10:00, Martin Oravec 15:00 |
| Dr. Horvat | Jana Procházková 09:00 |
| Dr. Žilková | Lukáš Novák 09:00 |

---

## Admin

| Meno | E-mail |
|------|--------|
| Admin Polyklinika | `admin@medicore.sk` |

Admin vidí:
- Dashboard s **celoklinickými** štatistikami (všetci lekári, celkovo 9 rezervácií dnes)
- Kalendár s prepínačom lekára — môže prechádzať rozvrh ľubovoľného lekára

---

## Procedúry v systéme

| Procedúra | Trvanie | Lekár(i) |
|-----------|---------|----------|
| Kardiologická prehliadka | 45 min | Johnson |
| EKG | 20 min | Johnson |
| Dermatologická prehliadka | 30 min | Parker |
| Kožné vyšetrenie | 30 min | Parker |
| Všeobecná konzultácia | 30 min | Chen |
| Neurologické vyšetrenie | 45 min | Novotný |
| Ortopedická konzultácia | 30 min | Horvat |
| Meranie zraku | 20 min | Žilková |
| Krvné testy | 15 min | všetci lekári |

---

## Demo scenáre

### UC1 – Registrácia nového pacienta
Klikni **Registrovať sa** na prihlasovacej obrazovke. Vytvor nový účet — systém automaticky prihlási a presmeruje na dashboard.

### UC2 – Zabudnuté heslo
Na prihlasovacej obrazovke klikni **Zabudli ste heslo?** Zadaj e-mail existujúceho pacienta (napr. `pacient@medicore.sk`).

### UC3 – Rezervovať termín
Prihlás sa ako ľubovoľný pacient → **Rezervovať termín** (4-krokový wizard: procedúra → lekár → termín → potvrdenie).

### UC4 – Zobraziť moje rezervácie
Prihlás sa ako pacient (napr. `pacient@medicore.sk`) → sekcia **Moje rezervácie** — vidíš 3 záložky: Nadchádzajúce / Minulé / Zrušené.

### UC5 – Zobraziť kalendár pacienta
Sekcia **Kalendár** v pacientskom rozhraní — mesačný pohľad s vyznačenými dňami.

### UC6 – Presunúť rezerváciu
V **Moje rezervácie** alebo **Dashboard** klikni **Presunúť** pri nadchádzajúcej rezervácii.

### UC7 – Zrušiť rezerváciu
Klikni **Zrušiť** pri rezervácii — systém blokuje zrušenie menej ako 24 hodín pred termínom.

### UC8 – Lekár: pridanie termínu
Prihlás sa ako lekár → **Definovať termíny** → pridaj nový termín s dátumom a časom.

### UC9 – Lekár: úprava termínu
V **Definovať termíny** klikni **Upraviť** pri existujúcom termíne.

### UC10 – Admin: správa kalendára
Prihlás sa ako `admin@medicore.sk` → **Kalendár** → vyber lekára z rozbaľovacieho menu.

### Notifikácie
Po každej akcii (rezervácia, presun, zrušenie) sa pacientovi zobrazí notifikácia v hornej časti dashboardu. Odmietnuť ich môžeš tlačidlom **Zatvoriť notifikácie**.
