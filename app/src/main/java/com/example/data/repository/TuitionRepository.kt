package com.example.data.repository

import com.example.data.local.TuitionDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TuitionRepository(private val dao: TuitionDao) {

    // Flows
    val allTeachers: Flow<List<TeacherEntity>> = dao.getAllTeachers()
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allPayments: Flow<List<PaymentEntity>> = dao.getAllPayments()

    fun getStudentsByTeacher(teacherId: Long): Flow<List<StudentEntity>> = dao.getStudentsByTeacher(teacherId)
    fun getSubjectsByTeacher(teacherId: Long): Flow<List<SubjectEntity>> = dao.getSubjectsByTeacher(teacherId)
    fun getRoutinesByTeacher(teacherId: Long): Flow<List<RoutineEntity>> = dao.getRoutinesByTeacher(teacherId)
    fun getLessonsByTeacher(teacherId: Long): Flow<List<LessonEntity>> = dao.getLessonsByTeacher(teacherId)
    fun getLessonsForStudent(studentId: Long): Flow<List<LessonEntity>> = dao.getLessonsForStudent(studentId)
    fun getHomeworkByTeacher(teacherId: Long): Flow<List<HomeworkEntity>> = dao.getHomeworkByTeacher(teacherId)
    fun getHomeworkByStudent(studentId: Long): Flow<List<HomeworkEntity>> = dao.getHomeworkByStudent(studentId)
    fun getAttendanceByTeacher(teacherId: Long): Flow<List<AttendanceEntity>> = dao.getAttendanceByTeacher(teacherId)
    fun getAttendanceByStudent(studentId: Long): Flow<List<AttendanceEntity>> = dao.getAttendanceByStudent(studentId)
    fun getPaymentsByTeacher(teacherId: Long): Flow<List<PaymentEntity>> = dao.getPaymentsByTeacher(teacherId)
    fun getPaymentsByStudent(studentId: Long): Flow<List<PaymentEntity>> = dao.getPaymentsByStudent(studentId)
    fun getNoticesByTeacher(teacherId: Long): Flow<List<NoticeEntity>> = dao.getNoticesByTeacher(teacherId)
    fun getNoticesForStudent(studentId: Long): Flow<List<NoticeEntity>> = dao.getNoticesForStudent(studentId)
    fun getPerformanceByTeacher(teacherId: Long): Flow<List<PerformanceEntity>> = dao.getPerformanceByTeacher(teacherId)
    fun getPerformanceByStudent(studentId: Long): Flow<List<PerformanceEntity>> = dao.getPerformanceByStudent(studentId)
    fun getNotesByStudent(studentId: Long): Flow<List<StudentNoteEntity>> = dao.getNotesByStudent(studentId)
    fun getMessagesForUser(userId: Long): Flow<List<MessageEntity>> = dao.getMessagesForUser(userId)
    fun getNotificationsForUser(userId: Long): Flow<List<AppNotificationEntity>> = dao.getNotificationsForUser(userId)

    // User / Auth
    suspend fun getUserByUsername(username: String): UserEntity? = dao.getUserByUsername(username)
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    suspend fun getUserById(userId: Long): UserEntity? = dao.getUserById(userId)
    suspend fun insertUser(user: UserEntity): Long = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    // Teacher
    suspend fun getTeacherById(id: Long): TeacherEntity? = dao.getTeacherById(id)
    suspend fun getTeacherByUserId(userId: Long): TeacherEntity? = dao.getTeacherByUserId(userId)
    suspend fun insertTeacher(teacher: TeacherEntity): Long = dao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: TeacherEntity) = dao.updateTeacher(teacher)
    suspend fun deleteTeacher(teacher: TeacherEntity) = dao.deleteTeacher(teacher)

    // Student
    suspend fun getStudentById(id: Long): StudentEntity? = dao.getStudentById(id)
    suspend fun getStudentByCode(code: String): StudentEntity? = dao.getStudentByCode(code)
    suspend fun getStudentByUserId(userId: Long): StudentEntity? = dao.getStudentByUserId(userId)
    suspend fun insertStudent(student: StudentEntity): Long = dao.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = dao.updateStudent(student)
    suspend fun deleteStudent(student: StudentEntity) = dao.deleteStudent(student)

    // Subject
    suspend fun insertSubject(subject: SubjectEntity): Long = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    // Routine
    suspend fun insertRoutine(routine: RoutineEntity): Long = dao.insertRoutine(routine)
    suspend fun updateRoutine(routine: RoutineEntity) = dao.updateRoutine(routine)
    suspend fun deleteRoutine(routine: RoutineEntity) = dao.deleteRoutine(routine)

    // Lesson
    suspend fun insertLesson(lesson: LessonEntity): Long = dao.insertLesson(lesson)
    suspend fun updateLesson(lesson: LessonEntity) = dao.updateLesson(lesson)
    suspend fun deleteLesson(lesson: LessonEntity) = dao.deleteLesson(lesson)

    // Homework
    suspend fun insertHomework(homework: HomeworkEntity): Long = dao.insertHomework(homework)
    suspend fun updateHomework(homework: HomeworkEntity) = dao.updateHomework(homework)
    suspend fun deleteHomework(homework: HomeworkEntity) = dao.deleteHomework(homework)

    // Attendance
    suspend fun insertAttendance(attendance: AttendanceEntity): Long = dao.insertAttendance(attendance)
    suspend fun updateAttendance(attendance: AttendanceEntity) = dao.updateAttendance(attendance)
    suspend fun deleteAttendance(attendance: AttendanceEntity) = dao.deleteAttendance(attendance)

    // Payment
    suspend fun insertPayment(payment: PaymentEntity): Long = dao.insertPayment(payment)
    suspend fun updatePayment(payment: PaymentEntity) = dao.updatePayment(payment)
    suspend fun deletePayment(payment: PaymentEntity) = dao.deletePayment(payment)

    // Notice
    suspend fun insertNotice(notice: NoticeEntity): Long = dao.insertNotice(notice)
    suspend fun updateNotice(notice: NoticeEntity) = dao.updateNotice(notice)
    suspend fun deleteNotice(notice: NoticeEntity) = dao.deleteNotice(notice)

    // Performance
    suspend fun insertPerformance(performance: PerformanceEntity): Long = dao.insertPerformance(performance)
    suspend fun deletePerformance(performance: PerformanceEntity) = dao.deletePerformance(performance)

    // Notes
    suspend fun insertNote(note: StudentNoteEntity): Long = dao.insertNote(note)
    suspend fun deleteNote(note: StudentNoteEntity) = dao.deleteNote(note)

    // Messages
    suspend fun insertMessage(message: MessageEntity): Long = dao.insertMessage(message)

    // Notifications
    suspend fun insertNotification(notification: AppNotificationEntity): Long = dao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)

    // Seed Demo Data if empty
    suspend fun preloadDemoDataIfNeeded() {
        val existingAdmin = dao.getUserByUsername("admin")
        if (existingAdmin != null) return

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // 1. Create Teacher / Tutor (Single Teacher Tuition System)
        val teacher1UserId = dao.insertUser(
            UserEntity(
                username = "teacher1",
                passwordHash = "123456",
                fullName = "আব্দুল্লাহ স্যার (Abdullah Sir)",
                phone = "01711223344",
                email = "teacher@tuitionbd.com",
                role = UserRole.TEACHER.name
            )
        )
        val teacher1Id = dao.insertTeacher(
            TeacherEntity(
                userId = teacher1UserId,
                name = "আব্দুল্লাহ স্যার (Abdullah Sir)",
                phone = "01711223344",
                email = "teacher@tuitionbd.com",
                address = "মিরপুর-১০, ঢাকা (Mirpur-10, Dhaka)",
                qualification = "M.Sc in Mathematics (DU)",
                joiningDate = "01/01/2023",
                monthlyIncome = 25000.0
            )
        )

        // Super Admin account also maps to the same teacher for full control
        val adminUserId = dao.insertUser(
            UserEntity(
                username = "admin",
                passwordHash = "admin123",
                fullName = "আব্দুল্লাহ স্যার (এডমিন)",
                phone = "01711223344",
                email = "admin@tuitionbd.com",
                role = UserRole.TEACHER.name
            )
        )

        // 3. Create Students for Abdullah Sir
        // Student 1: Rahim
        val student1UserId = dao.insertUser(
            UserEntity(
                username = "student1",
                passwordHash = "123456",
                fullName = "রহিম আহমেদ (Rahim Ahmed)",
                phone = "01911223344",
                email = "rahim@gmail.com",
                role = UserRole.STUDENT.name
            )
        )
        val student1Id = dao.insertStudent(
            StudentEntity(
                teacherId = teacher1Id,
                userId = student1UserId,
                name = "রহিম আহমেদ (Rahim Ahmed)",
                studentCode = "TBD-2024-001",
                phone = "01911223344",
                email = "rahim@gmail.com",
                dob = "12/04/2008",
                institution = "ঢাকা রেসিডেনসিয়াল মডেল কলেজ (DRMC)",
                className = "Class 10",
                section = "A",
                roll = "01",
                guardianName = "মো: রফিক আহমেদ",
                guardianPhone = "01722334455",
                address = "মিরপুর-২, ঢাকা",
                admissionDate = "10/01/2024",
                monthlyFee = 2500.0,
                paymentDueDay = 10
            )
        )

        // Student 2: Karim
        val student2UserId = dao.insertUser(
            UserEntity(
                username = "student2",
                passwordHash = "123456",
                fullName = "করিম হাসান (Karim Hasan)",
                phone = "01922334455",
                email = "karim@gmail.com",
                role = UserRole.STUDENT.name
            )
        )
        val student2Id = dao.insertStudent(
            StudentEntity(
                teacherId = teacher1Id,
                userId = student2UserId,
                name = "করিম হাসান (Karim Hasan)",
                studentCode = "TBD-2024-002",
                phone = "01922334455",
                email = "karim@gmail.com",
                dob = "20/08/2008",
                institution = "আইডিয়াল স্কুল অ্যান্ড কলেজ",
                className = "Class 10",
                section = "B",
                roll = "02",
                guardianName = "মো: জহিরুল হক",
                guardianPhone = "01833445566",
                address = "ফার্মগেট, ঢাকা",
                admissionDate = "12/01/2024",
                monthlyFee = 2500.0,
                paymentDueDay = 10
            )
        )

        // Student 3: Nusrat
        val student3UserId = dao.insertUser(
            UserEntity(
                username = "student3",
                passwordHash = "123456",
                fullName = "নুসরাত জাহান (Nusrat Jahan)",
                phone = "01933445566",
                email = "nusrat@gmail.com",
                role = UserRole.STUDENT.name
            )
        )
        val student3Id = dao.insertStudent(
            StudentEntity(
                teacherId = teacher1Id,
                userId = student3UserId,
                name = "নুসরাত জাহান (Nusrat Jahan)",
                studentCode = "TBD-2024-003",
                phone = "01933445566",
                email = "nusrat@gmail.com",
                dob = "05/11/2009",
                institution = "ভিকারুননিসা নূন স্কুল",
                className = "Class 9",
                section = "Morning",
                roll = "05",
                guardianName = "শাহানা বেগম",
                guardianPhone = "01744556677",
                address = "মোহাম্মদপুর, ঢাকা",
                admissionDate = "15/02/2024",
                monthlyFee = 2000.0,
                paymentDueDay = 10
            )
        )

        // 4. Create Subjects
        dao.insertSubject(SubjectEntity(teacherId = teacher1Id, name = "গণিত (Mathematics)", code = "MATH-101", colorHex = "#0F766E"))
        dao.insertSubject(SubjectEntity(teacherId = teacher1Id, name = "পদার্থবিজ্ঞান (Physics)", code = "PHY-102", colorHex = "#2563EB"))
        dao.insertSubject(SubjectEntity(teacherId = teacher1Id, name = "রসায়ন (Chemistry)", code = "CHEM-103", colorHex = "#D97706"))
        dao.insertSubject(SubjectEntity(teacherId = teacher1Id, name = "ইংরেজি (English)", code = "ENG-104", colorHex = "#7C3AED"))
        dao.insertSubject(SubjectEntity(teacherId = teacher1Id, name = "বাংলা (Bangla)", code = "BAN-105", colorHex = "#16A34A"))

        // 5. Weekly Class Routines
        dao.insertRoutine(
            RoutineEntity(
                teacherId = teacher1Id,
                dayOfWeek = "Saturday",
                startTime = "05:00 PM",
                endTime = "06:30 PM",
                subjectName = "গণিত (Mathematics)",
                studentOrGroup = "রহিম ও করিম (Class 10)",
                room = "হোম ব্যাচ রুম ১",
                note = "অধ্যায় ৩ বীজগণিত সমাধান"
            )
        )
        dao.insertRoutine(
            RoutineEntity(
                teacherId = teacher1Id,
                dayOfWeek = "Sunday",
                startTime = "06:00 PM",
                endTime = "07:30 PM",
                subjectName = "পদার্থবিজ্ঞান (Physics)",
                studentOrGroup = "নুসরাত জাহান (Class 9)",
                room = "অনলাইন Google Meet",
                note = "গতি ও বল অধ্যায়"
            )
        )
        dao.insertRoutine(
            RoutineEntity(
                teacherId = teacher1Id,
                dayOfWeek = "Monday",
                startTime = "05:00 PM",
                endTime = "06:30 PM",
                subjectName = "রসায়ন (Chemistry)",
                studentOrGroup = "রহিম ও করিম (Class 10)",
                room = "হোম ব্যাচ রুম ১",
                note = "পর্যায় সারণি ও মৌল"
            )
        )
        dao.insertRoutine(
            RoutineEntity(
                teacherId = teacher1Id,
                dayOfWeek = "Tuesday",
                startTime = "06:00 PM",
                endTime = "07:30 PM",
                subjectName = "গণিত (Mathematics)",
                studentOrGroup = "নুসরাত জাহান (Class 9)",
                room = "অনলাইন Google Meet",
                note = "জ্যামিতি উপপাদ্য"
            )
        )
        dao.insertRoutine(
            RoutineEntity(
                teacherId = teacher1Id,
                dayOfWeek = "Wednesday",
                startTime = "05:00 PM",
                endTime = "06:30 PM",
                subjectName = "ইংরেজি (English)",
                studentOrGroup = "সকল শিক্ষার্থী (All)",
                room = "মেইন হল",
                note = "Grammar & Composition"
            )
        )

        // 6. Lessons ("আজকের পড়া")
        dao.insertLesson(
            LessonEntity(
                teacherId = teacher1Id,
                studentId = student1Id,
                subjectName = "গণিত (Mathematics)",
                date = todayStr,
                chapter = "অধ্যায় ৩ - বীজগাণিতিক রাশি",
                topic = "বর্গ ও ঘনের সূত্রাবলী এবং মান নির্ণয়",
                description = "অনুশীলনী ৩.১ এর উদাহরণ ১-৫ এবং ১ থেকে ১০ পর্যন্ত প্রশ্নের সমাধান বুঝিয়ে দেওয়া হয়েছে।",
                pageNo = "৪৪ - ৪৮",
                status = "In Progress",
                teacherNote = "রহিম সূত্রগুলো ভালোভাবে মুখস্থ করেছে, প্রয়োগে আরও সতর্ক থাকতে হবে।"
            )
        )
        dao.insertLesson(
            LessonEntity(
                teacherId = teacher1Id,
                studentId = student2Id,
                subjectName = "পদার্থবিজ্ঞান (Physics)",
                date = todayStr,
                chapter = "অধ্যায় ২ - গতি (Motion)",
                topic = "গতির সমীকরণ ও লেখচিত্র",
                description = "v = u + at এবং s = ut + 1/2 at^2 সমীকরণ প্রতিপাদন ও ব্যবহার।",
                pageNo = "৩২ - ৩৭",
                status = "Completed",
                teacherNote = "গ্রাফের ঢাল নির্ণয়ে চমৎকার পারফর্ম করেছে।"
            )
        )

        // 7. Homework ("পড়ার কাজ")
        dao.insertHomework(
            HomeworkEntity(
                teacherId = teacher1Id,
                studentId = student1Id,
                subjectName = "গণিত (Mathematics)",
                title = "অনুশীলনী ৩.১ এর সব অংক সমাধান",
                description = "পৃষ্ঠা ৪৬ এর ৬, ৭, ৮, এবং ১০ নম্বর সৃজনশীল সমাধান খাতায় করে আনবে।",
                givenDate = todayStr,
                dueDate = "আগামীকাল (Tomorrow)",
                priority = PriorityLevel.IMPORTANT.name,
                status = HomeworkStatus.PENDING.name
            )
        )
        dao.insertHomework(
            HomeworkEntity(
                teacherId = teacher1Id,
                studentId = student2Id,
                subjectName = "পদার্থবিজ্ঞান (Physics)",
                title = "গতির তিনটি সমীকরণ লিখে প্রমাণ করো",
                description = "লেখচিত্রের সাহায্যে ৩টি সমীকরণ ব্যাখ্যা করে আনবে।",
                givenDate = todayStr,
                dueDate = "০৬/০৯/২০২৬",
                priority = PriorityLevel.NORMAL.name,
                status = HomeworkStatus.COMPLETED.name,
                studentSubmission = "খাতায় ৩টি প্রমাণ সম্পন্ন করা হয়েছে।"
            )
        )
        dao.insertHomework(
            HomeworkEntity(
                teacherId = teacher1Id,
                studentId = student3Id,
                subjectName = "ইংরেজি (English)",
                title = "Write a Paragraph on 'A Rainy Day'",
                description = "Write 150 words using proper tenses and vocabulary.",
                givenDate = todayStr,
                dueDate = "০৭/০৯/২০২৬",
                priority = PriorityLevel.URGENT.name,
                status = HomeworkStatus.PENDING.name
            )
        )

        // 8. Attendance Records
        dao.insertAttendance(AttendanceEntity(teacherId = teacher1Id, studentId = student1Id, date = todayStr, className = "Class 10", subjectName = "গণিত", status = AttendanceStatus.PRESENT.name, note = "সময়ে এসেছে"))
        dao.insertAttendance(AttendanceEntity(teacherId = teacher1Id, studentId = student2Id, date = todayStr, className = "Class 10", subjectName = "গণিত", status = AttendanceStatus.PRESENT.name, note = "উপস্থিত"))
        dao.insertAttendance(AttendanceEntity(teacherId = teacher1Id, studentId = student3Id, date = todayStr, className = "Class 9", subjectName = "পদার্থবিজ্ঞান", status = AttendanceStatus.LATE.name, note = "১০ মিনিট দেরিতে উপস্থিত"))
        // Previous dates for Rahim
        dao.insertAttendance(AttendanceEntity(teacherId = teacher1Id, studentId = student1Id, date = "02/09/2026", className = "Class 10", subjectName = "পদার্থবিজ্ঞান", status = AttendanceStatus.PRESENT.name))
        dao.insertAttendance(AttendanceEntity(teacherId = teacher1Id, studentId = student1Id, date = "01/09/2026", className = "Class 10", subjectName = "রসায়ন", status = AttendanceStatus.PRESENT.name))
        dao.insertAttendance(AttendanceEntity(teacherId = teacher1Id, studentId = student1Id, date = "31/08/2026", className = "Class 10", subjectName = "গণিত", status = AttendanceStatus.ABSENT.name, note = "অসুস্থতার জন্য ছুটি"))

        // 9. Payment Records
        dao.insertPayment(
            PaymentEntity(
                teacherId = teacher1Id,
                studentId = student1Id,
                monthYear = "সেপ্টেম্বর ২০২৬",
                amount = 2500.0,
                paidAmount = 2500.0,
                dueAmount = 0.0,
                paymentDate = todayStr,
                dueDate = "১০/০৯/২০২৬",
                paymentMethod = "bKash",
                transactionId = "TRX938472918",
                note = "বিকাশে ফি পরিশোধিত",
                status = PaymentStatus.PAID.name
            )
        )
        dao.insertPayment(
            PaymentEntity(
                teacherId = teacher1Id,
                studentId = student2Id,
                monthYear = "সেপ্টেম্বর ২০২৬",
                amount = 2500.0,
                paidAmount = 0.0,
                dueAmount = 2500.0,
                paymentDate = "",
                dueDate = "১০/০৯/২০২৬",
                paymentMethod = "Cash",
                transactionId = "",
                note = "১০ তারিখের মধ্যে দেওয়ার কথা",
                status = PaymentStatus.DUE.name
            )
        )
        dao.insertPayment(
            PaymentEntity(
                teacherId = teacher1Id,
                studentId = student3Id,
                monthYear = "আগস্ট ২০২৬",
                amount = 2000.0,
                paidAmount = 1000.0,
                dueAmount = 1000.0,
                paymentDate = "১৫/০৮/২০২৬",
                dueDate = "১০/০৮/২০২৬",
                paymentMethod = "Nagad",
                transactionId = "NGD820194812",
                note = "বাকি ১০০০ টাকা চলতি মাসে দিবে",
                status = PaymentStatus.OVERDUE.name
            )
        )

        // 10. Notices
        dao.insertNotice(
            NoticeEntity(
                teacherId = teacher1Id,
                title = "📢 আগামী শুক্রবার স্পেশাল গণিত মক টেস্ট",
                description = "সকল Class 10 শিক্ষার্থীদের আগামী শুক্রবার বিকাল ৪:০০ টায় বীজগণিত ও জ্যামিতি ৫০ নম্বরের মক টেস্টে অংশগ্রহণ করতে হবে। ক্যালকুলেটর সাথে রাখবে।",
                date = todayStr,
                targetType = "ALL",
                priority = PriorityLevel.URGENT.name,
                expiryDate = "১০/০৯/২০২৬"
            )
        )
        dao.insertNotice(
            NoticeEntity(
                teacherId = teacher1Id,
                title = "📌 টিউশন ফি পরিশোধ সংক্রান্ত নির্দেশনা",
                description = "সম্মানিত অভিভাবকবৃন্দ, সেপ্টেম্বর মাসের টিউশন ফি আগামী ১০ তারিখের মধ্যে বিকাশ অথবা নগদে পরিশোধের অনুরোধ করা হলো।",
                date = "০১/০৯/২০২৬",
                targetType = "ALL",
                priority = PriorityLevel.IMPORTANT.name,
                expiryDate = "১৫/০৯/২০২৬"
            )
        )

        // 11. Performance
        dao.insertPerformance(
            PerformanceEntity(
                teacherId = teacher1Id,
                studentId = student1Id,
                subjectName = "গণিত",
                topic = "বীজগাণিতিক রাশি",
                examName = "সাপ্তাহিক মূল্যায়ন পরীক্ষা ১",
                score = 23.5,
                totalMarks = 25.0,
                examDate = "৩০/০৮/২০২৬",
                teacherComment = "খুবই ভালো প্রস্তুতি। ক্যালকুলেশনে আরও দ্রুত হতে হবে।"
            )
        )
        dao.insertPerformance(
            PerformanceEntity(
                teacherId = teacher1Id,
                studentId = student2Id,
                subjectName = "পদার্থবিজ্ঞান",
                topic = "গতি ও ত্বরণ",
                examName = "সাপ্তাহিক মূল্যায়ন পরীক্ষা ১",
                score = 20.0,
                totalMarks = 25.0,
                examDate = "৩০/০৮/২০২৬",
                teacherComment = "গ্রাফিক সমাধানে চমৎকার, সমীকরণ ব্যবহারে সচেতন থাকা চাই।"
            )
        )

        // 12. Student Notes
        dao.insertNote(
            StudentNoteEntity(
                teacherId = teacher1Id,
                studentId = student1Id,
                content = "গণিতে অনেক ভালো উন্নতি করছে। সৃজনশীলে বেশ মনোযোগী।",
                isVisibleToStudent = true,
                date = todayStr
            )
        )
        dao.insertNote(
            StudentNoteEntity(
                teacherId = teacher1Id,
                studentId = student2Id,
                content = "হোমওয়ার্ক নিয়মিত করার ব্যাপারে আরেকটু সচেতন করা প্রয়োজন।",
                isVisibleToStudent = false,
                date = todayStr
            )
        )

        // 13. Messages
        dao.insertMessage(
            MessageEntity(
                senderId = teacher1UserId,
                senderName = "আব্দুল্লাহ স্যার",
                senderRole = "TEACHER",
                recipientId = student1UserId,
                recipientName = "রহিম আহমেদ",
                content = "আসসালামু আলাইকুম রহিম, আগামীকালের গণিত হোমওয়ার্ক শেষ হয়েছে?",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
        dao.insertMessage(
            MessageEntity(
                senderId = student1UserId,
                senderName = "রহিম আহমেদ",
                senderRole = "STUDENT",
                recipientId = teacher1UserId,
                recipientName = "আব্দুল্লাহ স্যার",
                content = "ওয়ালাইকুম আসসালাম স্যার, জি প্রায় শেষ। শেষ দুইটা অংক নিয়ে একটু প্রশ্ন আছে, ক্লাসে দেখাবো।",
                timestamp = System.currentTimeMillis() - 1800000
            )
        )

        // 14. Notifications
        dao.insertNotification(
            AppNotificationEntity(
                userId = student1UserId,
                title = "নতুন হোমওয়ার্ক দেওয়া হয়েছে",
                message = "গণিত অনুশীলনী ৩.১ এর সব অংক সমাধান করো।",
                type = "HOMEWORK",
                date = todayStr
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                userId = student2UserId,
                title = "বেতন বকেয়া রয়েছে",
                message = "আপনার সেপ্টেম্বর মাসের টিউশন ফি পরিশোধের শেষ তারিখ ১০ সেপ্টেম্বর।",
                type = "PAYMENT",
                date = todayStr
            )
        )
    }
}
