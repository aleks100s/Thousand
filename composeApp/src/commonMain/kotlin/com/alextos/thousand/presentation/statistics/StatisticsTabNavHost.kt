package com.alextos.thousand.presentation.statistics

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.statistics.dice_statistics.DiceStatisticsScreen
import com.alextos.thousand.presentation.statistics.games_statistics.GamesStatisticsScreen
import com.alextos.thousand.presentation.statistics.roll_statistics.RollsStatisticsScreen
import com.alextos.thousand.presentation.statistics.turn_statistics.TurnsStatisticsScreen

@Composable
fun StatisticsTabRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = StatisticsRoute.Statistics,
    ) {
        horizontalTransition<StatisticsRoute.Statistics> {
            StatisticsScreen(
                openGamesStatistics = {
                    navController.navigate(StatisticsRoute.GamesStatistics)
                },
                openTurnsStatistics = {
                    navController.navigate(StatisticsRoute.TurnsStatistics)
                },
                openRollsStatistics = {
                    navController.navigate(StatisticsRoute.RollsStatistics)
                },
                openDiceStatistics = {
                    navController.navigate(StatisticsRoute.DiceStatistics)
                },
            )
        }
        horizontalTransition<StatisticsRoute.GamesStatistics> {
            GamesStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<StatisticsRoute.TurnsStatistics> {
            TurnsStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<StatisticsRoute.RollsStatistics> {
            RollsStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<StatisticsRoute.DiceStatistics> {
            DiceStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
    }
}
