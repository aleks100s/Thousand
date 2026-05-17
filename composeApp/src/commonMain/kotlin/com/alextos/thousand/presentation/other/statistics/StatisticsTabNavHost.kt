package com.alextos.thousand.presentation.other.statistics

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alextos.thousand.common.horizontalTransition
import com.alextos.thousand.presentation.other.statistics.dice_statistics.DiceStatisticsScreen
import com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsScreen
import com.alextos.thousand.presentation.other.statistics.roll_statistics.RollsStatisticsScreen
import com.alextos.thousand.presentation.other.statistics.turn_statistics.TurnsStatisticsScreen

@Composable
fun StatisticsTabRoot(
    goBack: (() -> Unit)? = null,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = _root_ide_package_.com.alextos.thousand.presentation.other.statistics.StatisticsRoute.Statistics,
    ) {
        horizontalTransition<com.alextos.thousand.presentation.other.statistics.StatisticsRoute.Statistics> {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.StatisticsScreen(
                goBack = goBack,
                openGamesStatistics = {
                    navController.navigate(_root_ide_package_.com.alextos.thousand.presentation.other.statistics.StatisticsRoute.GamesStatistics)
                },
                openTurnsStatistics = {
                    navController.navigate(_root_ide_package_.com.alextos.thousand.presentation.other.statistics.StatisticsRoute.TurnsStatistics)
                },
                openRollsStatistics = {
                    navController.navigate(_root_ide_package_.com.alextos.thousand.presentation.other.statistics.StatisticsRoute.RollsStatistics)
                },
                openDiceStatistics = {
                    navController.navigate(_root_ide_package_.com.alextos.thousand.presentation.other.statistics.StatisticsRoute.DiceStatistics)
                },
            )
        }
        horizontalTransition<com.alextos.thousand.presentation.other.statistics.StatisticsRoute.GamesStatistics> {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<com.alextos.thousand.presentation.other.statistics.StatisticsRoute.TurnsStatistics> {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.turn_statistics.TurnsStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<com.alextos.thousand.presentation.other.statistics.StatisticsRoute.RollsStatistics> {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.roll_statistics.RollsStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
        horizontalTransition<com.alextos.thousand.presentation.other.statistics.StatisticsRoute.DiceStatistics> {
            _root_ide_package_.com.alextos.thousand.presentation.other.statistics.dice_statistics.DiceStatisticsScreen(
                goBack = navController::popBackStack,
            )
        }
    }
}
