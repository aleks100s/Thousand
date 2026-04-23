package com.alextos.thousand.data.service

import com.alextos.thousand.data.local.KeyValueStorage
import com.alextos.thousand.domain.service.StorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StorageServiceImpl(
    private val keyValueStorage: KeyValueStorage,
) : StorageService {
    override val isManualInputEnabled: Flow<Boolean> =
        keyValueStorage.getBoolean(KEY_IS_MANUAL_INPUT_ENABLED)
            .map { it ?: DEFAULT_IS_MANUAL_INPUT_ENABLED }

    override val isShakeEnabled: Flow<Boolean> =
        keyValueStorage.getBoolean(KEY_IS_SHAKE_ENABLED)
            .map { it ?: DEFAULT_IS_SHAKE_ENABLED }

    override val isNotificationEnabled: Flow<Boolean> =
        keyValueStorage.getBoolean(KEY_IS_NOTIFICATION_ENABLED)
            .map { it ?: DEFAULT_IS_NOTIFICATION_ENABLED }

    override suspend fun setManualInputEnabled(isEnabled: Boolean) {
        keyValueStorage.saveBoolean(KEY_IS_MANUAL_INPUT_ENABLED, isEnabled)
    }

    override suspend fun setShakeEnabled(isEnabled: Boolean) {
        keyValueStorage.saveBoolean(KEY_IS_SHAKE_ENABLED, isEnabled)
    }

    override suspend fun setNotificationEnabled(isEnabled: Boolean) {
        keyValueStorage.saveBoolean(KEY_IS_NOTIFICATION_ENABLED, isEnabled)
    }

    private companion object {
        const val KEY_IS_MANUAL_INPUT_ENABLED = "is_manual_input_enabled"
        const val KEY_IS_SHAKE_ENABLED = "is_shake_enabled"
        const val KEY_IS_NOTIFICATION_ENABLED = "is_notification_enabled"

        const val DEFAULT_IS_MANUAL_INPUT_ENABLED = false
        const val DEFAULT_IS_SHAKE_ENABLED = true
        const val DEFAULT_IS_NOTIFICATION_ENABLED = true
    }
}
