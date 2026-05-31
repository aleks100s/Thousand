package com.alextos.thousand.presentation.menu

data class MenuState(
    val isFirstLaunch: Boolean = true,
    val tiles: List<MenuTile> = listOf(
        MenuTile(
            title = "Обучение",
            description = "Тестовая партия с подсказками",
            size = MenuTileSize.Default,
            action = MenuTileAction.Tutorial,
        ),
        MenuTile(
            title = "Правила игры",
            description = "Комбинации, бочки и болты",
            size = MenuTileSize.Default,
            action = MenuTileAction.Rules,
        ),
        MenuTile(
            title = "История игр",
            description = "Сохраненные партии",
            size = MenuTileSize.Default,
            action = MenuTileAction.GamesHistory,
        ),
        MenuTile(
            title = "Статистика",
            description = "Игры, ходы, броски и кубики",
            size = MenuTileSize.Default,
            action = MenuTileAction.Statistics,
        ),
        MenuTile(
            title = "Новая игра",
            description = "Создать новую партию",
            size = MenuTileSize.Large,
            action = MenuTileAction.NewGame,
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
    GamesHistory,
    Statistics,
    NewGame,
}
