package com.utku.smartexam.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Settings : Screen("settings")
    object TeacherDashboard : Screen("teacher_dashboard")
    object StudentDashboard : Screen("student_dashboard")
    object CreateExam : Screen("create_exam/{teacherId}") {
        fun createRoute(teacherId: Long) = "create_exam/$teacherId"
    }
    object EditExam : Screen("edit_exam/{teacherId}/{examId}") {
        fun createRoute(teacherId: Long, examId: Long) = "edit_exam/$teacherId/$examId"
    }
    object ExamDetail : Screen("exam_detail/{examId}") {
        fun createRoute(examId: Long) = "exam_detail/$examId"
    }
    object TakeExam : Screen("take_exam/{studentId}/{examId}") {
        fun createRoute(studentId: Long, examId: Long) = "take_exam/$studentId/$examId"
    }
    object ExamResult : Screen("exam_result/{examId}/{resultId}") {
        fun createRoute(examId: Long, resultId: Long) = "exam_result/$examId/$resultId"
    }
    object StudentExamHistory : Screen("student_exam_history")
    object TeacherExamResults : Screen("teacher_exam_results/{examId}") {
        fun createRoute(examId: Long) = "teacher_exam_results/$examId"
    }
}
