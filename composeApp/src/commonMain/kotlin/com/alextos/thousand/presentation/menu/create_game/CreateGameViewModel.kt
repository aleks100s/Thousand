package com.alextos.thousand.presentation.menu.create_game

import com.alextos.thousand.domain.models.PlayerAvatar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.usecase.game.crud.CreateGameUseCase
import com.alextos.thousand.domain.usecase.user.GenerateBotNameUseCase
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.user.UpdateUserUseCase
import com.alextos.thousand.domain.usecase.user.DeleteUserUseCase
import kotlinx.coroutines.CancellationException
import com.alextos.thousand.domain.usecase.user.SaveUserUseCase
import com.alextos.thousand.domain.models.GameSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGameViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val createGameUseCase: CreateGameUseCase,
    private val generateBotNameUseCase: GenerateBotNameUseCase,
    private val storageService: StorageService,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(CreateGameState())
    val state: StateFlow<CreateGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateGameEvent>()
    val events: SharedFlow<CreateGameEvent> = _events.asSharedFlow()

    private var initialized = false

    fun onAction(action: CreateGameAction) {
        when (action) {
            CreateGameAction.Initialize -> initialize()
            CreateGameAction.HidePlayerEditor -> if (!state.value.isPlayerOperationRunning) closePlayerEditor()
            is CreateGameAction.SelectPlayerAvatar -> if (!state.value.isPlayerOperationRunning) _state.update { it.copy(playerAvatar = action.avatar) }
            CreateGameAction.SavePlayer -> savePlayer()
            CreateGameAction.ShowAddUserSheet -> showPlayerEditor(UserKind.LocalUser)
            CreateGameAction.ShowAddBotSheet -> showPlayerEditor(UserKind.Bot)
            CreateGameAction.OpenPlayersStep -> openPlayersStep()
            CreateGameAction.OpenSettingsStep -> openSettingsStep()
            CreateGameAction.TogglePlayersEditing -> setEditingPlayers(!state.value.isEditingPlayers)
            CreateGameAction.FinishPlayersEditing -> setEditingPlayers(false)
            is CreateGameAction.EditPlayer -> editPlayer(action.user)
            is CreateGameAction.UpdatePlayerName -> _state.update { it.copy(playerName = action.value, playerError = null) }
            is CreateGameAction.RequestDeletePlayer -> requestDeletePlayer(action.user)
            CreateGameAction.CancelDeletePlayer -> _state.update { it.copy(pendingDeleteUser = null) }
            CreateGameAction.ConfirmDeletePlayer -> deletePlayer()
            is CreateGameAction.ToggleUserSelection -> toggleUserSelection(action.user)
            is CreateGameAction.UpdateGameSettings -> updateGameSettings(action.settings)
            CreateGameAction.CreateGame -> createGame()
        }
    }

    private fun initialize() {
        if (initialized) return
        initialized = true

        viewModelScope.launch {
            getAllUsersUseCase().collect { users ->
                _state.update {
                    val selectedIds = it.selectedUsers.map { user -> user.id }.toSet()
                    it.copy(isLoadingUsers = false, users = users, selectedUsers = users.filter { user -> user.id in selectedIds }.toSet())
                }
            }
        }
        viewModelScope.launch {
            storageService.isNotificationEnabled.collect { isEnabled ->
                updateSettings { isNotificationEnabled = isEnabled }
            }
        }
        viewModelScope.launch {
            storageService.isManualInputEnabled.collect { isEnabled ->
                updateSettings { isVirtualDiceEnabled = isEnabled.not() }
            }
        }
        viewModelScope.launch {
            storageService.isShakeEnabled.collect { isEnabled ->
                updateSettings { isShakeEnabled = isEnabled }
            }
        }
    }

    private fun setEditingPlayers(editing: Boolean) {
        if (state.value.step != CreateGameStep.Players || state.value.isPlayerOperationRunning || state.value.isLoadingUsers) return
        _state.update { it.copy(isEditingPlayers = editing, playerError = null) }
    }

    private fun showPlayerEditor(kind: UserKind) {
        if (state.value.isPlayerOperationRunning || state.value.step != CreateGameStep.Players) return
        _state.update { it.copy(isPlayerEditorVisible = true, editingUser = null, playerKind = kind,
            playerAvatar = PlayerAvatar.Neutral,
            playerName = if (kind == UserKind.Bot) generateBotNameUseCase() else "", playerError = null) }
    }

    private fun editPlayer(user: User) {
        val current = state.value
        val actual = current.users.firstOrNull { it.id == user.id } ?: return
        if (!current.isEditingPlayers || current.isPlayerOperationRunning) return
        _state.update { it.copy(isPlayerEditorVisible = true, editingUser = actual, playerKind = actual.kind,
            playerAvatar = actual.avatar, playerName = actual.name, playerError = null) }
    }

    private fun openPlayersStep() {
        _state.update { it.copy(step = CreateGameStep.Players) }
    }

    private fun openSettingsStep() {
        val current = state.value
        if (current.isEditingPlayers || current.isPlayerOperationRunning || current.selectedUsers.size < 2) return
        _state.update { it.copy(step = CreateGameStep.Settings) }
    }

    private fun closePlayerEditor() {
        _state.update { it.copy(isPlayerEditorVisible = false, editingUser = null, playerName = "",
            playerKind = UserKind.LocalUser, playerError = null) }
    }

    private fun toggleUserSelection(user: User) {
        val current = state.value
        if (current.isEditingPlayers || current.isPlayerOperationRunning) return
        val actual = current.users.firstOrNull { it.id == user.id } ?: return
        _state.update { value ->
            val selected = value.selectedUsers.toMutableSet()
            val previous = selected.firstOrNull { it.id == actual.id }
            if (previous != null) selected.remove(previous) else selected.add(actual)
            value.copy(selectedUsers = selected)
        }
    }

    private fun savePlayer() {
        val current = state.value
        if (!current.canSavePlayer) return
        _state.update { it.copy(isPlayerOperationRunning = true, playerError = null) }
        viewModelScope.launch {
            try {
                val name = current.playerName.trim()
                val edited = current.editingUser?.copy(name = name, avatar = current.playerAvatar)
                if (edited == null) saveUserUseCase(name = name, kind = current.playerKind, avatar = current.playerAvatar)
                else {
                    updateUserUseCase(edited)
                    _state.update { value -> value.copy(selectedUsers = value.selectedUsers.map {
                        if (it.id == edited.id) edited else it
                    }.toSet()) }
                }
                closePlayerEditor()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.update { it.copy(playerError = "Не удалось сохранить игрока. Попробуйте ещё раз.") }
            } finally {
                _state.update { it.copy(isPlayerOperationRunning = false) }
            }
        }
    }

    private fun requestDeletePlayer(user: User) {
        val current = state.value
        val actual = current.users.firstOrNull { it.id == user.id } ?: return
        if (!current.isEditingPlayers || current.isPlayerOperationRunning || actual.kind == UserKind.MainUser) return
        _state.update { it.copy(pendingDeleteUser = actual, playerError = null) }
    }

    private fun deletePlayer() {
        val current = state.value
        val user = current.pendingDeleteUser ?: return
        if (!current.isEditingPlayers || current.isPlayerOperationRunning || user.kind == UserKind.MainUser) return
        _state.update { it.copy(isPlayerOperationRunning = true, pendingDeleteUser = null, playerError = null) }
        viewModelScope.launch {
            try {
                deleteUserUseCase(user.id)
                _state.update { value -> value.copy(selectedUsers = value.selectedUsers.filterNot { it.id == user.id }.toSet()) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.update { it.copy(playerError = "Не удалось удалить игрока. Попробуйте ещё раз.") }
            } finally {
                _state.update { it.copy(isPlayerOperationRunning = false) }
            }
        }
    }

    private fun createGame() {
        val current = state.value
        if (current.isEditingPlayers || current.isPlayerOperationRunning || current.selectedUsers.size < 2) return
        viewModelScope.launch {
            val state = state.value
            val game = createGameUseCase(
                users = state.selectedUsers,
                settings = state.gameSettings,
            )
            _events.emit(CreateGameEvent.OpenGame(game.id))
        }
    }

    private fun updateGameSettings(settings: GameSettings) {
        val oldSettings = state.value.gameSettings
        _state.update {
            it.copy(gameSettings = settings)
        }

        if (oldSettings.isNotificationEnabled != settings.isNotificationEnabled) {
            viewModelScope.launch {
                storageService.setNotificationEnabled(settings.isNotificationEnabled)
            }
        }
        if (oldSettings.isVirtualDiceEnabled != settings.isVirtualDiceEnabled) {
            viewModelScope.launch {
                storageService.setManualInputEnabled(settings.isVirtualDiceEnabled.not())
            }
        }
        if (oldSettings.isShakeEnabled != settings.isShakeEnabled) {
            viewModelScope.launch {
                storageService.setShakeEnabled(settings.isShakeEnabled)
            }
        }
    }

    private fun updateSettings(block: GameSettings.() -> Unit) {
        _state.update { state ->
            state.copy(
                gameSettings = state.gameSettings.copy().apply(block),
            )
        }
    }
}
