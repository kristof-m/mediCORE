# MediCORE — UI Screens (Figma)

## Design Language
- Primary color: teal `#1a9e8f` (buttons, active nav, accents)
- Background: light gray `#f5f5f5`
- Cards: white with subtle shadow
- Sidebar navigation (desktop layout)
- Font: clean sans-serif
- Logo: stethoscope icon + "MediCORE" text

---

## Screen 1 — Registrácia (UC1)
**Layout:** Centered card on gradient background (teal to blue)  
**Fields:**
- First Name / Last Name
- Email
- Password (with show/hide toggle)
- Confirm Password (with show/hide toggle)
- "I agree to Terms of Service and Privacy Policy" checkbox
- "Create account" button (full-width teal)
- "Already have an account? Log in" link at bottom

---

## Screen 2 — Prihlásenie (UC2)
**Layout:** Similar centered card  
**Fields:**
- Email
- Password (with show/hide)
- "Forgot password?" link
- "Sign in" button
- "Don't have an account? Register" link

---

## Screen 3 — Patient Dashboard (UC3 partial)
**Layout:** Left sidebar + main content area  
**Sidebar:** Logo, user avatar + name, nav links (Dashboard, My Appointments, Book Appointment, Profile), Log out  
**Main content:**
- Greeting: "Good evening, [Name]!"
- 3 stat cards: Upcoming (count), Completed (count), Cancelled (count)
- "Upcoming Appointments" list with cards showing:
  - Doctor avatar circle + name + specialty
  - Procedure name
  - Date, Time, Location (Building + Room)
  - "Reschedule" + "Cancel" buttons
- "Need help?" section at bottom

---

## Screen 4 — My Appointments (UC4)
**Layout:** Same sidebar  
**Main content:**
- Title "My Appointments"
- "Book New Appointment" button (top right)
- Tabs: Upcoming (N) | Past (N) | Cancelled (N)
- Appointment cards per tab:
  - Status badge (Upcoming = teal, Completed = green, Cancelled = gray)
  - Doctor name + specialty
  - Procedure
  - Date, Time, Location
  - "View Details" button + "Reschedule"/"Book Again" depending on tab

---

## Screen 5 — Book Appointment Wizard (UC5)
**Layout:** Full page, no sidebar  
**Step indicator:** 4 steps at top
- Step 1: Select Procedure
- Step 2: Select Doctor  
- Step 3: Select Time Slot
- Step 4: Confirm Booking

**Step 1 — Select Procedure:**
- Search bar "Search procedures or specialties..."
- Grid of procedure cards, each showing:
  - Icon + specialty badge (top right)
  - Procedure name (bold)
  - Description
  - Duration (clock icon + minutes)
  - "Select →" link

**Procedure examples from Figma:**
- General Consultation — 30 min — General Practice
- Cardiology Checkup — 45 min — Cardiology
- Physical Therapy — 60 min — Physiotherapy
- Eye Examination — 30 min — Ophthalmology
- Mental Health Consultation — 60 min — Psychology
- Minor Surgery Consultation — 45 min — Surgery
- Blood Work & Lab Tests — 20 min — Laboratory
- Prescription Refill — 15 min — Pharmacy

---

## Screen 6 — Doctor Calendar / Manage Availability (UC8, UC9)
**Layout:** Sidebar + calendar view  
**Main content:** Doctor's weekly/monthly calendar with time slots  
**Actions:** Add slot, Edit slot, Delete slot

---

## Implementation Notes
- Use JavaFX FXML for layout
- Match teal color `#1a9e8f` as primary
- Sidebar should be a shared component reused across screens
- Navigate between screens by swapping the Scene or using a StackPane
- For the wizard (UC5), use a single FXML with a step controller that shows/hides panes
