package com.alextos.thousand.application.navigation

import org.jetbrains.compose.resources.DrawableResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.menu_24px

enum class BottomTab(
    val route: String,
    val title: String,
    val iconResource: DrawableResource,
) {
    Game(
        route = "tab_game",
        title = "Игра",
        iconResource = Res.drawable.casino_24px,
    ),
    Other(
        route = "tab_other",
        title = "Меню",
        iconResource = Res.drawable.menu_24px,
    ),
}
