package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AttendanceAuditLog
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
import com.example.data.model.UserRole
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class Screen {
    DASHBOARD,
    DAILY_ATTENDANCE,
    CLASS_REPORTS,
    STUDENT_PROFILE,
    ADMIN_MANAGEMENT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AttendanceRepository(
        database.studentDao(),
        database.subjectDao(),
        database.facultyDao(),
        database.attendanceDao()
    )

    // Current Screen
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Active User / Role
    private val _currentRole = MutableStateFlow(UserRole.CLASS_ADVISER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentFacultyUser = MutableStateFlow<Faculty?>(null)
    val currentFacultyUser: StateFlow<Faculty?> = _currentFacultyUser.asStateFlow()

    private val _currentStudentUser = MutableStateFlow<Student?>(null)
    val currentStudentUser: StateFlow<Student?> = _currentStudentUser.asStateFlow()

    // Selected student for Profile View
    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    // Settings
    private val _odCountsAsPresent = MutableStateFlow(false)
    val odCountsAsPresent: StateFlow<Boolean> = _odCountsAsPresent.asStateFlow()

    private val _attendanceThreshold = MutableStateFlow(75.0)
    val attendanceThreshold: StateFlow<Double> = _attendanceThreshold.asStateFlow()

    // Attendance Marking Screen State
    private val _selectedDate = MutableStateFlow(getTodayDateKolkata())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    // Map of studentId -> AttendanceStatus
    private val _markingStatuses = MutableStateFlow<Map<Long, AttendanceStatus>>(emptyMap())
    val markingStatuses: StateFlow<Map<Long, AttendanceStatus>> = _markingStatuses.asStateFlow()

    // Attendance Save feedback
    private val _submissionMessage = MutableStateFlow<String?>(null)
    val submissionMessage: StateFlow<String?> = _submissionMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isDuplicateSession = MutableStateFlow(false)
    val isDuplicateSession: StateFlow<Boolean> = _isDuplicateSession.asStateFlow()

    // Core Observable Flows
    val students: StateFlow<List<Student>> = repository.activeStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facultyList: StateFlow<List<Faculty>> = repository.allFaculty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facultyAssignments: StateFlow<List<FacultySubjectAssignment>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AttendanceAuditLog>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<AttendanceSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summaries & Reports
    val studentSummaries: StateFlow<List<StudentAttendanceSummary>> = combine(
        _odCountsAsPresent,
        _attendanceThreshold
    ) { od, thresh -> Pair(od, thresh) }
        .flatMapLatest { (od, thresh) ->
            repository.getStudentAttendanceSummaries(od, thresh)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectSummaries: StateFlow<List<SubjectAttendanceSummary>> = repository.getSubjectAttendanceSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected student profile flows
    val selectedStudentProfile = combine(
        _selectedStudentId,
        studentSummaries
    ) { id, summaries ->
        if (id != null) summaries.find { it.student.id == id } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedStudentSubjectBreakdown: StateFlow<List<SubjectStudentBreakdown>> = combine(
        _selectedStudentId,
        _odCountsAsPresent
    ) { id, od -> Pair(id, od) }
        .flatMapLatest { (id, od) ->
            if (id != null) repository.getStudentSubjectBreakdowns(id, od) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudentHistory: StateFlow<List<StudentDateAttendanceItem>> = _selectedStudentId
        .flatMapLatest { id ->
            if (id != null) repository.getStudentHistory(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe settings
        viewModelScope.launch {
            repository.getSetting("od_counts_as_present").collect { setting ->
                _odCountsAsPresent.value = setting?.value == "true"
            }
        }
        viewModelScope.launch {
            repository.getSetting("low_attendance_threshold").collect { setting ->
                _attendanceThreshold.value = setting?.value?.toDoubleOrNull() ?: 75.0
            }
        }

        // Set initial faculty user (Adviser Madheshvaran)
        viewModelScope.launch {
            facultyList.collect { list ->
                if (_currentFacultyUser.value == null && list.isNotEmpty()) {
                    val adviser = list.find { it.role == "CLASS_ADVISER" } ?: list.first()
                    _currentFacultyUser.value = adviser
                }
            }
        }

        // Initialize default subject for attendance
        viewModelScope.launch {
            subjects.collect { subList ->
                if (_selectedSubjectId.value == null && subList.isNotEmpty()) {
                    _selectedSubjectId.value = subList.first().id
                    checkSessionDuplication()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _submissionMessage.value = null
    }

    fun selectRole(role: UserRole) {
        _currentRole.value = role
        when (role) {
            UserRole.ADMIN -> {
                _currentFacultyUser.value = facultyList.value.find { it.role == "ADMIN" }
                _currentStudentUser.value = null
            }
            UserRole.CLASS_ADVISER -> {
                _currentFacultyUser.value = facultyList.value.find { it.role == "CLASS_ADVISER" }
                _currentStudentUser.value = null
            }
            UserRole.HOD -> {
                _currentFacultyUser.value = facultyList.value.find { it.role == "HOD" }
                _currentStudentUser.value = null
            }
            UserRole.FACULTY -> {
                _currentFacultyUser.value = facultyList.value.find { it.role == "FACULTY" }
                    ?: facultyList.value.firstOrNull()
                _currentStudentUser.value = null
            }
            UserRole.STUDENT -> {
                // Default to first student
                val st = students.value.firstOrNull()
                _currentStudentUser.value = st
                _selectedStudentId.value = st?.id
                _currentScreen.value = Screen.STUDENT_PROFILE
            }
        }
    }

    fun setStudentUser(student: Student) {
        _currentStudentUser.value = student
        _selectedStudentId.value = student.id
        _currentRole.value = UserRole.STUDENT
        _currentScreen.value = Screen.STUDENT_PROFILE
    }

    fun viewStudentDetails(studentId: Long) {
        _selectedStudentId.value = studentId
        _currentScreen.value = Screen.STUDENT_PROFILE
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        checkSessionDuplication()
    }

    fun setSelectedSubject(subjectId: Long) {
        _selectedSubjectId.value = subjectId
        checkSessionDuplication()
    }

    fun checkSessionDuplication() {
        val subjectId = _selectedSubjectId.value ?: return
        val date = _selectedDate.value
        viewModelScope.launch {
            val existing = repository.checkDuplicateSession(subjectId, date)
            _isDuplicateSession.value = existing != null
        }
    }

    fun setStudentStatus(studentId: Long, status: AttendanceStatus) {
        val current = _markingStatuses.value.toMutableMap()
        current[studentId] = status
        _markingStatuses.value = current
    }

    fun markAllPresent() {
        val active = students.value
        val map = active.associate { it.id to AttendanceStatus.PRESENT }
        _markingStatuses.value = map
    }

    fun clearMarkingList() {
        _markingStatuses.value = emptyMap()
    }

    fun saveAttendance() {
        val subjectId = _selectedSubjectId.value ?: run {
            _submissionMessage.value = "Error: Please select a subject."
            return
        }
        val date = _selectedDate.value
        val active = students.value
        val statusMap = _markingStatuses.value

        // Validation: verify every student has a status
        val unmarkedCount = active.count { !statusMap.containsKey(it.id) }
        if (unmarkedCount > 0) {
            _submissionMessage.value = "Validation Error: $unmarkedCount students have not been marked yet. Every student requires a status."
            return
        }

        val faculty = _currentFacultyUser.value
        val facultyId = faculty?.id ?: 1L
        val facultyName = faculty?.name ?: "Faculty"

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.saveAttendanceSession(
                subjectId = subjectId,
                date = date,
                facultyId = facultyId,
                facultyName = facultyName,
                studentStatuses = statusMap
            )
            _isSubmitting.value = false

            result.onSuccess {
                _submissionMessage.value = "Attendance successfully saved for $date! Persisted to database."
                _isDuplicateSession.value = true
            }.onFailure { error ->
                _submissionMessage.value = "Save Failed: ${error.message}"
            }
        }
    }

    fun updateAttendanceCorrection(
        sessionId: Long,
        studentId: Long,
        newStatus: AttendanceStatus,
        reason: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (reason.isBlank()) {
            onComplete(false, "Reason for attendance correction is mandatory.")
            return
        }
        val changedBy = _currentFacultyUser.value?.name ?: "Authorized Admin"
        viewModelScope.launch {
            val result = repository.correctStudentAttendance(sessionId, studentId, newStatus, reason, changedBy)
            result.onSuccess {
                onComplete(true, "Attendance corrected successfully and recorded in audit log.")
            }.onFailure {
                onComplete(false, "Correction failed: ${it.message}")
            }
        }
    }

    fun updateStudentInfo(student: Student, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateStudent(student)
                onResult(true, "Student record updated successfully.")
            } catch (e: Exception) {
                onResult(false, "Failed to update student: ${e.message}")
            }
        }
    }

    fun addNewStudent(regNo: String, name: String, onResult: (Boolean, String) -> Unit) {
        if (regNo.isBlank() || name.isBlank()) {
            onResult(false, "Register number and name cannot be empty.")
            return
        }
        viewModelScope.launch {
            try {
                val newStudent = Student(
                    registerNumber = regNo.trim().uppercase(Locale.getDefault()),
                    name = name.trim().uppercase(Locale.getDefault()),
                    department = "Information Technology",
                    year = "2nd Year",
                    academicYear = "2026-27"
                )
                repository.addStudent(newStudent)
                onResult(true, "Student added to 2nd Year IT master list.")
            } catch (e: Exception) {
                onResult(false, "Failed: Register Number may already exist.")
            }
        }
    }

    fun assignFaculty(facultyId: Long, subjectId: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.assignFacultyToSubject(facultyId, subjectId)
                onResult(true, "Faculty assigned to subject successfully.")
            } catch (e: Exception) {
                onResult(false, "Assignment failed: ${e.message}")
            }
        }
    }

    fun updateSettings(odAsPresent: Boolean, threshold: Double) {
        viewModelScope.launch {
            repository.setSetting("od_counts_as_present", odAsPresent.toString())
            repository.setSetting("low_attendance_threshold", threshold.toString())
            _odCountsAsPresent.value = odAsPresent
            _attendanceThreshold.value = threshold
        }
    }

    fun generateClassAttendanceCsv(): String {
        val summaries = studentSummaries.value
        val sb = StringBuilder()
        sb.append("INFO INSTITUTE OF ENGINEERING, COIMBATORE\n")
        sb.append("DEPARTMENT OF INFORMATION TECHNOLOGY (2026-27)\n")
        sb.append("Class: 2nd Year IT | Class Adviser: Madheshvaran | HOD: Palani\n")
        sb.append("Export Date: ${getTodayDateKolkata()}\n\n")
        sb.append("S.No,Register Number,Student Name,Total Classes,Present,Absent,On Duty,Percentage (%),Status\n")

        summaries.forEachIndexed { index, item ->
            val status = if (item.isLowAttendance) "LOW ATTENDANCE (<${attendanceThreshold.value}%)" else "Satisfactory"
            sb.append("${index + 1},${item.student.registerNumber},\"${item.student.name}\",${item.totalConducted},${item.presentCount},${item.absentCount},${item.onDutyCount},${item.percentage}%,$status\n")
        }
        return sb.toString()
    }

    companion object {
        fun getTodayDateKolkata(): String {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            return sdf.format(Date())
        }
    }
}
