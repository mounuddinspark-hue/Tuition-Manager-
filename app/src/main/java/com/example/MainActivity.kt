package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppScreen
import com.example.ui.screens.admin.*
import com.example.ui.screens.auth.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.student.*
import com.example.ui.screens.teacher.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TuitionViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val snackbarMessage by viewModel.snackbarMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            // Handle snackbar
            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearSnackbar()
                }
            }

            // Handle back button
            BackHandler(enabled = currentScreen !is AppScreen.Login && currentScreen !is AppScreen.TeacherDashboard && currentScreen !is AppScreen.StudentDashboard && currentScreen !is AppScreen.AdminDashboard) {
                if (!viewModel.goBack()) {
                    finish()
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            is AppScreen.Login -> LoginScreen(viewModel = viewModel)
                            is AppScreen.RegisterTeacher -> RegisterTeacherScreen(viewModel = viewModel)
                            is AppScreen.RegisterStudent -> RegisterStudentScreen(viewModel = viewModel)

                            // Teacher Screens
                            is AppScreen.TeacherDashboard -> TeacherDashboardScreen(viewModel = viewModel)
                            is AppScreen.TeacherStudents -> TeacherStudentListScreen(viewModel = viewModel)
                            is AppScreen.TeacherAddStudent -> AddStudentScreen(viewModel = viewModel)
                            is AppScreen.TeacherStudentDetail -> StudentDetailScreen(viewModel = viewModel)
                            is AppScreen.TeacherAttendance -> TeacherAttendanceScreen(viewModel = viewModel)
                            is AppScreen.TeacherRoutine -> TeacherRoutineScreen(viewModel = viewModel)
                            is AppScreen.TeacherLesson -> TeacherLessonScreen(viewModel = viewModel)
                            is AppScreen.TeacherHomework -> TeacherHomeworkScreen(viewModel = viewModel)
                            is AppScreen.TeacherPayments -> TeacherPaymentScreen(viewModel = viewModel)
                            is AppScreen.TeacherNotices -> TeacherNoticeScreen(viewModel = viewModel)
                            is AppScreen.TeacherReports -> TeacherReportsScreen(viewModel = viewModel)
                            is AppScreen.TeacherMessages -> TeacherMessagesScreen(viewModel = viewModel)
                            is AppScreen.TeacherProfile -> TeacherProfileScreen(viewModel = viewModel)

                            // Student Screens
                            is AppScreen.StudentDashboard -> StudentDashboardScreen(viewModel = viewModel)
                            is AppScreen.StudentRoutine -> StudentRoutineScreen(viewModel = viewModel)
                            is AppScreen.StudentHomework -> StudentHomeworkScreen(viewModel = viewModel)
                            is AppScreen.StudentAttendance -> StudentAttendanceScreen(viewModel = viewModel)
                            is AppScreen.StudentPayments -> StudentPaymentsScreen(viewModel = viewModel)
                            is AppScreen.StudentNotices -> StudentNoticesScreen(viewModel = viewModel)
                            is AppScreen.StudentProfile -> StudentProfileScreen(viewModel = viewModel)

                            // Admin Screens
                            is AppScreen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                            is AppScreen.AdminTeachers -> AdminTeachersScreen(viewModel = viewModel)
                            is AppScreen.AdminStudents -> AdminStudentsScreen(viewModel = viewModel)
                            is AppScreen.AdminReports -> TeacherReportsScreen(viewModel = viewModel)

                            // Shared
                            is AppScreen.FirebaseConfig -> FirebaseConfigScreen(viewModel = viewModel)

                            else -> TeacherDashboardScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
