package com.utku.smartexam.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ExamStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED_TIME_EXPIRED
}

@Entity(
    tableName = "exam_results",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("examId")]
)
data class ExamResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val examId: Long,
    val status: ExamStatus = ExamStatus.NOT_STARTED,
    val score: Double = 0.0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val blankAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val answers: String = "{}"
)
