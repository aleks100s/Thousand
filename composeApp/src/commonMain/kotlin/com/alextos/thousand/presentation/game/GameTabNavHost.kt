package com.alextos.thousand.presentation.game

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition

@Composable
fun GameTabNavHost() {
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
