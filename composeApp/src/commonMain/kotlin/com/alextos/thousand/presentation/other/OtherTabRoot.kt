package com.alextos.thousand.presentation.other

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.game.game_rules.GameRulesScreen
import com.alextos.thousand.presentation.game.tutorial_game.TutorialGameScreen
import com.alextos.thousand.presentation.statistics.StatisticsTabRoot

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
            StatisticsTabRoot(
                goBack = navController::popBackStack,
            )
        }
    }
}
