package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AttendanceStatus
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentStatus
import com.example.ui.components.SectionHeader
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val teacher by viewModel.currentTeacher.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState(initial = emptyList())
    val routines by viewModel.teacherRoutines.collectAsState(initial = emptyList())
    val allBatches = remember(routines) {
        val distinct = routines.map { it.studentOrGroup }.filter { it.isNotBlank() }.distinct()
        if (distinct.isNotEmpty()) distinct else listOf("ব্যাচ ১ (সকাল ৯টা)", "ব্যাচ ২ (বিকাল ৪টা)")
    }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPhotoUploadDialog by remember { mutableStateOf(false) }
    var seedStatusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBengali) "স্যারের প্রোফাইল ও এডমিন হাব" else "Teacher Profile & Admin Hub",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = SoftRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            // 1. TEACHER HERO PROFILE CARD WITH AVATAR & PHOTO UPLOAD
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Box with Camera overlay button
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.size(92.dp)
                            ) {
                                val avatarUrl = teacher?.avatarUrl
                                if (!avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Teacher Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .border(3.dp, EmeraldPrimary, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        AmberAccent
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = teacher?.name?.take(1)?.uppercase() ?: "T",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Camera badge to change photo
                                Surface(
                                    color = EmeraldPrimary,
                                    shape = CircleShape,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { showPhotoUploadDialog = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Change Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = teacher?.name ?: (if (isBengali) "মোঃ আব্দুল্লাহ স্যার" else "Md. Abdullah Sir"),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = teacher?.qualification ?: (if (isBengali) "বি.এসসি ইন সিএসই (প্রধান শিক্ষক)" else "B.Sc in CSE (Lead Instructor)"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isBengali) "পরিচালক ও প্রধান শিক্ষক" else "Director & Lead Instructor",
                                        color = EmeraldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AmberLight
                                ) {
                                    Text(
                                        text = if (isBengali) "এডমিন এক্সেস" else "Admin Access",
                                        color = AmberDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Profile Action Buttons: Edit Profile & Change Photo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showEditProfileDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBengali) "প্রোফাইল সম্পাদনা" else "Edit Profile",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showPhotoUploadDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBengali) "ছবি পরিবর্তন" else "Change Photo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stats row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${allStudents.size}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isBengali) "মোট শিক্ষার্থী" else "Students",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${allBatches.size}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = AmberDark
                                    )
                                    Text(
                                        text = if (isBengali) "মোট ব্যাচ" else "Batches",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "100%",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = SoftGreen
                                    )
                                    Text(
                                        text = if (isBengali) "এডমিন ক্ষমতা" else "Admin Power",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. LANGUAGE, THEME & ACCOUNT CONTROLS CARD
            item {
                SectionHeader(
                    title = if (isBengali) "ভাষা, থিম ও অ্যাকাউন্ট নিয়ন্ত্রণ" else "Language, Theme & Account",
                    subtitle = if (isBengali) "অ্যাপের ভাষা পরিবর্তন ও লগআউট নিয়ন্ত্রণ" else "Switch language and account preferences"
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Language Selector (বাংলা / English)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = if (isBengali) "অ্যাপ্লিকেশনের ভাষা" else "App Language",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isBengali) "বাংলা বা ইংরেজিতে পরিবর্তন করুন" else "Switch between Bangla and English",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = isBengali,
                                    onClick = { if (!isBengali) viewModel.toggleLanguage() },
                                    label = { Text("বাংলা") }
                                )
                                FilterChip(
                                    selected = !isBengali,
                                    onClick = { if (isBengali) viewModel.toggleLanguage() },
                                    label = { Text("EN") }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Theme Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = AmberDark
                                )
                                Column {
                                    Text(
                                        text = if (isBengali) "অ্যাপ থিম / মোড" else "Appearance",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isDarkMode) (if (isBengali) "ডার্ক মোড সক্রিয়" else "Dark Mode Enabled") else (if (isBengali) "লাইট মোড সক্রিয়" else "Light Mode Enabled"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Quick In-card Logout Button
                        FilledTonalButton(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SoftRed.copy(alpha = 0.12f),
                                contentColor = SoftRed
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBengali) "অ্যাকাউন্ট থেকে লগআউট করুন" else "Log Out from Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. TEACHER CONTACT & DETAILS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionHeader(
                        title = if (isBengali) "ব্যক্তিগত ও অ্যাকাডেমিক তথ্য" else "Personal & Academic Info"
                    )
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Info", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = if (isBengali) "মোবাইল নম্বর" else "Phone Number",
                            value = teacher?.phone ?: "01711223344"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = if (isBengali) "ইমেইল অ্যাড্রেস" else "Email Address",
                            value = teacher?.email ?: "abdullah.teacher@tuition.edu"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.School,
                            label = if (isBengali) "শিক্ষাগত যোগ্যতা ও বিষয়" else "Qualification & Subjects",
                            value = teacher?.qualification ?: (if (isBengali) "বিএসসি ও এমএসসি (গণিত), ঢাকা বিশ্ববিদ্যালয়" else "B.Sc & M.Sc in Mathematics, DU")
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = if (isBengali) "কোচিং / প্রাইভেট ঠিকানা" else "Center Address",
                            value = teacher?.address ?: (if (isBengali) "ধানমন্ডি, রোড নং ৪, ঢাকা" else "Dhanmondi, Road 4, Dhaka")
                        )
                    }
                }
            }

            // 4. 500+ USERS ENGINE & DATABASE PROTECTION CARD
            item {
                SectionHeader(
                    title = if (isBengali) "সিস্টেম স্কেলাবিলিটি ও সুরক্ষা (500+ Users)" else "Scalability & Protection (500+ Users)",
                    subtitle = if (isBengali) "৫০০+ শিক্ষার্থী ও শিক্ষক ধারণক্ষমতা সম্পন্ন হাই-পারফরম্যান্স ডেটাবেজ" else "High-performance indexed DB handling 500+ students & teachers"
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBengali) "রুম ডেটাবেজ ও ক্লাউড প্রটেকশন" else "Room DB & Cloud Protection",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isBengali) "ইনডেক্সিং সহ ৫০০+ শিক্ষার্থীর নিরবচ্ছিন্ন এক্সেস ও জিরো ক্র্যাশ" else "High-speed indexed queries, zero-lag offline persistence",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBengali) "বর্তমান লোডেড শিক্ষার্থী:" else "Active Database Records:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${allStudents.size} জন (500+ Capable)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                )
                            }
                        }

                        if (seedStatusMessage != null) {
                            Surface(
                                color = SoftGreenLight.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SoftGreen, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = seedStatusMessage ?: "",
                                        color = SoftGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.seed500StudentsCapacity()
                                seedStatusMessage = if (isBengali) "✅ ৫০০ শিক্ষার্থী ধারণক্ষমতা টেস্ট ডাটা সফলভাবে লোড হয়েছে!" else "✅ 500-student capacity test records loaded!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "৫০০ শিক্ষার্থী ধারণক্ষমতা টেস্ট ডাটা লোড করুন" else "Seed 500-Student Scale Capacity",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 5. ADMIN PANEL (INTEGRATED DIRECTLY IN TEACHER PROFILE)
            item {
                SectionHeader(
                    title = if (isBengali) "এডমিন প্যানেল ও কন্ট্রোল হাব" else "Admin Control Panel",
                    subtitle = if (isBengali) "সিস্টেমের সকল মাস্টার সেটিংস ও ম্যানেজমেন্ট" else "Master settings and system controls"
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberLight.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AmberAccent, AmberDark))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBengali) "মাস্টার এডমিন টুলস" else "Master Admin Tools",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AmberDark
                                )
                                Text(
                                    text = if (isBengali) "শিক্ষার্থী আইডি, রুটিন, নোটিশ ও ফি ব্যাকআপ" else "Student IDs, routines, notices & billing backup",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AmberDark.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        AdminToolItem(
                            icon = Icons.Default.Dashboard,
                            title = if (isBengali) "এডমিন সম্পূর্ণ ড্যাশবোর্ড" else "Full Admin Dashboard",
                            desc = if (isBengali) "সম্পূর্ণ প্রশাসনিক ওভারভিউ ও মেট্রিক্স" else "Complete supervisory overview",
                            onClick = { viewModel.openAdminPanel() }
                        )

                        AdminToolItem(
                            icon = Icons.Default.Groups,
                            title = if (isBengali) "শিক্ষার্থী তালিকা ও রোল/আইডি প্রদান" else "Student Management & IDs",
                            desc = if (isBengali) "নতুন শিক্ষার্থী যোগ ও আইডি নম্বর তৈরি করুন" else "Add students & assign permanent IDs",
                            onClick = { viewModel.navigateTo(AppScreen.AdminStudents) }
                        )

                        AdminToolItem(
                            icon = Icons.Default.BarChart,
                            title = if (isBengali) "মাসিক রিপোর্ট ও হিসাব-নিকাশ" else "Monthly Reports & Analytics",
                            desc = if (isBengali) "হাজিরা ও আয়ের সম্পূর্ণ পরিসংখ্যান" else "Full attendance and fee metrics",
                            onClick = { viewModel.navigateTo(AppScreen.AdminReports) }
                        )

                        AdminToolItem(
                            icon = Icons.Default.Visibility,
                            title = if (isBengali) "শিক্ষার্থী ভিউ প্রিভিউ করুন" else "Student View Preview",
                            desc = if (isBengali) "শিক্ষার্থী হিসেবে অ্যাপের চেহারা দেখুন" else "Preview the student portal interface",
                            onClick = { viewModel.openStudentViewAsTeacher() }
                        )
                    }
                }
            }

            // 6. LOGOUT ACTION
            item {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBengali) "লগআউট করুন" else "Log Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Edit Teacher Profile Dialog
    if (showEditProfileDialog) {
        EditTeacherProfileDialog(
            teacher = teacher,
            isBengali = isBengali,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, phone, email, qual, addr ->
                viewModel.updateTeacherProfile(name, phone, email, qual, addr)
            }
        )
    }

    // Teacher Photo Picker / Upload Dialog
    if (showPhotoUploadDialog) {
        TeacherPhotoPickerDialog(
            currentAvatarUrl = teacher?.avatarUrl,
            isBengali = isBengali,
            onDismiss = { showPhotoUploadDialog = false },
            onSaveAvatar = { newAvatar ->
                viewModel.updateTeacherAvatar(newAvatar)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val student by viewModel.currentStudent.collectAsState()
    val teacher by viewModel.currentTeacher.collectAsState()
    val attendances by viewModel.studentAttendance.collectAsState(initial = emptyList())
    val payments by viewModel.studentPayments.collectAsState(initial = emptyList())

    val totalAtt = attendances.size
    val presentAtt = attendances.count { it.status == AttendanceStatus.PRESENT.name }
    val absentAtt = attendances.count { it.status == AttendanceStatus.ABSENT.name }
    val lateAtt = attendances.count { it.status == AttendanceStatus.LATE.name }
    val attRate = if (totalAtt > 0) (presentAtt.toDouble() / totalAtt * 100).toInt() else 100

    val paidPayments = payments.filter { it.status == PaymentStatus.PAID.name }
    val duePayments = payments.filter { it.status == PaymentStatus.DUE.name || it.status == PaymentStatus.OVERDUE.name }
    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentEntity?>(null) }
    var showEditStudentDialog by remember { mutableStateOf(false) }
    var showStudentPhotoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBengali) "শিক্ষার্থীর প্রোফাইল" else "Student Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = SoftRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            // 1. STUDENT HERO PROFILE CARD
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        EmeraldPrimary.copy(alpha = 0.15f),
                                        SoftBlueLight.copy(alpha = 0.35f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Box with Camera overlay button
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.size(92.dp)
                            ) {
                                val avatarUrl = student?.avatarUrl
                                if (!avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Student Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .border(3.dp, SoftBlue, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(EmeraldPrimary, SoftBlue)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student?.name?.take(1)?.uppercase() ?: "S",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Camera badge to change photo
                                Surface(
                                    color = SoftBlue,
                                    shape = CircleShape,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { showStudentPhotoDialog = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Change Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = student?.name ?: "শিক্ষার্থী",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${student?.className ?: "Class 10"} • শাখা ${student?.section ?: "A"} • রোল: ${student?.roll ?: "01"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: Edit Profile & Change Photo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showEditStudentDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftBlue)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBengali) "প্রোফাইল সম্পাদনা" else "Edit Profile",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showStudentPhotoDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBengali) "ছবি পরিবর্তন" else "Change Photo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Attendance Summary Stats Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(vertical = 12.dp),
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
                                        .height(30.dp)
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
                                        .height(30.dp)
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
                }
            }

            // 2. LANGUAGE & ACCOUNT CONTROLS CARD
            item {
                SectionHeader(
                    title = if (isBengali) "ভাষা ও অ্যাকাউন্ট নিয়ন্ত্রণ" else "Language & Account Settings",
                    subtitle = if (isBengali) "অ্যাপের ভাষা নির্বাচন ও লগআউট" else "Manage language and sign out"
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Language Selector (বাংলা / English)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = SoftBlue)
                                Column {
                                    Text(
                                        text = if (isBengali) "অ্যাপ্লিকেশনের ভাষা" else "App Language",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isBengali) "বাংলা বা ইংরেজি নির্বাচন করুন" else "Switch Bangla and English",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = isBengali,
                                    onClick = { if (!isBengali) viewModel.toggleLanguage() },
                                    label = { Text("বাংলা") }
                                )
                                FilterChip(
                                    selected = !isBengali,
                                    onClick = { if (isBengali) viewModel.toggleLanguage() },
                                    label = { Text("EN") }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // In-card Logout Button
                        FilledTonalButton(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SoftRed.copy(alpha = 0.12f),
                                contentColor = SoftRed
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBengali) "স্টুডেন্ট আইডি থেকে লগআউট" else "Log Out from Student Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. DETAILED STUDENT & GUARDIAN INFO
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionHeader(
                        title = if (isBengali) "শিক্ষার্থী ও অভিভাবকের তথ্য" else "Student & Guardian Info"
                    )
                    IconButton(onClick = { showEditStudentDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Info", tint = SoftBlue)
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileInfoRow(
                            icon = Icons.Default.Class,
                            label = if (isBengali) "শ্রেণী ও শাখা" else "Class & Section",
                            value = "${student?.className ?: "Class 10"} • শাখা ${student?.section ?: "A"}"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.FormatListNumbered,
                            label = if (isBengali) "ক্লাস রোল নম্বর" else "Roll Number",
                            value = student?.roll ?: "01"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = if (isBengali) "শিক্ষার্থীর মোবাইল নম্বর" else "Student Phone",
                            value = student?.phone?.ifBlank { "01700000000" } ?: "01700000000"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = if (isBengali) "শিক্ষার্থীর ইমেইল" else "Student Email",
                            value = student?.email?.ifBlank { "student@tuition.edu" } ?: "student@tuition.edu"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = if (isBengali) "পিতা/অভিভাবকের নাম" else "Father / Guardian",
                            value = student?.guardianName ?: (if (isBengali) "জনাব রফিকুল ইসলাম" else "Md. Rafiqul Islam")
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = if (isBengali) "অভিভাবকের মোবাইল নম্বর" else "Guardian Phone",
                            value = student?.guardianPhone ?: "01811223344"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Default.Home,
                            label = if (isBengali) "বর্তমান ঠিকানা" else "Home Address",
                            value = student?.address ?: (if (isBengali) "মিরপুর-১০, ঢাকা" else "Mirpur-10, Dhaka")
                        )
                    }
                }
            }

            // 4. PAYMENT STATUS & RECEIPT DOWNLOAD
            item {
                SectionHeader(
                    title = if (isBengali) "বেতন ও মানি রিসিট ডাউনলোড" else "Fees & Money Receipts",
                    subtitle = if (isBengali) "পরিশোধিত সকল মাসের রসিদ ডাউনলোড করুন" else "Download receipts for paid months"
                )

                if (paidPayments.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isBengali) "কোনো পরিশোধিত রসিদ পাওয়া যায়নি" else "No payment records available",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        paidPayments.forEach { p ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${student?.name ?: "শিক্ষার্থী"} ${p.monthYear} মাসের বেতন পরিশোধ করেছে",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "পরিশোধিত: ৳${p.paidAmount.toInt()} • মাধ্যম: ${p.paymentMethod} (${p.paymentDate.ifBlank { p.dueDate }})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftGreen
                                        )
                                    }
                                    Button(
                                        onClick = { selectedPaymentForReceipt = p },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBengali) "রসিদ" else "Receipt", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. LOGOUT
            item {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBengali) "লগআউট করুন" else "Log Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Edit Student Profile Dialog
    if (showEditStudentDialog) {
        EditStudentProfileDialog(
            student = student,
            isBengali = isBengali,
            onDismiss = { showEditStudentDialog = false },
            onSave = { name, phone, email, className, section, roll, guardianName, guardianPhone, address ->
                val studId = student?.id ?: 0L
                if (studId != 0L) {
                    viewModel.updateStudentProfile(
                        studentId = studId,
                        name = name,
                        phone = phone,
                        email = email,
                        className = className,
                        section = section,
                        roll = roll,
                        guardianName = guardianName,
                        guardianPhone = guardianPhone,
                        address = address
                    )
                }
            }
        )
    }

    // Student Photo Picker / Upload Dialog
    if (showStudentPhotoDialog) {
        StudentPhotoPickerDialog(
            currentAvatarUrl = student?.avatarUrl,
            isBengali = isBengali,
            onDismiss = { showStudentPhotoDialog = false },
            onSaveAvatar = { newAvatar ->
                val studId = student?.id ?: 0L
                if (studId != 0L) {
                    viewModel.updateStudentAvatar(studId, newAvatar)
                }
            }
        )
    }

    val receiptPayment = selectedPaymentForReceipt
    val currentStud = student
    if (receiptPayment != null && currentStud != null) {
        com.example.ui.components.PaymentReceiptDialog(
            payment = receiptPayment,
            studentName = currentStud.name,
            studentCode = currentStud.studentCode,
            teacherName = teacher?.name ?: "আব্দুল্লাহ স্যার",
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AdminToolItem(
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AmberDark
            )
        }
    }
}
