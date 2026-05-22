package com.alextos.thousand.domain.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface NativeAuthenticatorService {
    val authenticationStatus: StateFlow<AuthenticationStatus>
    val hideMultiplayer: StateFlow<Boolean>
}

open class MutableNativeAuthenticatorService : NativeAuthenticatorService {
    private val _authenticationStatus = MutableStateFlow<AuthenticationStatus>(AuthenticationStatus.LoggedOut)
    private val _hideMultiplayer = MutableStateFlow(false)

    override val authenticationStatus: StateFlow<AuthenticationStatus> = _authenticationStatus.asStateFlow()
    override val hideMultiplayer: StateFlow<Boolean> = _hideMultiplayer.asStateFlow()

    fun updateAuthenticationStatus(status: AuthenticationStatus) {
        _authenticationStatus.value = status
    }

    fun updateHideMultiplayer(hideMultiplayer: Boolean) {
        _hideMultiplayer.value = hideMultiplayer
    }
}

sealed interface AuthenticationStatus {
    data class LoggedIn(val name: String): AuthenticationStatus
    data object LoggedOut: AuthenticationStatus
}
