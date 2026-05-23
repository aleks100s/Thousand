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
        private const val LOBBIES_NODE = "lobbies"
        private const val GAMES_NODE = "games"
    }

    override suspend fun createLobby(gameSettings: GameSettings): String {
        val gameID = Random.nextInt(1000, 10000).toString()
        val currentUser = Firebase.auth.currentUser
        gameSettings.host = currentUser?.uid
        gameSettings.players = listOf(GameSettings.Player(id = currentUser?.uid ?: "", currentUser?.displayName ?: "Без имени"))
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().reference
                .child(LOBBIES_NODE)
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
                .child(LOBBIES_NODE)
                .child(id)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val gameSettings = snapshot.toGameSettings() ?: return
                    val currentPlayer = GameSettings.Player(
                        id = currentUser?.uid.orEmpty(),
                        name = currentUser?.displayName ?: "Без имени",
                    )

                    if (currentPlayer.id.isNotEmpty() && gameSettings.players.none { it.id == currentPlayer.id }) {
                        gameSettings.players += currentPlayer
                        reference.setValue(gameSettings)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    trySend(gameSettings.toLobby())
                                } else {
                                    close(task.exception)
                                }
                            }
                    } else {
                        trySend(gameSettings.toLobby())
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

    private fun DataSnapshot.toGameSettings(): GameSettings? {
        if (!exists()) return null

        return GameSettings(
            isNotificationEnabled = child("isNotificationEnabled").getValue(Boolean::class.java) ?: true,
            isVirtualDiceEnabled = child("isVirtualDiceEnabled").getValue(Boolean::class.java) ?: true,
            isShakeEnabled = child("isShakeEnabled").getValue(Boolean::class.java) ?: true,
            hasStartLimit = child("hasStartLimit").getValue(Boolean::class.java) ?: true,
            isBarrel1Active = child("isBarrel1Active").getValue(Boolean::class.java) ?: true,
            isBarrel2Active = child("isBarrel2Active").getValue(Boolean::class.java) ?: true,
            isBarrel3Active = child("isBarrel3Active").getValue(Boolean::class.java) ?: false,
            isTripleBoltFineActive = child("isTripleBoltFineActive").getValue(Boolean::class.java) ?: true,
            isOvertakeFineActive = child("isOvertakeFineActive").getValue(Boolean::class.java) ?: true,
            host = child("host").getValue(String::class.java),
            players = child("players").children.map { playerSnapshot ->
                GameSettings.Player(
                    id = playerSnapshot.child("id").getValue(String::class.java).orEmpty(),
                    name = playerSnapshot.child("name").getValue(String::class.java) ?: "Без имени",
                )
            },
        )
    }

    private fun GameSettings.toLobby(): Lobby =
        Lobby(
            settings = this,
            isCurrentPlayerHost = Firebase.auth.currentUser?.uid == host,
        )
}
