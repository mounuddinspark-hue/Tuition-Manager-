package com.example.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun TeacherDashboardScreen(viewModel: TuitionViewModel) {
    val teacher by viewModel.currentTeacher.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()

    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())
    val routines by viewModel.teacherRoutines.collectAsState(initial = emptyList())
    val attendances by viewModel.teacherAttendance.collectAsState(initial = emptyList())
    val payments by viewModel.teacherPayments.collectAsState(initial = emptyList())
    val homeworks by viewModel.teacherHomework.collectAsState(initial = emptyList())
    val notices by viewModel.teacherNotices.collectAsState(initial = emptyList())

    // Calculations
    val todayDayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
    val todayRoutines = routines.filter { it.dayOfWeek.equals(todayDayOfWeek, ignoreCase = true) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val todayStr = sdf.format(Date())
    val todayAttendance = attendances.filter { it.date == todayStr }
    val presentTodayCount = todayAttendance.count { it.status == AttendanceStatus.PRESENT.name }
    val absentTodayCount = todayAttendance.count { it.status == AttendanceStatus.ABSENT.name }

    val pendingPaymentsCount = payments.count { it.status == PaymentStatus.DUE.name || it.status == PaymentStatus.OVERDUE.name }
    val pendingHomeworkCount = homeworks.count { it.status == HomeworkStatus.PENDING.name }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = teacher?.name ?: (if (isBengali) "কন্ট্রোল ড্যাশবোর্ড" else "Control Dashboard"),
                subtitle = teacher?.qualification ?: "Tuition Manager BD"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.TeacherDashboard)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.TeacherAddStudent) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "শিক্ষার্থী যোগ" else "Add Student", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Welcome Header Card (Click to view full Teacher Profile & Admin Hub)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.TeacherProfile) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = teacher?.name ?: (if (isBengali) "শিক্ষক ও পরিচালক" else "Teacher & Instructor"),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${teacher?.qualification ?: "B.Sc / M.Sc"} • ${teacher?.phone ?: "01711223344"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(6.dp).size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick Action Grid
            item {
                SectionHeader(
                    title = if (isBengali) "কুইক অ্যাকশন (Quick Actions)" else "Quick Actions",
                    subtitle = if (isBengali) "প্রয়োজনীয় কাজ দ্রুত সম্পন্ন করুন" else "Fast shortcuts for frequent tasks"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.PersonAdd,
                        label = if (isBengali) "শিক্ষার্থী যোগ" else "Add Student",
                        bgColor = EmeraldContainer,
                        iconTint = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherAddStudent) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.CheckCircle,
                        label = if (isBengali) "উপস্থিতি গ্রহণ" else "Attendance",
                        bgColor = SoftGreenLight,
                        iconTint = SoftGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherAttendance) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Assignment,
                        label = if (isBengali) "হোমওয়ার্ক" else "Homework",
                        bgColor = SoftBlueLight,
                        iconTint = SoftBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherHomework) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.CalendarMonth,
                        label = if (isBengali) "ক্লাস রুটিন" else "Class Routine",
                        bgColor = AmberLight,
                        iconTint = AmberDark,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherRoutine) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Payments,
                        label = if (isBengali) "বেতন গ্রহণ" else "Add Payment",
                        bgColor = EmeraldContainer,
                        iconTint = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherPayments) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Campaign,
                        label = if (isBengali) "নোটিশ তৈরি" else "New Notice",
                        bgColor = PurpleLight,
                        iconTint = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherNotices) }
                    )
                }
            }

            // Stat Metrics Cards Grid
            item {
                SectionHeader(
                    title = if (isBengali) "পরিসংখ্যান ও অবস্থা (Overview)" else "Overview Stats"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = if (isBengali) "মোট শিক্ষার্থী" else "Total Students",
                        value = "${students.size}",
                        subtitle = if (isBengali) "সক্রিয় ব্যাচ" else "Active students",
                        icon = Icons.Default.Groups,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherStudents) }
                    )
                    StatMetricCard(
                        title = if (isBengali) "আজকের ক্লাস" else "Today's Classes",
                        value = "${todayRoutines.size}",
                        subtitle = todayDayOfWeek,
                        icon = Icons.Default.CalendarToday,
                        accentColor = AmberAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherRoutine) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = if (isBengali) "আজ উপস্থিত" else "Present Today",
                        value = "$presentTodayCount",
                        subtitle = if (isBengali) "অনুপস্থিত: $absentTodayCount" else "Absent: $absentTodayCount",
                        icon = Icons.Default.CheckCircle,
                        accentColor = SoftGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherAttendance) }
                    )
                    StatMetricCard(
                        title = if (isBengali) "বকেয়া বেতন" else "Pending Fees",
                        value = "$pendingPaymentsCount",
                        subtitle = if (isBengali) "তাগাদা দিন" else "Send reminder",
                        icon = Icons.Default.Warning,
                        accentColor = SoftRed,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherPayments) }
                    )
                }
            }

            // Today's Classes Section ("আজকের ক্লাস")
            item {
                SectionHeader(
                    title = if (isBengali) "আজকের ক্লাস ($todayDayOfWeek)" else "Today's Classes ($todayDayOfWeek)",
                    actionText = if (isBengali) "সব রুটিন দেখুন" else "Full Routine",
                    onActionClick = { viewModel.navigateTo(AppScreen.TeacherRoutine) }
                )

                if (todayRoutines.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.EventBusy,
                            message = if (isBengali) "আজ কোনো ক্লাস নেই" else "No classes scheduled for today",
                            description = if (isBengali) "সাপ্তাহিক রুটিন দেখতে রুটিন পেজে যান।" else "Check your weekly routine schedule."
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        todayRoutines.forEach { routine ->
                            ClassScheduleCard(routine = routine)
                        }
                    }
                }
            }

            // Quick Students Carousel
            item {
                SectionHeader(
                    title = if (isBengali) "শিক্ষার্থীবৃন্দ (Students)" else "Students",
                    actionText = if (isBengali) "সকল শিক্ষার্থী (${students.size})" else "View All (${students.size})",
                    onActionClick = { viewModel.navigateTo(AppScreen.TeacherStudents) }
                )

                if (students.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.School,
                        message = if (isBengali) "এখনো কোনো শিক্ষার্থী যোগ করা হয়নি" else "No students added yet",
                        actionButtonText = if (isBengali) "+ শিক্ষার্থী যোগ করুন" else "+ Add Student",
                        onActionClick = { viewModel.navigateTo(AppScreen.TeacherAddStudent) }
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(students) { student ->
                            StudentMiniCard(student = student, onClick = {
                                viewModel.selectStudentForDetails(student)
                            })
                        }
                    }
                }
            }

            // Recent Notices
            item {
                SectionHeader(
                    title = if (isBengali) "সাম্প্রতিক নোটিশ (Notices)" else "Recent Notices",
                    actionText = if (isBengali) "নতুন নোটিশ" else "Add Notice",
                    onActionClick = { viewModel.navigateTo(AppScreen.TeacherNotices) }
                )

                if (notices.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.NotificationsNone,
                        message = if (isBengali) "কোনো সক্রিয় নোটিশ নেই" else "No active notices posted"
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notices.take(2).forEach { notice ->
                            NoticeCardItem(notice = notice)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ClassScheduleCard(routine: RoutineEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (routine.isCancelled) SoftRedLight else EmeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (routine.isCancelled) Icons.Default.Cancel else Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (routine.isCancelled) SoftRed else EmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = routine.subjectName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (routine.isCancelled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(বাতিল / Cancelled)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⏰ ${routine.startTime} - ${routine.endTime} • 👤 ${routine.studentOrGroup}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (routine.room.isNotBlank()) {
                    Text(
                        text = "📍 ${routine.room}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StudentMiniCard(
    student: StudentEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = student.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = student.className,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldContainer
            ) {
                Text(
                    text = "৳${student.monthlyFee.toInt()}/মাস",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun NoticeCardItem(notice: NoticeEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (notice.priority) {
                            "URGENT" -> SoftRedLight
                            "IMPORTANT" -> AmberLight
                            else -> SoftBlueLight
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = when (notice.priority) {
                        "URGENT" -> SoftRed
                        "IMPORTANT" -> AmberDark
                        else -> SoftBlue
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notice.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📅 ${notice.date}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
