package com.alextos.thousand.data.service

import com.alextos.thousand.data.mappers.firebase.FirebaseUserMapper
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest as FirebaseUserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidAccountService : MutableNativeAccountService(), FirebaseAuth.AuthStateListener {
    private var savedAppearance: String? = null
    private var pendingAppearance: String? = null

    init {
        Firebase.auth.addAuthStateListener(this)
    }

    override fun updateLocalAppearance(user: User) {
        super.updateLocalAppearance(user)
        if (Firebase.auth.currentUser?.uid != user.id) return
        val key = "${user.id}:${user.color.name}:${user.avatar.name}"
        if (key == pendingAppearance || (pendingAppearance == null && key == savedAppearance)) return
        pendingAppearance = key
        FirebaseDatabase.getInstance().reference.child(USERS_NODE).child(user.id)
            .updateChildren(FirebaseUserMapper.appearanceDictionary(user))
            .addOnSuccessListener {
                // An older completion must not acknowledge a more recent appearance.
                if (pendingAppearance == key) {
                    savedAppearance = key
                    pendingAppearance = null
                }
            }
            .addOnFailureListener { error ->
                if (pendingAppearance == key) pendingAppearance = null
                FirebaseCrashlytics.getInstance().recordException(error)
            }
    }

    // Firebase user observation

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        savedAppearance = null
        pendingAppearance = null
        val user = auth.currentUser
        if (user != null) {
            updateRemoteUserInfo(user)
        } else {
            clearUserProfile()
        }
    }

    private fun updateRemoteUserInfo(user: FirebaseUser) {
        val userReference = FirebaseDatabase.getInstance().reference
            .child(USERS_NODE)
            .child(user.uid)
        userReference
            .get()
            .addOnSuccessListener { snapshot ->
                val values = mutableMapOf<String, Any>(
                    NAME_NODE to user.name,
                    PLATFORM_NODE to ANDROID_PLATFORM,
                )

                if (snapshot.exists().not()) {
                    values[GAME_COUNT_NODE] = 0
                    values[WIN_COUNT_NODE] = 0
                    values[RATING_NODE] = 0
                } else {
                    if (snapshot.hasChild(GAME_COUNT_NODE).not()) {
                        values[GAME_COUNT_NODE] = 0
                    }
                    if (snapshot.hasChild(WIN_COUNT_NODE).not()) {
                        values[WIN_COUNT_NODE] = 0
                    }
                    if (snapshot.hasChild(RATING_NODE).not()) {
                        values[RATING_NODE] = 0
                    }
                }

                userReference
                    .updateChildren(values)
                    .addOnFailureListener { error ->
                        FirebaseCrashlytics.getInstance().recordException(error)
                    }
                    .addOnSuccessListener {
                        updateUserProfile(id = user.uid, name = user.name)
                    }
            }
            .addOnFailureListener { error ->
                FirebaseCrashlytics.getInstance().recordException(error)
            }
    }

    override suspend fun signUp(email: String, password: String, name: String) {
        suspendCancellableCoroutine { continuation ->
            fun finish(error: Exception? = null) {
                if (error != null) {
                    continuation.cancel(error)
                } else if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful.not()) {
                        handleAuthenticationError(task.exception)
                        finish(task.exception)
                        return@addOnCompleteListener
                    }

                    val result = task.result
                    val user = result.user ?: FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        handleAuthenticationError(IllegalStateException("Firebase user is null after email sign-in."))
                        finish(IllegalStateException("Firebase user is null after email sign-in."))
                        return@addOnCompleteListener
                    }

                    updateFirebaseUser(name = name)
                    finish()
                }
        }
    }

    override suspend fun logIn(email: String, password: String) {
        suspendCancellableCoroutine { continuation ->
            fun finish(error: Exception? = null) {
                if (error != null) {
                    continuation.cancel(error)
                } else if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful.not()) {
                        handleAuthenticationError(task.exception)
                        finish(task.exception)
                        return@addOnCompleteListener
                    }

                    val result = task.result
                    val user = result.user ?: FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        handleAuthenticationError(IllegalStateException("Firebase user is null after email sign-in."))
                        finish(IllegalStateException("Firebase user is null after email sign-in."))
                        return@addOnCompleteListener
                    }

                    finish()
                }
        }
    }

    override fun updateUserName(name: String) {
        updateFirebaseUser(name = name)
    }

    override fun signOut() {
        Firebase.auth.signOut()
        clearUserProfile()
    }

    override suspend fun deleteAccount() {
        val userId = Firebase.auth.currentUser?.uid ?: userProfile.value?.id.orEmpty()
        if (userId.isBlank()) return

        suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().reference
                .child(USERS_NODE)
                .child(userId)
                .removeValue()
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        signOut()
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { error ->
                    FirebaseCrashlytics.getInstance().recordException(error)
                    continuation.cancel(error)
                }
        }
    }

    private fun updateFirebaseUser(name: String) {
        val currentUser = Firebase.auth.currentUser ?: return
        val request = FirebaseUserProfileChangeRequest.Builder()
        request.displayName = name
        currentUser.updateProfile(request.build())
            .addOnSuccessListener {
                updateRemoteUserInfo(currentUser)
            }
    }

    // Helpers

    private fun handleAuthenticationError(error: Exception?) {
        clearUserProfile()
        error?.let {
            FirebaseCrashlytics.getInstance().recordException(it)
        }
    }

    companion object {
        private const val USERS_NODE = "users"
        private const val NAME_NODE = "name"
        private const val PLATFORM_NODE = "platform"
        private const val GAME_COUNT_NODE = "gameCount"
        private const val WIN_COUNT_NODE = "winCount"
        private const val RATING_NODE = "rating"
        private const val ANDROID_PLATFORM = "Android"
    }
}

private val FirebaseUser.name: String
    get() = displayName ?: email ?: uid
