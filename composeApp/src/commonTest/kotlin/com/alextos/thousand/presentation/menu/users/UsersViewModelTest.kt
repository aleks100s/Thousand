package com.alextos.thousand.presentation.menu.users

import androidx.lifecycle.ViewModelStore
import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.service.MutableNativeAccountService
import com.alextos.thousand.domain.usecase.statistics.GamesStatisticsUseCase
import com.alextos.thousand.domain.usecase.user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class UsersViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()
    private val main = User("main", "Основной", UserKind.MainUser)
    private val local = User("local", "Маша")

    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun tearDown() { stores.forEach { it.clear() }; Dispatchers.resetMain() }

    private fun viewModel(repo: UsersRepository): UsersViewModel = UsersViewModel(
        GetAllUsersUseCase(repo), DeleteUserUseCase(repo), UpdateUserUseCase(repo, MutableNativeAccountService()),
        SaveUserUseCase(repo), GamesStatisticsUseCase(repo),
    ).also { vm -> stores += ViewModelStore().also { it.put("users", vm) } }

    @Test fun addingValidatesNamesAndSavesALocalPlayer() = runTest {
        val repo = UsersRepository(listOf(main, local))
        val vm = viewModel(repo)
        vm.onAction(UsersAction.StartAddUser)
        assertFalse(vm.state.value.canSaveEditingUser)
        vm.onAction(UsersAction.UpdateEditingUserName("  мАША  "))
        assertNotNull(vm.state.value.editingUserNameError)
        vm.onAction(UsersAction.SaveEditingUser)
        assertEquals(2, repo.users.value.size)
        vm.onAction(UsersAction.UpdateEditingUserName("  Алексей  "))
        vm.onAction(UsersAction.SaveEditingUser)
        assertEquals("Алексей", repo.users.value.last().name)
        assertEquals(UserKind.LocalUser, repo.users.value.last().kind)
        assertFalse(vm.state.value.isEditorVisible)
    }

    @Test fun editingPreservesIdentityAndKindAndAllowsOwnName() = runTest {
        val repo = UsersRepository(listOf(main, local))
        val vm = viewModel(repo)
        vm.onAction(UsersAction.StartRenameUser(local))
        assertTrue(vm.state.value.canSaveEditingUser)
        vm.onAction(UsersAction.UpdateEditingUserName("Мария"))
        vm.onAction(UsersAction.SaveEditingUser)
        assertEquals(local.copy(name = "Мария"), repo.users.value.last())
        assertEquals(2, repo.users.value.size)
    }

    @Test fun deleteNeedsConfirmationAndCannotDeleteMainUser() = runTest {
        val repo = UsersRepository(listOf(main, local))
        val vm = viewModel(repo)
        vm.onAction(UsersAction.RequestDeleteUser(local))
        assertEquals(local, vm.state.value.pendingDeleteUser)
        assertEquals(2, repo.users.value.size)
        vm.onAction(UsersAction.CancelDeleteUser)
        assertNull(vm.state.value.pendingDeleteUser)
        vm.onAction(UsersAction.DeleteUser(main))
        assertEquals(2, repo.users.value.size)
        vm.onAction(UsersAction.RequestDeleteUser(local))
        vm.onAction(UsersAction.DeleteUser(local))
        assertEquals(listOf(main), repo.users.value)
    }

    @Test fun statisticsFollowUserIdAcrossRenamesAndExcludeUnfinishedGames() = runTest {
        val repo = UsersRepository(listOf(main, local))
        repo.games.value = listOf(
            Game(players = listOf(Player(user = local.copy(name = "Старое имя"), isWinner = true))),
            Game(players = listOf(Player(user = local), Player(user = main, isWinner = true))),
            Game(players = listOf(Player(user = local))),
        )
        val vm = viewModel(repo)
        assertEquals(UserStatisticsUi(wins = 1, games = 2), vm.state.value.statistics[local.id])
    }

    @Test fun savingFailureKeepsDraftAndAllowsRetry() = runTest {
        val repo = UsersRepository(listOf(main))
        val vm = viewModel(repo)
        vm.onAction(UsersAction.StartAddUser)
        vm.onAction(UsersAction.UpdateEditingUserName("Новый"))
        repo.failSave = true
        vm.onAction(UsersAction.SaveEditingUser)
        assertEquals("Новый", vm.state.value.editingUserName)
        assertNotNull(vm.state.value.saveError)
        assertTrue(vm.state.value.canSaveEditingUser)
        repo.failSave = false
        vm.onAction(UsersAction.SaveEditingUser)
        assertFalse(vm.state.value.isEditorVisible)
    }
}

private class UsersRepository(initial: List<User>) : GameRepository {
    val users = MutableStateFlow(initial)
    val games = MutableStateFlow(emptyList<Game>())
    var failSave = false
    override fun getAllUsers() = users
    override fun getAllGames() = games
    override suspend fun saveUser(user: User) {
        check(!failSave)
        users.value = users.value.filterNot { it.id == user.id } + user
    }
    override suspend fun deleteUser(userId: String) { users.value = users.value.filterNot { it.id == userId } }
    override suspend fun getMainUser(): User? = users.value.firstOrNull { it.kind == UserKind.MainUser }
    override suspend fun replaceUser(previous: User, new: User) = error("Unused")
    override suspend fun createGame(game: Game): Game = error("Unused")
    override suspend fun saveGame(game: Game) = error("Unused")
    override suspend fun getGame(id: Long): Game? = error("Unused")
    override suspend fun getAllTurns(gameID: Long): List<Turn> = error("Unused")
    override fun getAllTurns() = MutableStateFlow(emptyList<Turn>())
    override fun getTurnEffectCount(userId: String, effect: Effect) = MutableStateFlow(0)
    override suspend fun saveTurn(turn: Turn, game: Game): Turn = error("Unused")
    override suspend fun deleteGame(gameId: Long) = error("Unused")
}
