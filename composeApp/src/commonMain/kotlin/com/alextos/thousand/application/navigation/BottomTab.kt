package com.alextos.thousand.application.navigation

import org.jetbrains.compose.resources.DrawableResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.donut_small_24px

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
    Statistics(
        route = "tab_statistics",
        title = "Статистика",
        iconResource = Res.drawable.donut_small_24px,
    ),
}
