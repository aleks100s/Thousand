package com.alextos.thousand.domain.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface NativeAccountService {
    val isAuthorized: StateFlow<Boolean>
    val authorizedUserName: StateFlow<String?>
    val hideMultiplayer: StateFlow<Boolean>

    suspend fun logIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
}

open class MutableNativeAccountService : NativeAccountService {
    private val _isAuthorized = MutableStateFlow(false)
    private val _authorizedUserName = MutableStateFlow<String?>(null)
    private val _hideMultiplayer = MutableStateFlow(false)

    override val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()
    override val authorizedUserName: StateFlow<String?> = _authorizedUserName.asStateFlow()
    override val hideMultiplayer: StateFlow<Boolean> = _hideMultiplayer.asStateFlow()

    override suspend fun logIn(email: String, password: String) = Unit
    override suspend fun signUp(email: String, password: String) = Unit

    fun updateIsAuthorized(isAuthorized: Boolean) {
        _isAuthorized.value = isAuthorized
        if (isAuthorized.not()) {
            _authorizedUserName.value = null
        }
    }

    fun updateAuthorizedUserName(name: String?) {
        _authorizedUserName.value = name
    }

    fun updateHideMultiplayer(hideMultiplayer: Boolean) {
        _hideMultiplayer.value = hideMultiplayer
    }
}
