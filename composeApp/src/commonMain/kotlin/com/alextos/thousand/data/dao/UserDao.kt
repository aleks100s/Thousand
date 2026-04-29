package com.alextos.thousand.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.alextos.thousand.data.models.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun delete(userId: Long)
}
