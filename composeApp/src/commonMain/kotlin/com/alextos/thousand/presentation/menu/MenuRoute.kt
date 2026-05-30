package com.alextos.thousand.presentation.menu

import kotlinx.serialization.Serializable

sealed interface MenuRoute {
    @Serializable
    data object Menu : MenuRoute

    @Serializable
    data object Rules : MenuRoute

    @Serializable
    data object Tutorial : MenuRoute

    @Serializable
    data object Statistics : MenuRoute

    @Serializable
    data object Users : MenuRoute
}
