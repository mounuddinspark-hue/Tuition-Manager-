package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    SUPER_ADMIN,
    TEACHER,
    STUDENT
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    LEAVE
}

enum class PaymentStatus {
    PAID,
    DUE,
    PARTIAL,
    OVERDUE
}

enum class HomeworkStatus {
    PENDING,
    COMPLETED,
    LATE
}

enum class PriorityLevel {
    NORMAL,
    IMPORTANT,
    URGENT
}

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"])
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val role: String, // SUPER_ADMIN, TEACHER, STUDENT
    val status: String = "ACTIVE",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "teachers",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["phone"])
    ]
)
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val qualification: String,
    val joiningDate: String,
    val status: String = "ACTIVE",
    val monthlyIncome: Double = 0.0,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["studentCode"], unique = true),
        Index(value = ["teacherId"]),
        Index(value = ["userId"]),
        Index(value = ["className"]),
        Index(value = ["status"])
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val userId: Long,
    val name: String,
    val studentCode: String,
    val phone: String,
    val email: String,
    val dob: String,
    val institution: String,
    val className: String,
    val section: String,
    val roll: String,
    val guardianName: String,
    val guardianPhone: String,
    val address: String,
    val admissionDate: String,
    val monthlyFee: Double,
    val paymentDueDay: Int = 10,
    val status: String = "ACTIVE",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val name: String,
    val code: String,
    val colorHex: String = "#0F766E",
    val iconName: String = "menu_book"
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val dayOfWeek: String, // Saturday, Sunday, Monday, Tuesday, Wednesday, Thursday, Friday
    val startTime: String, // e.g. "05:00 PM"
    val endTime: String,   // e.g. "06:00 PM"
    val subjectName: String,
    val studentOrGroup: String,
    val room: String = "Home / Online",
    val note: String = "",
    val isCancelled: Boolean = false,
    val cancelReason: String = ""
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val studentId: Long = 0, // 0 = all
    val subjectName: String,
    val date: String,
    val chapter: String,
    val topic: String,
    val description: String,
    val pageNo: String,
    val status: String = "In Progress",
    val teacherNote: String = ""
)

@Entity(tableName = "homeworks")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val studentId: Long,
    val subjectName: String,
    val title: String,
    val description: String,
    val givenDate: String,
    val dueDate: String,
    val priority: String = "NORMAL",
    val status: String = "PENDING", // PENDING, COMPLETED, LATE
    val studentSubmission: String = ""
)

@Entity(
    tableName = "attendances",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["date"]),
        Index(value = ["teacherId"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val studentId: Long,
    val date: String, // DD/MM/YYYY
    val className: String,
    val subjectName: String,
    val status: String, // PRESENT, ABSENT, LATE, LEAVE
    val note: String = ""
)

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["teacherId"]),
        Index(value = ["monthYear"]),
        Index(value = ["status"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val studentId: Long,
    val monthYear: String, // e.g. "September 2026"
    val amount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val paymentDate: String,
    val dueDate: String,
    val paymentMethod: String, // CASH, BKASH, NAGAD, ROCKET, BANK, OTHER
    val transactionId: String = "",
    val note: String = "",
    val status: String // PAID, DUE, PARTIAL, OVERDUE
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val title: String,
    val description: String,
    val date: String,
    val targetType: String = "ALL", // ALL, SPECIFIC
    val targetStudentId: Long = 0,
    val priority: String = "NORMAL", // NORMAL, IMPORTANT, URGENT
    val expiryDate: String = ""
)

@Entity(tableName = "performances")
data class PerformanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val studentId: Long,
    val subjectName: String,
    val topic: String,
    val examName: String,
    val score: Double,
    val totalMarks: Double = 100.0,
    val examDate: String,
    val teacherComment: String = ""
)

@Entity(tableName = "student_notes")
data class StudentNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val studentId: Long,
    val content: String,
    val isVisibleToStudent: Boolean = false,
    val date: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val senderName: String,
    val senderRole: String,
    val recipientId: Long,
    val recipientName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String, // HOMEWORK, PAYMENT, NOTICE, CLASS_CANCEL, ROUTINE, ATTENDANCE, MESSAGE
    val date: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
