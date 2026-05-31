package com.alextos.thousand.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alextos.thousand.domain.service.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MenuViewModel(
    private val storageService: StorageService
) : ViewModel() {
    private val _state = MutableStateFlow(MenuState())
    val state: StateFlow<MenuState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            storageService.isFirstLaunch.collect { isFirstLaunch ->
                _state.update {
                    it.copy(isFirstLaunch = isFirstLaunch)
                }
            }
        }
    }

    fun onAction(action: MenuAction) {
        when (action) {
            MenuAction.CompleteFirstLaunch -> completeFirstLaunch()
        }
    }

    private fun completeFirstLaunch() {
        viewModelScope.launch {
            storageService.setFirstLaunch(false)
        }
    }
}
