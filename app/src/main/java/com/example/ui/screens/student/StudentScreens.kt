package com.example.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.navigation.AppScreen
import com.example.ui.screens.teacher.ClassScheduleCard
import com.example.ui.screens.teacher.NoticeCardItem
import com.example.ui.screens.teacher.QuickActionButton
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentDashboardScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val student by viewModel.currentStudent.collectAsState()
    val teacher by viewModel.currentTeacher.collectAsState()

    val routines by viewModel.studentRoutines.collectAsState(initial = emptyList())
    val lessons by viewModel.studentLessons.collectAsState(initial = emptyList())
    val homeworks by viewModel.studentHomework.collectAsState(initial = emptyList())
    val attendances by viewModel.studentAttendance.collectAsState(initial = emptyList())
    val payments by viewModel.studentPayments.collectAsState(initial = emptyList())
    val notices by viewModel.studentNotices.collectAsState(initial = emptyList())

    val todayDayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
    val todayRoutines = routines.filter { it.dayOfWeek.equals(todayDayOfWeek, ignoreCase = true) }

    val totalAtt = attendances.size
    val presentAtt = attendances.count { it.status == AttendanceStatus.PRESENT.name }
    val absentAtt = attendances.count { it.status == AttendanceStatus.ABSENT.name }
    val lateAtt = attendances.count { it.status == AttendanceStatus.LATE.name }
    val attRate = if (totalAtt > 0) (presentAtt.toDouble() / totalAtt * 100).toInt() else 100

    val duePayment = payments.find { it.status == PaymentStatus.DUE.name || it.status == PaymentStatus.OVERDUE.name }
    val latestPaidPayment = payments.filter { it.status == PaymentStatus.PAID.name }.maxByOrNull { it.id }
    val pendingHwList = homeworks.filter { it.status == HomeworkStatus.PENDING.name }
    val pendingHwCount = pendingHwList.size

    var selectedNoticeToRead by remember { mutableStateOf<NoticeEntity?>(null) }
    var homeworkToSubmitDirectly by remember { mutableStateOf<HomeworkEntity?>(null) }
    var paymentReceiptToShow by remember { mutableStateOf<PaymentEntity?>(null) }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = student?.name ?: (if (isBengali) "শিক্ষার্থী ড্যাশবোর্ড" else "Student Dashboard"),
                subtitle = "${student?.className ?: "Class 10"} • রোল: ${student?.roll ?: "01"}"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.StudentDashboard)
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
            // 1. UNIQUE STUDENT PROFILE HERO CARD (Includes Teacher-Assigned User ID, Teacher Info, Batch, Class, Roll)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.StudentProfile) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        EmeraldPrimary.copy(alpha = 0.12f),
                                        SoftBlueLight.copy(alpha = 0.35f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student?.name?.take(1) ?: "S",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isBengali) "শিক্ষার্থী পোর্টাল" else "Student Portal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldPrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (isBengali) "প্রোফাইল দেখুন ›" else "View Profile ›",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = student?.name ?: "শিক্ষার্থী",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 19.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${student?.className ?: "Class 10"} • শাখা ${student?.section ?: "A"} • রোল: ${student?.roll ?: "01"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "ব্যাচ: ${if (isBengali) "সকাল ৯টা (ব্যাচ ১)" else "Morning 9 AM"}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // TEACHER-ASSIGNED USER ID BADGE & TEACHER NAME
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ID: ${student?.studentCode ?: "TBD-2024-001"}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftBlueLight
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "শিক্ষক: ${teacher?.name ?: "আব্দুল্লাহ স্যার"}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SoftBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. STUDENT QUICK ACTIONS GRID (Requested: Assignment, Homework, Notice, Attendance)
            item {
                SectionHeader(
                    title = if (isBengali) "প্রয়োজনীয় সেবা (Student Services)" else "Student Services"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Assignment,
                        label = if (isBengali) "হোমওয়ার্ক জমা" else "Homework",
                        bgColor = AmberLight,
                        iconTint = AmberDark,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentHomework) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.CheckCircle,
                        label = if (isBengali) "উপস্থিতি খাতা" else "Attendance",
                        bgColor = SoftGreenLight,
                        iconTint = SoftGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentAttendance) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Campaign,
                        label = if (isBengali) "নোটিশ বোর্ড" else "Notices",
                        bgColor = PurpleLight,
                        iconTint = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentNotices) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.CalendarMonth,
                        label = if (isBengali) "ক্লাস রুটিন" else "Routine",
                        bgColor = SoftBlueLight,
                        iconTint = SoftBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentRoutine) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.ReceiptLong,
                        label = if (isBengali) "বেতন ও রসিদ" else "Tuition Fees",
                        bgColor = EmeraldContainer,
                        iconTint = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentPayments) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Call,
                        label = if (isBengali) "শিক্ষক যোগাযোগ" else "Contact",
                        bgColor = SoftRedLight,
                        iconTint = SoftRed,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.showSnackbar(
                                if (isBengali) "শিক্ষক: ${teacher?.name ?: "আব্দুল্লাহ স্যার"}, মোবাইল: ${teacher?.phone ?: "01711223344"}" 
                                else "Teacher: ${teacher?.name}, Phone: ${teacher?.phone}"
                            )
                        }
                    )
                }
            }

            // 3. STAT METRICS: Attendance Rate & Monthly Breakdown, Pending Homework
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = if (isBengali) "উপস্থিতির হার" else "Attendance",
                        value = "$attRate%",
                        subtitle = if (isBengali) "উপস্থিত: $presentAtt | অনুপস্থিত: $absentAtt" else "Present: $presentAtt | Absent: $absentAtt",
                        icon = Icons.Default.CheckCircle,
                        accentColor = SoftGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentAttendance) }
                    )
                    StatMetricCard(
                        title = if (isBengali) "বাকি বাড়ির কাজ" else "Pending HW",
                        value = "$pendingHwCount",
                        subtitle = if (isBengali) "এখনই জমা দিন" else "Submit now",
                        icon = Icons.Default.Assignment,
                        accentColor = AmberDark,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.StudentHomework) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Monthly Attendance Breakdown (Present, Absent, Late)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.StudentAttendance) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = SoftGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "এই মাসের উপস্থিতির সংক্ষিপ্ত খতিয়ান" else "Monthly Attendance Summary",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$presentAtt দিন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SoftGreen
                                )
                                Text(
                                    text = if (isBengali) "উপস্থিত" else "Present",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$absentAtt দিন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SoftRed
                                )
                                Text(
                                    text = if (isBengali) "অনুপস্থিত" else "Absent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$lateAtt দিন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AmberDark
                                )
                                Text(
                                    text = if (isBengali) "দেরি (Late)" else "Late",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Payment Status Update Banner with Receipt Download Option (e.g., Arif paid January fee)
                if (latestPaidPayment != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldPrimary, SoftGreen))),
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${student?.name ?: "শিক্ষার্থী"} ${latestPaidPayment.monthYear} মাসের বেতন পরিশোধ করেছে",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "পরিশোধিত: ৳${latestPaidPayment.paidAmount.toInt()} (${latestPaidPayment.paymentMethod}) • তারিখ: ${latestPaidPayment.paymentDate.ifBlank { "N/A" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { paymentReceiptToShow = latestPaidPayment },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBengali) "স্লিপ" else "Slip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Payment Alert Banner if Due
                if (duePayment != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftRedLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppScreen.StudentPayments) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SoftRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBengali) "টিউশন ফি বকেয়া রয়েছে" else "Tuition Fee Due",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SoftRed
                                )
                                Text(
                                    text = "${duePayment.monthYear} মাসের বকেয়া: ৳${duePayment.dueAmount.toInt()} (শেষ তারিখ: ${duePayment.dueDate})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftRed
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SoftRed)
                        }
                    }
                }
            }

            // 4. TODAY'S CLASS SCHEDULE ("আজকের ক্লাস")
            item {
                SectionHeader(
                    title = if (isBengali) "আজকের ক্লাস ($todayDayOfWeek)" else "Today's Class ($todayDayOfWeek)",
                    actionText = if (isBengali) "সম্পূর্ণ রুটিন" else "Full Routine",
                    onActionClick = { viewModel.navigateTo(AppScreen.StudentRoutine) }
                )

                if (todayRoutines.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.EventBusy,
                            message = if (isBengali) "আজ আপনার কোনো নির্ধারিত ক্লাস নেই" else "No classes scheduled for today"
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayRoutines.forEach { r ->
                            ClassScheduleCard(routine = r)
                        }
                    }
                }
            }

            // 5. PENDING HOMEWORK & ASSIGNMENTS (With Direct 1-Tap Submit)
            item {
                SectionHeader(
                    title = if (isBengali) "বাকি বাড়ির কাজ ও এসাইনমেন্ট" else "Pending Assignments",
                    actionText = if (isBengali) "সব দেখুন" else "View All",
                    onActionClick = { viewModel.navigateTo(AppScreen.StudentHomework) }
                )

                if (pendingHwList.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGreenLight.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = SoftGreen, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBengali) "সাবাশ! সব বাড়ির কাজ সম্পন্ন হয়েছে" else "Great! All homework completed",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftGreen,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isBengali) "নতুন বাড়ির কাজ দেওয়া হলে এখানে দেখা যাবে।" else "New assignments will appear here.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF14532D)
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pendingHwList.take(2).forEach { hw ->
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
                                        Surface(
                                            color = AmberLight,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = hw.subjectName,
                                                color = AmberDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Surface(
                                            color = SoftRedLight,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "জমা বাকি • শেষ সময়: ${hw.dueDate}",
                                                color = SoftRed,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = hw.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = hw.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Button(
                                        onClick = { homeworkToSubmitDirectly = hw },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBengali) "উত্তর বা এসাইনমেন্ট জমা দিন" else "Submit Assignment", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. LATEST NOTICES
            item {
                SectionHeader(
                    title = if (isBengali) "নোটিশ ও জরুরি বিজ্ঞপ্তি" else "Notices & Announcements",
                    actionText = if (isBengali) "সব নোটিশ" else "All Notices",
                    onActionClick = { viewModel.navigateTo(AppScreen.StudentNotices) }
                )

                if (notices.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Campaign,
                        message = if (isBengali) "কোনো নতুন নোটিশ নেই" else "No new notices"
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notices.take(2).forEach { notice ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedNoticeToRead = notice }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(PurpleLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Campaign, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notice.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = notice.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Homework Submission Dialog
    if (homeworkToSubmitDirectly != null) {
        StudentHomeworkSubmitDialog(
            homework = homeworkToSubmitDirectly!!,
            isBengali = isBengali,
            onDismiss = { homeworkToSubmitDirectly = null },
            onSubmit = { text, file ->
                viewModel.submitHomeworkAnswer(homeworkToSubmitDirectly!!, text, file)
                homeworkToSubmitDirectly = null
            }
        )
    }

    // Read Notice Dialog
    if (selectedNoticeToRead != null) {
        AlertDialog(
            onDismissRequest = { selectedNoticeToRead = null },
            icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = PurpleAccent) },
            title = { Text(selectedNoticeToRead!!.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "তারিখ: ${selectedNoticeToRead!!.date}",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(selectedNoticeToRead!!.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { selectedNoticeToRead = null }) {
                    Text(if (isBengali) "পড়েছি" else "Close")
                }
            }
        )
    }

    // Payment Money Receipt Dialog
    val receiptToShow = paymentReceiptToShow
    val currentStudentObj = student
    if (receiptToShow != null && currentStudentObj != null) {
        PaymentReceiptDialog(
            payment = receiptToShow,
            studentName = currentStudentObj.name,
            studentCode = currentStudentObj.studentCode,
            teacherName = teacher?.name ?: "আব্দুল্লাহ স্যার",
            onDismiss = { paymentReceiptToShow = null }
        )
    }
}

// Student Homework Screen with Filter Tabs & Rich Submission Experience
@Composable
fun StudentHomeworkScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val homeworks by viewModel.studentHomework.collectAsState(initial = emptyList())
    var homeworkToSubmit by remember { mutableStateOf<HomeworkEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredHomeworks = when (selectedFilter) {
        "PENDING" -> homeworks.filter { it.status == HomeworkStatus.PENDING.name }
        "COMPLETED" -> homeworks.filter { it.status == HomeworkStatus.COMPLETED.name }
        else -> homeworks
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "আমার বাড়ির কাজ ও এসাইনমেন্ট" else "My Homework & Assignments",
                subtitle = if (isBengali) "মোট কাজ: ${homeworks.size} টি" else "Total: ${homeworks.size}"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.StudentHomework)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("সকল (${homeworks.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "PENDING",
                    onClick = { selectedFilter = "PENDING" },
                    label = { Text("⏳ জমা বাকি (${homeworks.count { it.status == HomeworkStatus.PENDING.name }})") }
                )
                FilterChip(
                    selected = selectedFilter == "COMPLETED",
                    onClick = { selectedFilter = "COMPLETED" },
                    label = { Text("✅ জমা দেওয়া (${homeworks.count { it.status == HomeworkStatus.COMPLETED.name }})") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredHomeworks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.AssignmentTurnedIn,
                    message = if (isBengali) "কোনো বাড়ির কাজ পাওয়া যায়নি" else "No assignments in this filter"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredHomeworks) { hw ->
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
                                        Text(hw.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("বিষয়: ${hw.subjectName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    StatusBadge(status = hw.status)
                                }

                                Text(hw.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                // Student Submission Preview
                                if (hw.studentSubmission.isNotBlank()) {
                                    Surface(
                                        color = SoftGreenLight,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "✅ আপনার জমা দেওয়া সমাধান:",
                                                color = SoftGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = hw.studentSubmission,
                                                color = Color(0xFF14532D),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("জমা দেওয়ার শেষ সময়: ${hw.dueDate}", style = MaterialTheme.typography.labelSmall)

                                    if (hw.status != HomeworkStatus.COMPLETED.name) {
                                        Button(
                                            onClick = { homeworkToSubmit = hw },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isBengali) "উত্তর জমা দিন" else "Submit Answer", fontSize = 12.sp)
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

    if (homeworkToSubmit != null) {
        StudentHomeworkSubmitDialog(
            homework = homeworkToSubmit!!,
            isBengali = isBengali,
            onDismiss = { homeworkToSubmit = null },
            onSubmit = { text, file ->
                viewModel.submitHomeworkAnswer(homeworkToSubmit!!, text, file)
                homeworkToSubmit = null
            }
        )
    }
}

// Interactive Homework Submission Dialog with Attachment Simulation
@Composable
fun StudentHomeworkSubmitDialog(
    homework: HomeworkEntity,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var submissionText by remember { mutableStateOf("") }
    var attachedFileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBengali) "এসাইনমেন্ট / বাড়ির কাজ জমা দিন" else "Submit Homework")
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(homework.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("বিষয়: ${homework.subjectName} • শেষ তারিখ: ${homework.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(
                    text = if (isBengali) "১. উত্তর বা সমাধান লিখুন:" else "1. Write your answer/notes:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = submissionText,
                    onValueChange = { submissionText = it },
                    placeholder = { Text(if (isBengali) "এখানে আপনার সমাধান বা ব্যাখ্যা লিখুন..." else "Type your solution here...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = if (isBengali) "২. খাতার ছবি বা পিডিএফ ফাইল সংযুক্ত করুন:" else "2. Attach copy photo or PDF:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                // Attachment simulation chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { attachedFileName = "গণিত_অনুশীলনী_খাতার_ছবি.jpg" },
                        label = { Text("📸 খাতার ছবি.jpg", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { attachedFileName = "এসাইনমেন্ট_নোট.pdf" },
                        label = { Text("📄 নোট.pdf", fontSize = 11.sp) }
                    )
                }

                if (attachedFileName.isNotBlank()) {
                    Surface(
                        color = SoftBlueLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সংযুক্ত: $attachedFileName",
                                color = SoftBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { attachedFileName = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = SoftBlue)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (submissionText.isNotBlank() || attachedFileName.isNotBlank()) {
                        val finalAnswer = if (submissionText.isBlank()) "সমাধান সংযুক্ত ফাইলে দেওয়া হয়েছে।" else submissionText
                        onSubmit(finalAnswer, attachedFileName)
                    }
                },
                enabled = submissionText.isNotBlank() || attachedFileName.isNotBlank()
            ) {
                Text(if (isBengali) "জমা দিন" else "Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}

// Student Attendance & Absence Screen
@Composable
fun StudentAttendanceScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val attendances by viewModel.studentAttendance.collectAsState(initial = emptyList())
    var filterStatus by remember { mutableStateOf("ALL") }

    val total = attendances.size
    val present = attendances.count { it.status == AttendanceStatus.PRESENT.name }
    val absent = attendances.count { it.status == AttendanceStatus.ABSENT.name }
    val late = attendances.count { it.status == AttendanceStatus.LATE.name }
    val percent = if (total > 0) (present.toDouble() / total * 100).toInt() else 100

    val filteredList = when (filterStatus) {
        "PRESENT" -> attendances.filter { it.status == AttendanceStatus.PRESENT.name }
        "ABSENT" -> attendances.filter { it.status == AttendanceStatus.ABSENT.name }
        else -> attendances
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "আমার উপস্থিতি ও অনুপস্থিতি" else "My Attendance Records",
                subtitle = "গড় উপস্থিতি: $percent%"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.StudentAttendance)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Attendance Summary Overview Card
            Card(
                shape = RoundedCornerShape(18.dp),
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
                        Column {
                            Text(
                                text = if (isBengali) "উপস্থিতির শতকরা হার" else "Attendance Rate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (percent >= 80) EmeraldPrimary else AmberDark
                                )
                            )
                        }

                        Surface(
                            color = if (percent >= 80) SoftGreenLight else AmberLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (percent >= 80) "🌟 চমৎকার উপস্থিতি" else "⚠️ উপস্থিতি বাড়ানো প্রয়োজন",
                                color = if (percent >= 80) SoftGreen else AmberDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(if (isBengali) "মোট ক্লাস" else "Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$present", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SoftGreen))
                            Text(if (isBengali) "উপস্থিত" else "Present", style = MaterialTheme.typography.bodySmall, color = SoftGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$absent", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SoftRed))
                            Text(if (isBengali) "অনুপস্থিত" else "Absent", style = MaterialTheme.typography.bodySmall, color = SoftRed)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$late", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AmberDark))
                            Text(if (isBengali) "দেরি" else "Late", style = MaterialTheme.typography.bodySmall, color = AmberDark)
                        }
                    }
                }
            }

            // Attendance Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == "ALL",
                    onClick = { filterStatus = "ALL" },
                    label = { Text("সকল দিন ($total)") }
                )
                FilterChip(
                    selected = filterStatus == "PRESENT",
                    onClick = { filterStatus = "PRESENT" },
                    label = { Text("🟢 উপস্থিত ($present)") }
                )
                FilterChip(
                    selected = filterStatus == "ABSENT",
                    onClick = { filterStatus = "ABSENT" },
                    label = { Text("🔴 অনুপস্থিত ($absent)") }
                )
            }

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.EventBusy,
                    message = if (isBengali) "কোনো উপস্থিতি রেকর্ড পাওয়া যায়নি" else "No attendance records found"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList) { att ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (att.status) {
                                                    AttendanceStatus.PRESENT.name -> SoftGreenLight
                                                    AttendanceStatus.ABSENT.name -> SoftRedLight
                                                    else -> AmberLight
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (att.status) {
                                                AttendanceStatus.PRESENT.name -> Icons.Default.Check
                                                AttendanceStatus.ABSENT.name -> Icons.Default.Close
                                                else -> Icons.Default.Schedule
                                            },
                                            contentDescription = null,
                                            tint = when (att.status) {
                                                AttendanceStatus.PRESENT.name -> SoftGreen
                                                AttendanceStatus.ABSENT.name -> SoftRed
                                                else -> AmberDark
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(text = "তারিখ: ${att.date}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = "বিষয়: ${att.subjectName} • মন্তব্য: ${att.note.ifBlank { "নিয়মিত ক্লাস" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
}

// Student Notices Screen
@Composable
fun StudentNoticesScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val notices by viewModel.studentNotices.collectAsState(initial = emptyList())
    var selectedNotice by remember { mutableStateOf<NoticeEntity?>(null) }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "নোটিশ ও ঘোষণা বোর্ড" else "Notices & Announcements",
                subtitle = if (isBengali) "মোট নোটিশ: ${notices.size} টি" else "Total: ${notices.size}"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.StudentNotices)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (notices.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Campaign,
                    message = if (isBengali) "বর্তমানে কোনো নোটিশ নেই" else "No notices available"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(notices) { notice ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNotice = notice }
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = when (notice.priority) {
                                            PriorityLevel.URGENT.name -> SoftRedLight
                                            PriorityLevel.IMPORTANT.name -> AmberLight
                                            else -> PurpleLight
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = when (notice.priority) {
                                                PriorityLevel.URGENT.name -> "🔴 অতীব জরুরি"
                                                PriorityLevel.IMPORTANT.name -> "🟡 গুরুত্বপূর্ণ"
                                                else -> "📢 সাধারণ নোটিশ"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (notice.priority) {
                                                PriorityLevel.URGENT.name -> SoftRed
                                                PriorityLevel.IMPORTANT.name -> AmberDark
                                                else -> PurpleAccent
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = notice.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Text(
                                    text = notice.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedNotice != null) {
        AlertDialog(
            onDismissRequest = { selectedNotice = null },
            icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = PurpleAccent) },
            title = { Text(selectedNotice!!.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("তারিখ: ${selectedNotice!!.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(selectedNotice!!.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { selectedNotice = null }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

// Student Class Routine Screen
@Composable
fun StudentRoutineScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val routines by viewModel.studentRoutines.collectAsState(initial = emptyList())

    val daysOfWeek = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val daysBangla = listOf("শনিবার", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার")

    val todayDayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
    var selectedDay by remember { mutableStateOf(todayDayOfWeek) }

    val filteredRoutines = routines.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "আমার ক্লাস রুটিন" else "My Class Routine",
                subtitle = "আজ: $todayDayOfWeek"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.StudentRoutine)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    message = if (isBengali) "$selectedDay এ কোনো নির্ধারিত ক্লাস নেই" else "No classes scheduled for $selectedDay"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRoutines) { r ->
                        ClassScheduleCard(routine = r)
                    }
                }
            }
        }
    }
}

// Student Tuition Payments & Money Receipt Screen
@Composable
fun StudentPaymentsScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val payments by viewModel.studentPayments.collectAsState(initial = emptyList())
    val student by viewModel.currentStudent.collectAsState()
    val teacher by viewModel.currentTeacher.collectAsState()
    val selectedPayment by viewModel.selectedPayment.collectAsState()

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "আমার টিউশন ফি ও রসিদ" else "My Tuition Fees & Receipts",
                subtitle = if (isBengali) "মাসিক বেতন বিবরণী" else "Monthly fee statement"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.StudentPayments)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (payments.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Payments,
                    message = if (isBengali) "কোনো পেমেন্ট রেকর্ড নেই" else "No payments recorded"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(payments) { p ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    Text("পরিশোধিত: ৳${p.paidAmount.toInt()} (${p.paymentMethod})", color = SoftGreen, fontWeight = FontWeight.Bold)
                                    if (p.dueAmount > 0) {
                                        Text("বকেয়া: ৳${p.dueAmount.toInt()}", color = SoftRed, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("তারিখ: ${p.paymentDate.ifBlank { p.dueDate }}", style = MaterialTheme.typography.labelSmall)
                                    Button(
                                        onClick = { viewModel.selectPaymentForReceipt(p) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBengali) "মানি রিসিট দেখুন" else "View Receipt", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedPayment != null && student != null) {
        PaymentReceiptDialog(
            payment = selectedPayment!!,
            studentName = student!!.name,
            studentCode = student!!.studentCode,
            teacherName = teacher?.name ?: "আব্দুল্লাহ স্যার",
            onDismiss = { viewModel.clearSelectedPayment() }
        )
    }
}
