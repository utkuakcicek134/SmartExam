package com.utku.smartexam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.utku.smartexam.data.model.User
import com.utku.smartexam.data.model.UserRole
import com.utku.smartexam.ui.auth.LoginScreen
import com.utku.smartexam.ui.auth.RegisterScreen
import com.utku.smartexam.ui.settings.SettingsScreen
import com.utku.smartexam.ui.student.ExamResultScreen
import com.utku.smartexam.ui.student.StudentDashboardScreen
import com.utku.smartexam.ui.student.TakeExamScreen
import com.utku.smartexam.ui.teacher.CreateExamScreen
import com.utku.smartexam.ui.teacher.TeacherDashboardScreen
import com.utku.smartexam.ui.teacher.TeacherExamResultsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    onLanguageChanged: () -> Unit = {}
) {
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLanguageChanged = onLanguageChanged
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    when (user.role) {
                        UserRole.TEACHER -> navController.navigate(Screen.TeacherDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        UserRole.STUDENT -> navController.navigate(Screen.StudentDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TeacherDashboard.route) {
            currentUser?.let { user ->
                TeacherDashboardScreen(
                    teacherId = user.id,
                    teacherName = user.fullName,
                    onCreateExam = {
                        navController.navigate(Screen.CreateExam.createRoute(user.id))
                    },
                    onEditExam = { examId ->
                        navController.navigate(Screen.EditExam.createRoute(user.id, examId))
                    },
                    onViewResults = { examId ->
                        navController.navigate(Screen.TeacherExamResults.createRoute(examId))
                    },
                    onSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onLogout = {
                        currentUser = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.CreateExam.route,
            arguments = listOf(navArgument("teacherId") { type = NavType.LongType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getLong("teacherId") ?: 0L
            CreateExamScreen(
                teacherId = teacherId,
                examId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditExam.route,
            arguments = listOf(
                navArgument("teacherId") { type = NavType.LongType },
                navArgument("examId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getLong("teacherId") ?: 0L
            val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
            CreateExamScreen(
                teacherId = teacherId,
                examId = examId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.TeacherExamResults.route,
            arguments = listOf(navArgument("examId") { type = NavType.LongType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
            TeacherExamResultsScreen(
                examId = examId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.StudentDashboard.route) {
            currentUser?.let { user ->
                StudentDashboardScreen(
                    studentId = user.id,
                    studentName = user.fullName,
                    onTakeExam = { examId ->
                        navController.navigate(Screen.TakeExam.createRoute(user.id, examId))
                    },
                    onViewResult = { examId, resultId ->
                        navController.navigate(Screen.ExamResult.createRoute(examId, resultId))
                    },
                    onSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onLogout = {
                        currentUser = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.TakeExam.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.LongType },
                navArgument("examId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
            TakeExamScreen(
                studentId = studentId,
                examId = examId,
                onExamComplete = { eId, resultId ->
                    navController.navigate(Screen.ExamResult.createRoute(eId, resultId)) {
                        popUpTo(Screen.StudentDashboard.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ExamResult.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.LongType },
                navArgument("resultId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
            val resultId = backStackEntry.arguments?.getLong("resultId") ?: 0L
            ExamResultScreen(
                examId = examId,
                resultId = resultId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
