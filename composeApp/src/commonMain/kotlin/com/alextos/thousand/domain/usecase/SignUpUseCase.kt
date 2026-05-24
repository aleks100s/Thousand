package com.alextos.thousand.domain.usecase

import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.service.NativeAccountService

class SignUpUseCase(
    private val accountService: NativeAccountService,
    private val repository: GameRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ) {
        val user = repository.getMainUser() ?: return
        accountService.signUp(email, password, user.name)
    }
}