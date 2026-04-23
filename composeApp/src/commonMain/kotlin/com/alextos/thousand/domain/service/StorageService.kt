package com.alextos.thousand.domain.service

import kotlinx.coroutines.flow.Flow

interface StorageService {
    val isManualInputEnabled: Flow<Boolean>

    val isShakeEnabled: Flow<Boolean>

    val isNotificationEnabled: Flow<Boolean>

    suspend fun setManualInputEnabled(isEnabled: Boolean)

    suspend fun setShakeEnabled(isEnabled: Boolean)

    suspend fun setNotificationEnabled(isEnabled: Boolean)
}
