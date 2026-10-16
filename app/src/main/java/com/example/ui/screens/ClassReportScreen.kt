package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentAttendanceSummary
import com.example.ui.components.AttendanceProgressBar
import com.example.ui.theme.CollegeBluePrimary
import com.example.ui.theme.CollegeBlueSecondary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentContainer
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassReportScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val studentSummaries by viewModel.studentSummaries.collectAsState()
    val subjectSummaries by viewModel.subjectSummaries.collectAsState()
    val threshold by viewModel.attendanceThreshold.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("REG_NO") } // REG_NO, LOWEST_PCT, HIGHEST_PCT

    val tabs = listOf("Master Register", "Low Attendance (<${threshold.toInt()}%)", "Subject Reports", "Session Logs")

    val filteredList = remember(studentSummaries, searchQuery, sortBy, selectedTabIndex) {
        var list = studentSummaries.filter {
            it.student.name.contains(searchQuery, ignoreCase = true) ||
                    it.student.registerNumber.contains(searchQuery, ignoreCase = true)
        }

        if (selectedTabIndex == 1) {
            list = list.filter { it.isLowAttendance }
        }

        when (sortBy) {
            "LOWEST_PCT" -> list.sortedBy { it.percentage }
            "HIGHEST_PCT" -> list.sortedByDescending { it.percentage }
            else -> list.sortedBy { it.student.registerNumber }
        }
    }

    Scaffold(
        modifier = modifier.testTag("class_report_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Attendance Reports & Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "INFO INSTITUTE OF ENGINEERING • IT 2026-27",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        modifier = Modifier.testTag("reports_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val csvContent = viewModel.generateClassAttendanceCsv()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                putExtra(Intent.EXTRA_SUBJECT, "IIE_Attendance_Report_2nd_Year_IT.csv")
                                type = "text/csv"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export Attendance CSV")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.testTag("export_csv_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export CSV",
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
            // Report Tabs
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

            // Search & Sort Bar for Student Tabs
            if (selectedTabIndex == 0 || selectedTabIndex == 1) {
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
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            sortBy = when (sortBy) {
                                "REG_NO" -> "LOWEST_PCT"
                                "LOWEST_PCT" -> "HIGHEST_PCT"
                                else -> "REG_NO"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CollegeBlueSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (sortBy) {
                                "LOWEST_PCT" -> "Sort: Lowest %"
                                "HIGHEST_PCT" -> "Sort: Highest %"
                                else -> "Sort: Reg No"
                            },
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0, 1 -> {
                    // Master Register / Low Attendance Register
                    if (selectedTabIndex == 1 && filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎉 Excellent! No students are currently below the ${threshold.toInt()}% attendance threshold.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusPresent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                Text(
                                    text = "Showing ${filteredList.size} students (Tap any student to view detailed profile)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            itemsIndexed(filteredList) { index, item ->
                                StudentReportItemCard(
                                    index = index + 1,
                                    summary = item,
                                    threshold = threshold,
                                    onClick = { viewModel.viewStudentDetails(item.student.id) }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Subject Attendance Summaries
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(subjectSummaries) { sub ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "${sub.subject.code} — ${sub.subject.name}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CollegeBluePrimary
                                            )
                                            Text(
                                                text = "Faculty In-Charge: ${sub.facultyName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${sub.averagePercentage}%",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sub.averagePercentage < threshold) StatusAbsent else StatusPresent
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    AttendanceProgressBar(percentage = sub.averagePercentage, threshold = threshold)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Total Sessions: ${sub.totalClasses}", style = MaterialTheme.typography.labelSmall)
                                        Text(text = "Present: ${sub.presentCount}", style = MaterialTheme.typography.labelSmall, color = StatusPresent)
                                        Text(text = "Absent: ${sub.absentCount}", style = MaterialTheme.typography.labelSmall, color = StatusAbsent)
                                        Text(text = "On Duty: ${sub.odCount}", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Session Logs
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (sessions.isEmpty()) {
                            item {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No attendance sessions submitted yet.")
                                }
                            }
                        }

                        items(sessions) { session ->
                            val subject = subjectSummaries.find { it.subject.id == session.subjectId }?.subject
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
                                            text = "${session.date} • ${subject?.code ?: "SUB"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CollegeBluePrimary
                                        )
                                        Text(
                                            text = subject?.name ?: "Subject #${session.subjectId}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = "Marked by: ${session.markedByName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Saved in DB ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusPresent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentReportItemCard(
    index: Int,
    summary: StudentAttendanceSummary,
    threshold: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("report_student_${summary.student.registerNumber}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (summary.isLowAttendance) StatusAbsentContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$index.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(26.dp)
                    )
                    Column {
                        Text(
                            text = summary.student.registerNumber,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CollegeBluePrimary
                        )
                        Text(
                            text = summary.student.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${summary.percentage}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.isLowAttendance) StatusAbsent else StatusPresent
                    )
                    if (summary.isLowAttendance) {
                        Text(
                            text = "LOW ATTENDANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusAbsent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            AttendanceProgressBar(percentage = summary.percentage, threshold = threshold)

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Conducted: ${summary.totalConducted}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "P: ${summary.presentCount} | A: ${summary.absentCount} | OD: ${summary.onDutyCount}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
