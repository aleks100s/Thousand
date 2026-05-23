package com.alextos.thousand.data.repository

import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlinx.coroutines.suspendCancellableCoroutine

class MultiplayerRepositoryImpl : MultiplayerRepository {
    companion object {
        private const val GAMES_NODE = "games"
    }

    override suspend fun createLobby(gameSettings: GameSettings): String {
        val gameID = Random.nextInt(1000, 10000).toString()
        val currentUser = Firebase.auth.currentUser
        gameSettings.host = currentUser?.uid
        gameSettings.players = listOf(GameSettings.Player(id = currentUser?.uid ?: "", currentUser?.displayName ?: "Без имени"))
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

    override fun connectToLobby(id: String): Flow<Lobby> {
        val currentUser = Firebase.auth.currentUser
        return callbackFlow {
            val reference = FirebaseDatabase.getInstance().reference
                .child(GAMES_NODE)
                .child(id)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val gameSettings = snapshot.getValue(GameSettings::class.java) ?: return
                    if (gameSettings.players.none { it.id == currentUser?.uid }) {
                        gameSettings.players += listOf(GameSettings.Player(id = currentUser?.uid ?: "", currentUser?.displayName ?: "Без имени"))
                        reference.setValue(gameSettings)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    trySend(Lobby(gameSettings, currentUser?.uid == gameSettings.host))
                                } else {
                                    close(task.exception)
                                }
                            }
                    } else {
                        trySend(Lobby(gameSettings, currentUser?.uid == gameSettings.host))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            reference.addValueEventListener(listener)

            awaitClose {
                reference.removeEventListener(listener)
            }
        }
    }
}
