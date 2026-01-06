package com.utku.smartexam.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.utku.smartexam.data.model.Exam
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    @Query("SELECT * FROM exams WHERE id = :examId")
    suspend fun getExamById(examId: Long): Exam?

    @Query("SELECT * FROM exams WHERE id = :examId")
    fun getExamByIdFlow(examId: Long): Flow<Exam?>

    @Query("SELECT * FROM exams WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getExamsByTeacher(teacherId: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE isPublished = 1 ORDER BY examDate DESC")
    fun getPublishedExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE isPublished = 1 AND examDate >= :currentDate ORDER BY examDate ASC")
    fun getUpcomingExams(currentDate: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE isPublished = 1 AND examDate < :currentDate ORDER BY examDate DESC")
    fun getPastExams(currentDate: Long): Flow<List<Exam>>

    @Query("UPDATE exams SET isPublished = :isPublished WHERE id = :examId")
    suspend fun updatePublishStatus(examId: Long, isPublished: Boolean)
}
