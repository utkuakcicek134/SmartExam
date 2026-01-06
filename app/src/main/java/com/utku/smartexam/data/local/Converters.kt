package com.utku.smartexam.data.local

import androidx.room.TypeConverter
import com.utku.smartexam.data.model.ExamStatus
import com.utku.smartexam.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(role: String): UserRole = UserRole.valueOf(role)

    @TypeConverter
    fun fromExamStatus(status: ExamStatus): String = status.name

    @TypeConverter
    fun toExamStatus(status: String): ExamStatus = ExamStatus.valueOf(status)
}
