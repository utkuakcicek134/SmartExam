package com.utku.smartexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.utku.smartexam.data.model.ExamResult
import com.utku.smartexam.data.model.ExamStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(examResult: ExamResult): Long

    @Update
    suspend fun updateExamResult(examResult: ExamResult)

    @Query("SELECT * FROM exam_results WHERE id = :resultId")
    suspend fun getExamResultById(resultId: Long): ExamResult?

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND examId = :examId LIMIT 1")
    suspend fun getExamResultByStudentAndExam(studentId: Long, examId: Long): ExamResult?

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND examId = :examId LIMIT 1")
    fun getExamResultByStudentAndExamFlow(studentId: Long, examId: Long): Flow<ExamResult?>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId ORDER BY completedAt DESC")
    fun getExamResultsByStudent(studentId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE examId = :examId ORDER BY score DESC")
    fun getExamResultsByExam(examId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND status IN (:statuses)")
    fun getExamResultsByStudentAndStatus(studentId: Long, statuses: List<ExamStatus>): Flow<List<ExamResult>>

    @Query("SELECT EXISTS(SELECT 1 FROM exam_results WHERE studentId = :studentId AND examId = :examId)")
    suspend fun hasStudentTakenExam(studentId: Long, examId: Long): Boolean

    @Query("UPDATE exam_results SET status = :status, score = :score, correctAnswers = :correct, incorrectAnswers = :incorrect, blankAnswers = :blank, completedAt = :completedAt, answers = :answers WHERE id = :resultId")
    suspend fun updateExamResultScore(
        resultId: Long,
        status: ExamStatus,
        score: Double,
        correct: Int,
        incorrect: Int,
        blank: Int,
        completedAt: Long,
        answers: String
    )
}
