package com.utku.smartexam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    STUDENT,
    TEACHER
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val fullName: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)
