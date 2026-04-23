package com.alextos.thousand.data.local

import kotlinx.coroutines.flow.Flow

interface KeyValueStorage {
    fun getString(key: String): Flow<String?>

    suspend fun saveString(key: String, value: String)

    fun getBoolean(key: String): Flow<Boolean?>

    suspend fun saveBoolean(key: String, value: Boolean)

    fun getInt(key: String): Flow<Int?>

    suspend fun saveInt(key: String, value: Int)

    fun getLong(key: String): Flow<Long?>

    suspend fun saveLong(key: String, value: Long)

    suspend fun remove(key: String)

    suspend fun clear()
}
