package com.alextos.thousand.presentation.menu

data class MenuState(
    val isFirstLaunch: Boolean = true,
    val hasLocalGames: Boolean = false,
    val tiles: List<MenuTile> = listOf(
        MenuTile(
            title = "Играть на одном устройстве",
            description = "Играть на одном устройстве",
            size = MenuTileSize.Large,
            action = MenuTileAction.LocalGame,
        ),
        MenuTile(
            title = "Играть онлайн",
            description = "Игры с другими игроками",
            size = MenuTileSize.Large,
            action = MenuTileAction.Multiplayer,
        ),
        MenuTile(
            title = "Обучение",
            description = "Быстро и просто",
            size = MenuTileSize.Default,
            action = MenuTileAction.Tutorial,
        ),
        MenuTile(
            title = "Правила",
            description = "Все комбинации",
            size = MenuTileSize.Default,
            action = MenuTileAction.Rules,
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
