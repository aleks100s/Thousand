package com.alextos.thousand.presentation.menu

data class MenuState(
    val isFirstLaunch: Boolean = true,
    val hasLocalGames: Boolean = false,
    val tiles: List<MenuTile> = listOf(
        MenuTile(
            title = "Локальная игра",
            description = "Играть на одном устройстве",
            size = MenuTileSize.Large,
            action = MenuTileAction.LocalGame,
        ),
        MenuTile(
            title = "Мультиплеер",
            description = "Игры с другими игроками",
            size = MenuTileSize.Large,
            action = MenuTileAction.Multiplayer,
        ),
        MenuTile(
            title = "Правила",
            description = "Комбинации, бочки и болты",
            size = MenuTileSize.Default,
            action = MenuTileAction.Rules,
        ),
        MenuTile(
            title = "Обучение",
            description = "Тестовая партия с подсказками",
            size = MenuTileSize.Default,
            action = MenuTileAction.Tutorial,
        ),
    ),
)

data class MenuTile(
    val title: String,
    val description: String,
    val size: MenuTileSize,
    val action: MenuTileAction,
)

enum class MenuTileSize {
    Default,
    Large,
}

enum class MenuTileAction {
    Tutorial,
    Rules,
    Multiplayer,
    LocalGame,
}
