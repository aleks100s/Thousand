package com.alextos.thousand.application.navigation

enum class BottomTab(
    val route: String,
    val title: String,
    val iconLabel: String,
) {
    Game(
        route = "tab_game",
        title = "Игра",
        iconLabel = "И",
    ),
    Statistics(
        route = "tab_statistics",
        title = "Статистика",
        iconLabel = "С",
    ),
}
