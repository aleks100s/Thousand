package com.alextos.thousand.presentation.menu

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.menu.create_game.CreateGameScreen
import com.alextos.thousand.presentation.menu.game_results.GameResultsScreen
import com.alextos.thousand.presentation.menu.game_score.GameScoreScreen
import com.alextos.thousand.presentation.menu.play_game.PlayGameScreen
import com.alextos.thousand.presentation.menu.game_list.GamesListScreen
import com.alextos.thousand.presentation.menu.game_rules.GameRulesScreen
import com.alextos.thousand.presentation.menu.statistics.StatisticsRoot
import com.alextos.thousand.presentation.menu.tutorial_game.TutorialGameScreen
import com.alextos.thousand.presentation.menu.users.UsersScreen

@Composable
fun MenuTabRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MenuRoute.Menu,
    ) {
        horizontalTransition<MenuRoute.Menu> {
            MenuScreen(
                onCreateGame = {
                    navController.navigate(MenuRoute.CreateGame)
                },
                openGamesHistory = {
                    navController.navigate(MenuRoute.GamesList)
                },
                openRules = {
                    navController.navigate(MenuRoute.Rules)
                },
                openTutorial = {
                    navController.navigate(MenuRoute.TutorialGame)
                },
                openStatistics = {
                    navController.navigate(MenuRoute.Statistics)
                },
                openUsers = {
                    navController.navigate(MenuRoute.Users)
                },
            )
        }
        horizontalTransition<MenuRoute.Rules> {
            GameRulesScreen(
                onGoBack = navController::popBackStack,
            )
        }
        horizontalTransition<MenuRoute.TutorialGame> {
            TutorialGameScreen(
                onGoBack = navController::popBackStack,
                onFinish = navController::popBackStack,
            )
        }
        horizontalTransition<MenuRoute.GamesList> {
            GamesListScreen(
                goBack = navController::popBackStack,
                onGameClick = { game ->
                    if (game.isFinished) {
                        navController.navigate(MenuRoute.GameScore(game.id))
                    } else {
                        navController.navigate(MenuRoute.PlayGame(game.id))
                    }
                },
                openGame = { gameId ->
                    navController.navigate(MenuRoute.PlayGame(gameId))
                },
            )
        }
        horizontalTransition<MenuRoute.CreateGame> {
            CreateGameScreen(
                goBack = navController::popBackStack,
                openGame = { gameId ->
                    navController.navigate(MenuRoute.GamesList) {
                        popUpTo(MenuRoute.Menu)
                    }
                    navController.navigate(MenuRoute.PlayGame(gameId))
                },
            )
        }
        horizontalTransition<MenuRoute.Statistics> {
            StatisticsRoot(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<MenuRoute.Users> {
            UsersScreen(
                onGoBack = navController::popBackStack,
            )
        }
        horizontalTransition<MenuRoute.PlayGame> { _ ->
            PlayGameScreen(
                onGoBack = navController::popBackStack,
                onScoreClick = {
                    navController.navigate(MenuRoute.GameScore(it.id))
                },
                onFinishGame = {
                    navController.navigate(MenuRoute.GameScore(it.id)) {
                        popUpTo(MenuRoute.Menu)
                    }
                },
            )
        }
        horizontalTransition<MenuRoute.GameScore> { _ ->
            GameScoreScreen(
                onGoBack = navController::popBackStack,
                onResultsClick = { game ->
                    navController.navigate(MenuRoute.GameResults(game.id))
                },
            )
        }
        horizontalTransition<MenuRoute.GameResults> { _ ->
            GameResultsScreen(
                onGoBack = navController::popBackStack,
            )
        }
    }
}
