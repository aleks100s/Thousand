package com.alextos.thousand.data.repository

import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.presentation.game.components.GameSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlinx.coroutines.suspendCancellableCoroutine

class MultiplayerRepositoryImpl : MultiplayerRepository {
    companion object {
        private const val GAMES_NODE = "games"
    }

    override suspend fun createLobby(gameSettings: GameSettings): String {
        val gameID = Random.nextInt(1000, 10000).toString()
        gameSettings.host = Firebase.auth.currentUser?.uid
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().reference
                .child(GAMES_NODE)
                .child(gameID)
                .setValue(gameSettings)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) {
                            continuation.resume(gameID)
                        }
                    } else {
                        continuation.cancel(
                            task.exception ?: IllegalStateException("Failed to create lobby.")
                        )
                    }
                }
        }
    }
}
