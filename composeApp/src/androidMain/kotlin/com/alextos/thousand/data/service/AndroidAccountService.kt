package com.alextos.thousand.data.service

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.PlayersClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest as FirebaseUserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidAccountService(
    private val application: Application,
) : MutableNativeAccountService(), Application.ActivityLifecycleCallbacks, FirebaseAuth.AuthStateListener {
    private var hasAttemptedAuthentication = false

    init {
        updateHideMultiplayer(false)
        PlayGamesSdk.initialize(application)
        application.registerActivityLifecycleCallbacks(this)
        Firebase.auth.addAuthStateListener(this)
    }

    override fun onActivityResumed(activity: Activity) {
        val user = Firebase.auth.currentUser
        if (user == null) {
            startSilentAuthenticationFlow(activity)
        } else {
            if (user.providerData.none { it.providerId == "password" }) {
                signInPlayGames(activity)
            }
        }
    }

    // Firebase user observation

    override fun onAuthStateChanged(auth: FirebaseAuth) {
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

    // Play Games authentication

    private fun startSilentAuthenticationFlow(activity: Activity) {
        if (hasAttemptedAuthentication || userProfile.value != null) return
        hasAttemptedAuthentication = true

        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful.not()) {
                    log(task.exception)
                    return@addOnCompleteListener
                }

                if (task.result.isAuthenticated) {
                    requestServerAuthCode(activity)
                } else {
                    signInPlayGames(activity)
                }
            }
    }

    private fun signInPlayGames(activity: Activity) {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.signIn()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result.isAuthenticated) {
                    val user = Firebase.auth.currentUser
                    if (user == null) {
                        requestServerAuthCode(activity)
                    } else {
                        updateUserProfile(id = user.uid, name = user.name)
                    }
                } else {
                    log(task.exception)
                }
            }
    }

    private fun requestServerAuthCode(activity: Activity) {
        val webClientId = application.stringResource(DEFAULT_WEB_CLIENT_ID)
        if (webClientId.isNullOrBlank()) {
            log(IllegalStateException("Missing $DEFAULT_WEB_CLIENT_ID. Configure Firebase web OAuth client for Play Games sign-in."))
            return
        }

        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.requestServerSideAccess(webClientId, false)
            .addOnSuccessListener(activity) { serverAuthCode ->
                authenticateFirebaseWithPlayGames(activity, serverAuthCode)
            }
            .addOnFailureListener(activity) { error ->
                log(error)
            }
    }

    private fun authenticateFirebaseWithPlayGames(
        activity: Activity,
        serverAuthCode: String,
    ) {
        val credential = PlayGamesAuthProvider.getCredential(serverAuthCode)
        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful.not()) {
                    handleAuthenticationError(task.exception)
                    return@addOnCompleteListener
                }

                val user = task.result?.user ?: FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    handleAuthenticationError(IllegalStateException("Firebase user is null after Play Games sign-in."))
                    return@addOnCompleteListener
                }

                getPlayGamesProfile(activity, user)
            }
    }

    private fun getPlayGamesProfile(
        activity: Activity,
        user: FirebaseUser,
    ) {
        val playersClient: PlayersClient = PlayGames.getPlayersClient(activity)
        playersClient.currentPlayer
            .addOnSuccessListener(activity) { player ->
                val name = user.displayName ?: player.displayName.ifEmpty { user.email ?: user.uid }
                updateFirebaseUser(name = name, photo = player.hiResImageUri)
            }
            .addOnFailureListener(activity) { error ->
                FirebaseCrashlytics.getInstance().recordException(error)
            }
    }

    private fun updateFirebaseUser(name: String, photo: Uri? = null) {
        val currentUser = Firebase.auth.currentUser ?: return
        val request = FirebaseUserProfileChangeRequest.Builder()
        request.displayName = name
        photo?.let {
            request.photoUri = it
        }
        currentUser.updateProfile(request.build())
            .addOnSuccessListener {
                updateRemoteUserInfo(currentUser)
            }
    }

    // Helpers

    private fun handleAuthenticationError(error: Exception?) {
        clearUserProfile()
        log(error)
    }

    private fun log(error: Exception?) {
        error?.let {
            FirebaseCrashlytics.getInstance().recordException(it)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    companion object {
        private const val DEFAULT_WEB_CLIENT_ID = "default_web_client_id"
        private const val USERS_NODE = "users"
        private const val NAME_NODE = "name"
        private const val PLATFORM_NODE = "platform"
        private const val GAME_COUNT_NODE = "gameCount"
        private const val WIN_COUNT_NODE = "winCount"
        private const val RATING_NODE = "rating"
        private const val ANDROID_PLATFORM = "Android"
    }
}

private fun Application.stringResource(name: String): String? {
    val resourceId = resources.getIdentifier(name, "string", packageName)
    if (resourceId == 0) return null
    return getString(resourceId)
}

private val FirebaseUser.name: String
    get() = displayName ?: email ?: uid