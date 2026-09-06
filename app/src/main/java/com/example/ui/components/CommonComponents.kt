package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.util.Locale
import com.example.data.model.*
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionTopBar(
    viewModel: TuitionViewModel,
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = { viewModel.goBack() },
    actions: @Composable RowScope.() -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTeacher by viewModel.currentTeacher.collectAsState()
    val currentStudent by viewModel.currentStudent.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()
    val isTeacherPreviewMode by viewModel.isTeacherPreviewMode.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Tuition BD",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Profile Chip with Sir's Name / Student's Name and Photo
                if (isTeacherPreviewMode) {
                    Button(
                        onClick = { viewModel.exitStudentPreviewToTeacher() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBengali) "এডমিন প্যানেল" else "Admin Hub",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (currentUser != null) {
                    val isTeacher = currentUser?.role == UserRole.TEACHER.name || currentUser?.role == UserRole.SUPER_ADMIN.name
                    val displayName = if (isTeacher) {
                        currentTeacher?.name ?: if (isBengali) "আব্দুল্লাহ স্যার" else "Abdullah Sir"
                    } else {
                        currentStudent?.name ?: if (isBengali) "শিক্ষার্থী" else "Student"
                    }
                    val photoUrl = if (isTeacher) currentTeacher?.avatarUrl.orEmpty() else currentStudent?.avatarUrl.orEmpty()

                    Surface(
                        onClick = {
                            if (isTeacher) {
                                viewModel.navigateTo(AppScreen.TeacherProfile)
                            } else {
                                viewModel.navigateTo(AppScreen.StudentProfile)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isTeacher) EmeraldContainer.copy(alpha = 0.55f) else SoftBlueLight.copy(alpha = 0.55f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (isTeacher) EmeraldPrimary else SoftBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayName.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = if (isTeacher) EmeraldPrimary else SoftBlueDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 84.dp)
                                )
                                Text(
                                    text = if (isTeacher) (if (isBengali) "প্রোফাইল" else "Teacher") else (if (isBengali) "আইডি: ${currentStudent?.studentCode ?: ""}" else "ID: ${currentStudent?.studentCode ?: ""}"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Language toggle (বাংলা / EN)
                FilledTonalButton(
                    onClick = { viewModel.toggleLanguage() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBengali) "বাংলা" else "EN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Logout button
                if (currentUser != null) {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = SoftRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Dark mode toggle
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                actions()
            }
        }
    }
}

@Composable
fun TuitionBottomNav(
    viewModel: TuitionViewModel,
    currentScreen: AppScreen
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()

    if (currentUser == null) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        when (currentUser?.role) {
            UserRole.SUPER_ADMIN.name -> {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.AdminDashboard,
                    onClick = { viewModel.navigateTo(AppScreen.AdminDashboard) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(if (isBengali) "কন্ট্রোল" else "Hub") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.AdminStudents,
                    onClick = { viewModel.navigateTo(AppScreen.AdminStudents) },
                    icon = { Icon(Icons.Default.School, contentDescription = "Students") },
                    label = { Text(if (isBengali) "শিক্ষার্থী ও আইডি" else "Students") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.AdminReports,
                    onClick = { viewModel.navigateTo(AppScreen.AdminReports) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                    label = { Text(if (isBengali) "রিপোর্ট" else "Reports") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.AdminSettings,
                    onClick = { viewModel.navigateTo(AppScreen.AdminSettings) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(if (isBengali) "সেটিংস" else "Settings") }
                )
            }
            UserRole.TEACHER.name -> {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TeacherDashboard,
                    onClick = { viewModel.navigateTo(AppScreen.TeacherDashboard) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(if (isBengali) "হোম" else "Home") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TeacherStudents || currentScreen == AppScreen.TeacherAddStudent || currentScreen == AppScreen.TeacherStudentDetail,
                    onClick = { viewModel.navigateTo(AppScreen.TeacherStudents) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Students") },
                    label = { Text(if (isBengali) "শিক্ষার্থী" else "Students") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TeacherRoutine,
                    onClick = { viewModel.navigateTo(AppScreen.TeacherRoutine) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Classes") },
                    label = { Text(if (isBengali) "ক্লাস" else "Classes") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TeacherPayments,
                    onClick = { viewModel.navigateTo(AppScreen.TeacherPayments) },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Payments") },
                    label = { Text(if (isBengali) "বেতন" else "Payment") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TeacherAttendance ||
                            currentScreen == AppScreen.TeacherHomework ||
                            currentScreen == AppScreen.TeacherLesson ||
                            currentScreen == AppScreen.TeacherNotices ||
                            currentScreen == AppScreen.TeacherSettings ||
                            currentScreen == AppScreen.TeacherReports,
                    onClick = { viewModel.navigateTo(AppScreen.TeacherAttendance) },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                    label = { Text(if (isBengali) "মেনু" else "More") }
                )
            }
            UserRole.STUDENT.name -> {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.StudentDashboard,
                    onClick = { viewModel.navigateTo(AppScreen.StudentDashboard) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(if (isBengali) "হোম" else "Home") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.StudentRoutine,
                    onClick = { viewModel.navigateTo(AppScreen.StudentRoutine) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Routine") },
                    label = { Text(if (isBengali) "রুটিন" else "Routine") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.StudentHomework,
                    onClick = { viewModel.navigateTo(AppScreen.StudentHomework) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Homework") },
                    label = { Text(if (isBengali) "কাজ" else "Homework") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.StudentAttendance,
                    onClick = { viewModel.navigateTo(AppScreen.StudentAttendance) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Attendance") },
                    label = { Text(if (isBengali) "উপস্থিতি" else "Attendance") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.StudentPayments,
                    onClick = { viewModel.navigateTo(AppScreen.StudentPayments) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Payments") },
                    label = { Text(if (isBengali) "বেতন" else "Payment") }
                )
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "PRESENT", "PAID", "COMPLETED", "ACTIVE" -> Triple(SoftGreenLight, SoftGreen, when (status.uppercase()) {
            "PRESENT" -> "উপস্থিত (Present)"
            "PAID" -> "পরিশোধিত (Paid)"
            "COMPLETED" -> "সম্পন্ন (Completed)"
            else -> "সক্রিয় (Active)"
        })
        "ABSENT", "OVERDUE", "LATE_SUBMIT", "INACTIVE" -> Triple(SoftRedLight, SoftRed, when (status.uppercase()) {
            "ABSENT" -> "অনুপস্থিত (Absent)"
            "OVERDUE" -> "বকেয়া (Overdue)"
            else -> "নিষ্ক্রিয় (Inactive)"
        })
        "DUE", "PENDING", "LATE" -> Triple(AmberLight, AmberDark, when (status.uppercase()) {
            "DUE" -> "বাকি (Due)"
            "PENDING" -> "চলমান (Pending)"
            "LATE" -> "দেরি (Late)"
            else -> "Pending"
        })
        "LEAVE", "PARTIAL" -> Triple(SoftBlueLight, SoftBlue, when (status.uppercase()) {
            "LEAVE" -> "ছুটি (Leave)"
            "PARTIAL" -> "আংশিক (Partial)"
            else -> "Leave"
        })
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    message: String,
    description: String? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = message,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )

        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
        }

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionButtonText)
            }
        }
    }
}

// ==========================================
// OFFICIAL BANGLADESH WALLET LOGOS & BADGES
// ==========================================

@Composable
fun BKashLogoBadge(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFE2136E),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "bKash Bird",
                    tint = Color(0xFFE2136E),
                    modifier = Modifier.size(12.dp)
                )
            }
            Column {
                Text(
                    text = "bKash বিকাশ",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun NagadLogoBadge(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFF7931E),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "Nagad Flame",
                    tint = Color(0xFFE51A24),
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = "নগদ Nagad",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun RocketLogoBadge(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF8C388E),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Rocket DBBL",
                    tint = Color(0xFF8C388E),
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = "Rocket রকেট",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun UpayLogoBadge(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF005696),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFFB600)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "upay UCB",
                    tint = Color(0xFF005696),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "upay উপায়",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun BankLogoBadge(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF1E3A8A),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "Bank Transfer",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "ব্যাংক ট্রান্সফার",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CashLogoBadge(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF059669),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = "Cash Hand-to-Hand",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "নগদ ক্যাশ গ্রহণ",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OfficialWalletBadge(paymentMethod: String, modifier: Modifier = Modifier) {
    when (paymentMethod.uppercase()) {
        "BKASH" -> BKashLogoBadge(modifier)
        "NAGAD" -> NagadLogoBadge(modifier)
        "ROCKET" -> RocketLogoBadge(modifier)
        "UPAY" -> UpayLogoBadge(modifier)
        "BANK" -> BankLogoBadge(modifier)
        else -> CashLogoBadge(modifier)
    }
}

@Composable
fun PaymentReceiptDialog(
    payment: PaymentEntity,
    studentName: String,
    studentCode: String,
    teacherName: String,
    onDismiss: () -> Unit
) {
    var downloaded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Official Receipt Guilloche-inspired Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF064E3B), Color(0xFF0F766E), Color(0xFF115E59))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "TUITION MANAGER BD",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "প্রাইভেট কেয়ার ও একাডেমি • মানি রিসিট",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "অফিসিয়াল বেতন পরিশোধ রশিদ (MONEY RECEIPT)",
                                color = Color(0xFFFEF08A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Receipt Body Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Voucher & Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ভাউচার নং (Voucher No)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "VCH-${payment.id + 202600}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "পরিশোধের তারিখ (Date)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = payment.paymentDate.ifBlank { "১০/০৯/২০২৬" },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.8.dp)

                    // Student and Teacher Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("শিক্ষার্থীর নাম:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(studentName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("আইডি: $studentCode", style = MaterialTheme.typography.bodySmall.copy(color = SoftBlueDark, fontWeight = FontWeight.SemiBold))
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("শিক্ষক / তত্ত্বাবধায়ক:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(teacherName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("মাস: ${payment.monthYear}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wallet & Payment Method Section with OFFICIAL BRAND LOGO
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "পরিশোধের মাধ্যম (Wallet):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OfficialWalletBadge(payment.paymentMethod)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ট্রানজেকশন আইডি (TrxID):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = payment.transactionId.ifBlank { "TRX${payment.id + 883921}" },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fees Breakdown Table
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("নির্ধারিত টিউশন ফি:", style = MaterialTheme.typography.bodySmall)
                                Text("৳${String.format(Locale.US, "%.2f", payment.amount)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("পরিশোধিত অর্থ (Paid Amount):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SoftGreen))
                                Text("৳${String.format(Locale.US, "%.2f", payment.paidAmount)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = SoftGreen))
                            }
                            if (payment.dueAmount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("বকেয়া অর্থ (Due):", style = MaterialTheme.typography.bodySmall.copy(color = SoftRed, fontWeight = FontWeight.Bold))
                                    Text("৳${String.format(Locale.US, "%.2f", payment.dueAmount)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SoftRed))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stamp & Signature Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official Rubber Stamp
                        Box(
                            modifier = Modifier
                                .rotate(-7f)
                                .border(2.dp, Color(0xFF059669), RoundedCornerShape(8.dp))
                                .background(Color(0xFF059669).copy(alpha = 0.10f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(15.dp)
                                )
                                Column {
                                    Text(
                                        text = "PAID & VERIFIED",
                                        color = Color(0xFF059669),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "পরিশোধিত ও অনুমোদিত",
                                        color = Color(0xFF059669),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Teacher Signature
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Abdullah Al Mamun",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            Text(
                                text = "শিক্ষকের স্বাক্ষর ও সীলমোহর",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barcode illustration
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                    ) {
                        val barWidth = 3f
                        var x = 10f
                        val totalW = size.width - 20f
                        while (x < totalW) {
                            val w = if ((x.toInt() % 7) == 0) barWidth * 2 else barWidth
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.6f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = w
                            )
                            x += w + if ((x.toInt() % 5) == 0) 4f else 2f
                        }
                    }

                    if (downloaded) {
                        Spacer(modifier = Modifier.height(8.dp))
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
                                    text = "বেতন রসিদ ডিভাইসে সফলভাবে সেভ হয়েছে!",
                                    color = SoftGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("বন্ধ করুন")
                        }
                        Button(
                            onClick = { downloaded = true },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Download", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ডাউনলোড স্লিপ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
