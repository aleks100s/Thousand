package com.alextos.thousand.presentation.game.create_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.models.UserKind
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.usecase.game.crud.CreateGameUseCase
import com.alextos.thousand.domain.usecase.user.GenerateBotNameUseCase
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
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
) : ViewModel() {
    private val _state = MutableStateFlow(CreateGameState())
    val state: StateFlow<CreateGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateGameEvent>()
    val events: SharedFlow<CreateGameEvent> = _events.asSharedFlow()

    private var initialized = false

    fun onAction(action: CreateGameAction) {
        when (action) {
            CreateGameAction.Initialize -> initialize()
            CreateGameAction.HideAddUserSheet -> hideAddUserSheet()
            CreateGameAction.SaveNewUser -> saveNewUser()
            CreateGameAction.ShowAddUserSheet -> showAddUserSheet()
            CreateGameAction.ShowAddBotSheet -> showAddBotSheet()
            CreateGameAction.OpenPlayersStep -> openPlayersStep()
            CreateGameAction.OpenSettingsStep -> openSettingsStep()
            is CreateGameAction.UpdateNewUserName -> updateNewUserName(action.value)
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
                    it.copy(users = users)
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

    private fun showAddUserSheet() {
        _state.update {
            it.copy(
                isAddUserSheetVisible = true,
                newUserName = "",
                newUserKind = UserKind.LocalUser,
            )
        }
    }

    private fun showAddBotSheet() {
        _state.update {
            it.copy(
                isAddUserSheetVisible = true,
                newUserName = generateBotNameUseCase(),
                newUserKind = UserKind.Bot,
            )
        }
    }

    private fun openPlayersStep() {
        _state.update {
            it.copy(step = CreateGameStep.Players)
        }
    }

    private fun openSettingsStep() {
        _state.update {
            if (it.selectedUsers.isEmpty()) {
                it
            } else {
                it.copy(step = CreateGameStep.Settings)
            }
        }
    }

    private fun hideAddUserSheet() {
        _state.update {
            it.copy(
                isAddUserSheetVisible = false,
                newUserName = "",
                newUserKind = UserKind.LocalUser,
            )
        }
    }

    private fun updateNewUserName(value: String) {
        _state.update {
            it.copy(newUserName = value)
        }
    }

    private fun toggleUserSelection(user: User) {
        _state.update { state ->
            val set = state.selectedUsers.toMutableSet()
            if (set.contains(user)) {
                set.remove(user)
            } else {
                set.add(user)
            }
            state.copy(selectedUsers = set)
        }
    }

    private fun saveNewUser() {
        val state = state.value
        if (state.canSaveNewUser.not()) return
        val userName = state.newUserName.trim()
        val userKind = state.newUserKind

        viewModelScope.launch {
            saveUserUseCase(
                name = userName,
                kind = userKind,
            )

            _state.update {
                it.copy(
                    newUserName = "",
                    newUserKind = UserKind.LocalUser,
                    isAddUserSheetVisible = false,
                )
            }
        }
    }

    private fun createGame() {
        viewModelScope.launch {
            val state = state.value
            val settings = state.gameSettings
            val game = createGameUseCase(
                users = state.selectedUsers,
                isShakeEnabled = settings.isShakeEnabled,
                isVirtualDiceEnabled = settings.isVirtualDiceEnabled,
                isNotificationEnabled = settings.isNotificationEnabled,
                hasStartLimit = settings.hasStartLimit,
                isBarrel1Active = settings.isBarrel1Active,
                isBarrel2Active = settings.isBarrel2Active,
                isBarrel3Active = settings.isBarrel3Active,
                isTripleBoltFineActive = settings.isTripleBoltFineActive,
                isOvertakeFineActive = settings.isOvertakeFineActive,
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
