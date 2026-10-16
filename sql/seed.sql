-- ====================================================================
-- INFO INSTITUTE OF ENGINEERING, COIMBATORE
-- DEPARTMENT OF INFORMATION TECHNOLOGY (2nd Year IT, 2026-27)
-- SEED DATA: 56 STUDENTS & 7 CURRICULUM SUBJECTS
-- ====================================================================

-- 1. SEED APP SETTINGS
INSERT INTO public.app_settings (key, value) VALUES
('od_counts_as_present', 'false'),
('low_attendance_threshold', '75'),
('college_name', 'INFO INSTITUTE OF ENGINEERING, COIMBATORE'),
('department', 'Information Technology (IT)'),
('class_adviser', 'Madheshvaran'),
('hod', 'Palani'),
('academic_year', '2026-27')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;

-- 2. SEED 7 SUBJECTS
INSERT INTO public.subjects (code, name, semester) VALUES
('MA3354', 'Discrete Mathematics', 3),
('CS3301', 'Data Structures', 3),
('CS3391', 'Object-Oriented Programming (OOPS)', 3),
('IT3301', 'Web Technology', 3),
('CS3351', 'Computer Organization and Architecture (COA)', 3),
('HS3152', 'English', 3),
('IT3311', 'SDC', 3)
ON CONFLICT (code) DO NOTHING;

-- 3. SEED EXACTLY 56 STUDENTS (Preserving exact register numbers and names)
INSERT INTO public.students (register_number, name, department, year, academic_year) VALUES
('25BITT001', 'ABISHEK M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT002', 'AISHRAJAS S', 'Information Technology', '2nd Year', '2026-27'),
('25BITT003', 'AKALYA M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT004', 'ANAGHA U', 'Information Technology', '2nd Year', '2026-27'),
('25BITT005', 'ARULRAJ T', 'Information Technology', '2nd Year', '2026-27'),
('25BITT006', 'ARUSUDHAN A', 'Information Technology', '2nd Year', '2026-27'),
('25BITT007', 'BARANIDHARAN', 'Information Technology', '2nd Year', '2026-27'),
('25BITT008', 'DHARANESH K', 'Information Technology', '2nd Year', '2026-27'),
('25BITT009', 'DHARANI L', 'Information Technology', '2nd Year', '2026-27'),
('25BITT010', 'DHARSIK A', 'Information Technology', '2nd Year', '2026-27'),
('25BITT011', 'DHIVAN K', 'Information Technology', '2nd Year', '2026-27'),
('25BITT012', 'DINESH R', 'Information Technology', '2nd Year', '2026-27'),
('25BITT013', 'ESAKKI RAJE', 'Information Technology', '2nd Year', '2026-27'),
('25BITT014', 'GIRUJEYAM M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT015', 'GNANA SEKARAM M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT016', 'GOBALA KRISHNAN S', 'Information Technology', '2nd Year', '2026-27'),
('25BITT017', 'GOPIKA N', 'Information Technology', '2nd Year', '2026-27'),
('25BITT018', 'GOWRISAN B', 'Information Technology', '2nd Year', '2026-27'),
('25BITT020', 'HARIHARAN G', 'Information Technology', '2nd Year', '2026-27'),
('25BITT021', 'HEMAVATHI G', 'Information Technology', '2nd Year', '2026-27'),
('25BITT022', 'JANANI G', 'Information Technology', '2nd Year', '2026-27'),
('25BITT023', 'JEBASTIN', 'Information Technology', '2nd Year', '2026-27'),
('25BITT024', 'JENITA S', 'Information Technology', '2nd Year', '2026-27'),
('25BITT025', 'JOTHIPRIYA P', 'Information Technology', '2nd Year', '2026-27'),
('25BITT027', 'KAVIYARASAN D', 'Information Technology', '2nd Year', '2026-27'),
('25BITT028', 'KISHORE M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT029', 'KUTTYMADHESHWARAN M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT030', 'MOHAMED ZUBER', 'Information Technology', '2nd Year', '2026-27'),
('25BITT031', 'MOULIDHARAN K', 'Information Technology', '2nd Year', '2026-27'),
('25BITT032', 'NIKISH MOHANRAJ', 'Information Technology', '2nd Year', '2026-27'),
('25BITT033', 'NIRANJANA V', 'Information Technology', '2nd Year', '2026-27'),
('25BITT034', 'NIVANGELLIN M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT035', 'PARAMESH P', 'Information Technology', '2nd Year', '2026-27'),
('25BITT036', 'PRAVIN R', 'Information Technology', '2nd Year', '2026-27'),
('25BITT037', 'RAGULAN B', 'Information Technology', '2nd Year', '2026-27'),
('25BITT038', 'RITHIKA R', 'Information Technology', '2nd Year', '2026-27'),
('25BITT039', 'SAKTHI SRI DEVIT', 'Information Technology', '2nd Year', '2026-27'),
('25BITT040', 'SANDHIYA M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT041', 'SANJAY V A', 'Information Technology', '2nd Year', '2026-27'),
('25BITT042', 'SANJITH K', 'Information Technology', '2nd Year', '2026-27'),
('25BITT043', 'SARANYA M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT044', 'SARAVANAKUMAR S', 'Information Technology', '2nd Year', '2026-27'),
('25BITT045', 'SARAVANAVEL C', 'Information Technology', '2nd Year', '2026-27'),
('25BITT046', 'SATHYA C', 'Information Technology', '2nd Year', '2026-27'),
('25BITT047', 'SHARAN S', 'Information Technology', '2nd Year', '2026-27'),
('25BITT048', 'SIVARAJ S', 'Information Technology', '2nd Year', '2026-27'),
('25BITT049', 'SUMAN D', 'Information Technology', '2nd Year', '2026-27'),
('25BITT050', 'TAMIL SELVAN N', 'Information Technology', '2nd Year', '2026-27'),
('25BITT051', 'THASHALAN B', 'Information Technology', '2nd Year', '2026-27'),
('25BITT053', 'VETRIVEL K', 'Information Technology', '2nd Year', '2026-27'),
('25BITT054', 'VETRIVEL M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT055', 'VISHNU KANTHAN M', 'Information Technology', '2nd Year', '2026-27'),
('25BITT056', 'YAZHINI S', 'Information Technology', '2nd Year', '2026-27'),
('25BITT057', 'YUVASRI S', 'Information Technology', '2nd Year', '2026-27'),
('25BIT301', 'HARI PRASADH G', 'Information Technology', '2nd Year', '2026-27'),
('25BIT302', 'PRADHEESH S', 'Information Technology', '2nd Year', '2026-27')
ON CONFLICT (register_number) DO NOTHING;
