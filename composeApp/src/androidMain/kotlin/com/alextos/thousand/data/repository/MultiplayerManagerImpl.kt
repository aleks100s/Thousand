package com.alextos.thousand.data.repository

import com.alextos.thousand.domain.repository.MultiplayerManager
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MultiplayerManagerImpl : MultiplayerManager {
    companion object {
        private const val LOBBIES_NODE = "lobbies"
        private const val GAMES_NODE = "games"
    }

    override suspend fun createLobby(gameSettings: GameSettings): String {
        val lobbyID = Random.nextInt(1000, 10000).toString()
        val currentUser = Firebase.auth.currentUser
        val host = currentUser?.uid ?: ""
        val players = listOf(
            User(
                id = currentUser?.uid ?: "",
                name = currentUser?.displayName ?: "Без имени",
            )
        )
        val lobby = Lobby(
            settings = gameSettings,
            players = players,
            host = host,
            id = lobbyID
        )
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().reference
                .child(LOBBIES_NODE)
                .child(lobbyID)
                .setValue(lobby.toDatabaseMap())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) {
                            continuation.resume(lobbyID)
                        }
                    } else {
                        continuation.cancel(
                            task.exception ?: IllegalStateException("Failed to create lobby.")
                        )
                    }
                }
        }
    }

    override suspend fun joinLobby(id: String) {
        val currentUser = Firebase.auth.currentUser ?: return

        val reference = FirebaseDatabase.getInstance().reference
            .child(LOBBIES_NODE)
            .child(id)

        return suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lobby = snapshot.toLobby() ?: run {
                        continuation.cancel(IllegalStateException("Failed to join lobby."))
                        return
                    }

                    val currentPlayer = User(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "Без имени",
                    )

                    if (lobby.players.none { it.id == currentPlayer.id }) {
                        lobby.players += currentPlayer
                        reference.setValue(lobby.toDatabaseMap())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful && continuation.isActive) {
                                    continuation.resume(Unit)
                                } else {
                                    continuation.cancel(task.exception)
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.cancel(error.toException())
                }

            }
            reference.addValueEventListener(listener)
        }
    }

    override fun connectToLobby(id: String): Flow<Lobby> {
        return callbackFlow {
            val reference = FirebaseDatabase.getInstance().reference
                .child(LOBBIES_NODE)
                .child(id)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lobby = snapshot.toLobby() ?: run {
                        close(IllegalStateException("Failed to connect to lobby."))
                        return
                    }

                    trySend(lobby)
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

    override suspend fun disconnectFromLobby(id: String) {
        val currentUser = Firebase.auth.currentUser ?: return

        val lobbyReference = FirebaseDatabase.getInstance().reference
            .child(LOBBIES_NODE)
            .child(id)

        suspendCancellableCoroutine { continuation ->
            lobbyReference
                .get()
                .addOnSuccessListener { snapshot ->
                    val lobby = snapshot.toLobby() ?: return@addOnSuccessListener

                    if (lobby.host == currentUser.uid) {
                        lobbyReference.removeValue()
                            .addOnSuccessListener {
                                if (continuation.isActive) {
                                    continuation.resume(Unit)
                                }
                            }
                            .addOnFailureListener { error ->
                                continuation.cancel(error)
                            }
                    } else {
                        val players = lobby.players.toMutableList()
                        players.removeIf { it.id == currentUser.uid }
                        lobby.players = players
                        lobbyReference.setValue(lobby.toDatabaseMap())
                            .addOnSuccessListener {
                                if (continuation.isActive) {
                                    continuation.resume(Unit)
                                }
                            }
                            .addOnFailureListener { error ->
                                continuation.cancel(error)
                            }
                    }
                }
                .addOnFailureListener { error ->
                    continuation.cancel(error)
                }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun startGame(id: String) {
        val lobbyReference = FirebaseDatabase.getInstance().reference
            .child(LOBBIES_NODE)
            .child(id)
        val gamesRoot = FirebaseDatabase.getInstance().reference
            .child(GAMES_NODE)

        val gameID = Uuid.random().toString()

        // gamesRoot.setValue()
        lobbyReference
            .child("game")
            .setValue(gameID)
            .addOnSuccessListener {
                lobbyReference
                    .removeValue()
                    .addOnSuccessListener {  }
            }
    }

    override fun userLobbies(): Flow<List<Lobby>> {
        val currentUser = Firebase.auth.currentUser ?: return emptyFlow()

        return callbackFlow {
            val reference = FirebaseDatabase.getInstance().reference
                .child(LOBBIES_NODE)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lobbies = mutableListOf<Lobby>()
                    for (child in snapshot.children) {
                        val lobby = child.toLobby() ?: continue
                        if (lobby.players.map { it.id }.toSet().contains(currentUser.uid)) {
                            lobbies.add(lobby)
                        }
                    }
                    trySend(lobbies)
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

    private fun DataSnapshot.toLobby(): Lobby? {
        if (!exists()) return null

        return Lobby(
            id = child("id").getValue(String::class.java) ?: "",
            settings = child("settings").toGameSettings(),
            players = child("players").children.map { playerSnapshot ->
                User(
                    id = playerSnapshot.child("id").getValue(String::class.java).orEmpty(),
                    name = playerSnapshot.child("name").getValue(String::class.java) ?: "Без имени",
                )
            },
            host = child("host").getValue(String::class.java).orEmpty(),
            game = child("game").getValue(String::class.java).orEmpty()
        )
    }

    private fun Lobby.toDatabaseMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "settings" to settings.toDatabaseMap(),
            "players" to players.map { player -> player.toLobbyPlayerMap() },
            "host" to host,
            "game" to game,
        )

    private fun User.toLobbyPlayerMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "name" to name,
        )

    private fun GameSettings.toDatabaseMap(): Map<String, Any> =
        mapOf(
            "isNotificationEnabled" to isNotificationEnabled,
            "isVirtualDiceEnabled" to isVirtualDiceEnabled,
            "isShakeEnabled" to isShakeEnabled,
            "hasStartLimit" to hasStartLimit,
            "isBarrel1Active" to isBarrel1Active,
            "isBarrel2Active" to isBarrel2Active,
            "isBarrel3Active" to isBarrel3Active,
            "isTripleBoltFineActive" to isTripleBoltFineActive,
            "isOvertakeFineActive" to isOvertakeFineActive,
        )

    private fun DataSnapshot.toGameSettings(): GameSettings =
        GameSettings(
            isNotificationEnabled = child("isNotificationEnabled").getValue(Boolean::class.java) ?: true,
            isVirtualDiceEnabled = child("isVirtualDiceEnabled").getValue(Boolean::class.java) ?: true,
            isShakeEnabled = child("isShakeEnabled").getValue(Boolean::class.java) ?: true,
            hasStartLimit = child("hasStartLimit").getValue(Boolean::class.java) ?: true,
            isBarrel1Active = child("isBarrel1Active").getValue(Boolean::class.java) ?: true,
            isBarrel2Active = child("isBarrel2Active").getValue(Boolean::class.java) ?: true,
            isBarrel3Active = child("isBarrel3Active").getValue(Boolean::class.java) ?: false,
            isTripleBoltFineActive = child("isTripleBoltFineActive").getValue(Boolean::class.java) ?: true,
            isOvertakeFineActive = child("isOvertakeFineActive").getValue(Boolean::class.java) ?: true,
        )
}
