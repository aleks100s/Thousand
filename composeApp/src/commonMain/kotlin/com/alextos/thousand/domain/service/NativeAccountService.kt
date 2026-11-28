package com.alextos.thousand.domain.service

import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

interface NativeAccountService {
    val userProfile: StateFlow<User?>

    suspend fun signInAnonymously()
    fun updateLocalAppearance(user: User) = Unit
    fun playerForAccount(id: String, name: String): User = User(id, name, UserKind.MainUser)
    suspend fun deleteAccount(
        deleteOwnedOnlineData: suspend (String) -> Unit,
        clearLocalData: suspend () -> Unit,
    )
}

open class MutableNativeAccountService : NativeAccountService {
    private val _userProfile = MutableStateFlow<User?>(null)
    private val sessionMutex = Mutex()
    private val isDeleting = MutableStateFlow(false)
    private val authenticationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingSignIn: Deferred<Unit>? = null

    override val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    override suspend fun signInAnonymously() {
        check(!isDeleting.value)
        val signIn = sessionMutex.withLock {
            check(!isDeleting.value)
            pendingSignIn?.takeIf { it.isActive }
                ?: authenticationScope.async { signInAnonymouslyPlatform() }.also { pendingSignIn = it }
        }
        signIn.await()
    }

    override suspend fun deleteAccount(
        deleteOwnedOnlineData: suspend (String) -> Unit,
        clearLocalData: suspend () -> Unit,
    ) {
        check(isDeleting.compareAndSet(expect = false, update = true))
        try {
            sessionMutex.withLock {
                try {
                    pendingSignIn?.await()
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                }

                val userId = currentAnonymousUserId()
                if (userId != null) deleteOwnedOnlineData(userId)

                withContext(NonCancellable) {
                    if (userId != null) deleteAccountPlatform()
                    clearUserProfile()
                    clearLocalData()
                }
            }
        } finally {
            isDeleting.value = false
        }
    }

    open fun currentAnonymousUserId(): String? = userProfile.value?.id

    open suspend fun signInAnonymouslyPlatform() = Unit

    open suspend fun deleteAccountPlatform() = Unit

    override fun updateLocalAppearance(user: User) {
        val profile = _userProfile.value ?: return
        if (profile.id == user.id) {
            _userProfile.value = profile.copy(name = user.name, color = user.color, avatar = user.avatar)
        }
    }

    override fun playerForAccount(id: String, name: String): User =
        _userProfile.value?.takeIf { it.id == id && it.name.isNotBlank() }
            ?: User(id, name, UserKind.MainUser)

    fun updateUserProfile(id: String, name: String) {
        val currentProfile = _userProfile.value?.takeIf { it.id == id }
        _userProfile.value = if (currentProfile != null) {
            currentProfile.copy(name = name.ifBlank { currentProfile.name })
        } else {
            User(id, name, UserKind.MainUser)
        }
    }

    fun clearUserProfile() {
        _userProfile.value = null
    }
}
