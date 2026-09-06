package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {

    // --- Users ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    // --- Teachers ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Delete
    suspend fun deleteTeacher(teacher: TeacherEntity)

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: Long): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE userId = :userId LIMIT 1")
    suspend fun getTeacherByUserId(userId: Long): TeacherEntity?

    @Query("SELECT * FROM teachers ORDER BY id DESC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    // --- Students ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE studentCode = :code LIMIT 1")
    suspend fun getStudentByCode(code: String): StudentEntity?

    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    suspend fun getStudentByUserId(userId: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getStudentsByTeacher(teacherId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY id DESC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    // --- Subjects ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects WHERE teacherId = :teacherId")
    fun getSubjectsByTeacher(teacherId: Long): Flow<List<SubjectEntity>>

    // --- Routines ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("SELECT * FROM routines WHERE teacherId = :teacherId ORDER BY id ASC")
    fun getRoutinesByTeacher(teacherId: Long): Flow<List<RoutineEntity>>

    // --- Lessons ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    @Query("SELECT * FROM lessons WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getLessonsByTeacher(teacherId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE (studentId = :studentId OR studentId = 0) ORDER BY id DESC")
    fun getLessonsForStudent(studentId: Long): Flow<List<LessonEntity>>

    // --- Homework ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity): Long

    @Update
    suspend fun updateHomework(homework: HomeworkEntity)

    @Delete
    suspend fun deleteHomework(homework: HomeworkEntity)

    @Query("SELECT * FROM homeworks WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getHomeworkByTeacher(teacherId: Long): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homeworks WHERE studentId = :studentId ORDER BY id DESC")
    fun getHomeworkByStudent(studentId: Long): Flow<List<HomeworkEntity>>

    // --- Attendance ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendances WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getAttendanceByTeacher(teacherId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE studentId = :studentId ORDER BY id DESC")
    fun getAttendanceByStudent(studentId: Long): Flow<List<AttendanceEntity>>

    // --- Payments ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getPaymentsByTeacher(teacherId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY id DESC")
    fun getPaymentsByStudent(studentId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    // --- Notices ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity): Long

    @Update
    suspend fun updateNotice(notice: NoticeEntity)

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity)

    @Query("SELECT * FROM notices WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getNoticesByTeacher(teacherId: Long): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE (targetType = 'ALL' OR targetStudentId = :studentId) ORDER BY id DESC")
    fun getNoticesForStudent(studentId: Long): Flow<List<NoticeEntity>>

    // --- Performance ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformance(performance: PerformanceEntity): Long

    @Delete
    suspend fun deletePerformance(performance: PerformanceEntity)

    @Query("SELECT * FROM performances WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getPerformanceByTeacher(teacherId: Long): Flow<List<PerformanceEntity>>

    @Query("SELECT * FROM performances WHERE studentId = :studentId ORDER BY id DESC")
    fun getPerformanceByStudent(studentId: Long): Flow<List<PerformanceEntity>>

    // --- Notes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudentNoteEntity): Long

    @Delete
    suspend fun deleteNote(note: StudentNoteEntity)

    @Query("SELECT * FROM student_notes WHERE studentId = :studentId ORDER BY id DESC")
    fun getNotesByStudent(studentId: Long): Flow<List<StudentNoteEntity>>

    // --- Messages ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE senderId = :userId OR recipientId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: Long): Flow<List<MessageEntity>>

    // --- Notifications ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY id DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<AppNotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)
}
