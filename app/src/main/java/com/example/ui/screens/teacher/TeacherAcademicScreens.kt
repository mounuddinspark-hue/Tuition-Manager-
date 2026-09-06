package com.example.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())
    val attendances by viewModel.teacherAttendance.collectAsState(initial = emptyList())

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var selectedDate by remember { mutableStateOf(sdf.format(Date())) }

    // Map today's attendance status for each student
    val studentAttendanceMap = remember(attendances, selectedDate) {
        attendances.filter { it.date == selectedDate }
            .associateBy({ it.studentId }, { it })
    }

    val presentCount = studentAttendanceMap.values.count { it.status == AttendanceStatus.PRESENT.name }
    val absentCount = studentAttendanceMap.values.count { it.status == AttendanceStatus.ABSENT.name }
    val lateCount = studentAttendanceMap.values.count { it.status == AttendanceStatus.LATE.name }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "দৈনিক উপস্থিতি (Attendance)" else "Daily Attendance",
                subtitle = "তারিখ: $selectedDate"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.TeacherAttendance)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Summary Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = if (isBengali) "উপস্থিত" else "Present",
                    value = "$presentCount",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SoftGreen,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = if (isBengali) "অনুপস্থিত" else "Absent",
                    value = "$absentCount",
                    icon = Icons.Default.Cancel,
                    accentColor = SoftRed,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = if (isBengali) "দেরি" else "Late",
                    value = "$lateCount",
                    icon = Icons.Default.Schedule,
                    accentColor = AmberDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header with Mark All Present
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "শিক্ষার্থী তালিকা (${students.size})" else "Students (${students.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = {
                        students.forEach { std ->
                            viewModel.markAttendance(
                                studentId = std.id,
                                status = AttendanceStatus.PRESENT,
                                className = std.className,
                                subjectName = "সাধারণ"
                            )
                        }
                    }
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBengali) "সবাইকে উপস্থিত করুন" else "Mark All Present")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (students.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PeopleOutline,
                    message = if (isBengali) "কোনো শিক্ষার্থী যুক্ত করা নেই" else "No students registered yet",
                    actionButtonText = if (isBengali) "শিক্ষার্থী যোগ করুন" else "Add Student",
                    onActionClick = { viewModel.navigateTo(AppScreen.TeacherAddStudent) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(students) { std ->
                        val currentAtt = studentAttendanceMap[std.id]
                        val status = currentAtt?.status ?: "NONE"

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = std.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${std.className} (রোল: ${std.roll}) • ${std.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (status != "NONE") {
                                        StatusBadge(status = status)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.markAttendance(
                                                studentId = std.id,
                                                status = AttendanceStatus.PRESENT,
                                                className = std.className,
                                                subjectName = "সাধারণ"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (status == AttendanceStatus.PRESENT.name) SoftGreen else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (status == AttendanceStatus.PRESENT.name) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text(if (isBengali) "উপস্থিত" else "Present", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.markAttendance(
                                                studentId = std.id,
                                                status = AttendanceStatus.ABSENT,
                                                className = std.className,
                                                subjectName = "সাধারণ"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (status == AttendanceStatus.ABSENT.name) SoftRed else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (status == AttendanceStatus.ABSENT.name) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text(if (isBengali) "অনুপস্থিত" else "Absent", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.markAttendance(
                                                studentId = std.id,
                                                status = AttendanceStatus.LATE,
                                                className = std.className,
                                                subjectName = "সাধারণ"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (status == AttendanceStatus.LATE.name) AmberAccent else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (status == AttendanceStatus.LATE.name) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text(if (isBengali) "দেরি" else "Late", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherRoutineScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val routines by viewModel.teacherRoutines.collectAsState(initial = emptyList())
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())

    val daysOfWeek = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val daysBangla = listOf("শনিবার", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার")

    val todayDayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
    var selectedDay by remember { mutableStateOf(todayDayOfWeek) }

    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var routineToModify by remember { mutableStateOf<RoutineEntity?>(null) }

    val filteredRoutines = routines.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "সাপ্তাহিক ক্লাস রুটিন" else "Weekly Routine",
                subtitle = if (isBengali) "আজ: $todayDayOfWeek" else "Today: $todayDayOfWeek"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.TeacherRoutine)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRoutineDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Class")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "ক্লাস যোগ" else "Add Class", fontWeight = FontWeight.Bold)
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

            // Days Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysOfWeek.indices.toList()) { index ->
                    val dayEng = daysOfWeek[index]
                    val dayBn = daysBangla[index]
                    val isSelected = selectedDay.equals(dayEng, ignoreCase = true)
                    val isToday = todayDayOfWeek.equals(dayEng, ignoreCase = true)

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDay = dayEng },
                        label = {
                            Text(
                                text = if (isToday) "⭐ ${if (isBengali) dayBn else dayEng}" else if (isBengali) dayBn else dayEng,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredRoutines.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CalendarToday,
                    message = if (isBengali) "$selectedDay এ কোনো নির্ধারিত ক্লাস নেই" else "No classes scheduled for $selectedDay",
                    description = if (isBengali) "নতুন ক্লাস শিডিউল করতে নিচের বোতামে চাপুন।" else "Schedule a class for this day.",
                    actionButtonText = if (isBengali) "+ ক্লাস যুক্ত করুন" else "+ Schedule Class",
                    onActionClick = { showAddRoutineDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRoutines) { r ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (r.isCancelled) SoftRedLight else EmeraldContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (r.isCancelled) Icons.Default.Cancel else Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = if (r.isCancelled) SoftRed else EmeraldPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = r.subjectName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "⏰ ${r.startTime} - ${r.endTime}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    if (r.isCancelled) {
                                        StatusBadge(status = "CANCELLED")
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "👥 শিক্ষার্থী / ব্যাচ: ${r.studentOrGroup}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (r.room.isNotBlank()) {
                                    Text(
                                        text = "📍 স্থান: ${r.room}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (r.note.isNotBlank()) {
                                    Text(
                                        text = "📝 বিষয়: ${r.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (r.isCancelled && r.cancelReason.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = SoftRedLight,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "বাতিলের কারণ: ${r.cancelReason}",
                                            color = SoftRed,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { routineToModify = r }) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBengali) "বাতিল / পরিবর্তন" else "Cancel / Reschedule")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Routine Dialog
    if (showAddRoutineDialog) {
        var subjectName by remember { mutableStateOf("গণিত (Mathematics)") }
        var startTime by remember { mutableStateOf("05:00 PM") }
        var endTime by remember { mutableStateOf("06:30 PM") }
        var studentOrGroup by remember { mutableStateOf("দশম শ্রেণী ব্যাচ ১") }
        var room by remember { mutableStateOf("হোম ব্যাচ রুম ১") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRoutineDialog = false },
            title = { Text(if (isBengali) "নতুন ক্লাস শিডিউল ($selectedDay)" else "Add Class ($selectedDay)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text(if (isBengali) "বিষয়" else "Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("শুরুর সময়") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("শেষের সময়") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = studentOrGroup,
                        onValueChange = { studentOrGroup = it },
                        label = { Text(if (isBengali) "শিক্ষার্থী বা ব্যাচের নাম" else "Student or Batch") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text(if (isBengali) "রুম / গুগল মিট লিংক" else "Room / Online Link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(if (isBengali) "টপিক বা মন্তব্য" else "Topic / Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveRoutine(
                        dayOfWeek = selectedDay,
                        startTime = startTime,
                        endTime = endTime,
                        subjectName = subjectName,
                        studentOrGroup = studentOrGroup,
                        room = room,
                        note = note,
                        onSuccess = { showAddRoutineDialog = false }
                    )
                }) {
                    Text(if (isBengali) "যুক্ত করুন" else "Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddRoutineDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Cancel / Reschedule Dialog
    if (routineToModify != null) {
        val r = routineToModify!!
        var isCancel by remember { mutableStateOf(true) }
        var reason by remember { mutableStateOf("জরুরি কাজের জন্য আজকের ক্লাস স্থগিত করা হলো।") }
        var newTime by remember { mutableStateOf(r.startTime) }

        AlertDialog(
            onDismissRequest = { routineToModify = null },
            title = { Text(if (isBengali) "ক্লাস বাতিল বা সময় পরিবর্তন" else "Cancel or Reschedule Class") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${r.subjectName} (${r.dayOfWeek} ${r.startTime})",
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isCancel,
                            onClick = { isCancel = true }
                        )
                        Text(if (isBengali) "ক্লাস বাতিল করুন" else "Cancel Class")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = !isCancel,
                            onClick = { isCancel = false }
                        )
                        Text(if (isBengali) "সময় পরিবর্তন" else "Reschedule")
                    }

                    if (!isCancel) {
                        OutlinedTextField(
                            value = newTime,
                            onValueChange = { newTime = it },
                            label = { Text("নতুন সময়") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("শিক্ষার্থীদের জন্য মেসেজ / কারণ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOrRescheduleRoutine(
                            routine = r,
                            isCancel = isCancel,
                            reason = reason,
                            newTime = if (!isCancel) newTime else ""
                        )
                        routineToModify = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isCancel) SoftRed else MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isCancel) "ক্লাস বাতিল নিশ্চিত করুন" else "সময় পরিবর্তন নিশ্চিত করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { routineToModify = null }) {
                    Text("ফিরে যান")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLessonScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val lessons by viewModel.teacherLessons.collectAsState(initial = emptyList())
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())
    var showAddLessonDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "আজকের পড়া (Daily Lessons)" else "Daily Lessons",
                subtitle = if (isBengali) "ক্লাসে যা পড়ানো হয়েছে" else "Lessons covered in class"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.TeacherAttendance)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLessonDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "পড়া যোগ" else "Add Lesson", fontWeight = FontWeight.Bold)
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

            if (lessons.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MenuBook,
                    message = if (isBengali) "কোনো পড়া রেকর্ড করা নেই" else "No lessons recorded",
                    description = if (isBengali) "আজকের ক্লাসে যা পড়ানো হয়েছে তা রেকর্ড করে রাখুন।" else "Record today's teaching details.",
                    actionButtonText = if (isBengali) "+ পড়া যোগ করুন" else "+ Add Lesson",
                    onActionClick = { showAddLessonDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(lessons) { l ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = l.subjectName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "📅 ${l.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "${l.chapter} : ${l.topic}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )

                                Text(
                                    text = l.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (l.pageNo.isNotBlank()) {
                                    Text(
                                        text = "📖 পৃষ্ঠা নম্বর: ${l.pageNo}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (l.teacherNote.isNotBlank()) {
                                    Text(
                                        text = "💡 শিক্ষকের মন্তব্য: ${l.teacherNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddLessonDialog) {
        var selectedStudentId by remember { mutableLongStateOf(students.firstOrNull()?.id ?: 0L) }
        var subjectName by remember { mutableStateOf("গণিত (Mathematics)") }
        var chapter by remember { mutableStateOf("অধ্যায় ৩ - বীজগাণিতিক রাশি") }
        var topic by remember { mutableStateOf("সূত্র ও মান নির্ণয়") }
        var description by remember { mutableStateOf("অনুশীলনী ৩.১ এর উদাহরণ ও অংক বুঝিয়ে দেওয়া হয়েছে।") }
        var pageNo by remember { mutableStateOf("৪৪-৪৮") }
        var teacherNote by remember { mutableStateOf("ভালো বুঝতে পেরেছে।") }

        AlertDialog(
            onDismissRequest = { showAddLessonDialog = false },
            title = { Text(if (isBengali) "আজকের পড়া যুক্ত করুন" else "Record Today's Lesson") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("বিষয়") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("অধ্যায়") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("টপিক") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("বিস্তারিত বিবরণ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = pageNo,
                        onValueChange = { pageNo = it },
                        label = { Text("পৃষ্ঠা নম্বর") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teacherNote,
                        onValueChange = { teacherNote = it },
                        label = { Text("শিক্ষকের মন্তব্য") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveLesson(
                        studentId = selectedStudentId,
                        subjectName = subjectName,
                        chapter = chapter,
                        topic = topic,
                        description = description,
                        pageNo = pageNo,
                        teacherNote = teacherNote,
                        onSuccess = { showAddLessonDialog = false }
                    )
                }) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddLessonDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeworkScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val homeworks by viewModel.teacherHomework.collectAsState(initial = emptyList())
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "হোমওয়ার্ক ও বাড়ির কাজ" else "Homework & Assignments",
                subtitle = if (isBengali) "মোট কাজ: ${homeworks.size} টি" else "Total: ${homeworks.size}"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.TeacherAttendance)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "কাজ দিন" else "Assign", fontWeight = FontWeight.Bold)
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

            if (homeworks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Assignment,
                    message = if (isBengali) "কোনো হোমওয়ার্ক দেওয়া নেই" else "No homework assigned",
                    actionButtonText = if (isBengali) "+ হোমওয়ার্ক দিন" else "+ Assign Homework",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(homeworks) { hw ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hw.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "বিষয়: ${hw.subjectName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    StatusBadge(status = hw.status)
                                }

                                Text(
                                    text = hw.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (hw.studentSubmission.isNotBlank()) {
                                    Surface(
                                        color = SoftGreenLight,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "শিক্ষার্থীর জমা দেওয়া উত্তর: ${hw.studentSubmission}",
                                            color = SoftGreen,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "জমা দেওয়ার শেষ সময়: ${hw.dueDate}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )

                                    FilledTonalButton(
                                        onClick = { viewModel.toggleHomeworkCompletion(hw) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (hw.status == HomeworkStatus.COMPLETED.name) "চলমান করুন" else "সম্পন্ন মার্ক করুন",
                                            fontSize = 12.sp
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

    if (showAddDialog) {
        var selectedStudentId by remember { mutableLongStateOf(students.firstOrNull()?.id ?: 0L) }
        var subjectName by remember { mutableStateOf("গণিত (Mathematics)") }
        var title by remember { mutableStateOf("অনুশীলনী ৩.১ এর সব অংক সমাধান") }
        var description by remember { mutableStateOf("পৃষ্ঠা ৪৬ এর ৬, ৭, ৮, এবং ১০ নম্বর সৃজনশীল সমাধান খাতায় করে আনবে।") }
        var dueDate by remember { mutableStateOf("আগামীকাল (Tomorrow)") }
        var priority by remember { mutableStateOf(PriorityLevel.IMPORTANT) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (isBengali) "নতুন হোমওয়ার্ক দিন" else "Assign Homework") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("বিষয়") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("শিরোনাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("বিস্তারিত কাজ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("জমা দেওয়ার তারিখ / সময়") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveHomework(
                        studentId = selectedStudentId,
                        subjectName = subjectName,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        onSuccess = { showAddDialog = false }
                    )
                }) {
                    Text("হোমওয়ার্ক দিন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
