package com.utku.smartexam.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utku.smartexam.data.model.Exam
import com.utku.smartexam.data.model.ExamResult
import com.utku.smartexam.data.model.ExamStatus
import com.utku.smartexam.data.model.Question
import com.utku.smartexam.data.repository.ExamRepository
import com.utku.smartexam.data.repository.ExamResultRepository
import com.utku.smartexam.ui.components.LoadingScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamResultScreenState(
    val exam: Exam? = null,
    val result: ExamResult? = null,
    val questions: List<Question> = emptyList(),
    val answers: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true,
    val canViewDetails: Boolean = false
)

@HiltViewModel
class ExamResultScreenViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val examResultRepository: ExamResultRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExamResultScreenState())
    val state: StateFlow<ExamResultScreenState> = _state.asStateFlow()

    fun loadResult(examId: Long, resultId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val exam = examRepository.getExamById(examId)
            val result = examResultRepository.getExamResultById(resultId)
            val questions = examRepository.getQuestionsByExamIdSync(examId)

            val canViewDetails = if (exam != null && result != null) {
                exam.showResultsImmediately || 
                System.currentTimeMillis() > getExamEndTime(exam)
            } else false

            val answers = if (result != null && canViewDetails) {
                examResultRepository.parseAnswers(result.answers)
            } else emptyMap()

            _state.update {
                it.copy(
                    exam = exam,
                    result = result,
                    questions = questions,
                    answers = answers,
                    isLoading = false,
                    canViewDetails = canViewDetails
                )
            }
        }
    }

    private fun getExamEndTime(exam: Exam): Long {
        val parts = exam.endTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 23
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 59
        
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = exam.examDate
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
        }
        return calendar.timeInMillis
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    examId: Long,
    resultId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExamResultScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(examId, resultId) {
        viewModel.loadResult(examId, resultId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Result") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.result?.let { result ->
                    state.exam?.let { exam ->
                        val passed = result.score >= exam.passingScore
                        val statusColor = when (result.status) {
                            ExamStatus.COMPLETED -> if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ExamStatus.FAILED_TIME_EXPIRED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }

                        Icon(
                            imageVector = when {
                                result.status == ExamStatus.FAILED_TIME_EXPIRED -> Icons.Default.TimerOff
                                passed -> Icons.Default.CheckCircle
                                else -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = statusColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when {
                                result.status == ExamStatus.FAILED_TIME_EXPIRED -> "Time Expired"
                                passed -> "Congratulations!"
                                else -> "Keep Trying!"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )

                        Text(
                            text = when {
                                result.status == ExamStatus.FAILED_TIME_EXPIRED -> "You ran out of time"
                                passed -> "You passed the exam!"
                                else -> "You didn't pass this time"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = exam.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${exam.subject} • Grade ${exam.gradeLevel}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "%.1f%%".format(result.score),
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )

                                Text(
                                    text = "Your Score",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Passing Score: ${exam.passingScore}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Detailed Results",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ResultStatItem(
                                        icon = Icons.Default.CheckCircle,
                                        label = "Correct",
                                        value = result.correctAnswers.toString(),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    ResultStatItem(
                                        icon = Icons.Default.Cancel,
                                        label = "Incorrect",
                                        value = result.incorrectAnswers.toString(),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    ResultStatItem(
                                        icon = Icons.Default.RadioButtonUnchecked,
                                        label = "Blank",
                                        value = result.blankAnswers.toString(),
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    ResultStatItem(
                                        icon = Icons.Default.Quiz,
                                        label = "Total",
                                        value = result.totalQuestions.toString(),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (state.canViewDetails && state.questions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Answer Review",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    state.questions.forEachIndexed { index, question ->
                                        val userAnswer = state.answers[question.id]
                                        val isCorrect = userAnswer == question.correctAnswer

                                        AnswerReviewItem(
                                            questionNumber = index + 1,
                                            questionText = question.questionText,
                                            userAnswer = userAnswer,
                                            correctAnswer = question.correctAnswer,
                                            isCorrect = isCorrect,
                                            getOptionText = { option ->
                                                when (option) {
                                                    "A" -> question.optionA
                                                    "B" -> question.optionB
                                                    "C" -> question.optionC
                                                    "D" -> question.optionD
                                                    "E" -> question.optionE ?: ""
                                                    else -> ""
                                                }
                                            }
                                        )

                                        if (index < state.questions.size - 1) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                        }
                                    }
                                }
                            }
                        } else if (!state.canViewDetails) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Detailed answers will be available after the exam period ends.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Dashboard")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ResultStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AnswerReviewItem(
    questionNumber: Int,
    questionText: String,
    userAnswer: String?,
    correctAnswer: String,
    isCorrect: Boolean,
    getOptionText: (String) -> String
) {
    Column {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Q$questionNumber.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.padding(start = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (userAnswer != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Your answer: $userAnswer",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    text = "Not answered",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (!isCorrect) {
                Text(
                    text = "Correct: $correctAnswer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
