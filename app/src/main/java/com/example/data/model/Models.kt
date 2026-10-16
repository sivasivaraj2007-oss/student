package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AttendanceStatus(val label: String) {
    PRESENT("Present"),
    ABSENT("Absent"),
    OD("On Duty")
}

enum class UserRole(val displayName: String) {
    ADMIN("Administrator"),
    FACULTY("Faculty"),
    CLASS_ADVISER("Class Adviser (Madheshvaran)"),
    HOD("Head of Department (Palani)"),
    STUDENT("Student Portal")
}

@Entity(
    tableName = "students",
    indices = [Index(value = ["registerNumber"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val registerNumber: String,
    val name: String,
    val department: String = "Information Technology",
    val year: String = "2nd Year",
    val academicYear: String = "2026-27",
    val isActive: Boolean = true
)

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["code"], unique = true)]
)
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val semester: Int = 3
)

@Entity(
    tableName = "faculty",
    indices = [Index(value = ["email"], unique = true)]
)
data class Faculty(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val role: String, // ADMIN, FACULTY, CLASS_ADVISER, HOD
    val pin: String = "1234"
)

@Entity(
    tableName = "faculty_subject_assignments",
    foreignKeys = [
        ForeignKey(
            entity = Faculty::class,
            parentColumns = ["id"],
            childColumns = ["facultyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["facultyId", "subjectId"], unique = true)]
)
data class FacultySubjectAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facultyId: Long,
    val subjectId: Long
)

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId", "date"], unique = true)]
)
data class AttendanceSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val date: String, // DD-MM-YYYY format in Asia/Kolkata
    val markedByFacultyId: Long,
    val markedByName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId", "studentId"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val status: String, // PRESENT, ABSENT, OD
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance_audit_logs")
data class AttendanceAuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val studentRegNo: String,
    val studentName: String,
    val subjectName: String,
    val attendanceDate: String,
    val previousStatus: String,
    val newStatus: String,
    val reason: String,
    val changedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)

// UI DTOs & Summaries
data class StudentAttendanceSummary(
    val student: Student,
    val totalConducted: Int,
    val presentCount: Int,
    val absentCount: Int,
    val onDutyCount: Int,
    val percentage: Double,
    val isLowAttendance: Boolean
)

data class SubjectAttendanceSummary(
    val subject: Subject,
    val facultyName: String,
    val totalClasses: Int,
    val averagePercentage: Double,
    val presentCount: Int,
    val absentCount: Int,
    val odCount: Int
)

data class SubjectStudentBreakdown(
    val subject: Subject,
    val conducted: Int,
    val attended: Int,
    val absent: Int,
    val onDuty: Int,
    val percentage: Double
)

data class StudentDateAttendanceItem(
    val date: String,
    val subjectName: String,
    val subjectCode: String,
    val status: AttendanceStatus,
    val markedBy: String
)
