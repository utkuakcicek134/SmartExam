package com.utku.smartexam.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utku.smartexam.data.model.Exam
import com.utku.smartexam.data.model.ExamResult
import com.utku.smartexam.data.model.ExamStatus
import com.utku.smartexam.data.model.Question
import com.utku.smartexam.data.repository.ExamRepository
import com.utku.smartexam.data.repository.ExamResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class StudentDashboardState(
    val studentId: Long = 0,
    val studentName: String = "",
    val availableExams: List<Exam> = emptyList(),
    val completedExams: List<Pair<ExamResult, Exam?>> = emptyList(),
    val isLoading: Boolean = false
)

data class TakeExamState(
    val exam: Exam? = null,
    val questions: List<Question> = emptyList(),
    val answers: MutableMap<Long, String> = mutableMapOf(),
    val currentQuestionIndex: Int = 0,
    val resultId: Long = 0,
    val remainingTimeSeconds: Long = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submittedResult: ExamResult? = null,
    val alreadyCompleted: Boolean = false
)

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val examResultRepository: ExamResultRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(StudentDashboardState())
    val dashboardState: StateFlow<StudentDashboardState> = _dashboardState.asStateFlow()

    private val _takeExamState = MutableStateFlow(TakeExamState())
    val takeExamState: StateFlow<TakeExamState> = _takeExamState.asStateFlow()

    private var timerJob: Job? = null

    fun setStudent(studentId: Long, studentName: String) {
        _dashboardState.update { it.copy(studentId = studentId, studentName = studentName) }
        loadDashboardData(studentId)
    }

    private fun loadDashboardData(studentId: Long) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true) }
            
            launch {
                examRepository.getPublishedExams().collect { exams ->
                    val availableExams = exams.filter { exam ->
                        val result = examResultRepository.getExamResultByStudentAndExam(studentId, exam.id)
                        result == null || result.status == ExamStatus.NOT_STARTED
                    }
                    _dashboardState.update { it.copy(availableExams = availableExams, isLoading = false) }
                }
            }

            launch {
                examResultRepository.getCompletedExamsByStudent(studentId).collect { results ->
                    val completedWithExams = results.map { result ->
                        val exam = examRepository.getExamById(result.examId)
                        result to exam
                    }
                    _dashboardState.update { it.copy(completedExams = completedWithExams) }
                }
            }
        }
    }

    fun startExam(studentId: Long, examId: Long) {
        viewModelScope.launch {
            _takeExamState.update { it.copy(isLoading = true, error = null) }

            val existingResult = examResultRepository.getExamResultByStudentAndExam(studentId, examId)
            if (existingResult != null && existingResult.status == ExamStatus.COMPLETED) {
                _takeExamState.update { 
                    it.copy(
                        isLoading = false, 
                        alreadyCompleted = true,
                        submittedResult = existingResult
                    ) 
                }
                return@launch
            }

            val exam = examRepository.getExamById(examId)
            val questions = examRepository.getQuestionsByExamIdSync(examId)

            if (exam == null) {
                _takeExamState.update { it.copy(isLoading = false, error = "Exam not found") }
                return@launch
            }

            val startResult = examResultRepository.startExam(studentId, examId)
            startResult.fold(
                onSuccess = { resultId ->
                    val existingAnswers = if (existingResult != null && existingResult.status == ExamStatus.IN_PROGRESS) {
                        examResultRepository.parseAnswers(existingResult.answers).toMutableMap()
                    } else {
                        mutableMapOf()
                    }

                    val remainingTime = calculateRemainingTime(exam, existingResult)

                    _takeExamState.update {
                        it.copy(
                            exam = exam,
                            questions = questions,
                            answers = existingAnswers,
                            resultId = resultId,
                            remainingTimeSeconds = remainingTime,
                            isLoading = false
                        )
                    }

                    startTimer()
                },
                onFailure = { e ->
                    _takeExamState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun calculateRemainingTime(exam: Exam, existingResult: ExamResult?): Long {
        val durationSeconds = exam.durationMinutes * 60L
        
        return if (existingResult != null && existingResult.startedAt != null) {
            val elapsedSeconds = (System.currentTimeMillis() - existingResult.startedAt) / 1000
            maxOf(0, durationSeconds - elapsedSeconds)
        } else {
            durationSeconds
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_takeExamState.value.remainingTimeSeconds > 0) {
                delay(1000)
                _takeExamState.update { 
                    it.copy(remainingTimeSeconds = it.remainingTimeSeconds - 1) 
                }
            }
            if (_takeExamState.value.submittedResult == null) {
                submitExam(isTimeExpired = true)
            }
        }
    }

    fun selectAnswer(questionId: Long, answer: String) {
        _takeExamState.update { state ->
            val newAnswers = state.answers.toMutableMap()
            newAnswers[questionId] = answer
            state.copy(answers = newAnswers)
        }
    }

    fun goToQuestion(index: Int) {
        _takeExamState.update { it.copy(currentQuestionIndex = index) }
    }

    fun nextQuestion() {
        _takeExamState.update { state ->
            if (state.currentQuestionIndex < state.questions.size - 1) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
            } else {
                state
            }
        }
    }

    fun previousQuestion() {
        _takeExamState.update { state ->
            if (state.currentQuestionIndex > 0) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex - 1)
            } else {
                state
            }
        }
    }

    fun submitExam(isTimeExpired: Boolean = false) {
        timerJob?.cancel()
        
        viewModelScope.launch {
            _takeExamState.update { it.copy(isSubmitting = true) }

            val state = _takeExamState.value

            if (isTimeExpired) {
                examResultRepository.markExamAsFailed(state.resultId)
                val result = examResultRepository.getExamResultById(state.resultId)
                _takeExamState.update { 
                    it.copy(isSubmitting = false, submittedResult = result) 
                }
            } else {
                val result = examResultRepository.submitExam(state.resultId, state.answers)
                result.fold(
                    onSuccess = { examResult ->
                        _takeExamState.update { 
                            it.copy(isSubmitting = false, submittedResult = examResult) 
                        }
                    },
                    onFailure = { e ->
                        _takeExamState.update { 
                            it.copy(isSubmitting = false, error = e.message) 
                        }
                    }
                )
            }
        }
    }

    fun resetTakeExamState() {
        timerJob?.cancel()
        _takeExamState.value = TakeExamState()
    }

    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
