package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.CollegeBluePrimary
import com.example.ui.theme.CollegeBlueSecondary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentContainer
import com.example.ui.theme.StatusOnDuty
import com.example.ui.theme.StatusOnDutyContainer
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentContainer
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAttendanceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val markingStatuses by viewModel.markingStatuses.collectAsState()
    val submissionMessage by viewModel.submissionMessage.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isDuplicateSession by viewModel.isDuplicateSession.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var showMarkAllConfirmDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var tempDateInput by remember { mutableStateOf(selectedDate) }

    LaunchedEffect(submissionMessage) {
        submissionMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val selectedSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()

    // Status counters
    val presentCount = markingStatuses.values.count { it == AttendanceStatus.PRESENT }
    val absentCount = markingStatuses.values.count { it == AttendanceStatus.ABSENT }
    val odCount = markingStatuses.values.count { it == AttendanceStatus.OD }
    val unmarkedCount = students.size - markingStatuses.size

    val filteredStudents = if (searchQuery.isBlank()) {
        students
    } else {
        students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.registerNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.testTag("daily_attendance_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Attendance Entry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "2nd Year IT • 56 Students",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        modifier = Modifier.testTag("back_to_dashboard_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CollegeBluePrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Controls: Subject & Date
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Subject Dropdown
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.let { "${it.code} — ${it.name}" } ?: "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Curriculum Subject (7 Total)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("subject_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text("${subject.code} — ${subject.name}") },
                                    onClick = {
                                        viewModel.setSelectedSubject(subject.id)
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date & Duplicate Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    tempDateInput = selectedDate
                                    showDatePickerDialog = true
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = CollegeBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Date: $selectedDate",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeBluePrimary
                                )
                            }
                        }

                        Text(
                            text = "Time Zone: Asia/Kolkata",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Duplicate Alert Banner
                    if (isDuplicateSession) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusAbsentContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusAbsent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Attendance already submitted for this subject on $selectedDate. Duplicate entries are blocked by DB constraint.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusAbsent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Live Status Counters Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusCountPill("Present", presentCount, StatusPresent, StatusPresentContainer)
                    StatusCountPill("Absent", absentCount, StatusAbsent, StatusAbsentContainer)
                    StatusCountPill("On Duty", odCount, StatusOnDuty, StatusOnDutyContainer)
                    StatusCountPill("Unmarked", unmarkedCount, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surface)
                }
            }

            // Action Buttons: Mark All Present & Clear & Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Reg No or Name...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("attendance_search_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Button(
                    onClick = { showMarkAllConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPresent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("mark_all_present_btn")
                ) {
                    Text("All Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Student List for Attendance
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(filteredStudents) { index, student ->
                    val status = markingStatuses[student.id]
                    StudentMarkingRow(
                        index = index + 1,
                        registerNumber = student.registerNumber,
                        name = student.name,
                        currentStatus = status,
                        onStatusChange = { newStatus ->
                            viewModel.setStudentStatus(student.id, newStatus)
                        }
                    )
                }
            }
        }

        // Floating Bottom Submission Bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$presentCount Present • $absentCount Absent • $odCount OD",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (unmarkedCount > 0) {
                            Text(
                                text = "⚠️ $unmarkedCount students pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusAbsent,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "✓ All 56 students marked",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusPresent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.saveAttendance() },
                        enabled = !isSubmitting && !isDuplicateSession,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CollegeBluePrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("submit_attendance_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Save Attendance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Mark All Present Confirmation Dialog
    if (showMarkAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllConfirmDialog = false },
            title = { Text("Mark All 56 Students Present?") },
            text = {
                Text(
                    "This sets all students to 'Present'. According to college workflow rules, please review the list carefully and mark any absent or on-duty students before submitting."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllPresent()
                        showMarkAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPresent)
                ) {
                    Text("Apply & Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Picker / Custom Date Dialog (Asia/Kolkata DD-MM-YYYY)
    if (showDatePickerDialog) {
        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = { Text("Select Attendance Date") },
            text = {
                Column {
                    Text("Enter date in DD-MM-YYYY format (Asia/Kolkata time zone):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempDateInput,
                        onValueChange = { tempDateInput = it },
                        singleLine = true,
                        placeholder = { Text("DD-MM-YYYY (e.g. 16-10-2026)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempDateInput.isNotBlank()) {
                            viewModel.setSelectedDate(tempDateInput.trim())
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Set Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusCountPill(label: String, count: Int, color: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun StudentMarkingRow(
    index: Int,
    registerNumber: String,
    name: String,
    currentStatus: AttendanceStatus?,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_row_$registerNumber"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (currentStatus) {
                AttendanceStatus.PRESENT -> StatusPresentContainer.copy(alpha = 0.5f)
                AttendanceStatus.ABSENT -> StatusAbsentContainer.copy(alpha = 0.5f)
                AttendanceStatus.OD -> StatusOnDutyContainer.copy(alpha = 0.5f)
                null -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Student Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(26.dp)
                )
                Column {
                    Text(
                        text = registerNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CollegeBluePrimary
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Attendance Toggle Buttons [P] [A] [OD]
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present Button
                val isP = currentStatus == AttendanceStatus.PRESENT
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onStatusChange(AttendanceStatus.PRESENT) }
                        .testTag("status_present_$registerNumber"),
                    color = if (isP) StatusPresent else Color.Transparent,
                    border = if (!isP) androidx.compose.foundation.BorderStroke(1.dp, StatusPresent) else null
                ) {
                    Text(
                        text = "P",
                        fontWeight = FontWeight.Bold,
                        color = if (isP) Color.White else StatusPresent,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Absent Button
                val isA = currentStatus == AttendanceStatus.ABSENT
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onStatusChange(AttendanceStatus.ABSENT) }
                        .testTag("status_absent_$registerNumber"),
                    color = if (isA) StatusAbsent else Color.Transparent,
                    border = if (!isA) androidx.compose.foundation.BorderStroke(1.dp, StatusAbsent) else null
                ) {
                    Text(
                        text = "A",
                        fontWeight = FontWeight.Bold,
                        color = if (isA) Color.White else StatusAbsent,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // On Duty Button
                val isOD = currentStatus == AttendanceStatus.OD
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onStatusChange(AttendanceStatus.OD) }
                        .testTag("status_od_$registerNumber"),
                    color = if (isOD) StatusOnDuty else Color.Transparent,
                    border = if (!isOD) androidx.compose.foundation.BorderStroke(1.dp, StatusOnDuty) else null
                ) {
                    Text(
                        text = "OD",
                        fontWeight = FontWeight.Bold,
                        color = if (isOD) Color.White else StatusOnDuty,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
