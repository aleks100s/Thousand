package com.alextos.thousand.domain.service

interface NativeAuthenticatorService {
    var delegate: NativeAuthenticatorDelegate?
}

interface NativeAuthenticatorDelegate {
    fun userAuthenticationChanged(status: AuthenticationStatus)
}

sealed interface AuthenticationStatus {
    data class LoggedIn(val name: String): AuthenticationStatus
    data object LoggedOut: AuthenticationStatus
}