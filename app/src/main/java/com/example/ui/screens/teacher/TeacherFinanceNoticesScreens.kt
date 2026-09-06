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
import androidx.compose.foundation.text.KeyboardOptions
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPaymentScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val payments by viewModel.teacherPayments.collectAsState(initial = emptyList())
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())
    val selectedPayment by viewModel.selectedPayment.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddPaymentDialog by remember { mutableStateOf(false) }

    val totalCollected = payments.sumOf { it.paidAmount }
    val totalDue = payments.sumOf { it.dueAmount }

    val filteredPayments = remember(payments, selectedFilter) {
        when (selectedFilter) {
            "PAID" -> payments.filter { it.status == PaymentStatus.PAID.name }
            "DUE" -> payments.filter { it.status == PaymentStatus.DUE.name }
            "OVERDUE" -> payments.filter { it.status == PaymentStatus.OVERDUE.name }
            else -> payments
        }
    }

    val studentMap = remember(students) { students.associateBy { it.id } }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "টিউশন ফি ও পেমেন্ট" else "Tuition Fee & Payments",
                subtitle = if (isBengali) "আদায়: ৳${totalCollected.toInt()} • বকেয়া: ৳${totalDue.toInt()}" else "Collected: ৳${totalCollected.toInt()}"
            )
        },
        bottomBar = {
            TuitionBottomNav(viewModel = viewModel, currentScreen = AppScreen.TeacherPayments)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPaymentDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "পেমেন্ট গ্রহণ" else "Add Payment", fontWeight = FontWeight.Bold)
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

            // Summary Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = if (isBengali) "মোট আদায়" else "Total Collected",
                    value = "৳${totalCollected.toInt()}",
                    subtitle = if (isBengali) "চলতি সেশন" else "This session",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = SoftGreen,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = if (isBengali) "মোট বকেয়া" else "Total Due",
                    value = "৳${totalDue.toInt()}",
                    subtitle = if (isBengali) "বকেয়া ফি" else "Due fees",
                    icon = Icons.Default.PendingActions,
                    accentColor = SoftRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text(if (isBengali) "সকল (${payments.size})" else "All") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "PAID",
                        onClick = { selectedFilter = "PAID" },
                        label = { Text(if (isBengali) "পরিশোধিত (Paid)" else "Paid") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "DUE",
                        onClick = { selectedFilter = "DUE" },
                        label = { Text(if (isBengali) "বাকি (Due)" else "Due") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "OVERDUE",
                        onClick = { selectedFilter = "OVERDUE" },
                        label = { Text(if (isBengali) "মেয়াদোত্তীর্ণ (Overdue)" else "Overdue") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredPayments.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Paid,
                    message = if (isBengali) "কোনো পেমেন্ট রেকর্ড পাওয়া যায়নি" else "No payment records found",
                    actionButtonText = if (isBengali) "+ পেমেন্ট এন্ট্রি করুন" else "+ Record Payment",
                    onActionClick = { showAddPaymentDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredPayments) { p ->
                        val std = studentMap[p.studentId]
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = std?.name ?: "শিক্ষার্থী (ID: ${p.studentId})",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "মাস: ${p.monthYear} • মাধ্যম: ${p.paymentMethod}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    StatusBadge(status = p.status)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "পরিশোধ: ৳${p.paidAmount.toInt()} / ৳${p.amount.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SoftGreen
                                        )
                                    )
                                    if (p.dueAmount > 0) {
                                        Text(
                                            text = "বকেয়া: ৳${p.dueAmount.toInt()}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SoftRed
                                            )
                                        )
                                    }
                                }

                                if (p.transactionId.isNotBlank()) {
                                    Text(
                                        text = "TrxID: ${p.transactionId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "তারিখ: ${p.paymentDate.ifBlank { p.dueDate }}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    FilledTonalButton(
                                        onClick = { viewModel.selectPaymentForReceipt(p) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("রসিদ দেখুন", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Payment Dialog
    if (showAddPaymentDialog) {
        var selectedStudentId by remember { mutableLongStateOf(students.firstOrNull()?.id ?: 0L) }
        var monthYear by remember { mutableStateOf("সেপ্টেম্বর ২০২৬") }
        var amountStr by remember { mutableStateOf(students.firstOrNull()?.monthlyFee?.toInt()?.toString() ?: "2500") }
        var paidAmountStr by remember { mutableStateOf("2500") }
        var paymentMethod by remember { mutableStateOf("bKash") }
        var transactionId by remember { mutableStateOf("TRX" + (10000000..99999999).random()) }
        var note by remember { mutableStateOf("বিকাশে টিউশন ফি পরিশোধ") }
        val methods = listOf("bKash", "Nagad", "Rocket", "Cash", "Bank Transfer")

        AlertDialog(
            onDismissRequest = { showAddPaymentDialog = false },
            title = { Text(if (isBengali) "পেমেন্ট এন্ট্রি করুন" else "Record Tuition Fee") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("শিক্ষার্থী নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(students) { std ->
                            FilterChip(
                                selected = selectedStudentId == std.id,
                                onClick = {
                                    selectedStudentId = std.id
                                    amountStr = std.monthlyFee.toInt().toString()
                                    paidAmountStr = std.monthlyFee.toInt().toString()
                                },
                                label = { Text(std.name) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = monthYear,
                        onValueChange = { monthYear = it },
                        label = { Text("মাসের নাম ও সাল") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("নির্ধারিত ফি (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paidAmountStr,
                            onValueChange = { paidAmountStr = it },
                            label = { Text("পরিশোধিত (৳)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("পরিশোধের মাধ্যম:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(methods) { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("ট্রানজেকশন আইডি (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("মন্তব্য") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 2500.0
                    val paid = paidAmountStr.toDoubleOrNull() ?: amount
                    val status = if (paid >= amount) PaymentStatus.PAID else PaymentStatus.DUE

                    viewModel.savePayment(
                        studentId = selectedStudentId,
                        monthYear = monthYear,
                        amount = amount,
                        paidAmount = paid,
                        paymentMethod = paymentMethod,
                        transactionId = transactionId,
                        note = note,
                        status = status,
                        onSuccess = { showAddPaymentDialog = false }
                    )
                }) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddPaymentDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Money Receipt Dialog
    if (selectedPayment != null) {
        val std = studentMap[selectedPayment!!.studentId]
        PaymentReceiptDialog(
            payment = selectedPayment!!,
            studentName = std?.name ?: "শিক্ষার্থী",
            studentCode = std?.studentCode ?: "TBD-2024",
            teacherName = "আব্দুল্লাহ স্যার (Abdullah Sir)",
            onDismiss = { viewModel.clearSelectedPayment() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherNoticeScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val notices by viewModel.teacherNotices.collectAsState(initial = emptyList())
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "নোটিশ ও ঘোষণা বোর্ড" else "Notice & Announcements",
                subtitle = if (isBengali) "মোট নোটিশ: ${notices.size} টি" else "Total: ${notices.size}"
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
                    Icon(Icons.Default.Campaign, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "নতুন নোটিশ" else "Add Notice", fontWeight = FontWeight.Bold)
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

            if (notices.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Campaign,
                    message = if (isBengali) "কোনো নোটিশ প্রকাশিত হয়নি" else "No notices published",
                    actionButtonText = if (isBengali) "+ নোটিশ তৈরি করুন" else "+ Create Notice",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(notices) { notice ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
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
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = notice.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    StatusBadge(status = notice.priority)
                                }

                                Text(
                                    text = notice.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("📅 প্রকাশের তারিখ: ${notice.date}", style = MaterialTheme.typography.labelSmall)
                                    if (notice.expiryDate.isNotBlank()) {
                                        Text("মেয়াদ: ${notice.expiryDate}", style = MaterialTheme.typography.labelSmall, color = SoftRed)
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
        var title by remember { mutableStateOf("📢 আগামী শুক্রবার স্পেশাল ক্লাস") }
        var description by remember { mutableStateOf("সকল শিক্ষার্থীকে আগামী শুক্রবার বিকাল ৪:০০ টায় উপস্থিত থাকার অনুরোধ করা হচ্ছে।") }
        var targetType by remember { mutableStateOf("ALL") }
        var selectedStudentId by remember { mutableLongStateOf(0L) }
        var priority by remember { mutableStateOf(PriorityLevel.IMPORTANT) }
        var expiryDate by remember { mutableStateOf("১৫/০৯/২০২৬") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (isBengali) "নতুন নোটিশ প্রকাশ করুন" else "Publish Notice") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("নোটিশের শিরোনাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("বিস্তারিত বিবরণ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Text("গুরুত্ব / Priority:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = priority == PriorityLevel.NORMAL,
                            onClick = { priority = PriorityLevel.NORMAL },
                            label = { Text("সাধারণ") }
                        )
                        FilterChip(
                            selected = priority == PriorityLevel.IMPORTANT,
                            onClick = { priority = PriorityLevel.IMPORTANT },
                            label = { Text("গুরুত্বপূর্ণ") }
                        )
                        FilterChip(
                            selected = priority == PriorityLevel.URGENT,
                            onClick = { priority = PriorityLevel.URGENT },
                            label = { Text("জরুরি") }
                        )
                    }
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("মেয়াদ শেষ হওয়ার তারিখ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveNotice(
                        title = title,
                        description = description,
                        targetType = targetType,
                        targetStudentId = selectedStudentId,
                        priority = priority,
                        expiryDate = expiryDate,
                        onSuccess = { showAddDialog = false }
                    )
                }) {
                    Text("প্রকাশ করুন")
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

@Composable
fun TeacherReportsScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())
    val payments by viewModel.teacherPayments.collectAsState(initial = emptyList())
    val attendances by viewModel.teacherAttendance.collectAsState(initial = emptyList())
    val homeworks by viewModel.teacherHomework.collectAsState(initial = emptyList())

    val totalCollected = payments.sumOf { it.paidAmount }
    val totalDue = payments.sumOf { it.dueAmount }
    val totalAtt = attendances.size
    val presentAtt = attendances.count { it.status == AttendanceStatus.PRESENT.name }
    val attRate = if (totalAtt > 0) (presentAtt.toDouble() / totalAtt * 100).toInt() else 100

    val completedHw = homeworks.count { it.status == HomeworkStatus.COMPLETED.name }
    val hwRate = if (homeworks.isNotEmpty()) (completedHw.toDouble() / homeworks.size * 100).toInt() else 100

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "রিপোর্ট ও অ্যানালিটিক্স" else "Reports & Analytics",
                subtitle = if (isBengali) "মাসিক পারফরম্যান্স সারসংক্ষেপ" else "Monthly Performance Summary"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Financial Report Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBengali) "অর্থনৈতিক হিসাব বিবরণী (Financial Overview)" else "Financial Overview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBengali) "মোট সম্ভাব্য ফি:" else "Total Expected:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("৳${(totalCollected + totalDue).toInt()}", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBengali) "মোট আদায়কৃত:" else "Total Collected:", color = SoftGreen)
                        Text("৳${totalCollected.toInt()}", color = SoftGreen, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBengali) "মোট বকেয়া:" else "Total Due:", color = SoftRed)
                        Text("৳${totalDue.toInt()}", color = SoftRed, fontWeight = FontWeight.Bold)
                    }

                    // Progress bar
                    val collectionRatio = if ((totalCollected + totalDue) > 0) (totalCollected / (totalCollected + totalDue)).toFloat() else 1f
                    LinearProgressIndicator(
                        progress = { collectionRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SoftGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "আদায়ের হার: ${(collectionRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Attendance & Academic Report Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBengali) "একাডেমিক অগ্রগতি (Academic Progress)" else "Academic Progress",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBengali) "গড় উপস্থিতি:" else "Avg Attendance:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$attRate%", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { attRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isBengali) "হোমওয়ার্ক সম্পন্ন করার হার:" else "Homework Completion:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$hwRate%", color = SoftBlue, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { hwRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SoftBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Student Rankings & Status
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBengali) "শিক্ষার্থী অনুযায়ী সংক্ষিপ্ত বিবরণী" else "Students Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    students.forEach { std ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(std.name, fontWeight = FontWeight.SemiBold)
                                Text("${std.className} • রোল: ${std.roll}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("৳${std.monthlyFee.toInt()}/মাস", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherMessagesScreen(viewModel: TuitionViewModel) {
    val isBengali by viewModel.isBengali.collectAsState()
    val messages by viewModel.userMessages.collectAsState(initial = emptyList())
    val students by viewModel.teacherStudents.collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedRecipientId by remember { mutableLongStateOf(students.firstOrNull()?.userId ?: 0L) }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TuitionTopBar(
                viewModel = viewModel,
                title = if (isBengali) "শিক্ষার্থী মেসেজিং (Chat)" else "Messages",
                subtitle = if (isBengali) "সরাসরি প্রশ্নোত্তর ও যোগাযোগ" else "Direct communication"
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
                .padding(16.dp)
        ) {
            // Recipient Selector
            Text(if (isBengali) "প্রাপক নির্বাচন করুন:" else "Select Student:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(students) { std ->
                    FilterChip(
                        selected = selectedRecipientId == std.userId,
                        onClick = { selectedRecipientId = std.userId },
                        label = { Text(std.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message Stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentUser?.id
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Send Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text(if (isBengali) "মেসেজ লিখুন..." else "Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val recipientStudent = students.find { it.userId == selectedRecipientId }
                            viewModel.sendMessage(
                                recipientUserId = selectedRecipientId,
                                recipientName = recipientStudent?.name ?: "Student",
                                content = messageText,
                                onSuccess = { messageText = "" }
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
