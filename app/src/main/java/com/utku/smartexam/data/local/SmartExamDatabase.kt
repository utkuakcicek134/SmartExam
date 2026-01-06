package com.utku.smartexam.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.utku.smartexam.data.local.dao.ExamDao
import com.utku.smartexam.data.local.dao.ExamResultDao
import com.utku.smartexam.data.local.dao.QuestionDao
import com.utku.smartexam.data.local.dao.UserDao
import com.utku.smartexam.data.model.Exam
import com.utku.smartexam.data.model.ExamResult
import com.utku.smartexam.data.model.Question
import com.utku.smartexam.data.model.User

@Database(
    entities = [User::class, Exam::class, Question::class, ExamResult::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartExamDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun examDao(): ExamDao
    abstract fun questionDao(): QuestionDao
    abstract fun examResultDao(): ExamResultDao
}
