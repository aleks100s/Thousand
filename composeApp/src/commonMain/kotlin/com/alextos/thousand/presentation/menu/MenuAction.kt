package com.alextos.thousand.presentation.menu

sealed interface MenuAction {
    data object CompleteFirstLaunch : MenuAction
}
