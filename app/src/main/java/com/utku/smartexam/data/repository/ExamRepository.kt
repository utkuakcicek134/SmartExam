package com.utku.smartexam.data.repository

import com.utku.smartexam.data.local.dao.ExamDao
import com.utku.smartexam.data.local.dao.QuestionDao
import com.utku.smartexam.data.model.Exam
import com.utku.smartexam.data.model.Question
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepository @Inject constructor(
    private val examDao: ExamDao,
    private val questionDao: QuestionDao
) {
    suspend fun createExam(exam: Exam, questions: List<Question>): Result<Long> {
        return try {
            val examId = examDao.insertExam(exam)
            val questionsWithExamId = questions.mapIndexed { index, question ->
                question.copy(examId = examId, orderIndex = index)
            }
            questionDao.insertQuestions(questionsWithExamId)
            Result.success(examId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExam(exam: Exam, questions: List<Question>): Result<Unit> {
        return try {
            examDao.updateExam(exam)
            questionDao.deleteQuestionsByExamId(exam.id)
            val questionsWithExamId = questions.mapIndexed { index, question ->
                question.copy(examId = exam.id, orderIndex = index)
            }
            questionDao.insertQuestions(questionsWithExamId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExam(exam: Exam): Result<Unit> {
        return try {
            examDao.deleteExam(exam)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExamById(examId: Long): Exam? = examDao.getExamById(examId)

    fun getExamByIdFlow(examId: Long): Flow<Exam?> = examDao.getExamByIdFlow(examId)

    fun getExamsByTeacher(teacherId: Long): Flow<List<Exam>> = examDao.getExamsByTeacher(teacherId)

    fun getPublishedExams(): Flow<List<Exam>> = examDao.getPublishedExams()

    fun getUpcomingExams(currentDate: Long): Flow<List<Exam>> = examDao.getUpcomingExams(currentDate)

    fun getPastExams(currentDate: Long): Flow<List<Exam>> = examDao.getPastExams(currentDate)

    suspend fun publishExam(examId: Long, publish: Boolean): Result<Unit> {
        return try {
            examDao.updatePublishStatus(examId, publish)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getQuestionsByExamId(examId: Long): Flow<List<Question>> = questionDao.getQuestionsByExamId(examId)

    suspend fun getQuestionsByExamIdSync(examId: Long): List<Question> = questionDao.getQuestionsByExamIdSync(examId)

    suspend fun getQuestionCount(examId: Long): Int = questionDao.getQuestionCount(examId)
}
