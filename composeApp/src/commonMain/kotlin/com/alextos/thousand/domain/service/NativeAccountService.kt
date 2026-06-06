package com.alextos.thousand.domain.service

import com.alextos.thousand.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface NativeAccountService {
    val userProfile: StateFlow<User?>
    val hideMultiplayer: StateFlow<Boolean>

    suspend fun logIn(email: String, password: String)
    suspend fun signUp(email: String, password: String, name: String)
    fun updateUserName(name: String)
    fun signOut()
    suspend fun deleteAccount()
}

open class MutableNativeAccountService : NativeAccountService {
    private val _userProfile = MutableStateFlow<User?>(null)
    private val _hideMultiplayer = MutableStateFlow(false)

    override val userProfile: StateFlow<User?> = _userProfile.asStateFlow()
    override val hideMultiplayer: StateFlow<Boolean> = _hideMultiplayer.asStateFlow()

    override suspend fun logIn(email: String, password: String) = Unit
    override suspend fun signUp(email: String, password: String, name: String) = Unit
    override fun updateUserName(name: String) = Unit
    override fun signOut() = Unit
    override suspend fun deleteAccount() = Unit

    fun updateUserProfile(id: String, name: String) {
        _userProfile.value = User(id = id, name = name)
    }

    fun clearUserProfile() {
        _userProfile.value = null
    }

    fun updateHideMultiplayer(hideMultiplayer: Boolean) {
        _hideMultiplayer.value = hideMultiplayer
    }
}
