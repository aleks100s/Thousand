package com.alextos.thousand.presentation.menu.game_score

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alextos.thousand.application.theme.AppBackground
import com.alextos.thousand.application.theme.ThousandTheme
import com.alextos.thousand.domain.models.*
import com.alextos.thousand.domain.usecase.game.FormatTurnEffectUseCase

@Preview
@Composable
internal fun GameScorePreview() {
    ThousandTheme {
        AppBackground { GameScoreContent(gameScorePreviewState(), {}) }
    }
}

internal fun gameScorePreviewState(): GameScoreState {
    val winner = Player(user = User(id = "preview-a", name = "Маша"), currentScore = 1000, isWinner = true)
    val opponent = Player(user = User(id = "preview-b", name = "Легендарный Хомяк"), currentScore = 750)
    val turn = Turn(id = 1, player = winner,
        rolls = listOf(DiceRoll(dice = listOf(1, 1, 1, 3, 4).map { Die(value = DieValue.entries[it - 1]) }, result = 100)),
        total = 100, effects = emptyList(),
        results = listOf(TurnResult(player = winner, scoreChange = 100, newScore = 1000)))
    return GameScoreState(
        isLoading = false,
        game = Game(id = 1, players = listOf(winner, opponent)),
        turns = listOf(turn.toHistoryUi(1, FormatTurnEffectUseCase())),
        scoreSeries = listOf(
            PlayerScoreSeries(winner.user.id, winner.user.name, listOf(0, 100, 100, 220, 170, 600, 600, 1000)),
            PlayerScoreSeries(opponent.user.id, opponent.user.name, listOf(0, 0, 200, 200, 350, 350, 750, 750)),
        ),
        playerStatistics = listOf(
            GameResultsPlayerStatistics(winner.user.id, winner.user.name, 95.5, 80.0, 35.4, 42.0),
            GameResultsPlayerStatistics(opponent.user.id, opponent.user.name, 75.0, 90.2, 20.0, 19.4),
        ),
    )
}
