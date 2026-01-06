package com.utku.smartexam.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utku.smartexam.data.model.Exam
import com.utku.smartexam.data.model.ExamResult
import com.utku.smartexam.data.model.ExamStatus
import com.utku.smartexam.data.model.User
import com.utku.smartexam.data.repository.ExamRepository
import com.utku.smartexam.data.repository.ExamResultRepository
import com.utku.smartexam.data.repository.UserRepository
import com.utku.smartexam.ui.components.LoadingScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherExamResultsState(
    val exam: Exam? = null,
    val results: List<Pair<ExamResult, User?>> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TeacherExamResultsViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val examResultRepository: ExamResultRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TeacherExamResultsState())
    val state: StateFlow<TeacherExamResultsState> = _state.asStateFlow()

    fun loadExamResults(examId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val exam = examRepository.getExamById(examId)
            _state.update { it.copy(exam = exam) }

            examResultRepository.getExamResultsByExam(examId).collect { results ->
                val resultsWithUsers = results.map { result ->
                    val user = userRepository.getUserById(result.studentId)
                    result to user
                }
                _state.update { it.copy(results = resultsWithUsers, isLoading = false) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExamResultsScreen(
    examId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TeacherExamResultsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(examId) {
        viewModel.loadExamResults(examId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Exam Results")
                        state.exam?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
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
        } else if (state.results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No results yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "No students have taken this exam yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    state.exam?.let { exam ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Statistics",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val completedResults = state.results.filter { 
                                    it.first.status == ExamStatus.COMPLETED 
                                }
                                val avgScore = if (completedResults.isNotEmpty()) {
                                    completedResults.map { it.first.score }.average()
                                } else 0.0
                                val passedCount = completedResults.count { 
                                    it.first.score >= exam.passingScore 
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatItem(
                                        label = "Total",
                                        value = state.results.size.toString()
                                    )
                                    StatItem(
                                        label = "Completed",
                                        value = completedResults.size.toString()
                                    )
                                    StatItem(
                                        label = "Avg Score",
                                        value = "%.1f%%".format(avgScore)
                                    )
                                    StatItem(
                                        label = "Passed",
                                        value = passedCount.toString()
                                    )
                                }
                            }
                        }
                    }
                }

                items(state.results) { (result, user) ->
                    StudentResultCard(
                        result = result,
                        studentName = user?.fullName ?: "Unknown Student",
                        passingScore = state.exam?.passingScore ?: 50
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun StudentResultCard(
    result: ExamResult,
    studentName: String,
    passingScore: Int
) {
    val passed = result.score >= passingScore
    val statusColor = when (result.status) {
        ExamStatus.COMPLETED -> if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        ExamStatus.FAILED_TIME_EXPIRED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "✓ ${result.correctAnswers}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "✗ ${result.incorrectAnswers}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "○ ${result.blankAnswers}",
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
                        ExamStatus.COMPLETED -> if (passed) "Passed" else "Failed"
                        ExamStatus.FAILED_TIME_EXPIRED -> "Time Expired"
                        ExamStatus.IN_PROGRESS -> "In Progress"
                        ExamStatus.NOT_STARTED -> "Not Started"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}
