package com.example.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentListScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val allStudents by viewModel.teacherStudents.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredStudents = remember(allStudents, searchQuery, selectedFilter) {
        allStudents.filter { student ->
            val matchesSearch = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.studentCode.contains(searchQuery, ignoreCase = true) ||
                    student.phone.contains(searchQuery) ||
                    student.className.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "CLASS_10" -> student.className.contains("10", ignoreCase = true)
                "CLASS_9" -> student.className.contains("9", ignoreCase = true)
                "ACTIVE" -> student.status == "ACTIVE"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "শিক্ষার্থী তালিকা" else "Student List",
                subtitle = if (isBengali) "মোট শিক্ষার্থী: ${allStudents.size} জন" else "Total: ${allStudents.size} students"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.TeacherStudents)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.TeacherAddStudent) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Student")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "নতুন শিক্ষার্থী" else "Add Student", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBengali) "নাম, আইডি, রোল বা ফোন নম্বর দিয়ে খুঁজুন..." else "Search by name, ID, roll or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text(if (isBengali) "সকল (${allStudents.size})" else "All (${allStudents.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "CLASS_10",
                        onClick = { selectedFilter = "CLASS_10" },
                        label = { Text(if (isBengali) "দশম শ্রেণী (Class 10)" else "Class 10") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "CLASS_9",
                        onClick = { selectedFilter = "CLASS_9" },
                        label = { Text(if (isBengali) "নবম শ্রেণী (Class 9)" else "Class 9") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "ACTIVE",
                        onClick = { selectedFilter = "ACTIVE" },
                        label = { Text(if (isBengali) "সক্রিয় (Active)" else "Active") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredStudents.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    message = if (isBengali) "কোনো শিক্ষার্থী পাওয়া যায়নি" else "No students found",
                    description = if (isBengali) "অন্য কোনো নাম বা ফিল্টার দিয়ে খুঁজুন।" else "Try searching with a different keyword or filter.",
                    actionButtonText = if (isBengali) "নতুন শিক্ষার্থী যোগ করুন" else "Add New Student",
                    onActionClick = { viewModel.navigateTo(AppScreen.TeacherAddStudent) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredStudents) { student ->
                        StudentCardItem(
                            student = student,
                            onClick = {
                                viewModel.selectStudentForDetails(student)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCardItem(
    student: StudentEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusBadge(status = student.status)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "আইডি: ${student.studentCode} • ${student.className} (রোল: ${student.roll})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "🏫 ${student.institution}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📞 ${student.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ফি: ৳${student.monthlyFee.toInt()}/মাস",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("15/05/2009") }
    var institution by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("Class 10") }
    var section by remember { mutableStateOf("A") }
    var roll by remember { mutableStateOf("01") }
    var guardianName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var monthlyFeeStr by remember { mutableStateOf("2500") }
    var paymentDueDayStr by remember { mutableStateOf("10") }
    var loginUsername by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("123456") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBengali) "নতুন শিক্ষার্থী যোগ" else "Add New Student") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isBengali) "শিক্ষার্থীর বিস্তারিত তথ্য" else "Student Information",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMsg = null
                    if (loginUsername.isBlank() && it.isNotBlank()) {
                        loginUsername = "std_" + it.lowercase().replace(" ", "").take(8)
                    }
                },
                label = { Text(if (isBengali) "শিক্ষার্থীর নাম *" else "Student Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text(if (isBengali) "শ্রেণী *" else "Class *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text(if (isBengali) "শাখা / সেকশন" else "Section") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = roll,
                    onValueChange = { roll = it },
                    label = { Text(if (isBengali) "রোল *" else "Roll *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = institution,
                onValueChange = { institution = it },
                label = { Text(if (isBengali) "স্কুল / কলেজ / মাদ্রাসা *" else "Institution *") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorMsg = null },
                    label = { Text(if (isBengali) "শিক্ষার্থীর মোবাইল *" else "Phone Number *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text(if (isBengali) "জন্ম তারিখ" else "Date of Birth") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = if (isBengali) "অভিভাবকের তথ্য" else "Guardian Information",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = guardianName,
                    onValueChange = { guardianName = it },
                    label = { Text(if (isBengali) "অভিভাবকের নাম *" else "Guardian Name *") },
                    leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text(if (isBengali) "অভিভাবকের ফোন *" else "Guardian Phone *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(if (isBengali) "ঠিকানা" else "Address") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = if (isBengali) "টিউশন ফি ও পেমেন্ট তারিখ" else "Tuition Fee & Payment Day",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = monthlyFeeStr,
                    onValueChange = { monthlyFeeStr = it },
                    label = { Text(if (isBengali) "মাসিক বেতন (টাকা) *" else "Monthly Fee (৳) *") },
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = paymentDueDayStr,
                    onValueChange = { paymentDueDayStr = it },
                    label = { Text(if (isBengali) "পরিশোধের শেষ দিন (যেমন ১০)" else "Due Day (e.g. 10)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = if (isBengali) "শিক্ষার্থীর লগইন তথ্য (Student Login)" else "Student Login Credentials",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = loginUsername,
                    onValueChange = { loginUsername = it },
                    label = { Text(if (isBengali) "ইউজারনেম" else "Username") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = loginPass,
                    onValueChange = { loginPass = it },
                    label = { Text(if (isBengali) "পাসওয়ার্ড" else "Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        errorMsg = if (isBengali) "শিক্ষার্থীর নাম ও ফোন নম্বর দিন" else "Please enter student name and phone"
                        return@Button
                    }
                    val fee = monthlyFeeStr.toDoubleOrNull() ?: 2000.0
                    val dueDay = paymentDueDayStr.toIntOrNull() ?: 10

                    viewModel.addStudent(
                        name = name,
                        phone = phone,
                        email = email,
                        dob = dob,
                        institution = institution.ifBlank { "আইডিয়াল স্কুল অ্যান্ড কলেজ" },
                        className = className,
                        section = section,
                        roll = roll,
                        guardianName = guardianName.ifBlank { "অভিভাবক" },
                        guardianPhone = guardianPhone.ifBlank { phone },
                        address = address.ifBlank { "ঢাকা" },
                        monthlyFee = fee,
                        paymentDueDay = dueDay,
                        loginUsername = loginUsername,
                        loginPass = loginPass,
                        onSuccess = {
                            viewModel.goBack()
                        }
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isBengali) "সংরক্ষণ করুন (Save Student)" else "Save Student", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val student by viewModel.selectedStudent.collectAsState()

    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("শিক্ষার্থী নির্বাচন করা হয়নি")
        }
        return
    }

    val std = student!!
    val attendances by viewModel.selectedStudentAttendance.collectAsState(initial = emptyList())
    val payments by viewModel.selectedStudentPayments.collectAsState(initial = emptyList())
    val homeworks by viewModel.selectedStudentHomework.collectAsState(initial = emptyList())
    val notes by viewModel.selectedStudentNotes.collectAsState(initial = emptyList())
    val performances by viewModel.selectedStudentPerformance.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        if (isBengali) "পরিচিতি" else "Overview",
        if (isBengali) "উপস্থিতি" else "Attendance",
        if (isBengali) "হোমওয়ার্ক" else "Homework",
        if (isBengali) "বেতন" else "Payments",
        if (isBengali) "ফলাফল" else "Performance",
        if (isBengali) "নোট" else "Notes"
    )

    var showAddNoteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(std.name) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteStudent(std) {
                            viewModel.goBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftRed)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Card Header
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = std.name.take(1),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = std.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "আইডি: ${std.studentCode} • ${std.className} (রোল: ${std.roll})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "🏫 ${std.institution}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> StudentOverviewTab(student = std)
                    1 -> StudentAttendanceTab(attendances = attendances, onMarkAttendance = { status ->
                        viewModel.markAttendance(
                            studentId = std.id,
                            status = status,
                            className = std.className,
                            subjectName = "সাধারণ"
                        )
                    })
                    2 -> StudentHomeworkTab(homeworks = homeworks)
                    3 -> StudentPaymentsTab(payments = payments, onSelectReceipt = { p ->
                        viewModel.selectPaymentForReceipt(p)
                    })
                    4 -> StudentPerformanceTab(performances = performances)
                    5 -> StudentNotesTab(notes = notes, onAddNoteClick = { showAddNoteDialog = true })
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        var isVisibleToStudent by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text(if (isBengali) "শিক্ষার্থীর জন্য নোট লিখুন" else "Add Student Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("নোটের বিষয়বস্তু...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isVisibleToStudent,
                            onCheckedChange = { isVisibleToStudent = it }
                        )
                        Text(if (isBengali) "শিক্ষার্থীকে দেখতে দেওয়া হোক" else "Visible to Student")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (noteText.isNotBlank()) {
                        viewModel.saveStudentNote(
                            studentId = std.id,
                            content = noteText,
                            isVisibleToStudent = isVisibleToStudent,
                            onSuccess = { showAddNoteDialog = false }
                        )
                    }
                }) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddNoteDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Payment Receipt Modal
    val selectedPayment by viewModel.selectedPayment.collectAsState()
    if (selectedPayment != null) {
        PaymentReceiptDialog(
            payment = selectedPayment!!,
            studentName = std.name,
            studentCode = std.studentCode,
            teacherName = "আব্দুল্লাহ স্যার",
            onDismiss = { viewModel.clearSelectedPayment() }
        )
    }
}

@Composable
fun StudentOverviewTab(student: StudentEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ব্যক্তিগত ও যোগাযোগ তথ্য", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                InfoItem("মোবাইল নম্বর:", student.phone)
                InfoItem("ইমেইল:", student.email.ifBlank { "উল্লেখ নেই" })
                InfoItem("জন্ম তারিখ:", student.dob)
                InfoItem("ঠিকানা:", student.address)
                InfoItem("ভর্তির তারিখ:", student.admissionDate)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("অভিভাবকের তথ্য", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                InfoItem("অভিভাবকের নাম:", student.guardianName)
                InfoItem("অভিভাবকের মোবাইল:", student.guardianPhone)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("টিউশন ও ফি সংক্রান্ত", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                InfoItem("মাসিক টিউশন ফি:", "৳${student.monthlyFee.toInt()}")
                InfoItem("ফি পরিশোধের নির্ধারিত দিন:", "প্রতি মাসের ${student.paymentDueDay} তারিখ")
                InfoItem("স্ট্যাটাস:", student.status)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun StudentAttendanceTab(
    attendances: List<AttendanceEntity>,
    onMarkAttendance: (AttendanceStatus) -> Unit
) {
    val total = attendances.size
    val present = attendances.count { it.status == AttendanceStatus.PRESENT.name }
    val absent = attendances.count { it.status == AttendanceStatus.ABSENT.name }
    val late = attendances.count { it.status == AttendanceStatus.LATE.name }
    val percent = if (total > 0) (present.toDouble() / total * 100).toInt() else 100

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Attendance metric card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$percent%", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                    Text("উপস্থিতির হার", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$present", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SoftGreen))
                    Text("উপস্থিত", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$absent", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SoftRed))
                    Text("অনুপস্থিত", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$late", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = AmberDark))
                    Text("দেরি", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Quick attendance marking buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onMarkAttendance(AttendanceStatus.PRESENT) },
                colors = ButtonDefaults.buttonColors(containerColor = SoftGreen),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("উপস্থিত ✓", fontSize = 12.sp)
            }
            Button(
                onClick = { onMarkAttendance(AttendanceStatus.ABSENT) },
                colors = ButtonDefaults.buttonColors(containerColor = SoftRed),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("অনুপস্থিত ✗", fontSize = 12.sp)
            }
            Button(
                onClick = { onMarkAttendance(AttendanceStatus.LATE) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("দেরি ⏰", fontSize = 12.sp)
            }
        }

        // Attendance history list
        if (attendances.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.EventBusy,
                message = "এখনো কোনো উপস্থিতি রেকর্ড নেই"
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(attendances) { att ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("তারিখ: ${att.date}", fontWeight = FontWeight.Bold)
                                if (att.note.isNotBlank()) {
                                    Text("মন্তব্য: ${att.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            StatusBadge(status = att.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentHomeworkTab(homeworks: List<HomeworkEntity>) {
    if (homeworks.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Assignment,
            message = "কোনো হোমওয়ার্ক দেওয়া নেই"
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(homeworks) { hw ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(hw.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            StatusBadge(status = hw.status)
                        }
                        Text(hw.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("দেওয়া হয়েছে: ${hw.givenDate}", style = MaterialTheme.typography.labelSmall)
                            Text("জমা: ${hw.dueDate}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentPaymentsTab(
    payments: List<PaymentEntity>,
    onSelectReceipt: (PaymentEntity) -> Unit
) {
    if (payments.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Payments,
            message = "কোনো পেমেন্ট রেকর্ড পাওয়া যায়নি"
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(payments) { p ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.monthYear, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            StatusBadge(status = p.status)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("পরিশোধিত: ৳${p.paidAmount} (${p.paymentMethod})", style = MaterialTheme.typography.bodyMedium, color = SoftGreen)
                            if (p.dueAmount > 0) {
                                Text("বকেয়া: ৳${p.dueAmount}", style = MaterialTheme.typography.bodyMedium, color = SoftRed)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("তারিখ: ${p.paymentDate.ifBlank { p.dueDate }}", style = MaterialTheme.typography.labelSmall)
                            TextButton(onClick = { onSelectReceipt(p) }) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("মানি রিসিট দেখুন", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentPerformanceTab(performances: List<PerformanceEntity>) {
    if (performances.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Leaderboard,
            message = "এখনো কোনো পরীক্ষার ফলাফল রেকর্ড করা হয়নি"
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(performances) { perf ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(perf.examName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "${perf.score} / ${perf.totalMarks.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )
                        }
                        Text("${perf.subjectName} • ${perf.topic}", style = MaterialTheme.typography.bodyMedium)
                        if (perf.teacherComment.isNotBlank()) {
                            Text("শিক্ষকের মন্তব্য: \"${perf.teacherComment}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("তারিখ: ${perf.examDate}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentNotesTab(
    notes: List<StudentNoteEntity>,
    onAddNoteClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onAddNoteClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AddComment, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("নতুন নোট যুক্ত করুন")
        }

        if (notes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.NoteAlt,
                message = "কোনো নোট নেই"
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes) { note ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(note.content, style = MaterialTheme.typography.bodyMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("তারিখ: ${note.date}", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    if (note.isVisibleToStudent) "শিক্ষার্থী দেখতে পাবে ✓" else "শিক্ষকের ব্যক্তিগত নোট 🔒",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (note.isVisibleToStudent) SoftGreen else AmberDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
