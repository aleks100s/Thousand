package com.alextos.thousand.domain.usecase.user

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.service.NativeAccountService

class UpdateUserUseCase(
    private val repository: GameRepository,
    private val accountService: NativeAccountService
) {
    suspend operator fun invoke(user: User) {
        val currentUser = accountService.userProfile.value
        if (currentUser != null && user.kind == UserKind.MainUser) {
            // Update current (remote) user if name has been changed
            if (currentUser.name != user.name) {
                accountService.updateUserName(user.name)
            }
        }
        repository.saveUser(user)
    }
}
