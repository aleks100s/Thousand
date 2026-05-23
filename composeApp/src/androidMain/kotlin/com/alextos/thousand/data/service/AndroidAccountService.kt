package com.alextos.thousand.data.service

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.PlayersClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase

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
        authenticate(activity)
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
                    signInPlayGames(activity)
                }
            }
    }

    private fun signInPlayGames(activity: Activity) {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.signIn()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result.isAuthenticated) {
                    requestServerAuthCode(activity)
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
                saveFirebaseUser(
                    uid = user.uid,
                    name = player.displayName,
                )
            }
            .addOnFailureListener(activity) { error ->
                FirebaseCrashlytics.getInstance().recordException(error)
                saveFirebaseUser(
                    uid = user.uid,
                    name = user.displayName ?: user.uid,
                )
            }
    }

    private fun saveFirebaseUser(
        uid: String,
        name: String,
    ) {
        updateAuthorizedUserName(name)
        updateIsAuthorized(true)

        FirebaseDatabase.getInstance()
            .reference
            .child(USERS_NODE)
            .child(uid)
            .setValue(mapOf(USERNAME_KEY to name))
            .addOnFailureListener { error ->
                FirebaseCrashlytics.getInstance().recordException(error)
            }
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
        private const val USERS_NODE = "users"
        private const val USERNAME_KEY = "username"
    }
}

private fun Application.stringResource(name: String): String? {
    val resourceId = resources.getIdentifier(name, "string", packageName)
    if (resourceId == 0) return null
    return getString(resourceId)
}
