package com.alextos.thousand.presentation.game

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.game.create_game.CreateGameScreen
import com.alextos.thousand.presentation.game.game.GameScreen
import com.alextos.thousand.presentation.game.game_list.GamesListScreen
import com.alextos.thousand.presentation.game.game_score.GameScoreScreen

@Composable
fun GameTabRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = GameRoute.GamesList,
    ) {
        horizontalTransition<GameRoute.GamesList> { _ ->
            GamesListScreen()
        }
        horizontalTransition<GameRoute.CreateGame> { _ ->
            CreateGameScreen()
        }
        horizontalTransition<GameRoute.Game> { _ ->
            GameScreen()
        }
        horizontalTransition<GameRoute.GameScore> { _ ->
            GameScoreScreen()
        }
    }
}
