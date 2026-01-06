package com.utku.smartexam.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("teacherId")]
)
data class Exam(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherId: Long,
    val name: String,
    val subject: String,
    val gradeLevel: String,
    val examDate: Long,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val passingScore: Int = 50,
    val showResultsImmediately: Boolean = true,
    val isPublished: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
