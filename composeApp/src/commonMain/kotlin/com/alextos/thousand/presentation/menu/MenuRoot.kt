package com.alextos.thousand.presentation.menu

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.game.statistics.StatisticsTabRoot
import com.alextos.thousand.presentation.game.game_rules.GameRulesScreen
import com.alextos.thousand.presentation.game.tutorial_game.TutorialGameScreen
import com.alextos.thousand.presentation.game.users.UsersScreen

@Composable
fun MenuRoot(
    onCreateGame: () -> Unit,
    onTutorialGame: () -> Unit,
    openGamesHistory: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MenuRoute.Menu,
    ) {
        horizontalTransition<MenuRoute.Menu> {
            MenuScreen(
                onCreateGame = onCreateGame,
                onTutorialGame = onTutorialGame,
                openGamesHistory = openGamesHistory,
                openRules = {
                    navController.navigate(MenuRoute.Rules)
                },
                openTutorial = {
                    navController.navigate(MenuRoute.Tutorial)
                },
                openStatistics = {
                    navController.navigate(MenuRoute.Statistics)
                },
            )
        }
        horizontalTransition<MenuRoute.Rules> {
            GameRulesScreen(
                onGoBack = navController::popBackStack,
            )
        }
        horizontalTransition<MenuRoute.Tutorial> {
            TutorialGameScreen(
                onGoBack = navController::popBackStack,
                onFinish = navController::popBackStack,
            )
        }
        horizontalTransition<MenuRoute.Statistics> {
            StatisticsTabRoot(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<MenuRoute.Users> {
            UsersScreen(
                onGoBack = navController::popBackStack,
            )
        }
    }
}
