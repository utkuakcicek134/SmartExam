package com.utku.smartexam.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.utku.smartexam.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE examId = :examId")
    suspend fun deleteQuestionsByExamId(examId: Long)

    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY orderIndex ASC")
    fun getQuestionsByExamId(examId: Long): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY orderIndex ASC")
    suspend fun getQuestionsByExamIdSync(examId: Long): List<Question>

    @Query("SELECT COUNT(*) FROM questions WHERE examId = :examId")
    suspend fun getQuestionCount(examId: Long): Int
}
