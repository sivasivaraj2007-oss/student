package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.ui.theme.CollegeBluePrimary
import com.example.ui.theme.CollegeBlueSecondary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val facultyList by viewModel.facultyList.collectAsState()
    val assignments by viewModel.facultyAssignments.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val odCountsAsPresent by viewModel.odCountsAsPresent.collectAsState()
    val threshold by viewModel.attendanceThreshold.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Student Master", "Faculty & Subjects", "Audit Trail", "College Rules")

    // Edit Student Dialog State
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var editRegNo by remember { mutableStateOf("") }
    var editName by remember { mutableStateOf("") }

    // Add Student Dialog State
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newRegNo by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }

    // Attendance Correction Dialog State
    var showCorrectionDialog by remember { mutableStateOf(false) }
    var corrSessionId by remember { mutableStateOf(sessions.firstOrNull()?.id ?: 0L) }
    var corrStudentId by remember { mutableStateOf(allStudents.firstOrNull()?.id ?: 0L) }
    var corrNewStatus by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var corrReason by remember { mutableStateOf("") }

    // Subject Assignment Dialog State
    var showAssignDialog by remember { mutableStateOf(false) }
    var assignSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var assignFacultyId by remember { mutableStateOf(facultyList.firstOrNull()?.id ?: 0L) }

    Scaffold(
        modifier = modifier.testTag("admin_management_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin & Academic Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "INFO INSTITUTE OF ENGINEERING • IT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        modifier = Modifier.testTag("admin_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CollegeBluePrimary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    // Student Master List Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2nd Year IT Students (${allStudents.size} Records)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeBluePrimary
                                )
                                Button(
                                    onClick = {
                                        newRegNo = ""
                                        newName = ""
                                        showAddStudentDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CollegeBluePrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Student", fontSize = 12.sp)
                                }
                            }
                        }

                        itemsIndexed(allStudents) { index, student ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(26.dp)
                                        )
                                        Column {
                                            Text(
                                                text = student.registerNumber,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CollegeBluePrimary
                                            )
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            editingStudent = student
                                            editRegNo = student.registerNumber
                                            editName = student.name
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Student",
                                            tint = CollegeBlueSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Faculty & Subjects Assignments
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Seven Subjects Faculty Mapping",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeBluePrimary
                                )
                                Button(
                                    onClick = { showAssignDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CollegeBluePrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reassign Faculty", fontSize = 12.sp)
                                }
                            }
                        }

                        items(subjects) { subject ->
                            val assignment = assignments.find { it.subjectId == subject.id }
                            val faculty = assignment?.let { a -> facultyList.find { it.id == a.facultyId } }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${subject.code} — ${subject.name}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CollegeBluePrimary
                                        )
                                        Text(
                                            text = "Assigned Faculty: ${faculty?.name ?: "Not Assigned"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Class Leadership Roles",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CollegeBluePrimary
                            )
                        }

                        items(facultyList.filter { it.role == "CLASS_ADVISER" || it.role == "HOD" }) { leader ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = CollegeBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${leader.name} (${if (leader.role == "HOD") "Head of Department (HOD)" else "Class Adviser"})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = leader.email,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Audit Trail & Attendance Correction
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Attendance Audit Trail (${auditLogs.size} Logs)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeBluePrimary
                                )
                                Button(
                                    onClick = {
                                        if (sessions.isNotEmpty() && allStudents.isNotEmpty()) {
                                            corrSessionId = sessions.first().id
                                            corrStudentId = allStudents.first().id
                                            corrReason = ""
                                            showCorrectionDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CollegeBlueSecondary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Correct Record", fontSize = 12.sp)
                                }
                            }
                        }

                        if (auditLogs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No manual corrections made yet. All original submissions are recorded.")
                                }
                            }
                        }

                        items(auditLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${log.attendanceDate} • ${log.subjectName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CollegeBluePrimary
                                        )
                                        Text(
                                            text = formatTimestamp(log.timestamp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Status changed: ${log.previousStatus} ➔ ${log.newStatus}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (log.newStatus == "PRESENT") StatusPresent else StatusAbsent
                                    )
                                    Text(
                                        text = "Mandatory Reason: \"${log.reason}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Authorized By: ${log.changedBy}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // System Settings & College Rules
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "COLLEGE RULES & POLICIES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CollegeBluePrimary
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "On Duty (OD) Policy",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (odCountsAsPresent) {
                                                    "OD is currently treated as Present in attendance percentage calculation."
                                                } else {
                                                    "OD is tracked separately and excluded from attended classes (Standard Anna University rule)."
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = odCountsAsPresent,
                                            onCheckedChange = { checked ->
                                                viewModel.updateSettings(checked, threshold)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (checked) "OD now counted in attendance percentage"
                                                        else "OD now treated as separate from attended classes"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Low Attendance Alert Threshold",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Students falling below this threshold will be flagged for Class Adviser Madheshvaran and HOD Palani.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(75.0, 80.0, 85.0).forEach { value ->
                                            Button(
                                                onClick = {
                                                    viewModel.updateSettings(odCountsAsPresent, value)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Threshold updated to ${value.toInt()}%")
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (threshold == value) CollegeBluePrimary else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "${value.toInt()}%",
                                                    color = if (threshold == value) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Institutional Metadata",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("College: INFO INSTITUTE OF ENGINEERING, COIMBATORE", style = MaterialTheme.typography.bodySmall)
                                    Text("Department: Information Technology (IT)", style = MaterialTheme.typography.bodySmall)
                                    Text("Class: 2nd Year IT (2026-27)", style = MaterialTheme.typography.bodySmall)
                                    Text("Class Adviser: Madheshvaran", style = MaterialTheme.typography.bodySmall)
                                    Text("Head of Department: Palani", style = MaterialTheme.typography.bodySmall)
                                    Text("Timezone: Asia/Kolkata (IST)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Student Dialog
    editingStudent?.let { student ->
        AlertDialog(
            onDismissRequest = { editingStudent = null },
            title = { Text("Edit Student Details") },
            text = {
                Column {
                    Text("Modify spelling or register number as needed:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editRegNo,
                        onValueChange = { editRegNo = it },
                        label = { Text("Register Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = student.copy(
                            registerNumber = editRegNo.trim().uppercase(Locale.getDefault()),
                            name = editName.trim().uppercase(Locale.getDefault())
                        )
                        viewModel.updateStudentInfo(updated) { success, msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                        editingStudent = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingStudent = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Student Dialog
    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Add Student to 2nd Year IT") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newRegNo,
                        onValueChange = { newRegNo = it },
                        label = { Text("Register Number (e.g. 25BITT...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name (e.g. PRAVIN R)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewStudent(newRegNo, newName) { success, msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                        showAddStudentDialog = false
                    }
                ) {
                    Text("Add Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Attendance Correction Dialog (With mandatory reason!)
    if (showCorrectionDialog) {
        AlertDialog(
            onDismissRequest = { showCorrectionDialog = false },
            title = { Text("Authorized Attendance Correction") },
            text = {
                Column {
                    Text("According to college rules, corrections are logged with an audit trail.")
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Session (Subject & Date):", style = MaterialTheme.typography.labelSmall)
                    val session = sessions.find { it.id == corrSessionId }
                    Text(
                        text = "${session?.date} — Subject #${session?.subjectId}",
                        fontWeight = FontWeight.Bold,
                        color = CollegeBluePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Student:", style = MaterialTheme.typography.labelSmall)
                    val student = allStudents.find { it.id == corrStudentId }
                    Text(
                        text = "${student?.registerNumber} — ${student?.name}",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("New Status:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttendanceStatus.values().forEach { st ->
                            val selected = corrNewStatus == st
                            Button(
                                onClick = { corrNewStatus = st },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) CollegeBluePrimary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(st.label, fontSize = 11.sp, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = corrReason,
                        onValueChange = { corrReason = it },
                        label = { Text("Mandatory Reason for Correction *") },
                        placeholder = { Text("e.g. Medical OD approved by Adviser Madheshvaran") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAttendanceCorrection(
                            sessionId = corrSessionId,
                            studentId = corrStudentId,
                            newStatus = corrNewStatus,
                            reason = corrReason
                        ) { success, msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                        showCorrectionDialog = false
                    },
                    enabled = corrReason.isNotBlank()
                ) {
                    Text("Apply & Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCorrectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reassign Faculty Dialog
    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Faculty to Subject") },
            text = {
                Column {
                    Text("Select Curriculum Subject:", style = MaterialTheme.typography.labelSmall)
                    subjects.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { assignSubjectId = sub.id }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sub.code} — ${sub.name}",
                                fontWeight = if (assignSubjectId == sub.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (assignSubjectId == sub.id) CollegeBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Faculty In-Charge:", style = MaterialTheme.typography.labelSmall)
                    facultyList.filter { it.role == "FACULTY" }.forEach { fac ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { assignFacultyId = fac.id }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fac.name,
                                fontWeight = if (assignFacultyId == fac.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (assignFacultyId == fac.id) CollegeBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignFaculty(assignFacultyId, assignSubjectId) { success, msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                        showAssignDialog = false
                    }
                ) {
                    Text("Save Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
    return sdf.format(Date(timestamp))
}
