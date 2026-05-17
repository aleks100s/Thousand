package com.alextos.thousand.presentation.other

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.other.game_rules.GameRulesScreen
import com.alextos.thousand.presentation.other.tutorial_game.TutorialGameScreen
import com.alextos.thousand.presentation.other.users.UsersScreen

@Composable
fun OtherTabRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = OtherRoute.Other,
    ) {
        horizontalTransition<OtherRoute.Other> {
            OtherScreen(
                openRules = {
                    navController.navigate(OtherRoute.Rules)
                },
                openTutorial = {
                    navController.navigate(OtherRoute.Tutorial)
                },
                openStatistics = {
                    navController.navigate(OtherRoute.Statistics)
                },
                openUsers = {
                    navController.navigate(OtherRoute.Users)
                },
            )
        }
        horizontalTransition<OtherRoute.Rules> {
            GameRulesScreen(
                onGoBack = navController::popBackStack,
            )
        }
        horizontalTransition<OtherRoute.Tutorial> {
            TutorialGameScreen(
                onGoBack = navController::popBackStack,
                onFinish = navController::popBackStack,
            )
        }
        horizontalTransition<OtherRoute.Statistics> {
            com.alextos.thousand.presentation.other.statistics.StatisticsTabRoot(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<OtherRoute.Users> {
            UsersScreen(
                onGoBack = navController::popBackStack,
            )
        }
    }
}
