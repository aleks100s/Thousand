package com.alextos.thousand.data.repository

import com.alextos.thousand.data.repository.mappers.toDatabaseMap
import com.alextos.thousand.data.repository.mappers.toGame
import com.alextos.thousand.data.repository.mappers.toLobby
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.repository.MultiplayerManager
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.Player
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
            reference.addListenerForSingleValueEvent(listener)
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

        val lobby: Lobby = suspendCancellableCoroutine { continuation ->
            lobbyReference
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lobby = snapshot.toLobby() ?: run {
                            continuation.cancel(IllegalStateException("Failed to start game."))
                            return
                        }

                        continuation.resume(lobby)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.cancel(error.toException())
                    }
                })
        }

        val game = Game(
            id = id.toLongOrNull() ?: 0L,
            settings = lobby.settings,
            players = lobby.players.shuffled().map {
                Player(user = it)
            }
        )
        val gameID = Uuid.random().toString()
        val gamesReference = FirebaseDatabase.getInstance().reference
            .child(GAMES_NODE)
            .child(gameID)

        return suspendCancellableCoroutine { continuation ->
            gamesReference
                .setValue(game.toDatabaseMap())
                .addOnSuccessListener {
                    lobbyReference
                        .child("game")
                        .setValue(gameID)
                        .addOnSuccessListener {
                            lobbyReference
                                .removeValue()
                                .addOnSuccessListener {
                                    continuation.resume(Unit)
                                }
                        }
                        .addOnFailureListener {
                            continuation.cancel(it)
                        }
                }
                .addOnFailureListener {
                    continuation.cancel(it)
                }
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

    override fun userGames(): Flow<List<Game>> {
        val currentUser = Firebase.auth.currentUser ?: return emptyFlow()

        return callbackFlow {
            val reference = FirebaseDatabase.getInstance().reference
                .child(GAMES_NODE)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val games = mutableListOf<Game>()
                    for (child in snapshot.children) {
                        val game: Game = child.toGame() ?: continue
                        if (game.players.map { it.user.id }.toSet().contains(currentUser.uid)) {
                            games.add(game)
                        }
                    }
                    trySend(games)
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
