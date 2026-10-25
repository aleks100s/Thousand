package com.alextos.thousand.presentation.menu.multiplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.alextos.thousand.domain.usecase.LogInUseCase
import com.alextos.thousand.domain.usecase.SignUpUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MultiplayerInvitationTest {
    @Test
    fun invitationWaitsForLoginThenOpensResolvedLobbyOnlyOnce() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService()
        val repository = FakeMultiplayerRepository()
        val vm = viewModel(account, repository)
        try {
            runCurrent()
            vm.onAction(MultiplayerAction.OpenInvitation("1234", 1))
            runCurrent()
            assertTrue(vm.state.value.isLoginSheetVisible)
            assertTrue(repository.joined.isEmpty())
            account.updateUserProfile("user", "Игрок")
            runCurrent()
            assertEquals(listOf("1234"), repository.joined)
            assertEquals(MultiplayerEvent.OpenLobby("resolved-key"), vm.events.first())
            vm.onAction(MultiplayerAction.OpenInvitation("1234", 1))
            runCurrent()
            assertEquals(1, repository.joined.size)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun pendingInvitationSurvivesViewModelRecreationBeforeLogin() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService()
        val repository = FakeMultiplayerRepository()
        val handle = SavedStateHandle(mapOf("pendingLobby" to "5678", "handledInvitation" to 3L))
        val vm = viewModel(account, repository, handle)
        try {
            runCurrent()
            assertTrue(vm.state.value.isLoginSheetVisible)
            account.updateUserProfile("user", "Игрок")
            runCurrent()
            assertEquals(listOf("5678"), repository.joined)
            assertEquals(MultiplayerEvent.OpenLobby("resolved-key"), vm.events.first())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun missingLobbyShowsErrorAndCanBeRetried() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService().apply { updateUserProfile("user", "Игрок") }
        val repository = FakeMultiplayerRepository().apply { failure = IllegalStateException("Лобби не найдено") }
        val vm = viewModel(account, repository)
        try {
            runCurrent()
            vm.onAction(MultiplayerAction.OpenInvitation("9999", 1))
            runCurrent()
            assertEquals("Лобби не найдено", vm.state.value.lobbyError)
            assertTrue(vm.state.value.isJoinLobbySheetVisible)
            assertEquals("9999", vm.state.value.lobbyId)
            repository.failure = null
            vm.onAction(MultiplayerAction.JoinLobby)
            runCurrent()
            assertEquals(MultiplayerEvent.OpenLobby("resolved-key"), vm.events.first())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    private fun viewModel(account: MutableNativeAccountService, repository: MultiplayerRepository, handle: SavedStateHandle = SavedStateHandle()) =
        MultiplayerViewModel(account, LogInUseCase(account), SignUpUseCase(account, UnusedGameRepository()), repository, handle)
}

private class FakeMultiplayerRepository : MultiplayerRepository {
    val joined = mutableListOf<String>()
    var failure: Exception? = null
    override suspend fun joinLobby(id: String): String { joined += id; failure?.let { throw it }; return "resolved-key" }
    override fun userLobbies(): Flow<List<Lobby>> = flowOf(emptyList())
    override fun userGames(): Flow<List<RemoteGame>> = flowOf(emptyList())
    override suspend fun createLobby(gameSettings: GameSettings): String = error("Unused")
    override fun connectToLobby(key: String): Flow<Lobby> = error("Unused")
    override suspend fun disconnectFromLobby(key: String) = Unit
    override suspend fun startGame(key: String) = Unit
    override fun observeGame(key: String): Flow<RemoteGame> = error("Unused")
    override suspend fun updateGame(game: RemoteGame) = Unit
    override suspend fun updatePlayerOnlineStatus(gameKey: String, isOnline: Boolean) = Unit
    override suspend fun finishGame(game: RemoteGame, userInfo: Map<String, RemoteUserInfo>) = Unit
    override suspend fun deleteGame(key: String) = Unit
    override suspend fun userInfo(userId: String): RemoteUserInfo? = null
    override fun observeUserInfo(userId: String): Flow<RemoteUserInfo?> = flowOf(null)
}

private class UnusedGameRepository : GameRepository {
    override fun getAllGames(): Flow<List<Game>> = error("Unused")
    override fun getAllUsers(): Flow<List<User>> = error("Unused")
    override suspend fun getMainUser(): User? = null
    override suspend fun saveUser(user: User) = Unit
    override suspend fun replaceUser(previous: User, new: User) = Unit
    override suspend fun deleteUser(userId: String) = Unit
    override suspend fun createGame(game: Game): Game = error("Unused")
    override suspend fun saveGame(game: Game) = Unit
    override suspend fun getGame(id: Long): Game? = null
    override suspend fun getAllTurns(gameID: Long): List<Turn> = emptyList()
    override fun getAllTurns(): Flow<List<Turn>> = flowOf(emptyList())
    override fun getTurnEffectCount(userId: String, effect: Effect): Flow<Int> = flowOf(0)
    override suspend fun saveTurn(turn: Turn, game: Game): Turn = error("Unused")
    override suspend fun deleteGame(gameId: Long) = Unit
}
