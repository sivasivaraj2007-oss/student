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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.CollegeHeader
import com.example.ui.components.RoleSelectorBar
import com.example.ui.components.StatCard
import com.example.ui.theme.CollegeBluePrimary
import com.example.ui.theme.CollegeBlueSecondary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentContainer
import com.example.ui.theme.StatusOnDuty
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val students by viewModel.students.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val studentSummaries by viewModel.studentSummaries.collectAsState()
    val subjectSummaries by viewModel.subjectSummaries.collectAsState()
    val threshold by viewModel.attendanceThreshold.collectAsState()
    val odCountsAsPresent by viewModel.odCountsAsPresent.collectAsState()

    val lowAttendanceCount = studentSummaries.count { it.isLowAttendance }
    val classAverage = if (studentSummaries.isNotEmpty()) {
        Math.round(studentSummaries.map { it.percentage }.average() * 10.0) / 10.0
    } else 0.0

    // Present / Absent / OD across all records
    val totalPresent = studentSummaries.sumOf { it.presentCount }
    val totalAbsent = studentSummaries.sumOf { it.absentCount }
    val totalOD = studentSummaries.sumOf { it.onDutyCount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // College Header
        item {
            CollegeHeader()
        }

        // Role Switcher Bar
        item {
            RoleSelectorBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.selectRole(it) }
            )
        }

        // Role specific greeting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CollegeBluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val roleTitle = when (currentRole) {
                            UserRole.CLASS_ADVISER -> "Class Adviser: Madheshvaran"
                            UserRole.HOD -> "Head of Department (HOD): Palani"
                            UserRole.ADMIN -> "System Administrator"
                            UserRole.FACULTY -> "Faculty Portal"
                            UserRole.STUDENT -> "Student Portal"
                        }
                        Text(
                            text = roleTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Department of IT • Academic Year 2026-27 (2nd Year)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Primary Metric Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "TOTAL STUDENTS",
                        value = "${students.size}",
                        subtitle = "Master list (2nd Yr)",
                        icon = Icons.Default.People,
                        accentColor = CollegeBluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "SUBJECTS",
                        value = "${subjects.size}",
                        subtitle = "Curriculum 2026",
                        icon = Icons.Default.Book,
                        accentColor = CollegeBlueSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "CLASS AVERAGE",
                        value = "$classAverage%",
                        subtitle = if (odCountsAsPresent) "OD counted as Present" else "OD excluded from attended",
                        icon = Icons.Default.CheckCircle,
                        accentColor = StatusPresent,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "LOW ATTENDANCE",
                        value = "$lowAttendanceCount",
                        subtitle = "< ${threshold.toInt()}% threshold alert",
                        icon = Icons.Default.Warning,
                        accentColor = if (lowAttendanceCount > 0) StatusAbsent else StatusPresent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Attendance Stats Breakdown Pill
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ATTENDANCE ACTIVITY SUMMARY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalPresent", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusPresent)
                            Text(text = "Total Present", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalAbsent", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusAbsent)
                            Text(text = "Total Absent", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalOD", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusOnDuty)
                            Text(text = "Total On Duty", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${sessions.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CollegeBluePrimary)
                            Text(text = "Classes Held", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Quick Navigation Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ACTIONS & WORKFLOWS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.DAILY_ATTENDANCE) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_daily_attendance_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CollegeBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Attendance", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.CLASS_REPORTS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_reports_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CollegeBlueSecondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reports & CSV", fontSize = 13.sp)
                    }
                }

                if (currentRole == UserRole.ADMIN || currentRole == UserRole.CLASS_ADVISER || currentRole == UserRole.HOD) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.ADMIN_MANAGEMENT) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nav_admin_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Master List, Faculty Assign & Settings", fontSize = 13.sp)
                    }
                }
            }
        }

        // At Risk / Low Attendance Alert Section
        if (lowAttendanceCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusAbsentContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusAbsent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LOW ATTENDANCE WARNING (< ${threshold.toInt()}%)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusAbsent
                            )
                        }
                        Text(
                            text = "$lowAttendanceCount students require immediate follow-up by Class Adviser Madheshvaran.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusAbsent.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        val atRiskList = studentSummaries.filter { it.isLowAttendance }.take(5)
                        atRiskList.forEach { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { viewModel.viewStudentDetails(item.student.id) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${item.student.registerNumber} • ${item.student.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Attended ${item.presentCount}/${item.totalConducted} classes",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${item.percentage}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusAbsent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seven Subjects Summary Section
        item {
            Text(
                text = "SEVEN SUBJECTS ATTENDANCE SUMMARY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(subjectSummaries) { subSummary ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subject_card_${subSummary.subject.code}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${subSummary.subject.code} — ${subSummary.subject.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CollegeBluePrimary
                            )
                            Text(
                                text = "Faculty: ${subSummary.facultyName} • ${subSummary.totalClasses} classes conducted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${subSummary.averagePercentage}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (subSummary.averagePercentage < threshold) StatusAbsent else StatusPresent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    AttendanceProgressBar(
                        percentage = subSummary.averagePercentage,
                        threshold = threshold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
