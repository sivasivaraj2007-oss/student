package com.example.data.repository

import com.example.data.local.AttendanceDao
import com.example.data.local.FacultyDao
import com.example.data.local.StudentDao
import com.example.data.local.SubjectDao
import com.example.data.model.AppSetting
import com.example.data.model.AttendanceAuditLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.AttendanceStatus
import com.example.data.model.Faculty
import com.example.data.model.FacultySubjectAssignment
import com.example.data.model.Student
import com.example.data.model.StudentAttendanceSummary
import com.example.data.model.StudentDateAttendanceItem
import com.example.data.model.Subject
import com.example.data.model.SubjectAttendanceSummary
import com.example.data.model.SubjectStudentBreakdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val subjectDao: SubjectDao,
    private val facultyDao: FacultyDao,
    private val attendanceDao: AttendanceDao
) {
    val activeStudents: Flow<List<Student>> = studentDao.getActiveStudents()
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val allFaculty: Flow<List<Faculty>> = facultyDao.getAllFaculty()
    val allAssignments: Flow<List<FacultySubjectAssignment>> = facultyDao.getAllAssignments()
    val allSessions: Flow<List<AttendanceSession>> = attendanceDao.getAllSessions()
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val auditLogs: Flow<List<AttendanceAuditLog>> = attendanceDao.getAuditLogs()

    fun getSetting(key: String): Flow<AppSetting?> = attendanceDao.getSetting(key)

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        attendanceDao.insertSetting(AppSetting(key = key, value = value))
    }

    suspend fun getSettingSync(key: String): String? = withContext(Dispatchers.IO) {
        attendanceDao.getSettingSync(key)?.value
    }

    suspend fun checkDuplicateSession(subjectId: Long, date: String): AttendanceSession? = withContext(Dispatchers.IO) {
        attendanceDao.getSessionBySubjectAndDate(subjectId, date)
    }

    suspend fun saveAttendanceSession(
        subjectId: Long,
        date: String,
        facultyId: Long,
        facultyName: String,
        studentStatuses: Map<Long, AttendanceStatus>
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // 1. Strict duplicate check
            val existing = attendanceDao.getSessionBySubjectAndDate(subjectId, date)
            if (existing != null) {
                return@withContext Result.failure(
                    IllegalStateException("Attendance already recorded for this subject on $date. Use correction screen to edit.")
                )
            }

            // 2. Validate all students marked
            val activeList = studentDao.getActiveStudentsList()
            val unmarked = activeList.filter { !studentStatuses.containsKey(it.id) }
            if (unmarked.isNotEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("${unmarked.size} students have not been assigned an attendance status.")
                )
            }

            // 3. Insert Session
            val session = AttendanceSession(
                subjectId = subjectId,
                date = date,
                markedByFacultyId = facultyId,
                markedByName = facultyName,
                createdAt = System.currentTimeMillis()
            )
            val sessionId = attendanceDao.insertSession(session)

            // 4. Insert Records
            val records = studentStatuses.map { (studentId, status) ->
                AttendanceRecord(
                    sessionId = sessionId,
                    studentId = studentId,
                    status = status.name,
                    updatedAt = System.currentTimeMillis()
                )
            }
            attendanceDao.insertRecords(records)

            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun correctStudentAttendance(
        sessionId: Long,
        studentId: Long,
        newStatus: AttendanceStatus,
        reason: String,
        changedBy: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val session = attendanceDao.getSessionById(sessionId)
                ?: return@withContext Result.failure(IllegalArgumentException("Session not found"))
            val subject = subjectDao.getSubjectByIdSync(session.subjectId)
            val student = studentDao.getStudentById(studentId)

            val sessionRecords = attendanceDao.getRecordsForSession(sessionId)
            val targetRecord = sessionRecords.find { it.studentId == studentId }
                ?: return@withContext Result.failure(IllegalArgumentException("Record for student not found in session"))

            val previousStatus = targetRecord.status

            if (previousStatus == newStatus.name) {
                return@withContext Result.success(Unit) // No change
            }

            // Update record
            val updated = targetRecord.copy(
                status = newStatus.name,
                updatedAt = System.currentTimeMillis()
            )
            attendanceDao.updateRecord(updated)

            // Record audit log
            val log = AttendanceAuditLog(
                sessionId = sessionId,
                studentId = studentId,
                studentRegNo = "",
                studentName = "",
                subjectName = subject?.name ?: "Subject #${session.subjectId}",
                attendanceDate = session.date,
                previousStatus = previousStatus,
                newStatus = newStatus.name,
                reason = reason,
                changedBy = changedBy,
                timestamp = System.currentTimeMillis()
            )
            attendanceDao.insertAuditLog(log)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStudent(student: Student): Long = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    suspend fun assignFacultyToSubject(facultyId: Long, subjectId: Long) = withContext(Dispatchers.IO) {
        facultyDao.deleteAssignmentsForSubject(subjectId)
        facultyDao.insertAssignment(FacultySubjectAssignment(facultyId = facultyId, subjectId = subjectId))
    }

    suspend fun removeFacultyAssignment(facultyId: Long, subjectId: Long) = withContext(Dispatchers.IO) {
        facultyDao.deleteAssignment(facultyId, subjectId)
    }

    suspend fun addSubject(subject: Subject): Long = withContext(Dispatchers.IO) {
        subjectDao.insertSubject(subject)
    }

    suspend fun addFaculty(faculty: Faculty): Long = withContext(Dispatchers.IO) {
        facultyDao.insertSingleFaculty(faculty)
    }

    // Reactive Analytics Flows
    fun getStudentAttendanceSummaries(odCountsAsPresent: Boolean, threshold: Double): Flow<List<StudentAttendanceSummary>> {
        return combine(allStudents, allSessions, allRecords) { students, sessions, records ->
            val totalSessionsCount = sessions.size
            val recordsByStudent = records.groupBy { it.studentId }

            students.map { student ->
                val studentRecords = recordsByStudent[student.id] ?: emptyList()
                val presentCount = studentRecords.count { it.status == AttendanceStatus.PRESENT.name }
                val absentCount = studentRecords.count { it.status == AttendanceStatus.ABSENT.name }
                val odCount = studentRecords.count { it.status == AttendanceStatus.OD.name }
                val conducted = studentRecords.size

                val effectiveAttended = if (odCountsAsPresent) presentCount + odCount else presentCount
                val percentage = if (conducted > 0) {
                    (effectiveAttended.toDouble() / conducted.toDouble()) * 100.0
                } else {
                    100.0
                }

                StudentAttendanceSummary(
                    student = student,
                    totalConducted = conducted,
                    presentCount = presentCount,
                    absentCount = absentCount,
                    onDutyCount = odCount,
                    percentage = Math.round(percentage * 10.0) / 10.0,
                    isLowAttendance = percentage < threshold && conducted > 0
                )
            }
        }
    }

    fun getSubjectAttendanceSummaries(): Flow<List<SubjectAttendanceSummary>> {
        return combine(allSubjects, allSessions, allRecords, allFaculty, allAssignments) { subjects, sessions, records, facultyList, assignments ->
            val sessionsBySubject = sessions.groupBy { it.subjectId }
            val recordsBySession = records.groupBy { it.sessionId }
            val assignmentBySubject = assignments.associateBy { it.subjectId }
            val facultyById = facultyList.associateBy { it.id }

            subjects.map { subject ->
                val subSessions = sessionsBySubject[subject.id] ?: emptyList()
                val totalClasses = subSessions.size
                var presentTotal = 0
                var absentTotal = 0
                var odTotal = 0

                subSessions.forEach { session ->
                    val recs = recordsBySession[session.id] ?: emptyList()
                    presentTotal += recs.count { it.status == AttendanceStatus.PRESENT.name }
                    absentTotal += recs.count { it.status == AttendanceStatus.ABSENT.name }
                    odTotal += recs.count { it.status == AttendanceStatus.OD.name }
                }

                val totalEntries = presentTotal + absentTotal + odTotal
                val avgPct = if (totalEntries > 0) {
                    (presentTotal.toDouble() / totalEntries.toDouble()) * 100.0
                } else {
                    0.0
                }

                val facultyId = assignmentBySubject[subject.id]?.facultyId
                val facultyName = facultyId?.let { facultyById[it]?.name } ?: "Unassigned"

                SubjectAttendanceSummary(
                    subject = subject,
                    facultyName = facultyName,
                    totalClasses = totalClasses,
                    averagePercentage = Math.round(avgPct * 10.0) / 10.0,
                    presentCount = presentTotal,
                    absentCount = absentTotal,
                    odCount = odTotal
                )
            }
        }
    }

    fun getStudentSubjectBreakdowns(studentId: Long, odCountsAsPresent: Boolean): Flow<List<SubjectStudentBreakdown>> {
        return combine(allSubjects, allSessions, allRecords) { subjects, sessions, records ->
            val sessionsById = sessions.associateBy { it.id }
            val studentRecords = records.filter { it.studentId == studentId }

            // Group student records by subjectId
            val recordsBySubject = mutableMapOf<Long, MutableList<AttendanceRecord>>()
            studentRecords.forEach { rec ->
                val session = sessionsById[rec.sessionId]
                if (session != null) {
                    recordsBySubject.getOrPut(session.subjectId) { mutableListOf() }.add(rec)
                }
            }

            subjects.map { subject ->
                val subRecs = recordsBySubject[subject.id] ?: emptyList()
                val conducted = subRecs.size
                val attended = subRecs.count { it.status == AttendanceStatus.PRESENT.name }
                val absent = subRecs.count { it.status == AttendanceStatus.ABSENT.name }
                val od = subRecs.count { it.status == AttendanceStatus.OD.name }

                val effectiveAttended = if (odCountsAsPresent) attended + od else attended
                val pct = if (conducted > 0) {
                    (effectiveAttended.toDouble() / conducted.toDouble()) * 100.0
                } else 100.0

                SubjectStudentBreakdown(
                    subject = subject,
                    conducted = conducted,
                    attended = attended,
                    absent = absent,
                    onDuty = od,
                    percentage = Math.round(pct * 10.0) / 10.0
                )
            }
        }
    }

    fun getStudentHistory(studentId: Long): Flow<List<StudentDateAttendanceItem>> {
        return combine(allSessions, allRecords, allSubjects) { sessions, records, subjects ->
            val subjectsById = subjects.associateBy { it.id }
            val sessionsById = sessions.associateBy { it.id }
            val studentRecords = records.filter { it.studentId == studentId }

            studentRecords.mapNotNull { rec ->
                val session = sessionsById[rec.sessionId] ?: return@mapNotNull null
                val subject = subjectsById[session.subjectId]
                val status = try {
                    AttendanceStatus.valueOf(rec.status)
                } catch (e: Exception) {
                    AttendanceStatus.PRESENT
                }

                StudentDateAttendanceItem(
                    date = session.date,
                    subjectName = subject?.name ?: "Subject #${session.subjectId}",
                    subjectCode = subject?.code ?: "",
                    status = status,
                    markedBy = session.markedByName
                )
            }.sortedByDescending { it.date }
        }
    }
}
