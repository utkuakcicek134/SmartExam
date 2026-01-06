package com.utku.smartexam.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.utku.smartexam.data.local.SmartExamDatabase
import com.utku.smartexam.data.local.dao.ExamDao
import com.utku.smartexam.data.local.dao.ExamResultDao
import com.utku.smartexam.data.local.dao.QuestionDao
import com.utku.smartexam.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartExamDatabase {
        return Room.databaseBuilder(
            context,
            SmartExamDatabase::class.java,
            "smart_exam_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: SmartExamDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideExamDao(database: SmartExamDatabase): ExamDao = database.examDao()

    @Provides
    @Singleton
    fun provideQuestionDao(database: SmartExamDatabase): QuestionDao = database.questionDao()

    @Provides
    @Singleton
    fun provideExamResultDao(database: SmartExamDatabase): ExamResultDao = database.examResultDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
