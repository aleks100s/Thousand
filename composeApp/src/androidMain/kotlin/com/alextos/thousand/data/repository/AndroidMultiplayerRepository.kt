package com.alextos.thousand.data.repository

import com.alextos.thousand.data.repository.mappers.toDatabaseMap
import com.alextos.thousand.data.repository.mappers.toFinishedGameStatisticsUpdates
import com.alextos.thousand.data.repository.mappers.toGame
import com.alextos.thousand.data.repository.mappers.toLobby
import com.alextos.thousand.data.repository.mappers.toRemoteUserInfo
import com.alextos.thousand.domain.models.GameButton
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.domain.models.Lobby
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.RemoteGame
import com.alextos.thousand.domain.models.RemoteUserInfo
import com.alextos.thousand.domain.models.RollAbility
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

class AndroidMultiplayerRepository : MultiplayerRepository {
    companion object {
        private const val USERS_NODE = "users"
        private const val LOBBIES_NODE = "lobbies"
        private const val GAMES_NODE = "games"
    }

    @OptIn(ExperimentalUuidApi::class)
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
        val key = Uuid.random().toString()
        val lobby = Lobby(
            settings = gameSettings,
            players = players,
            host = host,
            id = lobbyID,
            key = key
        )
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().reference
                .child(LOBBIES_NODE)
                .child(key)
                .setValue(lobby.toDatabaseMap())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (continuation.isActive) {
                            continuation.resume(key)
                        }
                    } else {
                        continuation.cancel(
                            task.exception ?: IllegalStateException("Failed to create lobby.")
                        )
                    }
                }
        }
    }

    override suspend fun joinLobby(id: String): String {
        val currentUser = Firebase.auth.currentUser ?: return ""

        val lobbies = FirebaseDatabase.getInstance().reference
            .child(LOBBIES_NODE)

        val query = lobbies.orderByChild("id").equalTo(id)

        return suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val child = snapshot.children.firstOrNull()
                    val lobby = child?.toLobby() ?: run {
                        continuation.cancel(IllegalStateException("Failed to join lobby."))
                        return
                    }

                    val key = child.key ?: run {
                        continuation.cancel(IllegalStateException("Failed to join lobby."))
                        return
                    }

                    val currentPlayer = User(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "Без имени",
                    )

                    if (lobby.players.none { it.id == currentPlayer.id }) {
                        lobby.players += currentPlayer
                        lobbies
                            .child(key)
                            .setValue(lobby.toDatabaseMap())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful && continuation.isActive) {
                                    continuation.resume(key)
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
            query.addListenerForSingleValueEvent(listener)
        }
    }

    override fun connectToLobby(key: String): Flow<Lobby> {
        return callbackFlow {
            val reference = FirebaseDatabase.getInstance().reference
                .child(LOBBIES_NODE)
                .child(key)

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

    override suspend fun disconnectFromLobby(key: String) {
        val currentUser = Firebase.auth.currentUser ?: return

        val lobbyReference = FirebaseDatabase.getInstance().reference
            .child(LOBBIES_NODE)
            .child(key)

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
    override suspend fun startGame(key: String) {
        val user = Firebase.auth.currentUser ?: return

        val lobbyReference = FirebaseDatabase.getInstance().reference
            .child(LOBBIES_NODE)
            .child(key)

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

        val gameID = Uuid.random().toString()
        val players = lobby.players.shuffled().mapIndexed { index, player ->
            Player(
                id = index.toLong(),
                user = player,
            )
        }
        val game = RemoteGame(
            id = lobby.id.toLongOrNull() ?: 0L,
            settings = lobby.settings,
            players = players,
            host = user.uid,
            key = gameID,
            currentPlayerIndex = 0,
            rollAbility = RollAbility.REQUIRED,
            buttons = listOf(GameButton.ROLL_THE_DICE)
        )
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
                                .addOnFailureListener {
                                    continuation.cancel(it)
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

    override fun observeGame(key: String): Flow<RemoteGame> {
        val user = Firebase.auth.currentUser ?: return emptyFlow()

        return callbackFlow {
            val reference = FirebaseDatabase.getInstance().reference
                .child(GAMES_NODE)
                .child(key)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val game = snapshot.toGame() ?: run {
                        close(IllegalStateException("Failed to fetch game"))
                        return
                    }

                    trySend(game)
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

    override suspend fun updateGame(game: RemoteGame) {
        suspendCancellableCoroutine { continuation ->
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference
                .child(GAMES_NODE)
                .child(game.key)
                .setValue(game.toDatabaseMap())
                .addOnSuccessListener {
                    if (game.isFinished()) {
                        val updates = game.toFinishedGameStatisticsUpdates()
                        if (updates.isEmpty()) {
                            continuation.resume(Unit)
                            return@addOnSuccessListener
                        }

                        databaseReference
                            .updateChildren(updates)
                            .addOnSuccessListener {
                                continuation.resume(Unit)
                            }
                            .addOnFailureListener {
                                continuation.cancel(it)
                            }
                    } else {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener {
                    continuation.cancel(it)
                }
        }
    }

    override suspend fun deleteGame(key: String) {
        suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().reference
                .child(GAMES_NODE)
                .child(key)
                .removeValue()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener {
                    continuation.cancel(it)
                }
        }

    }

    override suspend fun userInfo(userId: String): RemoteUserInfo? {
        if (userId.isBlank()) return null

        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().reference
                .child(USERS_NODE)
                .child(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (continuation.isActive) {
                        continuation.resume(snapshot.toRemoteUserInfo(userId))
                    }
                }
                .addOnFailureListener { error ->
                    continuation.cancel(error)
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

    override fun userGames(): Flow<List<RemoteGame>> {
        val currentUser = Firebase.auth.currentUser ?: return emptyFlow()

        return callbackFlow {
            val reference = FirebaseDatabase.getInstance().reference
                .child(GAMES_NODE)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val games = mutableListOf<RemoteGame>()
                    for (child in snapshot.children) {
                        val game: RemoteGame = child.toGame() ?: continue
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
