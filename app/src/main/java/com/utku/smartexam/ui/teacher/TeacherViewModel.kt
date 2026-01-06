package com.utku.smartexam.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utku.smartexam.data.model.Exam
import com.utku.smartexam.data.model.Question
import com.utku.smartexam.data.repository.ExamRepository
import com.utku.smartexam.data.repository.ExamResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TeacherDashboardState(
    val teacherId: Long = 0,
    val teacherName: String = "",
    val exams: List<Exam> = emptyList(),
    val isLoading: Boolean = false
)

data class CreateExamState(
    val examName: String = "",
    val subject: String = "",
    val gradeLevel: String = "",
    val examDate: Long = System.currentTimeMillis(),
    val startTime: String = "09:00",
    val endTime: String = "12:00",
    val durationMinutes: Int = 60,
    val passingScore: Int = 50,
    val showResultsImmediately: Boolean = true,
    val questions: List<QuestionState> = listOf(QuestionState()),
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val editExamId: Long? = null
)

data class QuestionState(
    val id: Long = 0,
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val optionE: String = "",
    val correctAnswer: String = "A"
)

@HiltViewModel
class TeacherViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val examResultRepository: ExamResultRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(TeacherDashboardState())
    val dashboardState: StateFlow<TeacherDashboardState> = _dashboardState.asStateFlow()

    private val _createExamState = MutableStateFlow(CreateExamState())
    val createExamState: StateFlow<CreateExamState> = _createExamState.asStateFlow()

    fun setTeacher(teacherId: Long, teacherName: String) {
        _dashboardState.update { it.copy(teacherId = teacherId, teacherName = teacherName) }
        loadExams(teacherId)
    }

    fun setTeacherForExam(teacherId: Long) {
        _dashboardState.update { it.copy(teacherId = teacherId) }
    }

    private fun loadExams(teacherId: Long) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true) }
            examRepository.getExamsByTeacher(teacherId).collect { exams ->
                _dashboardState.update { it.copy(exams = exams, isLoading = false) }
            }
        }
    }

    fun updateExamName(name: String) {
        _createExamState.update { it.copy(examName = name, error = null) }
    }

    fun updateSubject(subject: String) {
        _createExamState.update { it.copy(subject = subject, error = null) }
    }

    fun updateGradeLevel(gradeLevel: String) {
        _createExamState.update { it.copy(gradeLevel = gradeLevel, error = null) }
    }

    fun updateExamDate(date: Long) {
        _createExamState.update { it.copy(examDate = date, error = null) }
    }

    fun updateStartTime(time: String) {
        _createExamState.update { it.copy(startTime = time, error = null) }
    }

    fun updateEndTime(time: String) {
        _createExamState.update { it.copy(endTime = time, error = null) }
    }

    fun updateDuration(duration: Int) {
        _createExamState.update { it.copy(durationMinutes = duration, error = null) }
    }

    fun updatePassingScore(score: Int) {
        _createExamState.update { it.copy(passingScore = score.coerceIn(0, 100), error = null) }
    }

    fun updateShowResultsImmediately(show: Boolean) {
        _createExamState.update { it.copy(showResultsImmediately = show) }
    }

    fun addQuestion() {
        _createExamState.update { 
            it.copy(questions = it.questions + QuestionState()) 
        }
    }

    fun removeQuestion(index: Int) {
        _createExamState.update { state ->
            if (state.questions.size > 1) {
                state.copy(questions = state.questions.toMutableList().apply { removeAt(index) })
            } else {
                state.copy(error = "At least one question is required")
            }
        }
    }

    fun updateQuestion(index: Int, question: QuestionState) {
        _createExamState.update { state ->
            state.copy(
                questions = state.questions.toMutableList().apply { set(index, question) },
                error = null
            )
        }
    }

    fun loadExamForEdit(examId: Long) {
        viewModelScope.launch {
            _createExamState.update { it.copy(isLoading = true) }
            
            val exam = examRepository.getExamById(examId)
            val questions = examRepository.getQuestionsByExamIdSync(examId)
            
            if (exam != null) {
                _createExamState.update {
                    it.copy(
                        isEditMode = true,
                        editExamId = examId,
                        examName = exam.name,
                        subject = exam.subject,
                        gradeLevel = exam.gradeLevel,
                        examDate = exam.examDate,
                        startTime = exam.startTime,
                        endTime = exam.endTime,
                        durationMinutes = exam.durationMinutes,
                        passingScore = exam.passingScore,
                        showResultsImmediately = exam.showResultsImmediately,
                        questions = questions.map { q ->
                            QuestionState(
                                id = q.id,
                                questionText = q.questionText,
                                optionA = q.optionA,
                                optionB = q.optionB,
                                optionC = q.optionC,
                                optionD = q.optionD,
                                optionE = q.optionE ?: "",
                                correctAnswer = q.correctAnswer
                            )
                        }.ifEmpty { listOf(QuestionState()) },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun saveExam() {
        val state = _createExamState.value
        val dashState = _dashboardState.value

        when {
            state.examName.isBlank() -> {
                _createExamState.update { it.copy(error = "Exam name is required") }
                return
            }
            state.subject.isBlank() -> {
                _createExamState.update { it.copy(error = "Subject is required") }
                return
            }
            state.gradeLevel.isBlank() -> {
                _createExamState.update { it.copy(error = "Grade level is required") }
                return
            }
            state.questions.any { it.questionText.isBlank() } -> {
                _createExamState.update { it.copy(error = "All questions must have text") }
                return
            }
            state.questions.any { 
                it.optionA.isBlank() || it.optionB.isBlank() || 
                it.optionC.isBlank() || it.optionD.isBlank() 
            } -> {
                _createExamState.update { it.copy(error = "Options A, B, C, D are required for all questions") }
                return
            }
        }

        viewModelScope.launch {
            _createExamState.update { it.copy(isLoading = true, error = null) }

            val exam = Exam(
                id = state.editExamId ?: 0,
                teacherId = dashState.teacherId,
                name = state.examName,
                subject = state.subject,
                gradeLevel = state.gradeLevel,
                examDate = state.examDate,
                startTime = state.startTime,
                endTime = state.endTime,
                durationMinutes = state.durationMinutes,
                passingScore = state.passingScore,
                showResultsImmediately = state.showResultsImmediately
            )

            val questions = state.questions.mapIndexed { index, q ->
                Question(
                    id = q.id,
                    examId = state.editExamId ?: 0,
                    questionText = q.questionText,
                    optionA = q.optionA,
                    optionB = q.optionB,
                    optionC = q.optionC,
                    optionD = q.optionD,
                    optionE = q.optionE.ifBlank { null },
                    correctAnswer = q.correctAnswer,
                    orderIndex = index
                )
            }

            val result = if (state.isEditMode) {
                examRepository.updateExam(exam, questions)
            } else {
                examRepository.createExam(exam, questions).map { }
            }

            result.fold(
                onSuccess = {
                    _createExamState.update { it.copy(isLoading = false, saveSuccess = true) }
                },
                onFailure = { e ->
                    _createExamState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun publishExam(examId: Long, publish: Boolean) {
        viewModelScope.launch {
            examRepository.publishExam(examId, publish)
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            examRepository.deleteExam(exam)
        }
    }

    fun resetCreateExamState() {
        _createExamState.value = CreateExamState()
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
