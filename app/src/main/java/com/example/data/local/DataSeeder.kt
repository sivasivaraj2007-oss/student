package com.example.data.local

import com.example.data.model.AppSetting
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.Faculty
import com.example.data.model.FacultySubjectAssignment
import com.example.data.model.Student
import com.example.data.model.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataSeeder {

    val INITIAL_STUDENTS = listOf(
        Pair("25BITT001", "ABISHEK M"),
        Pair("25BITT002", "AISHRAJAS S"),
        Pair("25BITT003", "AKALYA M"),
        Pair("25BITT004", "ANAGHA U"),
        Pair("25BITT005", "ARULRAJ T"),
        Pair("25BITT006", "ARUSUDHAN A"),
        Pair("25BITT007", "BARANIDHARAN"),
        Pair("25BITT008", "DHARANESH K"),
        Pair("25BITT009", "DHARANI L"),
        Pair("25BITT010", "DHARSIK A"),
        Pair("25BITT011", "DHIVAN K"),
        Pair("25BITT012", "DINESH R"),
        Pair("25BITT013", "ESAKKI RAJE"),
        Pair("25BITT014", "GIRUJEYAM M"),
        Pair("25BITT015", "GNANA SEKARAM M"),
        Pair("25BITT016", "GOBALA KRISHNAN S"),
        Pair("25BITT017", "GOPIKA N"),
        Pair("25BITT018", "GOWRISAN B"),
        Pair("25BITT020", "HARIHARAN G"),
        Pair("25BITT021", "HEMAVATHI G"),
        Pair("25BITT022", "JANANI G"),
        Pair("25BITT023", "JEBASTIN"),
        Pair("25BITT024", "JENITA S"),
        Pair("25BITT025", "JOTHIPRIYA P"),
        Pair("25BITT027", "KAVIYARASAN D"),
        Pair("25BITT028", "KISHORE M"),
        Pair("25BITT029", "KUTTYMADHESHWARAN M"),
        Pair("25BITT030", "MOHAMED ZUBER"),
        Pair("25BITT031", "MOULIDHARAN K"),
        Pair("25BITT032", "NIKISH MOHANRAJ"),
        Pair("25BITT033", "NIRANJANA V"),
        Pair("25BITT034", "NIVANGELLIN M"),
        Pair("25BITT035", "PARAMESH P"),
        Pair("25BITT036", "PRAVIN R"),
        Pair("25BITT037", "RAGULAN B"),
        Pair("25BITT038", "RITHIKA R"),
        Pair("25BITT039", "SAKTHI SRI DEVIT"),
        Pair("25BITT040", "SANDHIYA M"),
        Pair("25BITT041", "SANJAY V A"),
        Pair("25BITT042", "SANJITH K"),
        Pair("25BITT043", "SARANYA M"),
        Pair("25BITT044", "SARAVANAKUMAR S"),
        Pair("25BITT045", "SARAVANAVEL C"),
        Pair("25BITT046", "SATHYA C"),
        Pair("25BITT047", "SHARAN S"),
        Pair("25BITT048", "SIVARAJ S"),
        Pair("25BITT049", "SUMAN D"),
        Pair("25BITT050", "TAMIL SELVAN N"),
        Pair("25BITT051", "THASHALAN B"),
        Pair("25BITT053", "VETRIVEL K"),
        Pair("25BITT054", "VETRIVEL M"),
        Pair("25BITT055", "VISHNU KANTHAN M"),
        Pair("25BITT056", "YAZHINI S"),
        Pair("25BITT057", "YUVASRI S"),
        Pair("25BIT301", "HARI PRASADH G"),
        Pair("25BIT302", "PRADHEESH S")
    )

    val INITIAL_SUBJECTS = listOf(
        Pair("MA3354", "Discrete Mathematics"),
        Pair("CS3301", "Data Structures"),
        Pair("CS3391", "Object-Oriented Programming (OOPS)"),
        Pair("IT3301", "Web Technology"),
        Pair("CS3351", "Computer Organization and Architecture (COA)"),
        Pair("HS3152", "English"),
        Pair("IT3311", "SDC")
    )

    suspend fun seedDatabaseIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val studentDao = database.studentDao()
        val subjectDao = database.subjectDao()
        val facultyDao = database.facultyDao()
        val attendanceDao = database.attendanceDao()

        // 1. Seed Default Settings
        attendanceDao.insertSetting(AppSetting(key = "od_counts_as_present", value = "false"))
        attendanceDao.insertSetting(AppSetting(key = "low_attendance_threshold", value = "75"))
        attendanceDao.insertSetting(AppSetting(key = "college_name", value = "INFO INSTITUTE OF ENGINEERING, COIMBATORE"))
        attendanceDao.insertSetting(AppSetting(key = "department_name", value = "Information Technology (IT)"))
        attendanceDao.insertSetting(AppSetting(key = "class_name", value = "2nd Year IT (2026-27)"))

        // 2. Seed 56 Students if not present
        if (studentDao.getCount() == 0) {
            val studentEntities = INITIAL_STUDENTS.map { (regNo, name) ->
                Student(
                    registerNumber = regNo,
                    name = name,
                    department = "Information Technology",
                    year = "2nd Year",
                    academicYear = "2026-27",
                    isActive = true
                )
            }
            studentDao.insertStudents(studentEntities)
        }

        // 3. Seed 7 Subjects if not present
        if (subjectDao.getCount() == 0) {
            val subjectEntities = INITIAL_SUBJECTS.map { (code, name) ->
                Subject(
                    code = code,
                    name = name,
                    semester = 3
                )
            }
            subjectDao.insertSubjects(subjectEntities)
        }

        // 4. Seed Faculty & Leadership if not present
        if (facultyDao.getCount() == 0) {
            val admin = Faculty(name = "Administrator", email = "admin@infoengg.edu", role = "ADMIN", pin = "1234")
            val adviser = Faculty(name = "Madheshvaran", email = "madheshvaran@infoengg.edu", role = "CLASS_ADVISER", pin = "1234")
            val hod = Faculty(name = "Palani", email = "palani.hod@infoengg.edu", role = "HOD", pin = "1234")
            val f1 = Faculty(name = "Dr. Rajesh K.", email = "rajesh.it@infoengg.edu", role = "FACULTY", pin = "1234")
            val f2 = Faculty(name = "Dr. Revathi S.", email = "revathi.it@infoengg.edu", role = "FACULTY", pin = "1234")
            val f3 = Faculty(name = "Prof. Karthik N.", email = "karthik.it@infoengg.edu", role = "FACULTY", pin = "1234")
            val f4 = Faculty(name = "Prof. Priya M.", email = "priya.it@infoengg.edu", role = "FACULTY", pin = "1234")
            val f5 = Faculty(name = "Dr. Suresh V.", email = "suresh.it@infoengg.edu", role = "FACULTY", pin = "1234")

            val fIds = listOf(
                facultyDao.insertSingleFaculty(admin),
                facultyDao.insertSingleFaculty(adviser),
                facultyDao.insertSingleFaculty(hod),
                facultyDao.insertSingleFaculty(f1),
                facultyDao.insertSingleFaculty(f2),
                facultyDao.insertSingleFaculty(f3),
                facultyDao.insertSingleFaculty(f4),
                facultyDao.insertSingleFaculty(f5)
            )

            // Faculty assignments to the 7 subjects
            val subjects = subjectDao.getAllSubjectsList()
            if (subjects.size >= 7) {
                // f1 -> Discrete Mathematics (sub 0)
                facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = fIds[3], subjectId = subjects[0].id))
                // f2 -> Data Structures (sub 1)
                facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = fIds[4], subjectId = subjects[1].id))
                // f3 -> OOPS (sub 2)
                facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = fIds[5], subjectId = subjects[2].id))
                // f4 -> Web Technology (sub 3)
                facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = fIds[6], subjectId = subjects[3].id))
                // f5 -> COA (sub 4)
                facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = fIds[7], subjectId = subjects[4].id))
                // f1 -> English (sub 5)
                facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = fIds[3], subjectId = subjects[5].id))
                // f2 -> SDC (sub 6)
                facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = fIds[4], subjectId = subjects[6].id))
            }
        }

        // 5. Seed Realistic Initial Attendance Sessions across the past few days (e.g. 13-10-2026 to 15-10-2026)
        // so reports, charts, and low attendance analytics are immediately functional.
        if (attendanceDao.getAllSessionsList().isEmpty()) {
            val allStudents = studentDao.getActiveStudentsList()
            val allSubjects = subjectDao.getAllSubjectsList()
            val facultyList = facultyDao.getAllFacultyList()
            val sampleDates = listOf("13-10-2026", "14-10-2026", "15-10-2026")

            sampleDates.forEachIndexed { dateIndex, dateStr ->
                // Conduct 3-4 subjects per day
                val subjectsForDay = allSubjects.take(4 + (dateIndex % 3))
                subjectsForDay.forEach { subject ->
                    val facultyName = facultyList.find { it.role == "FACULTY" }?.name ?: "Faculty"
                    val session = AttendanceSession(
                        subjectId = subject.id,
                        date = dateStr,
                        markedByFacultyId = 4L,
                        markedByName = facultyName,
                        createdAt = System.currentTimeMillis() - (3 - dateIndex) * 86400000L
                    )
                    val sessionId = attendanceDao.insertSession(session)

                    // Mark students: ~88% present, a few absent, 1 or 2 OD
                    val records = allStudents.mapIndexed { studentIndex, student ->
                        val status = when {
                            // Specific students have lower attendance for testing low-attendance threshold (<75%)
                            (studentIndex == 6 || studentIndex == 18 || studentIndex == 34) && dateIndex >= 1 -> "ABSENT"
                            (studentIndex + dateIndex) % 19 == 0 -> "ABSENT"
                            (studentIndex + dateIndex) % 27 == 0 -> "OD"
                            else -> "PRESENT"
                        }
                        AttendanceRecord(
                            sessionId = sessionId,
                            studentId = student.id,
                            status = status
                        )
                    }
                    attendanceDao.insertRecords(records)
                }
            }
        }
    }
}
