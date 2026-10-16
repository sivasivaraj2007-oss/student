# INFO INSTITUTE OF ENGINEERING, COIMBATORE
## DEPARTMENT OF INFORMATION TECHNOLOGY (IT)
### Student Attendance Management System — 2nd Year IT (Academic Year: 2026-27)

A complete, production-ready Student Attendance Management System engineered for daily real-world college use.

---

## 1. Key Institutional Details

- **College Name:** INFO INSTITUTE OF ENGINEERING, COIMBATORE
- **Department:** Information Technology (IT)
- **Academic Class:** 2nd Year IT (2026-27)
- **Class Adviser:** Madheshvaran
- **Head of Department (HOD):** Palani
- **Time Zone:** Asia/Kolkata (`IST`)
- **Date Format:** `DD-MM-YYYY`
- **Theme:** Clean, academic blue-and-white theme

---

## 2. Core Curriculum Subjects (7 Total)

1. `MA3354` — Discrete Mathematics
2. `CS3301` — Data Structures
3. `CS3391` — Object-Oriented Programming (OOPS)
4. `IT3301` — Web Technology
5. `CS3351` — Computer Organization and Architecture (COA)
6. `HS3152` — English
7. `IT3311` — SDC (Skill Development Course)

---

## 3. Student Master List (56 Students)

The database includes exactly 56 student records with exact register numbers and names:
- `25BITT001` to `25BITT057` (with intentionally absent numbers preserved as specified)
- Lateral entry students: `25BIT301` (HARI PRASADH G) and `25BIT302` (PRADHEESH S)
- Student records are editable by authorized Administrators to allow spelling/registration corrections.

---

## 4. Role-Based Access Control (RBAC)

1. **System Administrator:**
   - Manage student master list (edit name, register number, add student, toggle active status).
   - Assign and reassign faculty to the 7 curriculum subjects.
   - Configure college rules: On-Duty (OD) calculation mode and low attendance alert threshold (default 75%).
   - Review complete attendance audit history.

2. **Faculty Member:**
   - Select curriculum subject and date (in `Asia/Kolkata` `DD-MM-YYYY`).
   - Mark students as **Present**, **Absent**, or **On Duty (OD)**.
   - Use "All Present" shortcut with review step.
   - Validation enforces that every student has an attendance status prior to saving.
   - **Duplicate Prevention:** Database unique index prevents double marking for the same subject on the same date.
   - Records are immediately persisted to the SQL database.

3. **Class Adviser — Madheshvaran:**
   - Complete class attendance overview.
   - Subject-wise and student-wise attendance reports.
   - Instant identification of students below threshold (< 75%).
   - Export CSV reports for official college registers.

4. **Head of Department (HOD) — Palani:**
   - Department-level and class-level dashboards.
   - Faculty subject conduction summaries and average percentages.
   - Trend analysis and student defaulter monitoring.

5. **Student Portal:**
   - View personalized attendance profile.
   - Overall attendance gauge and status badge.
   - Total classes conducted, attended, absent, and OD.
   - Subject-wise percentage breakdown.
   - Date-wise session history with status tags.
   - Anna University low attendance warning if < 75%.

---

## 5. Attendance Calculation & Rules

$$\text{Attendance Percentage} = \frac{\text{Classes Attended}}{\text{Total Conducted Classes}} \times 100$$

- **On Duty (OD) Handling:** OD is displayed separately. The administrator can configure whether OD counts towards attendance percentage or remains separate according to university/college regulations.
- **Audit Trail:** Corrections require a mandatory reason and log the timestamp, previous status, new status, and the user who authorized the change.

---

## 6. Database Files & Migrations

- **Android SQLite Persistence:** Built-in Room Database with DAOs, transactions, and foreign keys (`app/src/main/java/com/example/data/`).
- **PostgreSQL / Supabase Schema:** Full production SQL schema with Row Level Security (RLS) policies at `sql/schema.sql`.
- **Seed Script:** Production SQL seed script with all 56 students and 7 subjects at `sql/seed.sql`.

---

## 7. Verification Checklist

- [x] **56 Student Master Records:** Pre-populated and editable by Admin.
- [x] **7 Core Subjects:** Configured with faculty assignments.
- [x] **Duplicate Session Block:** Enforced by database constraint `(subjectId, date)`.
- [x] **Role Switching & Security:** Admin, Faculty, Adviser Madheshvaran, HOD Palani, and Student Portal.
- [x] **Real Persistence:** Room database stores sessions, student records, audit logs, and settings.
- [x] **Reports & Export:** Master register, low attendance list, subject reports, and CSV sharing.
- [x] **Adaptive Launcher Icon:** Custom college icon configured.
- [x] **Blue & White M3 Theme:** Professional academic styling.
