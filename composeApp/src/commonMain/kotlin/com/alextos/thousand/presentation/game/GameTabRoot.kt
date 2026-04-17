package com.alextos.thousand.presentation.game

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.game.create_game.CreateGameScreen
import com.alextos.thousand.presentation.game.play_game.PlayGameScreen
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
            GamesListScreen(
                onGameClick = { game ->
                    if (game.isFinished) {
                        navController.navigate(GameRoute.GameScore(game.id))
                    } else {
                        navController.navigate(GameRoute.PlayGame(game.id))
                    }
                },
                onCreateGame = {
                    navController.navigate(GameRoute.CreateGame)
                }
            )
        }
        horizontalTransition<GameRoute.CreateGame> { _ ->
            CreateGameScreen(goBack = navController::popBackStack)
        }
        horizontalTransition<GameRoute.PlayGame> { _ ->
            PlayGameScreen(
                onGoBack = navController::popBackStack,
            )
        }
        horizontalTransition<GameRoute.GameScore> { _ ->
            GameScoreScreen(
                onGoBack = navController::popBackStack,
            )
        }
    }
}
