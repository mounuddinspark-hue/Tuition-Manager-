package com.example.ui.navigation

sealed class AppScreen(val route: String) {
    // Auth
    object Splash : AppScreen("splash")
    object Login : AppScreen("login")
    object RegisterTeacher : AppScreen("register_teacher")
    object RegisterStudent : AppScreen("register_student")

    // Super Admin
    object AdminDashboard : AppScreen("admin_dashboard")
    object AdminTeachers : AppScreen("admin_teachers")
    object AdminStudents : AppScreen("admin_students")
    object AdminReports : AppScreen("admin_reports")
    object AdminSettings : AppScreen("admin_settings")

    // Teacher
    object TeacherDashboard : AppScreen("teacher_dashboard")
    object TeacherStudents : AppScreen("teacher_students")
    object TeacherAddStudent : AppScreen("teacher_add_student")
    object TeacherStudentDetail : AppScreen("teacher_student_detail")
    object TeacherAttendance : AppScreen("teacher_attendance")
    object TeacherRoutine : AppScreen("teacher_routine")
    object TeacherLesson : AppScreen("teacher_lesson")
    object TeacherHomework : AppScreen("teacher_homework")
    object TeacherPayments : AppScreen("teacher_payments")
    object TeacherNotices : AppScreen("teacher_notices")
    object TeacherSubjects : AppScreen("teacher_subjects")
    object TeacherPerformance : AppScreen("teacher_performance")
    object TeacherMessages : AppScreen("teacher_messages")
    object TeacherReports : AppScreen("teacher_reports")
    object TeacherProfile : AppScreen("teacher_profile")
    object TeacherSettings : AppScreen("teacher_settings")

    // Student
    object StudentDashboard : AppScreen("student_dashboard")
    object StudentRoutine : AppScreen("student_routine")
    object StudentLesson : AppScreen("student_lesson")
    object StudentHomework : AppScreen("student_homework")
    object StudentAttendance : AppScreen("student_attendance")
    object StudentPayments : AppScreen("student_payments")
    object StudentNotices : AppScreen("student_notices")
    object StudentMessages : AppScreen("student_messages")
    object StudentPerformance : AppScreen("student_performance")
    object StudentProfile : AppScreen("student_profile")
    object StudentSettings : AppScreen("student_settings")

    // Shared
    object FirebaseConfig : AppScreen("firebase_config")
}
