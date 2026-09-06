package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.TuitionRepository
import com.example.ui.navigation.AppScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TuitionRepository

    // Navigation & App State
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Login)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _backstack = mutableListOf<AppScreen>()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isBengali = MutableStateFlow(true)
    val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Auth State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentTeacher = MutableStateFlow<TeacherEntity?>(null)
    val currentTeacher: StateFlow<TeacherEntity?> = _currentTeacher.asStateFlow()

    private val _currentStudent = MutableStateFlow<StudentEntity?>(null)
    val currentStudent: StateFlow<StudentEntity?> = _currentStudent.asStateFlow()

    // Selected Items for Details / Modals
    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    private val _selectedPayment = MutableStateFlow<PaymentEntity?>(null)
    val selectedPayment: StateFlow<PaymentEntity?> = _selectedPayment.asStateFlow()

    private val _isTeacherPreviewMode = MutableStateFlow(false)
    val isTeacherPreviewMode: StateFlow<Boolean> = _isTeacherPreviewMode.asStateFlow()

    // Search and filter queries
    val studentSearchQuery = MutableStateFlow("")
    val studentClassFilter = MutableStateFlow("ALL")

    val teacherSearchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TuitionRepository(db.tuitionDao())
        viewModelScope.launch {
            repository.preloadDemoDataIfNeeded()
            // Auto login as Teacher (Abdullah Sir) by default so user immediately sees rich functional dashboard,
            // but can switch anytime!
            quickLoginAs(UserRole.TEACHER)
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleLanguage() {
        _isBengali.value = !_isBengali.value
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun navigateTo(screen: AppScreen, clearBackstack: Boolean = false) {
        if (clearBackstack) {
            _backstack.clear()
        } else {
            _backstack.add(_currentScreen.value)
        }
        _currentScreen.value = screen
    }

    fun goBack(): Boolean {
        if (_backstack.isNotEmpty()) {
            _currentScreen.value = _backstack.removeAt(_backstack.size - 1)
            return true
        }
        return false
    }

    // --- Authentication ---
    fun login(identifier: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleanId = identifier.trim()
            var user: UserEntity? = null
            
            // 1. Try by email directly
            if (cleanId.contains("@")) {
                user = repository.getUserByEmail(cleanId)
            }
            
            // 2. Try by username
            if (user == null) {
                user = repository.getUserByUsername(cleanId)
            }
            
            // 3. Try by studentCode
            if (user == null) {
                val studentByCode = repository.getStudentByCode(cleanId)
                if (studentByCode != null) {
                    user = repository.getUserById(studentByCode.userId)
                }
            }
            
            // 4. Try case-insensitive email match
            if (user == null) {
                user = repository.getUserByEmail(cleanId.lowercase(Locale.getDefault()))
            }

            if (user == null || user.passwordHash != pass) {
                onResult(false, if (_isBengali.value) "ভুল ইমেইল/ইউজার আইডি বা পাসওয়ার্ড" else "Invalid Email/ID or password")
                return@launch
            }

            setUserSession(user)
            onResult(true, if (_isBengali.value) "স্বাগতম ${user.fullName}!" else "Welcome ${user.fullName}!")
        }
    }

    fun openStudentViewAsTeacher(student: StudentEntity? = null) {
        viewModelScope.launch {
            _isTeacherPreviewMode.value = true
            val targetStudent = student ?: _selectedStudent.value ?: repository.allStudents.firstOrNull()?.firstOrNull()
            if (targetStudent != null) {
                _currentStudent.value = targetStudent
            }
            navigateTo(AppScreen.StudentDashboard)
            showSnackbar(if (_isBengali.value) "শিক্ষার্থী ভিউ পোর্টাল খোলা হয়েছে" else "Viewing Student Portal Preview")
        }
    }

    fun exitStudentPreviewToTeacher() {
        _isTeacherPreviewMode.value = false
        navigateTo(AppScreen.TeacherDashboard, clearBackstack = true)
        showSnackbar(if (_isBengali.value) "কন্ট্রোল প্যানেলে ফিরে এসেছেন" else "Returned to Admin Hub")
    }

    fun logout() {
        _currentUser.value = null
        _currentTeacher.value = null
        _currentStudent.value = null
        _selectedStudent.value = null
        _isTeacherPreviewMode.value = false
        _backstack.clear()
        navigateTo(AppScreen.Login, clearBackstack = true)
        showSnackbar(if (_isBengali.value) "লগআউট সফল হয়েছে" else "Logged out successfully")
    }

    fun loginWithGoogle(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // Check for Google teacher or primary teacher
            var user = repository.getUserByUsername("teacher_google")
            if (user == null) {
                user = repository.getUserByUsername("teacher1")
            }
            if (user == null) {
                val userId = repository.insertUser(
                    UserEntity(
                        username = "teacher_google",
                        passwordHash = "google_auth",
                        fullName = "আব্দুল্লাহ স্যার (Google Verified)",
                        phone = "01711223344",
                        email = "abdullah.teacher@gmail.com",
                        role = UserRole.TEACHER.name
                    )
                )
                repository.insertTeacher(
                    TeacherEntity(
                        userId = userId,
                        name = "আব্দুল্লাহ স্যার (Abdullah Sir)",
                        phone = "01711223344",
                        email = "abdullah.teacher@gmail.com",
                        address = "মিরপুর-১০, ঢাকা",
                        qualification = "M.Sc in Mathematics (DU)",
                        joiningDate = "01/01/2023",
                        monthlyIncome = 25000.0
                    )
                )
                user = repository.getUserById(userId)
            }
            if (user != null) {
                setUserSession(user)
                val msg = if (_isBengali.value) 
                    "গুগল অ্যাকাউন্ট দিয়ে শিক্ষক হিসেবে সফলভাবে প্রবেশ করেছেন (এডমিন প্যানেল এক্সেস সহ)!" 
                    else "Logged in via Google as Teacher with full Admin access!"
                showSnackbar(msg)
                onResult(true, msg)
            } else {
                onResult(false, if (_isBengali.value) "গুগল সাইন ইন সম্পন্ন করা সম্ভব হয়নি" else "Google sign in failed")
            }
        }
    }

    fun openAdminPanel() {
        navigateTo(AppScreen.AdminDashboard)
        showSnackbar(if (_isBengali.value) "টিচার অ্যাডমিন প্যানেলে প্রবেশ করেছেন" else "Switched to Teacher Admin Panel")
    }

    private suspend fun setUserSession(user: UserEntity) {
        _currentUser.value = user
        when (user.role) {
            UserRole.SUPER_ADMIN.name -> {
                _currentTeacher.value = null
                _currentStudent.value = null
                navigateTo(AppScreen.AdminDashboard, clearBackstack = true)
            }
            UserRole.TEACHER.name -> {
                val teacher = repository.getTeacherByUserId(user.id)
                _currentTeacher.value = teacher
                _currentStudent.value = null
                navigateTo(AppScreen.TeacherDashboard, clearBackstack = true)
            }
            UserRole.STUDENT.name -> {
                val student = repository.getStudentByUserId(user.id)
                _currentStudent.value = student
                if (student != null) {
                    _currentTeacher.value = repository.getTeacherById(student.teacherId)
                }
                navigateTo(AppScreen.StudentDashboard, clearBackstack = true)
            }
        }
    }

    fun quickLoginAs(role: UserRole, customUsername: String? = null) {
        viewModelScope.launch {
            val username = customUsername ?: when (role) {
                UserRole.SUPER_ADMIN -> "admin"
                UserRole.TEACHER -> "teacher1"
                UserRole.STUDENT -> "student1"
            }
            val user = repository.getUserByUsername(username)
            if (user != null) {
                setUserSession(user)
                showSnackbar(
                    if (_isBengali.value) "${user.fullName} হিসেবে লগইন করা হয়েছে"
                    else "Switched profile to ${user.fullName}"
                )
            }
        }
    }

    fun registerTeacher(
        name: String,
        phone: String,
        email: String,
        qualification: String,
        address: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val username = "t_" + System.currentTimeMillis().toString().takeLast(6)
            val userId = repository.insertUser(
                UserEntity(
                    username = username,
                    passwordHash = pass,
                    fullName = name,
                    phone = phone,
                    email = email,
                    role = UserRole.TEACHER.name
                )
            )
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val teacherId = repository.insertTeacher(
                TeacherEntity(
                    userId = userId,
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    qualification = qualification,
                    joiningDate = sdf.format(Date()),
                    monthlyIncome = 0.0
                )
            )
            // Pre-seed some default subjects for new teacher
            repository.insertSubject(SubjectEntity(teacherId = teacherId, name = "বাংলা", code = "BAN", colorHex = "#16A34A"))
            repository.insertSubject(SubjectEntity(teacherId = teacherId, name = "গণিত", code = "MATH", colorHex = "#0F766E"))
            repository.insertSubject(SubjectEntity(teacherId = teacherId, name = "ইংরেজি", code = "ENG", colorHex = "#7C3AED"))

            showSnackbar(if (_isBengali.value) "শিক্ষক রেজিস্ট্রেশন সফল হয়েছে! ইউজারনেম: $username" else "Teacher registered successfully! Username: $username")
            val user = repository.getUserById(userId)
            if (user != null) {
                setUserSession(user)
            }
            onSuccess()
        }
    }

    fun registerStudent(
        studentName: String,
        email: String,
        password: String,
        className: String,
        roll: String,
        teacherAssignedUserId: String,
        phone: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanId = teacherAssignedUserId.trim()
            val cleanEmail = email.trim()
            val cleanName = studentName.trim()

            if (cleanName.isBlank() || cleanId.isBlank() || password.isBlank() || className.isBlank() || roll.isBlank()) {
                onResult(false, if (_isBengali.value) "সকল প্রয়োজনীয় তথ্য (নাম, ইমেইল, পাসওয়ার্ড, শ্রেণী, রোল ও শিক্ষক নির্ধারিত আইডি) পূরণ করুন" else "Please fill all required fields")
                return@launch
            }

            // Check if user already exists
            val existingUser = repository.getUserByUsername(cleanId)
            if (existingUser != null) {
                onResult(false, if (_isBengali.value) "এই শিক্ষক নির্ধারিত User ID দিয়ে ইতিমধ্যে অ্যাকাউন্ট রয়েছে! অনুগ্রহ করে লগইন করুন।" else "An account with this Teacher-Assigned User ID already exists. Please log in.")
                return@launch
            }

            val allTeachersList = repository.allTeachers.firstOrNull() ?: emptyList()
            val teacher = _currentTeacher.value ?: allTeachersList.firstOrNull() ?: TeacherEntity(
                id = 1,
                userId = 2,
                name = "আব্দুল্লাহ স্যার (Abdullah Sir)",
                phone = "01711223344",
                email = "abdullah@tuitionbd.com",
                address = "মিরপুর-১০, ঢাকা",
                qualification = "M.Sc in Mathematics",
                joiningDate = "01/01/2023"
            )

            val userId = repository.insertUser(
                UserEntity(
                    username = cleanId,
                    passwordHash = password,
                    fullName = cleanName,
                    phone = phone.ifBlank { "01700000000" },
                    email = cleanEmail,
                    role = UserRole.STUDENT.name
                )
            )

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayStr = sdf.format(Date())

            val existingStudentWithCode = repository.getStudentByCode(cleanId)
            val studentId: Long
            if (existingStudentWithCode != null) {
                val updatedStudent = existingStudentWithCode.copy(
                    userId = userId,
                    name = cleanName,
                    email = cleanEmail,
                    phone = phone.ifBlank { existingStudentWithCode.phone },
                    className = className,
                    roll = roll
                )
                repository.updateStudent(updatedStudent)
                studentId = updatedStudent.id
            } else {
                studentId = repository.insertStudent(
                    StudentEntity(
                        teacherId = teacher.id,
                        userId = userId,
                        name = cleanName,
                        studentCode = cleanId,
                        phone = phone.ifBlank { "01700000000" },
                        email = cleanEmail,
                        dob = "01/01/2008",
                        institution = "স্কুল ও কলেজ",
                        className = className,
                        section = "A",
                        roll = roll,
                        guardianName = "অভিভাবক",
                        guardianPhone = phone.ifBlank { "01700000000" },
                        address = "ঢাকা, বাংলাদেশ",
                        admissionDate = todayStr,
                        monthlyFee = 2000.0,
                        paymentDueDay = 10
                    )
                )
            }

            // Seed initial homework/assignments for student
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 3)
            val dueStr = sdf.format(cal.time)

            repository.insertHomework(
                HomeworkEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    subjectName = "গণিত",
                    title = "অনুশীলনী ৩.১: বীজগণিতীয় সূত্রাবলি সমাধান",
                    description = "১ থেকে ১০ নং অংক সুন্দর করে খাতায় সমাধান করে ছবি বা টেক্সট আকারে জমা দিতে হবে।",
                    givenDate = todayStr,
                    dueDate = dueStr,
                    priority = PriorityLevel.URGENT.name,
                    status = HomeworkStatus.PENDING.name
                )
            )
            repository.insertHomework(
                HomeworkEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    subjectName = "ইংরেজি",
                    title = "Assignment: Paragraph on My Tuition Experience",
                    description = "Write a 150-word paragraph describing your study routine and goals.",
                    givenDate = todayStr,
                    dueDate = dueStr,
                    priority = PriorityLevel.NORMAL.name,
                    status = HomeworkStatus.PENDING.name
                )
            )

            // Seed attendance records
            repository.insertAttendance(
                AttendanceEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    date = todayStr,
                    className = className,
                    subjectName = "গণিত",
                    status = AttendanceStatus.PRESENT.name,
                    note = "সময়মতো উপস্থিত"
                )
            )

            // Seed welcome notice
            repository.insertNotice(
                NoticeEntity(
                    teacherId = teacher.id,
                    title = "স্বাগতম ${cleanName}!",
                    description = "Tuition Manager BD-তে আপনার শিক্ষার্থী অ্যাকাউন্ট সফলভাবে চালু হয়েছে। নিয়মিত বাড়ির কাজ জমা দিন ও উপস্থিতি বজায় রাখুন।",
                    date = todayStr,
                    targetType = "SPECIFIC",
                    targetStudentId = studentId,
                    priority = PriorityLevel.IMPORTANT.name
                )
            )

            val createdUser = repository.getUserById(userId)
            if (createdUser != null) {
                setUserSession(createdUser)
            }

            val successMsg = if (_isBengali.value) 
                "রেজিস্ট্রেশন সফল হয়েছে! স্বাগতম $cleanName (শিক্ষার্থী আইডি: $cleanId)" 
                else "Registration successful! Welcome $cleanName (Student ID: $cleanId)"
            showSnackbar(successMsg)
            onResult(true, successMsg)
        }
    }

    fun selectStudentForDetails(student: StudentEntity) {
        _selectedStudent.value = student
        navigateTo(AppScreen.TeacherStudentDetail)
    }

    fun selectStudent(student: StudentEntity) {
        _selectedStudent.value = student
    }

    fun selectPaymentForReceipt(payment: PaymentEntity) {
        _selectedPayment.value = payment
    }

    fun clearSelectedPayment() {
        _selectedPayment.value = null
    }

    // --- Reactive Data Streams ---
    val allTeachers: Flow<List<TeacherEntity>> = repository.allTeachers
    val allStudents: Flow<List<StudentEntity>> = repository.allStudents
    val allPayments: Flow<List<PaymentEntity>> = repository.allPayments

    // Teacher-scoped streams
    val teacherStudents: Flow<List<StudentEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getStudentsByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherSubjects: Flow<List<SubjectEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getSubjectsByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherRoutines: Flow<List<RoutineEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getRoutinesByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherLessons: Flow<List<LessonEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getLessonsByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherHomework: Flow<List<HomeworkEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getHomeworkByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherAttendance: Flow<List<AttendanceEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getAttendanceByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherPayments: Flow<List<PaymentEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getPaymentsByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherNotices: Flow<List<NoticeEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getNoticesByTeacher(teacher.id) else flowOf(emptyList())
    }

    val teacherPerformance: Flow<List<PerformanceEntity>> = _currentTeacher.flatMapLatest { teacher ->
        if (teacher != null) repository.getPerformanceByTeacher(teacher.id) else flowOf(emptyList())
    }

    // Student-scoped streams
    val studentRoutines: Flow<List<RoutineEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getRoutinesByTeacher(student.teacherId) else flowOf(emptyList())
    }

    val studentLessons: Flow<List<LessonEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getLessonsForStudent(student.id) else flowOf(emptyList())
    }

    val studentHomework: Flow<List<HomeworkEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getHomeworkByStudent(student.id) else flowOf(emptyList())
    }

    val studentAttendance: Flow<List<AttendanceEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getAttendanceByStudent(student.id) else flowOf(emptyList())
    }

    val studentPayments: Flow<List<PaymentEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getPaymentsByStudent(student.id) else flowOf(emptyList())
    }

    val studentNotices: Flow<List<NoticeEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getNoticesForStudent(student.id) else flowOf(emptyList())
    }

    val studentPerformance: Flow<List<PerformanceEntity>> = _currentStudent.flatMapLatest { student ->
        if (student != null) repository.getPerformanceByStudent(student.id) else flowOf(emptyList())
    }

    // Selected student specific streams for StudentDetailScreen
    val selectedStudentAttendance: Flow<List<AttendanceEntity>> = _selectedStudent.flatMapLatest { student ->
        if (student != null) repository.getAttendanceByStudent(student.id) else flowOf(emptyList())
    }

    val selectedStudentPayments: Flow<List<PaymentEntity>> = _selectedStudent.flatMapLatest { student ->
        if (student != null) repository.getPaymentsByStudent(student.id) else flowOf(emptyList())
    }

    val selectedStudentHomework: Flow<List<HomeworkEntity>> = _selectedStudent.flatMapLatest { student ->
        if (student != null) repository.getHomeworkByStudent(student.id) else flowOf(emptyList())
    }

    val selectedStudentNotes: Flow<List<StudentNoteEntity>> = _selectedStudent.flatMapLatest { student ->
        if (student != null) repository.getNotesByStudent(student.id) else flowOf(emptyList())
    }

    val selectedStudentPerformance: Flow<List<PerformanceEntity>> = _selectedStudent.flatMapLatest { student ->
        if (student != null) repository.getPerformanceByStudent(student.id) else flowOf(emptyList())
    }

    // User Messages & Notifications
    val userMessages: Flow<List<MessageEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getMessagesForUser(user.id) else flowOf(emptyList())
    }

    val userNotifications: Flow<List<AppNotificationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotificationsForUser(user.id) else flowOf(emptyList())
    }

    // --- Actions & CRUD Operations ---

    fun addStudent(
        name: String,
        phone: String,
        email: String,
        dob: String,
        institution: String,
        className: String,
        section: String,
        roll: String,
        guardianName: String,
        guardianPhone: String,
        address: String,
        monthlyFee: Double,
        paymentDueDay: Int,
        loginUsername: String,
        loginPass: String,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val username = loginUsername.ifBlank { "std_" + System.currentTimeMillis().toString().takeLast(6) }
            val pass = loginPass.ifBlank { "123456" }

            val userId = repository.insertUser(
                UserEntity(
                    username = username,
                    passwordHash = pass,
                    fullName = name,
                    phone = phone,
                    email = email,
                    role = UserRole.STUDENT.name
                )
            )

            val studentCode = "TBD-" + Calendar.getInstance().get(Calendar.YEAR) + "-" + String.format("%03d", (1..999).random())
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            repository.insertStudent(
                StudentEntity(
                    teacherId = teacher.id,
                    userId = userId,
                    name = name,
                    studentCode = studentCode,
                    phone = phone,
                    email = email,
                    dob = dob,
                    institution = institution,
                    className = className,
                    section = section,
                    roll = roll,
                    guardianName = guardianName,
                    guardianPhone = guardianPhone,
                    address = address,
                    admissionDate = sdf.format(Date()),
                    monthlyFee = monthlyFee,
                    paymentDueDay = paymentDueDay
                )
            )

            showSnackbar(if (_isBengali.value) "$name সফলভাবে শিক্ষার্থী হিসেবে যুক্ত হয়েছে! আইডি: $studentCode" else "Student $name added successfully! ID: $studentCode")
            onSuccess()
        }
    }

    fun updateStudent(student: StudentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _selectedStudent.value = student
            showSnackbar(if (_isBengali.value) "শিক্ষার্থীর তথ্য আপডেট করা হয়েছে" else "Student profile updated")
            onSuccess()
        }
    }

    fun deleteStudent(student: StudentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            showSnackbar(if (_isBengali.value) "শিক্ষার্থী মুছে ফেলা হয়েছে" else "Student deleted")
            onSuccess()
        }
    }

    fun markAttendance(
        studentId: Long,
        status: AttendanceStatus,
        className: String,
        subjectName: String,
        note: String = ""
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date())
            repository.insertAttendance(
                AttendanceEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    date = dateStr,
                    className = className,
                    subjectName = subjectName,
                    status = status.name,
                    note = note
                )
            )
            showSnackbar(if (_isBengali.value) "উপস্থিতি রেকর্ড সংরক্ষিত হয়েছে" else "Attendance recorded successfully")
        }
    }

    fun savePayment(
        studentId: Long,
        monthYear: String,
        amount: Double,
        paidAmount: Double,
        paymentMethod: String,
        transactionId: String,
        note: String,
        status: PaymentStatus,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val due = (amount - paidAmount).coerceAtLeast(0.0)

            repository.insertPayment(
                PaymentEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    monthYear = monthYear,
                    amount = amount,
                    paidAmount = paidAmount,
                    dueAmount = due,
                    paymentDate = if (paidAmount > 0) dateStr else "",
                    dueDate = "১০/" + dateStr.substringAfter("/"),
                    paymentMethod = paymentMethod,
                    transactionId = transactionId,
                    note = note,
                    status = status.name
                )
            )

            // Notify student
            val student = repository.getStudentById(studentId)
            if (student != null) {
                repository.insertNotification(
                    AppNotificationEntity(
                        userId = student.userId,
                        title = if (_isBengali.value) "বেতন আপডেট" else "Payment Update",
                        message = if (_isBengali.value) "$monthYear মাসের বেতন ৳$paidAmount পরিশোধ গ্রহণ করা হয়েছে।" else "$monthYear tuition fee ৳$paidAmount recorded.",
                        type = "PAYMENT",
                        date = dateStr
                    )
                )
            }

            showSnackbar(if (_isBengali.value) "পেমেন্ট রেকর্ড সফলভাবে সংরক্ষিত হয়েছে" else "Payment recorded successfully")
            onSuccess()
        }
    }

    fun saveHomework(
        studentId: Long,
        subjectName: String,
        title: String,
        description: String,
        dueDate: String,
        priority: PriorityLevel,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date())

            repository.insertHomework(
                HomeworkEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    subjectName = subjectName,
                    title = title,
                    description = description,
                    givenDate = dateStr,
                    dueDate = dueDate,
                    priority = priority.name,
                    status = HomeworkStatus.PENDING.name
                )
            )

            val student = repository.getStudentById(studentId)
            if (student != null) {
                repository.insertNotification(
                    AppNotificationEntity(
                        userId = student.userId,
                        title = if (_isBengali.value) "নতুন হোমওয়ার্ক: $title" else "New Homework: $title",
                        message = "$subjectName: $description (জমা দেওয়ার শেষ সময়: $dueDate)",
                        type = "HOMEWORK",
                        date = dateStr
                    )
                )
            }

            showSnackbar(if (_isBengali.value) "হোমওয়ার্ক সফলভাবে যুক্ত করা হয়েছে" else "Homework assigned successfully")
            onSuccess()
        }
    }

    fun toggleHomeworkCompletion(homework: HomeworkEntity) {
        viewModelScope.launch {
            val newStatus = if (homework.status == HomeworkStatus.COMPLETED.name) {
                HomeworkStatus.PENDING.name
            } else {
                HomeworkStatus.COMPLETED.name
            }
            repository.updateHomework(homework.copy(status = newStatus))
            showSnackbar(if (_isBengali.value) "হোমওয়ার্ক স্ট্যাটাস আপডেট হয়েছে" else "Homework status updated")
        }
    }

    fun submitHomeworkAnswer(homework: HomeworkEntity, submissionText: String, attachmentName: String = "") {
        viewModelScope.launch {
            val fullSubmission = if (attachmentName.isNotBlank()) {
                "$submissionText\n📎 সংযুক্ত ফাইল: $attachmentName"
            } else {
                submissionText
            }
            repository.updateHomework(
                homework.copy(
                    studentSubmission = fullSubmission,
                    status = HomeworkStatus.COMPLETED.name
                )
            )
            showSnackbar(if (_isBengali.value) "বাড়ির কাজ/এসাইনমেন্ট সফলভাবে জমা দেওয়া হয়েছে!" else "Homework/Assignment submitted successfully!")
        }
    }

    fun saveRoutine(
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        subjectName: String,
        studentOrGroup: String,
        room: String,
        note: String,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            repository.insertRoutine(
                RoutineEntity(
                    teacherId = teacher.id,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    subjectName = subjectName,
                    studentOrGroup = studentOrGroup,
                    room = room,
                    note = note
                )
            )
            showSnackbar(if (_isBengali.value) "ক্লাস রুটিন সংরক্ষিত হয়েছে" else "Class routine added")
            onSuccess()
        }
    }

    fun cancelOrRescheduleRoutine(
        routine: RoutineEntity,
        isCancel: Boolean,
        reason: String,
        newTime: String = ""
    ) {
        viewModelScope.launch {
            val updated = routine.copy(
                isCancelled = isCancel,
                cancelReason = reason,
                startTime = if (newTime.isNotBlank()) newTime else routine.startTime
            )
            repository.updateRoutine(updated)

            // Alert notice
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date())
            repository.insertNotice(
                NoticeEntity(
                    teacherId = routine.teacherId,
                    title = if (isCancel) "⚠️ ক্লাস বাতিল: ${routine.subjectName}" else "🔄 ক্লাস সময় পরিবর্তন: ${routine.subjectName}",
                    description = "$reason (${routine.dayOfWeek} ${routine.startTime})",
                    date = dateStr,
                    priority = PriorityLevel.URGENT.name
                )
            )
            showSnackbar(if (_isBengali.value) "ক্লাসের নোটিশ সকল শিক্ষার্থীর কাছে পাঠানো হয়েছে" else "Class notification dispatched to students")
        }
    }

    fun saveLesson(
        studentId: Long,
        subjectName: String,
        chapter: String,
        topic: String,
        description: String,
        pageNo: String,
        teacherNote: String,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            repository.insertLesson(
                LessonEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    subjectName = subjectName,
                    date = sdf.format(Date()),
                    chapter = chapter,
                    topic = topic,
                    description = description,
                    pageNo = pageNo,
                    status = "In Progress",
                    teacherNote = teacherNote
                )
            )
            showSnackbar(if (_isBengali.value) "আজকের পড়া সংরক্ষিত হয়েছে" else "Today's lesson saved")
            onSuccess()
        }
    }

    fun saveNotice(
        title: String,
        description: String,
        targetType: String,
        targetStudentId: Long,
        priority: PriorityLevel,
        expiryDate: String,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date())

            repository.insertNotice(
                NoticeEntity(
                    teacherId = teacher.id,
                    title = title,
                    description = description,
                    date = dateStr,
                    targetType = targetType,
                    targetStudentId = targetStudentId,
                    priority = priority.name,
                    expiryDate = expiryDate
                )
            )
            showSnackbar(if (_isBengali.value) "নোটিশ প্রকাশিত হয়েছে" else "Notice published")
            onSuccess()
        }
    }

    fun savePerformance(
        studentId: Long,
        subjectName: String,
        topic: String,
        examName: String,
        score: Double,
        totalMarks: Double,
        comment: String,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            repository.insertPerformance(
                PerformanceEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    subjectName = subjectName,
                    topic = topic,
                    examName = examName,
                    score = score,
                    totalMarks = totalMarks,
                    examDate = sdf.format(Date()),
                    teacherComment = comment
                )
            )
            showSnackbar(if (_isBengali.value) "পরীক্ষার ফলাফল সংরক্ষিত হয়েছে" else "Performance score saved")
            onSuccess()
        }
    }

    fun saveStudentNote(
        studentId: Long,
        content: String,
        isVisibleToStudent: Boolean,
        onSuccess: () -> Unit
    ) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            repository.insertNote(
                StudentNoteEntity(
                    teacherId = teacher.id,
                    studentId = studentId,
                    content = content,
                    isVisibleToStudent = isVisibleToStudent,
                    date = sdf.format(Date())
                )
            )
            showSnackbar(if (_isBengali.value) "নোট সংরক্ষিত হয়েছে" else "Note added")
            onSuccess()
        }
    }

    fun sendMessage(
        recipientUserId: Long,
        recipientName: String,
        content: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertMessage(
                MessageEntity(
                    senderId = user.id,
                    senderName = user.fullName,
                    senderRole = user.role,
                    recipientId = recipientUserId,
                    recipientName = recipientName,
                    content = content
                )
            )
            showSnackbar(if (_isBengali.value) "বার্তা পাঠানো হয়েছে" else "Message sent")
            onSuccess()
        }
    }

    fun saveSubject(name: String, code: String, colorHex: String, onSuccess: () -> Unit) {
        val teacher = _currentTeacher.value ?: return
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    teacherId = teacher.id,
                    name = name,
                    code = code,
                    colorHex = colorHex
                )
            )
            showSnackbar(if (_isBengali.value) "বিষয় যুক্ত হয়েছে" else "Subject added")
            onSuccess()
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            showSnackbar(if (_isBengali.value) "বিষয় মুছে ফেলা হয়েছে" else "Subject deleted")
        }
    }

    fun saveTeacher(
        name: String,
        phone: String,
        email: String,
        qualification: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val username = "t_" + System.currentTimeMillis().toString().takeLast(6)
            val userId = repository.insertUser(
                UserEntity(
                    username = username,
                    passwordHash = "123456",
                    fullName = name,
                    phone = phone,
                    email = email,
                    role = UserRole.TEACHER.name
                )
            )
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            repository.insertTeacher(
                TeacherEntity(
                    userId = userId,
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    qualification = qualification,
                    joiningDate = sdf.format(Date()),
                    monthlyIncome = 0.0
                )
            )
            showSnackbar(if (_isBengali.value) "শিক্ষক সফলভাবে যুক্ত করা হয়েছে" else "Teacher added successfully")
            onSuccess()
        }
    }

    fun toggleTeacherStatus(teacher: TeacherEntity) {
        viewModelScope.launch {
            val newStatus = if (teacher.status == "ACTIVE") "INACTIVE" else "ACTIVE"
            repository.updateTeacher(teacher.copy(status = newStatus))
            showSnackbar(if (_isBengali.value) "শিক্ষকের স্ট্যাটাস পরিবর্তন করা হয়েছে: $newStatus" else "Teacher status changed to $newStatus")
        }
    }

    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            showSnackbar(if (_isBengali.value) "শিক্ষক মুছে ফেলা হয়েছে" else "Teacher removed")
        }
    }

    // --- Profile Updates & Avatar Management ---
    fun updateTeacherProfile(
        name: String,
        phone: String,
        email: String,
        qualification: String,
        address: String,
        avatarUrl: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val current = _currentTeacher.value ?: return@launch
            val updated = current.copy(
                name = name.trim().ifBlank { current.name },
                phone = phone.trim().ifBlank { current.phone },
                email = email.trim().ifBlank { current.email },
                qualification = qualification.trim().ifBlank { current.qualification },
                address = address.trim().ifBlank { current.address },
                avatarUrl = avatarUrl ?: current.avatarUrl
            )
            repository.updateTeacher(updated)
            _currentTeacher.value = updated

            val user = _currentUser.value
            if (user != null && (user.id == updated.userId || user.role == UserRole.TEACHER.name || user.role == UserRole.SUPER_ADMIN.name)) {
                val updatedUser = user.copy(
                    fullName = updated.name,
                    phone = updated.phone,
                    email = updated.email,
                    avatarUrl = updated.avatarUrl
                )
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
            }

            showSnackbar(if (_isBengali.value) "স্যারের প্রোফাইল সফলভাবে আপডেট করা হয়েছে!" else "Teacher profile updated successfully!")
            onSuccess?.invoke()
        }
    }

    fun updateTeacherAvatar(avatarUri: String) {
        viewModelScope.launch {
            val current = _currentTeacher.value ?: return@launch
            val updated = current.copy(avatarUrl = avatarUri)
            repository.updateTeacher(updated)
            _currentTeacher.value = updated

            val user = _currentUser.value
            if (user != null) {
                val updatedUser = user.copy(avatarUrl = avatarUri)
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
            }
            showSnackbar(if (_isBengali.value) "স্যারের প্রোফাইল ছবি সফলভাবে আপডেট হয়েছে" else "Profile photo updated")
        }
    }

    fun updateStudentProfile(
        studentId: Long,
        name: String,
        phone: String,
        email: String,
        className: String,
        section: String,
        roll: String,
        guardianName: String,
        guardianPhone: String,
        address: String,
        avatarUrl: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val current = repository.getStudentById(studentId) ?: _currentStudent.value ?: return@launch
            val updated = current.copy(
                name = name.trim().ifBlank { current.name },
                phone = phone.trim().ifBlank { current.phone },
                email = email.trim().ifBlank { current.email },
                className = className.trim().ifBlank { current.className },
                section = section.trim().ifBlank { current.section },
                roll = roll.trim().ifBlank { current.roll },
                guardianName = guardianName.trim().ifBlank { current.guardianName },
                guardianPhone = guardianPhone.trim().ifBlank { current.guardianPhone },
                address = address.trim().ifBlank { current.address },
                avatarUrl = avatarUrl ?: current.avatarUrl
            )
            repository.updateStudent(updated)
            if (_currentStudent.value?.id == updated.id) {
                _currentStudent.value = updated
            }
            if (_selectedStudent.value?.id == updated.id) {
                _selectedStudent.value = updated
            }

            val user = _currentUser.value
            if (user != null && user.id == updated.userId) {
                val updatedUser = user.copy(
                    fullName = updated.name,
                    phone = updated.phone,
                    email = updated.email,
                    avatarUrl = updated.avatarUrl
                )
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
            }

            showSnackbar(if (_isBengali.value) "শিক্ষার্থীর প্রোফাইল সফলভাবে আপডেট করা হয়েছে!" else "Student profile updated successfully!")
            onSuccess?.invoke()
        }
    }

    fun updateStudentAvatar(studentId: Long, avatarUri: String) {
        viewModelScope.launch {
            val current = repository.getStudentById(studentId) ?: _currentStudent.value ?: return@launch
            val updated = current.copy(avatarUrl = avatarUri)
            repository.updateStudent(updated)
            if (_currentStudent.value?.id == updated.id) {
                _currentStudent.value = updated
            }
            if (_selectedStudent.value?.id == updated.id) {
                _selectedStudent.value = updated
            }
            val user = _currentUser.value
            if (user != null && user.id == updated.userId) {
                val updatedUser = user.copy(avatarUrl = avatarUri)
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
            }
            showSnackbar(if (_isBengali.value) "শিক্ষার্থীর প্রোফাইল ছবি আপডেট করা হয়েছে" else "Student photo updated")
        }
    }

    fun seed500StudentsCapacity() {
        viewModelScope.launch(Dispatchers.IO) {
            val teacher = _currentTeacher.value ?: repository.allTeachers.firstOrNull()?.firstOrNull() ?: return@launch
            val currentList = repository.allStudents.firstOrNull() ?: emptyList()
            val currentCount = currentList.size
            if (currentCount >= 500) {
                withContext(Dispatchers.Main) {
                    showSnackbar(if (_isBengali.value) "ডাটাবেসে ইতিমধ্যে ৫০০ জনের বেশি শিক্ষার্থী সংরক্ষিত রয়েছে ($currentCount জন)!" else "Database already holds $currentCount students (500+ capacity active)!")
                }
                return@launch
            }

            val needed = (500 - currentCount).coerceAtLeast(0)
            val batchCount = needed.coerceAtMost(100)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayStr = sdf.format(Date())

            val banglaFirstNames = listOf("রাকিব", "তানভীর", "সাকিব", "মেহেদী", "নাঈম", "আরিফুল", "সাদিয়া", "ফারিহা", "নুসরাত", "মাহমুদ", "রায়হান", "শামীম", "ফারহান", "তাসনিম", "আফরিন", "হাসিব", "শাহরিয়ার", "ইশতিয়াক", "রাফসান", "সুমাইয়া")
            val banglaLastNames = listOf("হাসান", "ইসলাম", "খান", "চৌধুরী", "রহমান", "আহমেদ", "মোল্লা", "মণ্ডল", "শিকদার", "ভূঁইয়া", "বেগম", "আক্তার", "মাহমুদ")
            val classes = listOf("Class 8", "Class 9", "Class 10", "HSC 1st Year", "HSC 2nd Year")
            val sections = listOf("A", "B", "C", "D")

            for (i in 1..batchCount) {
                val num = currentCount + i
                val fName = banglaFirstNames[(num + 3) % banglaFirstNames.size]
                val lName = banglaLastNames[(num * 7) % banglaLastNames.size]
                val fullName = "$fName $lName"
                val studentCode = "STU-${1000 + num}"
                val username = "student_$num"
                val className = classes[num % classes.size]
                val section = sections[num % sections.size]
                val roll = ((num % 60) + 1).toString()

                val uId = repository.insertUser(
                    UserEntity(
                        username = username,
                        passwordHash = "123456",
                        fullName = fullName,
                        phone = "017" + String.format(Locale.US, "%08d", (10000000 + num)),
                        email = "student$num@tuition.bd",
                        role = UserRole.STUDENT.name
                    )
                )

                val sId = repository.insertStudent(
                    StudentEntity(
                        teacherId = teacher.id,
                        userId = uId,
                        name = fullName,
                        studentCode = studentCode,
                        phone = "017" + String.format(Locale.US, "%08d", (10000000 + num)),
                        email = "student$num@tuition.bd",
                        dob = "15/05/2008",
                        institution = "ঢাকা রেসিডেনসিয়াল মডেল কলেজ",
                        className = className,
                        section = section,
                        roll = roll,
                        guardianName = "জনাব $lName",
                        guardianPhone = "018" + String.format(Locale.US, "%08d", (20000000 + num)),
                        address = "মিরপুর, ঢাকা",
                        admissionDate = todayStr,
                        monthlyFee = 2000.0,
                        paymentDueDay = 10,
                        status = "ACTIVE"
                    )
                )

                if (i <= 10) {
                    repository.insertPayment(
                        PaymentEntity(
                            teacherId = teacher.id,
                            studentId = sId,
                            monthYear = "September 2026",
                            amount = 2000.0,
                            paidAmount = 2000.0,
                            dueAmount = 0.0,
                            paymentDate = todayStr,
                            dueDate = "10/09/2026",
                            paymentMethod = if (i % 2 == 0) "BKASH" else "NAGAD",
                            transactionId = "TRX${System.currentTimeMillis().toString().takeLast(6)}$num",
                            status = PaymentStatus.PAID.name
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                showSnackbar(if (_isBengali.value) "$batchCount জন শিক্ষার্থী সফলভাবে ডাটাবেসে যুক্ত হয়েছে! সর্বমোট: ${currentCount + batchCount} জন (৫০০+ ধারণক্ষমতা সক্রিয়)" else "Added $batchCount students. Total students: ${currentCount + batchCount} (500+ capacity ready)")
            }
        }
    }
}
