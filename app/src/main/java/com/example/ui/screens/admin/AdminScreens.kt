package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun AdminDashboardScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val students by viewModel.allStudents.collectAsState(initial = emptyList())
    val payments by viewModel.allPayments.collectAsState(initial = emptyList())

    val totalCollected = payments.sumOf { it.paidAmount }
    val totalDue = payments.sumOf { it.dueAmount }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "টিচার এডমিন কন্ট্রোল" else "Teacher Admin Hub",
                subtitle = if (isBengali) "টিউশন পরিচালনা ও শিক্ষার্থী আইডি হাব" else "Single Teacher Tuition & Student Control"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.AdminDashboard)
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
            // Admin Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
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
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "টিচার এডমিন কন্ট্রোল হাব" else "Teacher Control Hub",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isBengali) "আপনার টিউশনের শিক্ষার্থী, ইউজার আইডি, রুটিন ও হিসাব" else "Manage your students, IDs, routine and finances",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Stat Metrics
            item {
                SectionHeader(title = if (isBengali) "টিউশন সার্বিক পরিসংখ্যান" else "Tuition Overview")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = if (isBengali) "মোট শিক্ষার্থী" else "Total Students",
                        value = "${students.size} জন",
                        subtitle = if (isBengali) "আপনার ছাত্র-ছাত্রী" else "Enrolled students",
                        icon = Icons.Default.School,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.AdminStudents) }
                    )
                    StatMetricCard(
                        title = if (isBengali) "মোট ব্যাচ ও শ্রেণী" else "Batches & Classes",
                        value = "৫টি ব্যাচ",
                        subtitle = if (isBengali) "সক্রিয় ব্যাচ" else "Active batches",
                        icon = Icons.Default.Class,
                        accentColor = SoftBlue,
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
                        title = if (isBengali) "মোট ফি আদায়" else "Total Collected",
                        value = "৳${totalCollected.toInt()}",
                        subtitle = if (isBengali) "আদায়কৃত অর্থ" else "Collected fees",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = SoftGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherPayments) }
                    )
                    StatMetricCard(
                        title = if (isBengali) "মোট বকেয়া" else "Total Due",
                        value = "৳${totalDue.toInt()}",
                        subtitle = if (isBengali) "বকেয়া টিউশন ফি" else "Pending dues",
                        icon = Icons.Default.PendingActions,
                        accentColor = SoftRed,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.TeacherPayments) }
                    )
                }
            }

            // Quick Management Actions
            item {
                SectionHeader(title = if (isBengali) "কুইক কন্ট্রোল অ্যাকশন" else "Quick Control Actions")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.AdminStudents) }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBengali) "শিক্ষার্থী ও আইডি" else "Students & IDs",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.TeacherAddStudent) }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBengali) "নতুন শিক্ষার্থী" else "Add Student",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.TeacherAttendance) }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, tint = AmberDark, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBengali) "উপস্থিতি খাতা" else "Attendance",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.TeacherRoutine) }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBengali) "ক্লাস রুটিন" else "Routine",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Cloud Backend Setup Shortcut Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.FirebaseConfig) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldPrimary)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "ফায়ারবেস ও ক্লাউড ব্যাকআপ গাইড" else "Firebase & Cloud Backup Guide",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isBengali) "অনলাইন ক্লাউড ডাটাবেস ও মাল্টি-ডিভাইস সিঙ্ক নির্দেশিকা" else "Cloud sync, backup and online database instructions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Student List with Teacher-Assigned User IDs
            item {
                SectionHeader(
                    title = if (isBengali) "শিক্ষার্থী ও ইউজার আইডি তালিকা (${students.size})" else "Students & User IDs (${students.size})",
                    actionText = if (isBengali) "সব দেখুন" else "View All",
                    onActionClick = { viewModel.navigateTo(AppScreen.AdminStudents) }
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    students.take(5).forEach { std ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectStudent(std)
                                    viewModel.navigateTo(AppScreen.TeacherStudentDetail)
                                }
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
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = std.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = std.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = EmeraldContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ID: ${std.studentCode}",
                                                color = EmeraldDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "${std.className} (রোল: ${std.roll})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                StatusBadge(status = std.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTeachersScreen(viewModel: TuitionViewModel) {
    // Redirect to AdminStudents since there is only one teacher
    AdminStudentsScreen(viewModel = viewModel)
}

@Composable
fun AdminStudentsScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val students by viewModel.allStudents.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "শিক্ষার্থী ও ইউজার আইডি তালিকা" else "Students & User IDs",
                subtitle = "মোট শিক্ষার্থী: ${students.size} জন"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.AdminStudents)
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
                    Text(if (isBengali) "নতুন শিক্ষার্থী" else "Add Student", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            items(students) { std ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectStudent(std)
                            viewModel.navigateTo(AppScreen.TeacherStudentDetail)
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(std.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            StatusBadge(status = std.status)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = EmeraldContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ইউজার ID: ${std.studentCode}",
                                    color = EmeraldDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "শ্রেণী: ${std.className} (রোল: ${std.roll})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (std.institution.isNotBlank()) {
                            Text("🏫 ${std.institution}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("📞 ${std.phone}", style = MaterialTheme.typography.bodySmall)
                            Text("৳${std.monthlyFee.toInt()}/মাস", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAdminCard(
    teacher: TeacherEntity,
    onToggleStatus: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = teacher.name.take(1),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(teacher.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(teacher.qualification, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatusBadge(status = teacher.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("📞 ফোন: ${teacher.phone} • ✉️ ${teacher.email.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
            Text("📍 ঠিকানা: ${teacher.address}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("📅 যোগদান: ${teacher.joiningDate}", style = MaterialTheme.typography.labelSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onToggleStatus) {
                    Text(if (teacher.status == "ACTIVE") "নিষ্ক্রিয় করুন" else "সক্রিয় করুন")
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftRed)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseConfigScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBengali) "ফায়ারবেস ও ক্লাউড সেটআপ" else "Firebase & Cloud Backend Guide") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🚀 ক্লাউড ফায়ারবেস ব্যাকএন্ড ইন্টিগ্রেশন নির্দেশিকা",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "Tuition Manager BD সম্পূর্ণ অফলাইন-রেডি ও লোকাল ডাটাবেস (Room DB) সম্বলিত। এটিকে অনলাইন ক্লাউড ফায়ারবেসে যুক্ত করার ৩টি সহজ ধাপ নিচে দেওয়া হলো:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            StepCard(
                number = "১",
                title = "ফায়ারবেস কনসোল প্রজেক্ট তৈরি",
                description = "1. console.firebase.google.com এ যান এবং 'Tuition Manager BD' নামে প্রজেক্ট তৈরি করুন।\n2. Android অ্যাপ অপশন বেছে নিয়ে প্যাকেজ নাম: com.aistudio.tuitionmanagerbd.xyz দিন।\n3. google-services.json ফাইলটি ডাউনলোড করে /app ফোল্ডারে পেস্ট করুন।"
            )

            StepCard(
                number = "২",
                title = "Authentication & Cloud Firestore চালু",
                description = "1. Firebase Authentication এ Email/Password ও Phone Auth সক্রিয় করুন।\n2. Cloud Firestore Database চালু করে security rules সেট করুন:\nallow read, write: if request.auth != null;"
            )

            StepCard(
                number = "৩",
                title = "রিয়েলটাইম সিঙ্ক ও নোটিফিকেশন",
                description = "1. TuitionRepository এ Firestore listener যুক্ত করে লোকাল Room DB এর সাথে অটো-সিঙ্ক হবে।\n2. Firebase Cloud Messaging (FCM) এর মাধ্যমে শিক্ষার্থীরা বেতন ও ক্লাসের তাৎক্ষণিক পুশ নোটিফিকেশন পাবে।"
            )

            Button(
                onClick = { viewModel.goBack() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("বুঝেছি, ড্যাশবোর্ডে ফিরে যান")
            }
        }
    }
}

@Composable
private fun StepCard(number: String, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
