package com.utku.smartexam.data.repository

import com.utku.smartexam.data.local.dao.UserDao
import com.utku.smartexam.data.model.User
import com.utku.smartexam.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun registerUser(user: User): Result<Long> {
        return try {
            if (userDao.isEmailExists(user.email)) {
                Result.failure(Exception("Email already exists"))
            } else {
                val userId = userDao.insertUser(user)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val user = userDao.login(email, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: Long): User? = userDao.getUserById(userId)

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    fun getUsersByRole(role: UserRole): Flow<List<User>> = userDao.getUsersByRole(role)

    suspend fun isEmailExists(email: String): Boolean = userDao.isEmailExists(email)
}
