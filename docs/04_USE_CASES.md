# MediCORE — Use Cases

## UC1 — Registrácia používateľa (Mišo)
**Aktér:** Pacient  
**Predpodmienky:** Používateľ nemá účet  
**Hlavný tok:**
1. Používateľ otvorí registračnú stránku
2. Systém zobrazí formulár
3. Používateľ zadá meno, priezvisko, email, heslo
4. Používateľ potvrdí registráciu
5. Systém overí správnosť a úplnosť údajov
6. Systém vytvorí účet v databáze
7. Systém informuje o úspešnej registrácii

**Alternatívne toky:**
- `5a` Neplatné údaje → chybové hlásenie → späť na krok 3
- `5b` Email už existuje → informuj → odporuč prihlásenie

---

## UC2 — Prihlásenie používateľa (Marek)
**Aktér:** Pacient, Lekár, AdministratívnyPracovník  
**Predpodmienky:** Účet existuje  
**Hlavný tok:**
1. Používateľ otvorí prihlasovaciu stránku
2. Systém zobrazí formulár
3. Používateľ zadá email a heslo
4. Používateľ potvrdí prihlásenie
5. Systém overí údaje v databáze
6. Systém prihlási a presmeruje na hlavnú stránku

**Alternatívne toky:**
- `3a` Zabudnuté heslo → zadaj email → pošli odkaz na obnovu
- `5a` Nesprávne údaje → chybové hlásenie

---

## UC3 — Zobrazenie kalendára (Mišo)
**Aktér:** Pacient  
**Predpodmienky:** Používateľ je prihlásený  
**Hlavný tok:**
1. Používateľ otvorí sekciu kalendára
2. Systém zobrazí zoznam lekárov/ambulancií
3. Používateľ vyberie lekára/ambulanciu
4. Systém načíta kalendár
5. Systém zobrazí dostupné a rezervované termíny
6. Používateľ prechádza medzi dňami/týždňami

**Alternatívne toky:**
- `4a` Kalendár neobsahuje termíny → informuj

---

## UC4 — Manipulácia s rezerváciou (Samuel)
**Aktér:** Pacient  
**Predpodmienky:** Pacient je prihlásený, má aspoň jednu aktívnu rezerváciu  
**Hlavný tok:**
1. Pacient otvorí sekciu svojich rezervácií
2. Systém zobrazí zoznam aktívnych rezervácií
3. Pacient vyberie rezerváciu
4. Systém zobrazí dostupné akcie (presunúť / zrušiť)
5. Pacient vyberie akciu
6. Systém spustí príslušný UC (UC6 alebo UC7)

**Alternatívne toky:**
- `2a` Žiadne aktívne rezervácie → informuj → presmeruj na hlavnú stránku

---

## UC5 — Rezervácia termínu vyšetrenia (Samuel)
**Aktér:** Pacient  
**Predpodmienky:** Pacient je prihlásený  
**Hlavný tok (4-krokový wizard):**
1. Pacient otvorí sekciu rezervácie termínu
2. Systém zobrazí zoznam dostupných procedúr/vyšetrení
3. Pacient vyberie procedúru
4. Systém zobrazí dostupných lekárov pre danú procedúru
5. Pacient vyberie lekára
6. Systém zobrazí dostupné termíny vybraného lekára
7. Pacient vyberie termín
8. Systém overí dostupnosť termínu
9. Systém vytvorí rezerváciu a uloží do databázy
10. Systém zobrazí potvrdenie

**Alternatívne toky:**
- `3a` Žiadni lekári pre procedúru → informuj → späť na krok 3
- `6a` Lekár nemá voľné termíny → informuj → späť na krok 5
- `8a` Termín medzičasom obsadený → informuj → späť na krok 6

---

## UC6 — Presun rezervovaného termínu (Peťo)
**Aktér:** Pacient  
**Predpodmienky:** Pacient má existujúcu rezerváciu  
**Hlavný tok:**
1. Pacient vyberie rezerváciu na presun
2. Systém zobrazí dostupné alternatívne termíny
3. Pacient vyberie nový termín
4. Systém overí dostupnosť
5. Systém aktualizuje rezerváciu
6. Systém zobrazí potvrdenie zmeny

**Alternatívne toky:**
- `2a` Žiadne alternatívne termíny → informuj → koniec

---

## UC7 — Zrušenie rezervácie (Mišo)
**Aktér:** Pacient  
**Predpodmienky:** Pacient má existujúcu rezerváciu  
**Hlavný tok:**
1. Pacient otvorí zoznam rezervácií
2. Systém zobrazí aktívne rezervácie
3. Pacient vyberie rezerváciu na zrušenie
4. Systém overí oprávnenosť zrušenia (pravidlá rezervácie)
5. Systém zobrazí potvrdzovaciu výzvu
6. Pacient potvrdí zrušenie
7. Systém odstráni rezerváciu
8. Systém zobrazí potvrdenie

**Alternatívne toky:**
- `4a` Zrušenie nie je povolené → informuj → koniec
- `6a` Pacient nepotvrdí → rezervácia zostáva

---

## UC8 — Definovanie dostupných termínov lekárom (Marek)
**Aktér:** Lekár  
**Predpodmienky:** Lekár je prihlásený  
**Hlavný tok:**
1. Lekár zvolí pridať/upraviť dostupné termíny
2. Lekár zadá obdobie dostupnosti, čas začiatku/konca, typ procedúr
3. Lekár potvrdí vytvorenie/úpravu
4. Systém skontroluje konflikty v kalendári
5. Systém uloží termíny a procedúry
6. Systém sprístupní termíny pacientom

**Alternatívne toky:**
- `4a` Konflikt s existujúcim termínom → upozorni lekára → späť na krok 1

---

## UC9 — Správa kalendára lekára (Peťo)
**Aktér:** Lekár / AdministratívnyPracovník  
**Predpodmienky:** Prihlásený  
**Hlavný tok:**
1. Lekár vyberie termín na úpravu
2. Lekár upraví alebo odstráni termín
3. Systém aktualizuje kalendár

**Alternatívne toky:**
- `1a` Termín má rezerváciu → upozorni na konflikt → lekár rozhodne či pokračovať
- `2a` Lekár zruší zmenu → kalendár zostáva nezmenený
