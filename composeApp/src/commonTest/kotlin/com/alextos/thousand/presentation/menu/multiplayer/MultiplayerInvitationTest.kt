package com.alextos.thousand.presentation.menu.multiplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.repository.MultiplayerRepository
import com.alextos.thousand.domain.service.MutableNativeAccountService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MultiplayerInvitationTest {
    @Test
    fun invitationShowsConnectingStateUntilLobbyIsResolved() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService().apply { updateUserProfile("user", "Игрок") }
        val joinResult = CompletableDeferred<String>()
        val repository = FakeMultiplayerRepository().apply { this.joinResult = joinResult }
        val vm = viewModel(account, repository)
        try {
            runCurrent()
            vm.onAction(MultiplayerAction.OpenInvitation("1234", 1))
            runCurrent()
            assertTrue(vm.state.value.isJoiningLobby)

            joinResult.complete("resolved-key")
            runCurrent()
            assertFalse(vm.state.value.isJoiningLobby)
            assertEquals(MultiplayerEvent.OpenLobby("resolved-key"), vm.events.first())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun joiningFromSheetShowsLoaderAndRestoresCodeAfterTenSeconds() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService().apply { updateUserProfile("user", "Игрок") }
        val repository = FakeMultiplayerRepository().apply { joinResult = CompletableDeferred() }
        val vm = viewModel(account, repository)
        try {
            runCurrent()
            vm.onAction(MultiplayerAction.ShowJoinLobbySheet)
            vm.onAction(MultiplayerAction.UpdateLobbyId("A7K2M"))
            vm.onAction(MultiplayerAction.JoinLobby)
            runCurrent()
            assertEquals(listOf("A7K2M"), repository.joined)
            assertTrue(vm.state.value.isJoiningLobby)

            advanceTimeBy(9_999)
            runCurrent()
            assertTrue(vm.state.value.isJoiningLobby)

            advanceTimeBy(1)
            runCurrent()
            assertFalse(vm.state.value.isJoiningLobby)
            assertTrue(vm.state.value.isJoinLobbySheetVisible)
            assertEquals("A7K2M", vm.state.value.lobbyId)
            assertNotNull(vm.state.value.lobbyError)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun fiveCharacterInvitationJoinsUsingCanonicalUppercaseCode() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService()
        val repository = FakeMultiplayerRepository()
        val vm = viewModel(account, repository)
        try {
            account.updateUserProfile("user", "Игрок")
            runCurrent()
            vm.onAction(MultiplayerAction.OpenInvitation("a7k2m", 123))
            runCurrent()
            assertEquals(listOf("A7K2M"), repository.joined)
            assertEquals(MultiplayerEvent.OpenLobby("resolved-key"), vm.events.first())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    @Test
    fun invitationWaitsForAnonymousConnectionThenOpensResolvedLobbyOnlyOnce() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService()
        val repository = FakeMultiplayerRepository()
        val vm = viewModel(account, repository)
        try {
            runCurrent()
            vm.onAction(MultiplayerAction.OpenInvitation("1234", 1))
            runCurrent()
            assertTrue(vm.state.value.isConnecting)
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
    fun pendingInvitationSurvivesViewModelRecreationBeforeAnonymousConnection() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val account = MutableNativeAccountService()
        val repository = FakeMultiplayerRepository()
        val handle = SavedStateHandle(mapOf("pendingLobby" to "5678", "handledInvitation" to 3L))
        val vm = viewModel(account, repository, handle)
        try {
            runCurrent()
            assertTrue(vm.state.value.isConnecting)
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
            assertFalse(vm.state.value.isJoiningLobby)
            assertTrue(vm.state.value.isJoinLobbySheetVisible)
            assertEquals("9999", vm.state.value.lobbyId)
            repository.failure = null
            vm.onAction(MultiplayerAction.JoinLobby)
            runCurrent()
            assertEquals(MultiplayerEvent.OpenLobby("resolved-key"), vm.events.first())
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }

    private fun viewModel(account: MutableNativeAccountService, repository: MultiplayerRepository, handle: SavedStateHandle = SavedStateHandle()) =
        MultiplayerViewModel(account, repository, handle)
}

private class FakeMultiplayerRepository : MultiplayerRepository {
    val joined = mutableListOf<String>()
    var failure: Exception? = null
    var joinResult: CompletableDeferred<String>? = null

    override suspend fun joinLobby(id: String): String {
        joined += id
        failure?.let { throw it }
        return joinResult?.await() ?: "resolved-key"
    }
    override fun userLobbies(): Flow<List<Lobby>> = flowOf(emptyList())
    override fun userGames(): Flow<List<RemoteGame>> = flowOf(emptyList())
    override suspend fun createLobby(gameSettings: GameSettings): String = error("Unused")
    override fun connectToLobby(key: String): Flow<Lobby> = error("Unused")
    override suspend fun disconnectFromLobby(key: String) = Unit
    override suspend fun startGame(key: String) = Unit
    override fun observeGame(key: String): Flow<RemoteGame> = error("Unused")
    override suspend fun sendReaction(gameKey: String, reaction: UserReaction) = Unit
    override suspend fun updateGame(game: RemoteGame) = Unit
    override suspend fun updatePlayerOnlineStatus(gameKey: String, isOnline: Boolean) = Unit
    override suspend fun saveGameResult(game: RemoteGame): FinishedGameSaveResult? = null
    override suspend fun deleteGame(key: String) = Unit
}
