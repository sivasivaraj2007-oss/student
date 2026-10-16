-- ====================================================================
-- INFO INSTITUTE OF ENGINEERING, COIMBATORE
-- DEPARTMENT OF INFORMATION TECHNOLOGY
-- STUDENT ATTENDANCE MANAGEMENT SYSTEM — SUPABASE POSTGRESQL SCHEMA
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. USER ROLES ENUM
CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'FACULTY',
    'CLASS_ADVISER',
    'HOD',
    'STUDENT'
);

CREATE TYPE attendance_status AS ENUM (
    'PRESENT',
    'ABSENT',
    'OD'
);

-- 3. PROFILES / USERS (Linked with Supabase Auth)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    role user_role NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now())
);

-- 4. STUDENTS MASTER TABLE (2nd Year IT, 2026-27)
CREATE TABLE IF NOT EXISTS public.students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    register_number VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL DEFAULT 'Information Technology',
    year VARCHAR(20) NOT NULL DEFAULT '2nd Year',
    academic_year VARCHAR(20) NOT NULL DEFAULT '2026-27',
    auth_user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now())
);

CREATE INDEX idx_students_reg_no ON public.students(register_number);
CREATE INDEX idx_students_dept_year ON public.students(department, year);

-- 5. SUBJECTS TABLE (7 Core Curriculum Subjects)
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    semester INTEGER NOT NULL DEFAULT 3,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now())
);

-- 6. FACULTY SUBJECT ASSIGNMENTS
CREATE TABLE IF NOT EXISTS public.faculty_subject_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    faculty_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now()),
    CONSTRAINT unique_faculty_subject UNIQUE (faculty_id, subject_id)
);

CREATE INDEX idx_fsa_subject ON public.faculty_subject_assignments(subject_id);
CREATE INDEX idx_fsa_faculty ON public.faculty_subject_assignments(faculty_id);

-- 7. ATTENDANCE SESSIONS
CREATE TABLE IF NOT EXISTS public.attendance_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subject_id UUID NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    marked_by UUID NOT NULL REFERENCES public.profiles(id),
    marked_by_name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now()),
    -- Strict constraint: prevent duplicate attendance session for same subject and date!
    CONSTRAINT unique_subject_date_session UNIQUE (subject_id, attendance_date)
);

CREATE INDEX idx_attendance_sessions_date ON public.attendance_sessions(attendance_date);
CREATE INDEX idx_attendance_sessions_subject ON public.attendance_sessions(subject_id);

-- 8. ATTENDANCE RECORDS (Per-student status)
CREATE TABLE IF NOT EXISTS public.attendance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES public.attendance_sessions(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES public.students(id) ON DELETE CASCADE,
    status attendance_status NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now()),
    -- Strict constraint: one record per student per session
    CONSTRAINT unique_session_student_record UNIQUE (session_id, student_id)
);

CREATE INDEX idx_attendance_records_student ON public.attendance_records(student_id);
CREATE INDEX idx_attendance_records_session ON public.attendance_records(session_id);
CREATE INDEX idx_attendance_records_status ON public.attendance_records(status);

-- 9. ATTENDANCE AUDIT LOGS (Tracks authorized corrections with reasons)
CREATE TABLE IF NOT EXISTS public.attendance_audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES public.attendance_sessions(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES public.students(id) ON DELETE CASCADE,
    previous_status attendance_status NOT NULL,
    new_status attendance_status NOT NULL,
    reason TEXT NOT NULL,
    changed_by UUID NOT NULL REFERENCES public.profiles(id),
    changed_by_name TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now())
);

CREATE INDEX idx_audit_session ON public.attendance_audit_logs(session_id);
CREATE INDEX idx_audit_student ON public.attendance_audit_logs(student_id);

-- 10. SYSTEM SETTINGS
CREATE TABLE IF NOT EXISTS public.app_settings (
    key VARCHAR(50) PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('Asia/Kolkata', now())
);

-- ====================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.students ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.faculty_subject_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;

-- Profiles: Authenticated users can read profiles; Admins can update
CREATE POLICY "Public profiles are viewable by authenticated users"
ON public.profiles FOR SELECT TO authenticated USING (true);

-- Students: Faculty, Class Adviser, HOD, and Admin can view all students. Students can view only their own record.
CREATE POLICY "Staff can view all students"
ON public.students FOR SELECT TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.profiles
        WHERE profiles.id = auth.uid()
        AND profiles.role IN ('ADMIN', 'FACULTY', 'CLASS_ADVISER', 'HOD')
    )
    OR auth_user_id = auth.uid()
);

-- Only Admin can insert or update student records
CREATE POLICY "Admins can manage students"
ON public.students FOR ALL TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.profiles
        WHERE profiles.id = auth.uid() AND profiles.role = 'ADMIN'
    )
);

-- Subjects: Viewable by all authenticated users
CREATE POLICY "Subjects are viewable by authenticated users"
ON public.subjects FOR SELECT TO authenticated USING (true);

-- Attendance Sessions:
-- Staff can view all sessions
CREATE POLICY "Staff can view sessions"
ON public.attendance_sessions FOR SELECT TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.profiles
        WHERE profiles.id = auth.uid()
        AND profiles.role IN ('ADMIN', 'FACULTY', 'CLASS_ADVISER', 'HOD')
    )
);

-- Faculty can insert sessions for their assigned subjects
CREATE POLICY "Faculty can insert sessions for assigned subjects"
ON public.attendance_sessions FOR INSERT TO authenticated
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.faculty_subject_assignments
        WHERE faculty_subject_assignments.faculty_id = auth.uid()
        AND faculty_subject_assignments.subject_id = attendance_sessions.subject_id
    )
    OR EXISTS (
        SELECT 1 FROM public.profiles
        WHERE profiles.id = auth.uid() AND profiles.role IN ('ADMIN', 'CLASS_ADVISER')
    )
);

-- Attendance Records:
-- Students can read only their own attendance records
CREATE POLICY "Students can view only own attendance records"
ON public.attendance_records FOR SELECT TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.students
        WHERE students.id = attendance_records.student_id
        AND students.auth_user_id = auth.uid()
    )
    OR EXISTS (
        SELECT 1 FROM public.profiles
        WHERE profiles.id = auth.uid()
        AND profiles.role IN ('ADMIN', 'FACULTY', 'CLASS_ADVISER', 'HOD')
    )
);

-- Faculty/Admin can insert records
CREATE POLICY "Staff can insert attendance records"
ON public.attendance_records FOR INSERT TO authenticated
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.profiles
        WHERE profiles.id = auth.uid()
        AND profiles.role IN ('ADMIN', 'FACULTY', 'CLASS_ADVISER')
    )
);

-- Audit logs: Read by Staff, Insert by Admin/Adviser
CREATE POLICY "Staff can view audit logs"
ON public.attendance_audit_logs FOR SELECT TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.profiles
        WHERE profiles.id = auth.uid()
        AND profiles.role IN ('ADMIN', 'CLASS_ADVISER', 'HOD')
    )
);
