package com.alextos.thousand.domain.service

import com.alextos.thousand.domain.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface NativeAccountService {
    val userProfile: StateFlow<UserProfile?>
    val hideMultiplayer: StateFlow<Boolean>

    suspend fun logIn(email: String, password: String)
    suspend fun signUp(email: String, password: String, name: String)
    fun updatePlayerName(name: String)
    fun signOut()
}

open class MutableNativeAccountService : NativeAccountService {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    private val _hideMultiplayer = MutableStateFlow(false)

    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    override val hideMultiplayer: StateFlow<Boolean> = _hideMultiplayer.asStateFlow()

    override suspend fun logIn(email: String, password: String) = Unit
    override suspend fun signUp(email: String, password: String, name: String) = Unit
    override fun updatePlayerName(name: String) = Unit
    override fun signOut() = Unit

    fun updateUserProfile(id: String, name: String) {
        _userProfile.value = UserProfile(id = id, name = name)
    }

    fun clearUserProfile() {
        _userProfile.value = null
    }

    fun updateHideMultiplayer(hideMultiplayer: Boolean) {
        _hideMultiplayer.value = hideMultiplayer
    }
}
