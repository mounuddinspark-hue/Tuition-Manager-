package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

enum class AuthPortalTab {
    LOGIN,
    STUDENT_SIGNUP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    var currentTab by remember { mutableStateOf(AuthPortalTab.LOGIN) }

    // Unified Login state (Email or User ID + Password)
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Student Sign Up state
    var newStudentName by remember { mutableStateOf("") }
    var newStudentEmail by remember { mutableStateOf("") }
    var newStudentPassword by remember { mutableStateOf("") }
    var newStudentConfirmPassword by remember { mutableStateOf("") }
    var newStudentClass by remember { mutableStateOf("Class 10") }
    var newStudentRoll by remember { mutableStateOf("") }
    var teacherAssignedId by remember { mutableStateOf("") }
    var newStudentPhone by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showCredentialHelper by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Hero Header with Soft Gradient Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EmeraldDark,
                                EmeraldPrimary
                            )
                        )
                    )
                    .padding(top = 36.dp, bottom = 28.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Tuition BD",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Text(
                        text = "Tuition Manager BD",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBengali) "🇧🇩 স্মার্ট টিউশন ও একাডেমি ম্যানেজমেন্ট" else "Smart Tuition & Academy Management",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Segmented Tabs: Sign In vs Student Sign-Up
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isLoginSelected = currentTab == AuthPortalTab.LOGIN
                        val loginBg by animateColorAsState(
                            targetValue = if (isLoginSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            animationSpec = tween(200),
                            label = "loginTabBg"
                        )
                        val loginText = if (isLoginSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(loginBg)
                                .clickable {
                                    currentTab = AuthPortalTab.LOGIN
                                    errorMessage = null
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = loginText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBengali) "লগইন (Sign In)" else "Sign In",
                                    color = loginText,
                                    fontWeight = if (isLoginSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        val isSignupSelected = currentTab == AuthPortalTab.STUDENT_SIGNUP
                        val signupBg by animateColorAsState(
                            targetValue = if (isSignupSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            animationSpec = tween(200),
                            label = "signupTabBg"
                        )
                        val signupText = if (isSignupSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(signupBg)
                                .clickable {
                                    currentTab = AuthPortalTab.STUDENT_SIGNUP
                                    errorMessage = null
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AppRegistration,
                                    contentDescription = null,
                                    tint = signupText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBengali) "শিক্ষার্থী সাইন আপ" else "Student Sign Up",
                                    color = signupText,
                                    fontWeight = if (isSignupSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Message if any
                if (errorMessage != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftRedLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SoftRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMessage!!, color = SoftRed, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }

                // TAB 1: UNIFIED SECURE PORTAL SIGN IN (Teacher uses email, student uses ID/code)
                if (currentTab == AuthPortalTab.LOGIN) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = if (isBengali) "পোর্টাল একাউন্টে সাইন-ইন" else "Sign In to Portal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isBengali) "আপনার নিবন্ধিত ইমেইল বা আইডি এবং পাসওয়ার্ড দিয়ে প্রবেশ করুন" else "Enter your registered email or ID and password",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = loginIdentifier,
                                    onValueChange = { loginIdentifier = it; errorMessage = null },
                                    label = { Text(if (isBengali) "ইমেইল অথবা ইউজার আইডি *" else "Email or User ID *") },
                                    placeholder = { Text(if (isBengali) "যেমন: ইমেইল বা স্টুডেন্ট আইডি" else "e.g. email or student ID") },
                                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it; errorMessage = null },
                                    label = { Text(if (isBengali) "পাসওয়ার্ড *" else "Password *") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Icon(
                                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { showForgotPasswordDialog = true },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (isBengali) "পাসওয়ার্ড ভুলে গেছেন?" else "Forgot Password?",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                                            errorMessage = if (isBengali) "ইমেইল/আইডি এবং পাসওয়ার্ড প্রদান করুন" else "Enter email/ID and password"
                                            return@Button
                                        }
                                        isLoading = true
                                        errorMessage = null
                                        viewModel.login(loginIdentifier.trim(), loginPassword) { success, msg ->
                                            isLoading = false
                                            if (!success) errorMessage = msg
                                        }
                                    },
                                    enabled = !isLoading && !isGoogleLoading,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isBengali) "প্রবেশ করুন (Sign In)" else "Sign In",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Google Sign In Button
                                GoogleSignInButton(
                                    isLoading = isGoogleLoading,
                                    onClick = {
                                        isGoogleLoading = true
                                        errorMessage = null
                                        viewModel.loginWithGoogle { success, msg ->
                                            isGoogleLoading = false
                                            if (!success) errorMessage = msg
                                        }
                                    }
                                )
                            }
                        }

                        // Discreet Credential Helper for Convenience
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showCredentialHelper = !showCredentialHelper },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isBengali) "💡 দ্রুত প্রবেশ তথ্য ও অটো-ফিল" else "💡 Quick Credentials & Auto-fill",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showCredentialHelper) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                AnimatedVisibility(visible = showCredentialHelper) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Teacher Email Fill
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = EmeraldContainer,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    loginIdentifier = "teacher@tuitionbd.com"
                                                    loginPassword = "123456"
                                                    errorMessage = null
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("ইমেইল: teacher@tuitionbd.com", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldDark)
                                                    Text("পাসওয়ার্ড: 123456 (ট্যাপ করে অটো-ফিল করুন)", fontSize = 11.sp, color = EmeraldDark.copy(alpha = 0.8f))
                                                }
                                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        // Student ID Fill
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SoftBlueLight,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    loginIdentifier = "student1"
                                                    loginPassword = "123456"
                                                    errorMessage = null
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.School, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("শিক্ষার্থী আইডি: student1 (বা TBD-2024-001)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SoftBlue)
                                                    Text("পাসওয়ার্ড: 123456 (ট্যাপ করে অটো-ফিল করুন)", fontSize = 11.sp, color = SoftBlue.copy(alpha = 0.8f))
                                                }
                                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = SoftBlue, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: STUDENT SIGN UP MODE (Explicit User Request:
                // "পাসওয়ার্ড ইমেইল স্টুডেন্ট নাম শ্রেনী রুল শিক্ষক নির্ধারিত usr ID")
                if (currentTab == AuthPortalTab.STUDENT_SIGNUP) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AppRegistration, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isBengali) "শিক্ষার্থী সাইন আপ ফরম" else "Student Sign Up Form",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = if (isBengali) "শিক্ষকের দেওয়া আইডি নম্বর দিয়ে একাউন্ট খুলুন" else "Register using your Teacher-Assigned User ID",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    // Teacher Assigned ID (CRITICAL FIELD REQUESTED BY USER)
                                    OutlinedTextField(
                                        value = teacherAssignedId,
                                        onValueChange = { teacherAssignedId = it; errorMessage = null },
                                        label = { Text(if (isBengali) "শিক্ষক নির্ধারিত User ID / কোড *" else "Teacher-Assigned User ID *") },
                                        placeholder = { Text("যেমন: TBD-2026-001 বা STD-101") },
                                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = EmeraldPrimary) },
                                        supportingText = {
                                            Text(
                                                if (isBengali) "💡 শিক্ষক আপনাকে যে ইউজার আইডি দিয়েছেন তা এখানে লিখুন" 
                                                else "Enter the User ID assigned by your teacher"
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Student Name
                                    OutlinedTextField(
                                        value = newStudentName,
                                        onValueChange = { newStudentName = it; errorMessage = null },
                                        label = { Text(if (isBengali) "শিক্ষার্থীর পুরো নাম *" else "Student Full Name *") },
                                        placeholder = { Text("যেমন: তাহমিদ ইসলাম") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Student Email
                                    OutlinedTextField(
                                        value = newStudentEmail,
                                        onValueChange = { newStudentEmail = it; errorMessage = null },
                                        label = { Text(if (isBengali) "ইমেইল এড্রেস *" else "Email Address *") },
                                        placeholder = { Text("e.g. student@gmail.com") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Class and Roll in a Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newStudentClass,
                                            onValueChange = { newStudentClass = it; errorMessage = null },
                                            label = { Text(if (isBengali) "শ্রেণী *" else "Class *") },
                                            placeholder = { Text("১০ম শ্রেণী") },
                                            leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = newStudentRoll,
                                            onValueChange = { newStudentRoll = it; errorMessage = null },
                                            label = { Text(if (isBengali) "রোল নম্বর *" else "Roll No *") },
                                            placeholder = { Text("০১") },
                                            leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Password
                                    OutlinedTextField(
                                        value = newStudentPassword,
                                        onValueChange = { newStudentPassword = it; errorMessage = null },
                                        label = { Text(if (isBengali) "পাসওয়ার্ড *" else "Password *") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Confirm Password
                                    OutlinedTextField(
                                        value = newStudentConfirmPassword,
                                        onValueChange = { newStudentConfirmPassword = it; errorMessage = null },
                                        label = { Text(if (isBengali) "পাসওয়ার্ড নিশ্চিত করুন *" else "Confirm Password *") },
                                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Optional Mobile Phone
                                    OutlinedTextField(
                                        value = newStudentPhone,
                                        onValueChange = { newStudentPhone = it },
                                        label = { Text(if (isBengali) "মোবাইল নম্বর (ঐচ্ছিক)" else "Phone Number (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Submit Button
                                    Button(
                                        onClick = {
                                            if (newStudentName.isBlank() || teacherAssignedId.isBlank() || newStudentEmail.isBlank() || newStudentPassword.isBlank() || newStudentClass.isBlank() || newStudentRoll.isBlank()) {
                                                errorMessage = if (isBengali) "নাম, শিক্ষক নির্ধারিত আইডি, ইমেইল, পাসওয়ার্ড, শ্রেণী ও রোল পূরণ করুন" else "Please fill all required fields"
                                                return@Button
                                            }
                                            if (newStudentPassword != newStudentConfirmPassword) {
                                                errorMessage = if (isBengali) "পাসওয়ার্ড দুটি মিলছে না" else "Passwords do not match"
                                                return@Button
                                            }
                                            isLoading = true
                                            errorMessage = null
                                            viewModel.registerStudent(
                                                studentName = newStudentName,
                                                email = newStudentEmail,
                                                password = newStudentPassword,
                                                className = newStudentClass,
                                                roll = newStudentRoll,
                                                teacherAssignedUserId = teacherAssignedId,
                                                phone = newStudentPhone,
                                                onResult = { success, msg ->
                                                    isLoading = false
                                                    if (!success) {
                                                        errorMessage = msg
                                                    }
                                                }
                                            )
                                        },
                                        enabled = !isLoading,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isBengali) "সাইন আপ সম্পন্ন করুন ও প্রবেশ করুন" else "Complete Sign Up & Login",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }

                                    // Switch back to login
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isBengali) "ইতিমধ্যে একাউন্ট আছে? " else "Already have an account? ")
                                        TextButton(onClick = { currentTab = AuthPortalTab.LOGIN }) {
                                            Text(if (isBengali) "লগইন করুন" else "Log In", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Info
                Text(
                    text = "Tuition Manager BD • Mobile & Web App",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text(if (isBengali) "পাসওয়ার্ড রিসেট (Reset Password)" else "Reset Password") },
            text = {
                Text(
                    if (isBengali)
                        "পাসওয়ার্ড ভুলে গেলে সংশ্লিষ্ট শিক্ষক বা সুপার অ্যাডমিনের সাথে যোগাযোগ করুন। অথবা ডেমো পাসওয়ার্ড ব্যবহার করুন:\n\n• শিক্ষক: 123456\n• শিক্ষার্থী: 123456\n• অ্যাডমিন: admin123"
                    else
                        "Please contact your Teacher or Super Admin to reset password. Or use demo credentials:\n\n• Teacher: 123456\n• Student: 123456\n• Admin: admin123"
                )
            },
            confirmButton = {
                Button(onClick = { showForgotPasswordDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

// Google Styled Sign-In Button with 4-color "G" Emblem
@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                // Official Google "G" 4-color symbol drawn in Canvas
                GoogleGIcon(modifier = Modifier.size(22.dp))

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Google দিয়ে সাইন ইন করুন (Sign in with Google)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "১ ক্লিকে নিরাপদ ও দ্রুত প্রবেশ",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Canvas-drawn Google "G" 4-color Emblem
@Composable
fun GoogleGIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.22f
        val radius = (size.width - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Red arc (top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 190f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Yellow arc (left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Green arc (bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 30f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Blue arc & bar (right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 320f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x, center.y),
            end = Offset(size.width - (strokeWidth / 3f), center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

// Standalone Register Teacher Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTeacherScreen(viewModel: TuitionViewModel) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val isBengali by viewModel.isBengali.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBengali) "শিক্ষক রেজিস্ট্রেশন (Teacher Registration)" else "Teacher Registration") },
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
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EmeraldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBengali) "নতুন শিক্ষক অ্যাকাউন্ট তৈরি করুন। এটি আপনাকে টিউশনের সম্পূর্ণ এডমিন প্যানেল এক্সেস দেবে।" else "Create teacher account with full admin privileges.",
                        fontSize = 13.sp,
                        color = EmeraldDark
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMsg = null },
                label = { Text(if (isBengali) "পুরো নাম *" else "Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; errorMsg = null },
                label = { Text(if (isBengali) "মোবাইল নম্বর *" else "Mobile Number *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(if (isBengali) "ইমেইল (ঐচ্ছিক)" else "Email (Optional)") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = qualification,
                onValueChange = { qualification = it },
                label = { Text(if (isBengali) "শিক্ষাগত যোগ্যতা (যেমন: M.Sc in Mathematics)" else "Qualification") },
                leadingIcon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(if (isBengali) "ঠিকানা / এলাকা" else "Address / Location") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMsg = null },
                label = { Text(if (isBengali) "পাসওয়ার্ড *" else "Password *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMsg = null },
                label = { Text(if (isBengali) "পাসওয়ার্ড নিশ্চিত করুন *" else "Confirm Password *") },
                leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

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
                    if (name.isBlank() || phone.isBlank() || password.isBlank()) {
                        errorMsg = if (isBengali) "নাম, মোবাইল নম্বর ও পাসওয়ার্ড আবশ্যক" else "Name, phone and password required"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMsg = if (isBengali) "পাসওয়ার্ড মিলছে না" else "Passwords do not match"
                        return@Button
                    }
                    viewModel.registerTeacher(
                        name = name,
                        phone = phone,
                        email = email,
                        qualification = qualification,
                        address = address,
                        pass = password,
                        onSuccess = {
                            viewModel.navigateTo(AppScreen.TeacherDashboard, clearBackstack = true)
                        }
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isBengali) "নিবন্ধন সম্পন্ন করুন ও এডমিন এক্সেস পান" else "Complete Registration & Get Admin Access", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Standalone Register Student Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStudentScreen(viewModel: TuitionViewModel) {
    var studentName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("Class 10") }
    var roll by remember { mutableStateOf("") }
    var teacherAssignedId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val isBengali by viewModel.isBengali.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBengali) "শিক্ষার্থী সাইন আপ" else "Student Sign Up") },
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
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftBlueLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SoftBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBengali) "শিক্ষকের দেওয়া User ID দিয়ে সাইন আপ করুন। এরপর বাড়ির কাজ ও এসাইনমেন্ট জমা দিতে পারবেন এবং উপস্থিতি ও নোটিশ দেখতে পারবেন।" else "Sign up using your teacher-assigned User ID to submit homework and view attendance & notices.",
                        fontSize = 12.sp,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            OutlinedTextField(
                value = teacherAssignedId,
                onValueChange = { teacherAssignedId = it; errorMessage = null },
                label = { Text(if (isBengali) "শিক্ষক নির্ধারিত User ID *" else "Teacher-Assigned User ID *") },
                placeholder = { Text("e.g. TBD-2026-001") },
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it; errorMessage = null },
                label = { Text(if (isBengali) "শিক্ষার্থীর পুরো নাম *" else "Student Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text(if (isBengali) "ইমেইল এড্রেস *" else "Email Address *") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it; errorMessage = null },
                    label = { Text(if (isBengali) "শ্রেণী *" else "Class *") },
                    leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = roll,
                    onValueChange = { roll = it; errorMessage = null },
                    label = { Text(if (isBengali) "রোল *" else "Roll *") },
                    leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(if (isBengali) "মোবাইল নম্বর" else "Mobile Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text(if (isBengali) "পাসওয়ার্ড *" else "Password *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text(if (isBengali) "পাসওয়ার্ড নিশ্চিত করুন *" else "Confirm Password *") },
                leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (studentName.isBlank() || teacherAssignedId.isBlank() || email.isBlank() || password.isBlank() || className.isBlank() || roll.isBlank()) {
                        errorMessage = if (isBengali) "সকল তথ্য সঠিকভাবে পূরণ করুন" else "Please fill all required fields"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = if (isBengali) "পাসওয়ার্ড দুটি মিলছে না" else "Passwords do not match"
                        return@Button
                    }
                    isLoading = true
                    viewModel.registerStudent(
                        studentName = studentName,
                        email = email,
                        password = password,
                        className = className,
                        roll = roll,
                        teacherAssignedUserId = teacherAssignedId,
                        phone = phone,
                        onResult = { success, msg ->
                            isLoading = false
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (isBengali) "রেজিস্ট্রেশন সম্পন্ন করুন ও প্রবেশ করুন" else "Complete Registration & Enter", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
