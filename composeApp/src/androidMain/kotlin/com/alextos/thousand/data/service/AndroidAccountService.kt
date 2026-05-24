package com.alextos.thousand.data.service

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.PlayersClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidAccountService(
    private val application: Application,
) : MutableNativeAccountService(), Application.ActivityLifecycleCallbacks {
    private var hasAttemptedAuthentication = false

    init {
        updateHideMultiplayer(false)
        PlayGamesSdk.initialize(application)
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        val user = Firebase.auth.currentUser
        if (user == null) {
            authenticate(activity)
        } else {
            user.displayName?.let {
                saveFirebaseUser(it)
            }
            val info = user.providerData
            if (info.none { it.providerId == "password" }) {
                signInPlayGames(activity, false)
            }
        }
    }

    override fun updatePlayerName(name: String) {
        val currentUser = Firebase.auth.currentUser ?: return
        saveFirebaseUser(name)
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

                    saveFirebaseUser(name = name)
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

                    saveFirebaseUser(name = user.displayName ?: user.uid)
                    finish()
                }
        }
    }

    private fun authenticate(activity: Activity) {
        if (hasAttemptedAuthentication || isAuthorized.value) return
        hasAttemptedAuthentication = true

        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful.not()) {
                    handleAuthenticationError(task.exception)
                    return@addOnCompleteListener
                }

                if (task.result.isAuthenticated) {
                    requestServerAuthCode(activity)
                } else {
                    signInPlayGames(activity, true)
                }
            }
    }

    private fun signInPlayGames(activity: Activity, connectWithFirebase: Boolean) {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.signIn()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result.isAuthenticated) {
                    if (connectWithFirebase) {
                        requestServerAuthCode(activity)
                    }
                } else {
                    handleAuthenticationError(task.exception)
                }
            }
    }

    private fun requestServerAuthCode(activity: Activity) {
        val webClientId = application.stringResource(DEFAULT_WEB_CLIENT_ID)
        if (webClientId.isNullOrBlank()) {
            handleAuthenticationError(
                IllegalStateException("Missing $DEFAULT_WEB_CLIENT_ID. Configure Firebase web OAuth client for Play Games sign-in.")
            )
            return
        }

        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.requestServerSideAccess(webClientId, false)
            .addOnSuccessListener(activity) { serverAuthCode ->
                authorizeFirebase(activity, serverAuthCode)
            }
            .addOnFailureListener(activity) { error ->
                handleAuthenticationError(error)
            }
    }

    private fun authorizeFirebase(
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

                saveFirebaseUser(activity, user)
            }
    }

    private fun saveFirebaseUser(
        activity: Activity,
        user: FirebaseUser,
    ) {
        val playersClient: PlayersClient = PlayGames.getPlayersClient(activity)
        playersClient.currentPlayer
            .addOnSuccessListener(activity) { player ->
                saveFirebaseUser(name = user.displayName ?: player.displayName.ifEmpty { user.uid })
            }
            .addOnFailureListener(activity) { error ->
                FirebaseCrashlytics.getInstance().recordException(error)
                saveFirebaseUser(name = user.displayName ?: user.uid)
            }
    }

    private fun saveFirebaseUser(name: String) {
        updateAuthorizedUserName(name)
        updateIsAuthorized(true)
        val request = UserProfileChangeRequest.Builder()
        request.displayName = name
        Firebase.auth.currentUser?.updateProfile(request.build())
    }

    private fun handleAuthenticationError(error: Exception?) {
        updateIsAuthorized(false)
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
    }
}

private fun Application.stringResource(name: String): String? {
    val resourceId = resources.getIdentifier(name, "string", packageName)
    if (resourceId == 0) return null
    return getString(resourceId)
}
