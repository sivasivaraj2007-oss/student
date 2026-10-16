package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AdminManagementScreen
import com.example.ui.screens.ClassReportScreen
import com.example.ui.screens.DailyAttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.StudentProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AttendanceApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun AttendanceApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button for sub-screens
    if (currentScreen != Screen.DASHBOARD) {
        BackHandler {
            viewModel.navigateTo(Screen.DASHBOARD)
        }
    }

    when (currentScreen) {
        Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
        Screen.DAILY_ATTENDANCE -> DailyAttendanceScreen(viewModel = viewModel)
        Screen.CLASS_REPORTS -> ClassReportScreen(viewModel = viewModel)
        Screen.STUDENT_PROFILE -> StudentProfileScreen(viewModel = viewModel)
        Screen.ADMIN_MANAGEMENT -> AdminManagementScreen(viewModel = viewModel)
    }
}
