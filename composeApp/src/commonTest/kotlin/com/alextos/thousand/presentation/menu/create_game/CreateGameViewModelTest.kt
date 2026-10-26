package com.alextos.thousand.presentation.menu.create_game

import androidx.lifecycle.ViewModelStore
import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.usecase.game.crud.CreateGameUseCase
import com.alextos.thousand.domain.usecase.user.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CreateGameViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()
    private val main = User("main", "Основной", UserKind.MainUser)
    private val bot = User("bot", "Бот", UserKind.Bot)
    private val local = User("local", "Маша")

    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun tearDown() { stores.forEach { it.clear() }; Dispatchers.resetMain() }

    private fun viewModel(repo: PlayersRepository): CreateGameViewModel = CreateGameViewModel(
        GetAllUsersUseCase(repo), SaveUserUseCase(repo), CreateGameUseCase(repo), GenerateBotNameUseCase(), PlayersStorage(),
        UpdateUserUseCase(repo, MutableNativeAccountService()), DeleteUserUseCase(repo),
    ).also { vm ->
        stores += ViewModelStore().also { it.put("create-game", vm) }
        vm.onAction(CreateGameAction.Initialize)
    }

    @Test fun newLocalPlayersUseDifferentMutedColorsWhilePaletteHasFreeColors() = runTest {
        val repo = PlayersRepository(listOf(main, local))
        val save = SaveUserUseCase(repo)
        save("Новый игрок", avatar = PlayerAvatar.Male)
        save("Ещё игрок", avatar = PlayerAvatar.Female)
        assertEquals(4, repo.users.value.map { it.color }.toSet().size)
        assertEquals(PlayerColor.Gold, repo.users.value.single { it.id == main.id }.color)
    }

    @Test fun restoringAccountAfterLocalUsersRestoresSavedAvatarForLobby() = runTest {
        val saved = main.copy(avatar = PlayerAvatar.Female)
        val repo = PlayersRepository(listOf(saved))
        val account = MutableNativeAccountService()
        val vm = com.alextos.thousand.application.AppViewModel(
            GetAllUsersUseCase(repo), account, UpdateUserUseCase(repo, account), ReplaceMainUserUseCase(repo),
        )
        stores += ViewModelStore().also { it.put("app", vm) }
        account.updateUserProfile(saved.id, saved.name)
        assertEquals(saved.avatar, account.playerForAccount(saved.id, saved.name).avatar)
        assertEquals(saved.color, account.playerForAccount(saved.id, saved.name).color)
    }

    @Test fun mainOnboardingSavesChosenAvatarAndGoldButAutomaticNameUsesNeutral() = runTest {
        val repo = PlayersRepository(emptyList())
        val account = MutableNativeAccountService()
        val vm = com.alextos.thousand.presentation.onboarding.OnboardingViewModel(SaveUserUseCase(repo), account)
        stores += ViewModelStore().also { it.put("onboarding", vm) }
        vm.onAction(com.alextos.thousand.presentation.onboarding.OnboardingAction.UpdateName("Маша"))
        vm.onAction(com.alextos.thousand.presentation.onboarding.OnboardingAction.SelectAvatar(PlayerAvatar.Female))
        vm.onAction(com.alextos.thousand.presentation.onboarding.OnboardingAction.SaveUser)
        assertEquals(PlayerAvatar.Female, repo.users.value.single().avatar)
        assertEquals(PlayerColor.Gold, repo.users.value.single().color)
        repo.users.value = emptyList()
        account.updateUserProfile("account", "Автоматическое имя")
        assertEquals(PlayerAvatar.Neutral, repo.users.value.single().avatar)
        assertEquals(PlayerColor.Gold, repo.users.value.single().color)
    }

    @Test fun editingDoesNotChangeSelectionOrAdvanceAndCanFinishWithoutPlayers() = runTest {
        val vm = viewModel(PlayersRepository(listOf(main, bot)))
        vm.onAction(CreateGameAction.TogglePlayersEditing)
        vm.onAction(CreateGameAction.ToggleUserSelection(bot))
        vm.onAction(CreateGameAction.OpenSettingsStep)
        vm.onAction(CreateGameAction.CreateGame)
        assertTrue(vm.state.value.isEditingPlayers)
        assertTrue(vm.state.value.selectedUsers.isEmpty())
        assertEquals(CreateGameStep.Players, vm.state.value.step)
        vm.onAction(CreateGameAction.FinishPlayersEditing)
        assertFalse(vm.state.value.isEditingPlayers)
        vm.onAction(CreateGameAction.ToggleUserSelection(main))
        vm.onAction(CreateGameAction.OpenSettingsStep)
        assertEquals(CreateGameStep.Players, vm.state.value.step)
        vm.onAction(CreateGameAction.ToggleUserSelection(bot))
        vm.onAction(CreateGameAction.OpenSettingsStep)
        assertEquals(CreateGameStep.Settings, vm.state.value.step)
    }

    @Test fun renamingASelectedBotPreservesSelectionIdentityAndKind() = runTest {
        val repo = PlayersRepository(listOf(main, bot))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.ToggleUserSelection(main))
        vm.onAction(CreateGameAction.ToggleUserSelection(bot))
        vm.onAction(CreateGameAction.TogglePlayersEditing)
        vm.onAction(CreateGameAction.EditPlayer(bot))
        assertTrue(vm.state.value.canSavePlayer)
        vm.onAction(CreateGameAction.UpdatePlayerName("  Новое имя  "))
        vm.onAction(CreateGameAction.SavePlayer)
        val renamed = bot.copy(name = "Новое имя")
        assertEquals(setOf(main, renamed), vm.state.value.selectedUsers)
        assertEquals(renamed, repo.users.value.last())
        assertTrue(vm.state.value.isEditingPlayers)
        assertFalse(vm.state.value.isPlayerEditorVisible)
        vm.onAction(CreateGameAction.FinishPlayersEditing)
        vm.onAction(CreateGameAction.OpenSettingsStep)
        vm.onAction(CreateGameAction.CreateGame)
        assertEquals(setOf(main, renamed), repo.created!!.players.map { it.user }.toSet())
    }

    @Test fun mainPlayerCanChangeNameAndAvatarButCannotBeDeleted() = runTest {
        val repo = PlayersRepository(listOf(main, bot))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.EditPlayer(bot))
        vm.onAction(CreateGameAction.RequestDeletePlayer(bot))
        assertFalse(vm.state.value.isPlayerEditorVisible)
        assertNull(vm.state.value.pendingDeleteUser)
        vm.onAction(CreateGameAction.TogglePlayersEditing)
        vm.onAction(CreateGameAction.EditPlayer(main))
        vm.onAction(CreateGameAction.RequestDeletePlayer(main))
        vm.onAction(CreateGameAction.ConfirmDeletePlayer)
        assertTrue(vm.state.value.isPlayerEditorVisible)
        assertNull(vm.state.value.pendingDeleteUser)
        vm.onAction(CreateGameAction.UpdatePlayerName("Новое имя"))
        vm.onAction(CreateGameAction.SelectPlayerAvatar(PlayerAvatar.Female))
        vm.onAction(CreateGameAction.SavePlayer)
        assertEquals(main.copy(name = "Новое имя", avatar = PlayerAvatar.Female), repo.users.value.single { it.id == main.id })
        assertEquals(PlayerColor.Gold, repo.users.value.single { it.id == main.id }.color)
        assertEquals(2, repo.users.value.size)
    }

    @Test fun deletingSelectedPlayerClearsSelectionAndCancelDoesNotDelete() = runTest {
        val repo = PlayersRepository(listOf(main, bot))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.ToggleUserSelection(main))
        vm.onAction(CreateGameAction.ToggleUserSelection(bot))
        vm.onAction(CreateGameAction.TogglePlayersEditing)
        vm.onAction(CreateGameAction.RequestDeletePlayer(bot))
        vm.onAction(CreateGameAction.CancelDeletePlayer)
        assertEquals(2, repo.users.value.size)
        vm.onAction(CreateGameAction.RequestDeletePlayer(bot))
        vm.onAction(CreateGameAction.ConfirmDeletePlayer)
        assertEquals(listOf(main), repo.users.value)
        assertEquals(setOf(main), vm.state.value.selectedUsers)
        vm.onAction(CreateGameAction.FinishPlayersEditing)
        vm.onAction(CreateGameAction.OpenSettingsStep)
        assertEquals(CreateGameStep.Players, vm.state.value.step)
    }

    @Test fun addAndEditValidateDuplicatesAndCloseDiscardsDraft() = runTest {
        val repo = PlayersRepository(listOf(main, local))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.ShowAddUserSheet)
        assertFalse(vm.state.value.canSavePlayer)
        vm.onAction(CreateGameAction.UpdatePlayerName("  мАША  "))
        assertNotNull(vm.state.value.playerNameError)
        vm.onAction(CreateGameAction.SavePlayer)
        assertEquals(2, repo.users.value.size)
        vm.onAction(CreateGameAction.HidePlayerEditor)
        assertEquals("", vm.state.value.playerName)
        vm.onAction(CreateGameAction.TogglePlayersEditing)
        vm.onAction(CreateGameAction.EditPlayer(local))
        assertTrue(vm.state.value.canSavePlayer)
        vm.onAction(CreateGameAction.UpdatePlayerName(main.name))
        assertFalse(vm.state.value.canSavePlayer)
        vm.onAction(CreateGameAction.HidePlayerEditor)
        assertEquals(local, repo.users.value.last())
    }

    @Test fun addingHumanAndBotUsesTheSameEditorAndPreservesKind() = runTest {
        val repo = PlayersRepository(listOf(main))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.ShowAddUserSheet)
        vm.onAction(CreateGameAction.UpdatePlayerName("  Алексей  "))
        vm.onAction(CreateGameAction.SelectPlayerAvatar(PlayerAvatar.Male))
        vm.onAction(CreateGameAction.SavePlayer)
        assertEquals(UserKind.LocalUser, repo.users.value.last().kind)
        assertEquals("Алексей", repo.users.value.last().name)
        assertEquals(PlayerAvatar.Male, repo.users.value.last().avatar)
        assertNotEquals(PlayerColor.Gold, repo.users.value.last().color)
        vm.onAction(CreateGameAction.ShowAddBotSheet)
        assertTrue(vm.state.value.playerName.isNotBlank())
        vm.onAction(CreateGameAction.SavePlayer)
        assertEquals(UserKind.Bot, repo.users.value.last().kind)
    }

    @Test fun selectedUsersReconcileWithRepositoryById() = runTest {
        val repo = PlayersRepository(listOf(main, local))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.ToggleUserSelection(local))
        val renamed = local.copy(name = "Мария")
        repo.users.value = listOf(main, renamed)
        assertEquals(setOf(renamed), vm.state.value.selectedUsers)
        repo.users.value = listOf(main)
        assertTrue(vm.state.value.selectedUsers.isEmpty())
    }

    @Test fun failureKeepsDraftAndRetryWorks() = runTest {
        val repo = PlayersRepository(listOf(main))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.ShowAddUserSheet)
        vm.onAction(CreateGameAction.UpdatePlayerName("Новый"))
        repo.failSave = true
        vm.onAction(CreateGameAction.SavePlayer)
        assertEquals("Новый", vm.state.value.playerName)
        assertNotNull(vm.state.value.playerError)
        assertTrue(vm.state.value.canSavePlayer)
        repo.failSave = false
        vm.onAction(CreateGameAction.SavePlayer)
        assertFalse(vm.state.value.isPlayerEditorVisible)
    }

    @Test fun savingBlocksDoubleSubmitDismissAndModeChanges() = runTest {
        val repo = PlayersRepository(listOf(main))
        val vm = viewModel(repo)
        vm.onAction(CreateGameAction.TogglePlayersEditing)
        vm.onAction(CreateGameAction.ShowAddUserSheet)
        vm.onAction(CreateGameAction.UpdatePlayerName("Новый"))
        repo.saveGate = CompletableDeferred()
        vm.onAction(CreateGameAction.SavePlayer)
        vm.onAction(CreateGameAction.SavePlayer)
        vm.onAction(CreateGameAction.HidePlayerEditor)
        vm.onAction(CreateGameAction.FinishPlayersEditing)
        assertTrue(vm.state.value.isPlayerOperationRunning)
        assertTrue(vm.state.value.isPlayerEditorVisible)
        assertTrue(vm.state.value.isEditingPlayers)
        repo.saveGate!!.complete(Unit)
        assertEquals(2, repo.users.value.size)
        assertFalse(vm.state.value.isPlayerOperationRunning)
    }
}

