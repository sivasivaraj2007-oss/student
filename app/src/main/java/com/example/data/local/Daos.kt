package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppSetting
import com.example.data.model.AttendanceAuditLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.Faculty
import com.example.data.model.FacultySubjectAssignment
import com.example.data.model.Student
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY registerNumber ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY registerNumber ASC")
    fun getActiveStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY registerNumber ASC")
    suspend fun getActiveStudentsList(): List<Student>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE registerNumber = :regNo LIMIT 1")
    fun getStudentByRegNo(regNo: String): Flow<Student?>

    @Query("SELECT * FROM students WHERE registerNumber = :regNo LIMIT 1")
    suspend fun getStudentByRegNoSync(regNo: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getCount(): Int
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY code ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects ORDER BY code ASC")
    suspend fun getAllSubjectsList(): List<Subject>

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectById(id: Long): Flow<Subject?>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectByIdSync(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getCount(): Int
}

@Dao
interface FacultyDao {
    @Query("SELECT * FROM faculty ORDER BY name ASC")
    fun getAllFaculty(): Flow<List<Faculty>>

    @Query("SELECT * FROM faculty ORDER BY name ASC")
    suspend fun getAllFacultyList(): List<Faculty>

    @Query("SELECT * FROM faculty WHERE id = :id")
    fun getFacultyById(id: Long): Flow<Faculty?>

    @Query("SELECT * FROM faculty WHERE role = :role")
    fun getFacultyByRole(role: String): Flow<List<Faculty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(facultyList: List<Faculty>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleFaculty(faculty: Faculty): Long

    @Update
    suspend fun updateFaculty(faculty: Faculty)

    @Query("SELECT COUNT(*) FROM faculty")
    suspend fun getCount(): Int

    @Query("SELECT * FROM faculty_subject_assignments")
    fun getAllAssignments(): Flow<List<FacultySubjectAssignment>>

    @Query("SELECT * FROM faculty_subject_assignments")
    suspend fun getAllAssignmentsList(): List<FacultySubjectAssignment>

    @Query("SELECT * FROM faculty_subject_assignments WHERE facultyId = :facultyId")
    fun getAssignmentsForFaculty(facultyId: Long): Flow<List<FacultySubjectAssignment>>

    @Query("SELECT * FROM faculty_subject_assignments WHERE subjectId = :subjectId")
    fun getAssignmentsForSubject(subjectId: Long): Flow<List<FacultySubjectAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: FacultySubjectAssignment): Long

    @Query("DELETE FROM faculty_subject_assignments WHERE subjectId = :subjectId")
    suspend fun deleteAssignmentsForSubject(subjectId: Long)

    @Query("DELETE FROM faculty_subject_assignments WHERE facultyId = :facultyId AND subjectId = :subjectId")
    suspend fun deleteAssignment(facultyId: Long, subjectId: Long)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions ORDER BY id DESC")
    suspend fun getAllSessionsList(): List<AttendanceSession>

    @Query("SELECT * FROM attendance_sessions WHERE subjectId = :subjectId AND date = :date LIMIT 1")
    suspend fun getSessionBySubjectAndDate(subjectId: Long, date: String): AttendanceSession?

    @Query("SELECT * FROM attendance_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): AttendanceSession?

    @Query("SELECT * FROM attendance_sessions WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getSessionsForSubject(subjectId: Long): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions WHERE date = :date")
    fun getSessionsForDate(date: String): Flow<List<AttendanceSession>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: AttendanceSession): Long

    @Update
    suspend fun updateSession(session: AttendanceSession)

    @Query("DELETE FROM attendance_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun getRecordsForSession(sessionId: Long): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getRecordsForSessionFlow(sessionId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId")
    fun getRecordsForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId")
    suspend fun getRecordsForStudentList(studentId: Long): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecordsList(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecord>)

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Query("SELECT * FROM attendance_audit_logs ORDER BY timestamp DESC")
    fun getAuditLogs(): Flow<List<AttendanceAuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AttendanceAuditLog)

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<AppSetting?>

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingSync(key: String): AppSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)
}
