package com.utku.smartexam.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.utku.smartexam.R
import com.utku.smartexam.data.model.Exam
import com.utku.smartexam.data.model.ExamResult
import com.utku.smartexam.data.model.ExamStatus
import com.utku.smartexam.ui.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    studentId: Long,
    studentName: String,
    onTakeExam: (Long) -> Unit,
    onViewResult: (Long, Long) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(studentId) {
        viewModel.setStudent(studentId, studentName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(stringResource(R.string.student_dashboard))
                        Text(
                            text = stringResource(R.string.welcome_user, studentName),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.logout))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.available_exams)) },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.my_results)) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
            }

            if (state.isLoading) {
                LoadingScreen()
            } else {
                when (selectedTab) {
                    0 -> AvailableExamsTab(
                        exams = state.availableExams,
                        onTakeExam = onTakeExam,
                        formatDate = viewModel::formatDate
                    )
                    1 -> CompletedExamsTab(
                        completedExams = state.completedExams,
                        onViewResult = onViewResult,
                        formatDate = viewModel::formatDate
                    )
                }
            }
        }
    }
}

@Composable
fun AvailableExamsTab(
    exams: List<Exam>,
    onTakeExam: (Long) -> Unit,
    formatDate: (Long) -> String
) {
    if (exams.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_available_exams),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(R.string.check_back_later),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exams) { exam ->
                AvailableExamCard(
                    exam = exam,
                    onTakeExam = { onTakeExam(exam.id) },
                    formatDate = formatDate
                )
            }
        }
    }
}

@Composable
fun AvailableExamCard(
    exam: Exam,
    onTakeExam: () -> Unit,
    formatDate: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = exam.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${exam.subject} • ${exam.gradeLevel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(exam.examDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${exam.startTime} - ${exam.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.minutes_short, exam.durationMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Grade,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.pass_threshold, exam.passingScore),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onTakeExam,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.start_exam))
            }
        }
    }
}

@Composable
fun CompletedExamsTab(
    completedExams: List<Pair<ExamResult, Exam?>>,
    onViewResult: (Long, Long) -> Unit,
    formatDate: (Long) -> String
) {
    if (completedExams.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_completed_exams),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(R.string.exam_history_here),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(completedExams) { (result, exam) ->
                CompletedExamCard(
                    result = result,
                    exam = exam,
                    onViewResult = { onViewResult(result.examId, result.id) },
                    formatDate = formatDate
                )
            }
        }
    }
}

@Composable
fun CompletedExamCard(
    result: ExamResult,
    exam: Exam?,
    onViewResult: () -> Unit,
    formatDate: (Long) -> String
) {
    val passed = exam != null && result.score >= exam.passingScore
    val statusColor = when (result.status) {
        ExamStatus.COMPLETED -> if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        ExamStatus.FAILED_TIME_EXPIRED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewResult
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam?.name ?: stringResource(R.string.exam),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (exam != null) {
                    Text(
                        text = "${exam.subject} • ${exam.gradeLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                result.completedAt?.let {
                    Text(
                        text = stringResource(R.string.completed_date, formatDate(it)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "%.1f%%".format(result.score),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = when (result.status) {
                        ExamStatus.COMPLETED -> if (passed) stringResource(R.string.passed) else stringResource(R.string.failed)
                        ExamStatus.FAILED_TIME_EXPIRED -> stringResource(R.string.time_expired)
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}
