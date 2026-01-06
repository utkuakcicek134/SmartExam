package com.utku.smartexam.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utku.smartexam.data.local.dao.ExamResultDao
import com.utku.smartexam.data.local.dao.QuestionDao
import com.utku.smartexam.data.model.ExamResult
import com.utku.smartexam.data.model.ExamStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamResultRepository @Inject constructor(
    private val examResultDao: ExamResultDao,
    private val questionDao: QuestionDao,
    private val gson: Gson
) {
    suspend fun startExam(studentId: Long, examId: Long): Result<Long> {
        return try {
            val existingResult = examResultDao.getExamResultByStudentAndExam(studentId, examId)
            if (existingResult != null && existingResult.status == ExamStatus.COMPLETED) {
                return Result.failure(Exception("You have already completed this exam"))
            }
            
            if (existingResult != null && existingResult.status == ExamStatus.IN_PROGRESS) {
                return Result.success(existingResult.id)
            }

            val totalQuestions = questionDao.getQuestionCount(examId)
            val examResult = ExamResult(
                studentId = studentId,
                examId = examId,
                status = ExamStatus.IN_PROGRESS,
                totalQuestions = totalQuestions,
                startedAt = System.currentTimeMillis()
            )
            val resultId = examResultDao.insertExamResult(examResult)
            Result.success(resultId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitExam(
        resultId: Long,
        answers: Map<Long, String>
    ): Result<ExamResult> {
        return try {
            val examResult = examResultDao.getExamResultById(resultId)
                ?: return Result.failure(Exception("Exam result not found"))

            val questions = questionDao.getQuestionsByExamIdSync(examResult.examId)
            
            var correct = 0
            var incorrect = 0
            var blank = 0

            questions.forEach { question ->
                val answer = answers[question.id]
                when {
                    answer.isNullOrEmpty() -> blank++
                    answer == question.correctAnswer -> correct++
                    else -> incorrect++
                }
            }

            val score = if (questions.isNotEmpty()) {
                (correct.toDouble() / questions.size) * 100
            } else 0.0

            val answersJson = gson.toJson(answers)

            examResultDao.updateExamResultScore(
                resultId = resultId,
                status = ExamStatus.COMPLETED,
                score = score,
                correct = correct,
                incorrect = incorrect,
                blank = blank,
                completedAt = System.currentTimeMillis(),
                answers = answersJson
            )

            val updatedResult = examResultDao.getExamResultById(resultId)
            Result.success(updatedResult!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markExamAsFailed(resultId: Long): Result<Unit> {
        return try {
            val examResult = examResultDao.getExamResultById(resultId)
                ?: return Result.failure(Exception("Exam result not found"))

            examResultDao.updateExamResult(
                examResult.copy(
                    status = ExamStatus.FAILED_TIME_EXPIRED,
                    completedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExamResultById(resultId: Long): ExamResult? = examResultDao.getExamResultById(resultId)

    suspend fun getExamResultByStudentAndExam(studentId: Long, examId: Long): ExamResult? =
        examResultDao.getExamResultByStudentAndExam(studentId, examId)

    fun getExamResultByStudentAndExamFlow(studentId: Long, examId: Long): Flow<ExamResult?> =
        examResultDao.getExamResultByStudentAndExamFlow(studentId, examId)

    fun getExamResultsByStudent(studentId: Long): Flow<List<ExamResult>> =
        examResultDao.getExamResultsByStudent(studentId)

    fun getExamResultsByExam(examId: Long): Flow<List<ExamResult>> =
        examResultDao.getExamResultsByExam(examId)

    fun getCompletedExamsByStudent(studentId: Long): Flow<List<ExamResult>> =
        examResultDao.getExamResultsByStudentAndStatus(
            studentId,
            listOf(ExamStatus.COMPLETED, ExamStatus.FAILED_TIME_EXPIRED)
        )

    suspend fun hasStudentTakenExam(studentId: Long, examId: Long): Boolean =
        examResultDao.hasStudentTakenExam(studentId, examId)

    fun parseAnswers(answersJson: String): Map<Long, String> {
        return try {
            val type = object : TypeToken<Map<Long, String>>() {}.type
            gson.fromJson(answersJson, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
