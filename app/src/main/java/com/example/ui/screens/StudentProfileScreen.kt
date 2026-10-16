package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.AttendanceProgressBar
import com.example.ui.components.RoleSelectorBar
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CollegeBluePrimary
import com.example.ui.theme.CollegeBlueSecondary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentContainer
import com.example.ui.theme.StatusOnDuty
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val profileSummary by viewModel.selectedStudentProfile.collectAsState()
    val subjectBreakdown by viewModel.selectedStudentSubjectBreakdown.collectAsState()
    val attendanceHistory by viewModel.selectedStudentHistory.collectAsState()
    val threshold by viewModel.attendanceThreshold.collectAsState()
    val odCountsAsPresent by viewModel.odCountsAsPresent.collectAsState()

    var studentDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("student_profile_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Student Attendance Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = profileSummary?.student?.name ?: "Individual Record",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    if (currentRole != UserRole.STUDENT) {
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                            modifier = Modifier.testTag("profile_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CollegeBluePrimary)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role bar if in student portal or admin view
            item {
                RoleSelectorBar(
                    currentRole = currentRole,
                    onRoleSelected = { viewModel.selectRole(it) }
                )
            }

            // Student Selector Dropdown (Allows selecting which of the 56 students to inspect)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { studentDropdownExpanded = true }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SELECT STUDENT (56 MASTER RECORDS)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = profileSummary?.student?.let { "${it.registerNumber} • ${it.name}" } ?: "Select Student",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeBluePrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = CollegeBluePrimary
                            )
                        }

                        DropdownMenu(
                            expanded = studentDropdownExpanded,
                            onDismissRequest = { studentDropdownExpanded = false }
                        ) {
                            allStudents.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text("${st.registerNumber} — ${st.name}") },
                                    onClick = {
                                        viewModel.viewStudentDetails(st.id)
                                        studentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            profileSummary?.let { summary ->
                val student = summary.student

                // Profile Hero Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CollegeBluePrimary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Register No: ${student.registerNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "2nd Year IT • Info Institute of Engineering",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Overall Attendance Percentage Banner
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "OVERALL ATTENDANCE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${summary.percentage}%",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                    if (summary.isLowAttendance) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = StatusAbsent
                                        ) {
                                            Text(
                                                text = "ALERT < ${threshold.toInt()}%",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = StatusPresent
                                        ) {
                                            Text(
                                                text = "SATISFACTORY",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Low Attendance Warning Card
                if (summary.isLowAttendance) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusAbsentContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusAbsent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Attendance Shortage Alert!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusAbsent
                                    )
                                    Text(
                                        text = "Current attendance is ${summary.percentage}%, which is below the mandatory ${threshold.toInt()}% Anna University requirement. Contact Class Adviser Madheshvaran.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusAbsent
                                    )
                                }
                            }
                        }
                    }
                }

                // Breakdown Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "CONDUCTED",
                            value = "${summary.totalConducted}",
                            icon = Icons.Default.CheckCircle,
                            accentColor = CollegeBluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "PRESENT",
                            value = "${summary.presentCount}",
                            icon = Icons.Default.CheckCircle,
                            accentColor = StatusPresent,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "ABSENT",
                            value = "${summary.absentCount}",
                            icon = Icons.Default.Warning,
                            accentColor = StatusAbsent,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "ON DUTY",
                            value = "${summary.onDutyCount}",
                            icon = Icons.Default.CheckCircle,
                            accentColor = StatusOnDuty,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Subject-Wise Attendance Breakdown Table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "SUBJECT-WISE ATTENDANCE BREAKDOWN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CollegeBluePrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            subjectBreakdown.forEach { item ->
                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "${item.subject.code} • ${item.subject.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Conducted: ${item.conducted} | Attended: ${item.attended} | Absent: ${item.absent} | OD: ${item.onDuty}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${item.percentage}%",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.percentage < threshold && item.conducted > 0) StatusAbsent else StatusPresent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AttendanceProgressBar(percentage = item.percentage, threshold = threshold)
                                }
                            }
                        }
                    }
                }

                // Date-Wise Attendance History List
                item {
                    Text(
                        text = "DATE-WISE ATTENDANCE LOG",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (attendanceHistory.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Text("No date-wise records found for this student.")
                            }
                        }
                    }
                }

                items(attendanceHistory) { historyItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${historyItem.date} • ${historyItem.subjectCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeBluePrimary
                                )
                                Text(
                                    text = historyItem.subjectName,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "Marked by: ${historyItem.markedBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = historyItem.status)
                        }
                    }
                }
            }
        }
    }
}
