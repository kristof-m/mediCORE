# MediCORE — Implementation Priorities

## Must Have (core UC flows for submission)
1. **UC1** Registration screen + DB insert
2. **UC2** Login screen + authentication
3. **UC4** My Appointments list + Cancel/Reschedule buttons
4. **UC5** Book Appointment 4-step wizard (full flow)
5. **UC7** Cancel reservation (with confirmation dialog)
6. **UC6** Reschedule reservation (select new slot)

## Nice to Have
7. **UC3** Calendar view (doctor's availability overview)
8. **UC8** Doctor — define availability slots
9. **UC9** Doctor — manage calendar

## Not Required for Prototype
- Email sending (mock/skip forgot password)
- Admin panel
- Notifications

---

## Screen Navigation Flow
```
Login ──────────────────────────────────► Dashboard (Pacient)
  │                                            │
  │                                     ┌──────┴───────┐
  ▼                                     ▼              ▼
Register                         My Appointments   Book Appointment
                                        │               │
                                  ┌─────┴─────┐    Step 1: Procedure
                                  ▼           ▼    Step 2: Doctor
                               Cancel    Reschedule  Step 3: Time Slot
                                              │    Step 4: Confirm
                                         (UC6 flow)
```

## Architecture Pattern
- **MVC** — Model (model/), View (FXML), Controller (controller/)
- **DAO pattern** — all DB access goes through DAO classes
- **SessionManager** — singleton holding current logged-in `Pouzivatel`
- **SceneManager** (utility) — helper to switch between FXML scenes

## Key Implementation Notes
- Never put SQL in controllers — always use DAOs
- `SessionManager.getInstance().getCurrentUser()` gives the logged-in user
- Check `user.getTyp()` to determine role and show correct UI
- All user-facing strings in Slovak with diacritics
- Password: SHA-256 hash before storing/comparing
- Date format for display: `dd.MM.yyyy HH:mm`
