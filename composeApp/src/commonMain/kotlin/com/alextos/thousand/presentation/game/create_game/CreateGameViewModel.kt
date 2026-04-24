package com.alextos.thousand.presentation.game.create_game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.models.User
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.usecase.game.CreateGameUseCase
import com.alextos.thousand.domain.usecase.game.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.game.SaveUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGameViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val createGameUseCase: CreateGameUseCase,
    private val storageService: StorageService,
) : ViewModel() {
    private val _state = MutableStateFlow(CreateGameState())
    val state: StateFlow<CreateGameState> = _state.asStateFlow()
    private var initialized = false

    fun onAction(action: CreateGameAction) {
        when (action) {
            CreateGameAction.Initialize -> initialize()
            CreateGameAction.HideAddUserSheet -> hideAddUserSheet()
            CreateGameAction.ConsumeCreatedGame -> consumeCreatedGame()
            CreateGameAction.SaveNewUser -> saveNewUser()
            CreateGameAction.ShowAddUserSheet -> showAddUserSheet()
            is CreateGameAction.UpdateNewUserName -> updateNewUserName(action.value)
            is CreateGameAction.ToggleUserSelection -> toggleUserSelection(action.user)
            is CreateGameAction.SetNotificationEnabled -> setNotificationEnabled(action.isEnabled)
            is CreateGameAction.SetVirtualDiceEnabled -> setVirtualDiceEnabled(action.isEnabled)
            is CreateGameAction.SetShakeEnabled -> setShakeEnabled(action.isEnabled)
            is CreateGameAction.SetHasStartLimit -> setHasStartLimit(action.isEnabled)
            is CreateGameAction.SetBarrel1Active -> setBarrel1Active(action.isEnabled)
            is CreateGameAction.SetBarrel2Active -> setBarrel2Active(action.isEnabled)
            is CreateGameAction.SetBarrel3Active -> setBarrel3Active(action.isEnabled)
            is CreateGameAction.SetTripleBoltFineActive -> setTripleBoltFineActive(action.isEnabled)
            is CreateGameAction.SetOvertakeFineActive -> setOvertakeFineActive(action.isEnabled)
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
                _state.update {
                    it.copy(isNotificationEnabled = isEnabled)
                }
            }
        }
        viewModelScope.launch {
            storageService.isManualInputEnabled.collect { isEnabled ->
                _state.update {
                    it.copy(isVirtualDiceEnabled = isEnabled.not())
                }
            }
        }
        viewModelScope.launch {
            storageService.isShakeEnabled.collect { isEnabled ->
                _state.update {
                    it.copy(isShakeEnabled = isEnabled)
                }
            }
        }
    }

    private fun showAddUserSheet() {
        _state.update {
            it.copy(
                isAddUserSheetVisible = true,
                newUserName = "",
            )
        }
    }

    private fun hideAddUserSheet() {
        _state.update {
            it.copy(
                isAddUserSheetVisible = false,
                newUserName = ""
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
        val userName = state.value.newUserName.trim()
        if (userName.isBlank()) return

        viewModelScope.launch {
            saveUserUseCase(userName)

            _state.update {
                it.copy(
                    newUserName = "",
                    isAddUserSheetVisible = false
                )
            }
        }
    }

    private fun createGame() {
        viewModelScope.launch {
            val state = state.value
            val game = createGameUseCase(
                users = state.selectedUsers,
                isShakeEnabled = state.isShakeEnabled,
                isVirtualDiceEnabled = state.isVirtualDiceEnabled,
                isNotificationEnabled = state.isNotificationEnabled,
                hasStartLimit = state.hasStartLimit,
                isBarrel1Active = state.isBarrel1Active,
                isBarrel2Active = state.isBarrel2Active,
                isBarrel3Active = state.isBarrel3Active,
                isTripleBoltFineActive = state.isTripleBoltFineActive,
                isOvertakeFineActive = state.isOvertakeFineActive
            )
            _state.update {
                it.copy(createdGameId = game.id)
            }
        }
    }

    private fun consumeCreatedGame() {
        _state.update {
            it.copy(createdGameId = null)
        }
    }

    private fun setNotificationEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            storageService.setNotificationEnabled(isEnabled)
        }
    }

    private fun setVirtualDiceEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            storageService.setManualInputEnabled(isEnabled.not())
        }
    }

    private fun setShakeEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            storageService.setShakeEnabled(isEnabled)
        }
    }

    private fun setHasStartLimit(isEnabled: Boolean) {
        _state.update {
            it.copy(hasStartLimit = isEnabled)
        }
    }

    private fun setBarrel1Active(isEnabled: Boolean) {
        _state.update {
            it.copy(isBarrel1Active = isEnabled)
        }
    }

    private fun setBarrel2Active(isEnabled: Boolean) {
        _state.update {
            it.copy(isBarrel2Active = isEnabled)
        }
    }

    private fun setBarrel3Active(isEnabled: Boolean) {
        _state.update {
            it.copy(isBarrel3Active = isEnabled)
        }
    }

    private fun setTripleBoltFineActive(isEnabled: Boolean) {
        _state.update {
            it.copy(isTripleBoltFineActive = isEnabled)
        }
    }

    private fun setOvertakeFineActive(isEnabled: Boolean) {
        _state.update {
            it.copy(isOvertakeFineActive = isEnabled)
        }
    }
}
