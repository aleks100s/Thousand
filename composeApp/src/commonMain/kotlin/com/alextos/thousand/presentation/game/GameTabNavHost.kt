package com.alextos.thousand.presentation.game

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun GameTabNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = GameRoute.GAMES_LIST.value,
    ) {
        composable(GameRoute.GAMES_LIST.value) {
            GamesListScreen()
        }
        composable(GameRoute.CREATE_GAME.value) {
            CreateGameScreen()
        }
        composable(GameRoute.GAME.value) {
            GameScreen()
        }
        composable(GameRoute.GAME_SCORE.value) {
            GameScoreScreen()
        }
    }
}
