package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.service.NativeAccountService

class LogInUseCase(private val accountService: NativeAccountService) {
    suspend operator fun invoke(
        email: String,
        password: String
    ) {
        accountService.logIn(email, password)
    }
}