private class PlayersStorage : StorageService {
    override val isManualInputEnabled = MutableStateFlow(false)
    override val isShakeEnabled = MutableStateFlow(true)
    override val isNotificationEnabled = MutableStateFlow(true)
    override val isFirstLaunch = MutableStateFlow(false)
    override suspend fun setManualInputEnabled(isEnabled: Boolean) { isManualInputEnabled.value = isEnabled }
    override suspend fun setShakeEnabled(isEnabled: Boolean) { isShakeEnabled.value = isEnabled }
    override suspend fun setNotificationEnabled(isEnabled: Boolean) { isNotificationEnabled.value = isEnabled }
    override suspend fun setFirstLaunch(isFirstLaunch: Boolean) { this.isFirstLaunch.value = isFirstLaunch }
}

private class PlayersRepository(initial: List<User>) : GameRepository {
    val users = MutableStateFlow(initial)
    var created: Game? = null
    var failSave = false
    var saveGate: CompletableDeferred<Unit>? = null
    override fun getAllUsers() = users
    override fun getAllGames() = MutableStateFlow(emptyList<Game>())
    override suspend fun saveUser(user: User) {
        check(!failSave)
        saveGate?.await()
        users.value = users.value.filterNot { it.id == user.id } + user
    }
    override suspend fun deleteUser(userId: String) { users.value = users.value.filterNot { it.id == userId } }
    override suspend fun getMainUser(): User? = users.value.firstOrNull { it.kind == UserKind.MainUser }
    override suspend fun replaceUser(previous: User, new: User) = error("Unused")
    override suspend fun createGame(game: Game): Game { created = game; return game }
    override suspend fun saveGame(game: Game) = error("Unused")
    override suspend fun getGame(id: Long): Game? = error("Unused")
    override suspend fun getAllTurns(gameID: Long): List<Turn> = error("Unused")
    override fun getAllTurns() = MutableStateFlow(emptyList<Turn>())
    override fun getTurnEffectCount(userId: String, effect: Effect) = MutableStateFlow(0)
    override suspend fun saveTurn(turn: Turn, game: Game): Turn = error("Unused")
    override suspend fun deleteGame(gameId: Long) = error("Unused")
}
