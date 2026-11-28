package com.alextos.thousand.data.service

import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidAccountService : MutableNativeAccountService() {
    init {
        Firebase.auth.addAuthStateListener { auth ->
            if (auth.currentUser?.isAnonymous != true) clearUserId()
        }
    }

    override suspend fun signInAnonymouslyPlatform() {
        val auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser?.isAnonymous == true) {
            updateUserId(currentUser.uid)
            return
        }

        clearUserId()
        if (currentUser != null) auth.signOut()

        suspendCancellableCoroutine { continuation ->
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user ?: auth.currentUser
                    if (user != null && user.isAnonymous) {
                        updateUserId(user.uid)
                        if (continuation.isActive) continuation.resume(Unit)
                    } else {
                        val error = IllegalStateException("Firebase anonymous user is missing after sign-in.")
                        clearUserId()
                        FirebaseCrashlytics.getInstance().recordException(error)
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
                } else {
                    val error = task.exception ?: IllegalStateException("Firebase anonymous sign-in failed.")
                    clearUserId()
                    FirebaseCrashlytics.getInstance().recordException(error)
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
            }
        }
    }

    override fun currentAnonymousUserId(): String? =
        Firebase.auth.currentUser?.takeIf { it.isAnonymous }?.uid

    override suspend fun deleteAccountPlatform() {
        val user = Firebase.auth.currentUser?.takeIf { it.isAnonymous } ?: return
        suspendCancellableCoroutine { continuation ->
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (continuation.isActive) continuation.resume(Unit)
                } else {
                    val error = task.exception ?: IllegalStateException("Firebase account deletion failed.")
                    FirebaseCrashlytics.getInstance().recordException(error)
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
            }
        }
    }
}
