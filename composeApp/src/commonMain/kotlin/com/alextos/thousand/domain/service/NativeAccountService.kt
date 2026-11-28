package com.alextos.thousand.domain.service

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
    val userId: StateFlow<String?>

    suspend fun signInAnonymously()
    suspend fun deleteAccount(
        deleteOwnedOnlineData: suspend (String) -> Unit,
        clearLocalData: suspend () -> Unit,
    )
}

open class MutableNativeAccountService : NativeAccountService {
    private val _userId = MutableStateFlow<String?>(null)
    private val sessionMutex = Mutex()
    private val isDeleting = MutableStateFlow(false)
    private val authenticationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingSignIn: Deferred<Unit>? = null

    override val userId: StateFlow<String?> = _userId.asStateFlow()

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
                    clearUserId()
                    clearLocalData()
                }
            }
        } finally {
            isDeleting.value = false
        }
    }

    open fun currentAnonymousUserId(): String? = userId.value

    open suspend fun signInAnonymouslyPlatform() = Unit

    open suspend fun deleteAccountPlatform() = Unit

    fun updateUserId(id: String) {
        _userId.value = id
    }

    fun clearUserId() {
        _userId.value = null
    }
}
