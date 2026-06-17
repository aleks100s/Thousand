package com.alextos.thousand.application

sealed interface AppAction {
    data object LaunchFinished : AppAction
}
