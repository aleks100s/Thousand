package com.alextos.thousand.presentation.game.game_score

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import com.alextos.thousand.domain.models.TurnEffect
import com.alextos.thousand.domain.models.TurnResult
import com.alextos.thousand.presentation.game.components.GameHeaderView
import com.alextos.thousand.presentation.game.components.SingleDieView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameScoreScreen(
    onGoBack: () -> Unit,
) {
    val viewModel: GameScoreViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GameScoreAction.LoadGame)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
    ) { modifier ->
        val game = state.game
        if (game == null) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                item {
                    GameHeaderView(game)
                }

                items(state.turns) { turn ->
                    TurnHeaderView(player = game.players.first { it == turn.player })
                    TurnView(turn)
                }
            }
        }
    }
}

@Composable
private fun TurnHeaderView(player: Player) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Ходит $player", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth()) {
            (1..5).forEach {
                Text(
                    modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
                    text = "№$it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
                text = "Сумма",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TurnView(turn: Turn) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        turn.rolls.forEach {
            DiceRollView(it)
        }

        if (turn.rolls.count() > 1) {
            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth()) {
                (1..5).forEach { _ ->
                    EmptyCell()
                }

                ResultCell(
                    result = turn.total,
                    tint = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.onTertiary
                )
            }
        }

        turn.effects.forEach {
            TurnEffectView(it, currentPlayer = turn.player)
        }

        TurnResultView(turn.results)
    }
}

@Composable
private fun DiceRollView(roll: DiceRoll) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        roll.dice.forEach {
            DieView(modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally), die = it)
        }

        (roll.dice.count()..4).forEach { _ ->
            EmptyCell()
        }

        ResultCell(
            result = roll.result,
            tint = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun DieView(modifier: Modifier, die: Die) {
    SingleDieView(modifier.size(32.dp), dieValue = die.value)
}

@Composable
private fun RowScope.EmptyCell() {
    Box(modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally).padding(vertical = 12.dp))
}

@Composable
private fun RowScope.ResultCell(result: Int, tint: Color, textColor: Color) {
    Text(
        modifier = Modifier
            .background(tint)
            .weight(1f)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(vertical = 12.dp),
        text = result.toString(),
        color = textColor
    )
}

@Composable
private fun TurnEffectView(effect: TurnEffect, currentPlayer: Player) {
    Row(modifier = Modifier
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .padding(vertical = 8.dp, horizontal = 16.dp)
        .fillMaxWidth()
    ) {
        Text(
            text = effect.text(currentPlayer),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TurnResultView(results: List<TurnResult>) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        results.forEach {
            PlayerScoreView(it)
        }
    }
}

@Composable
private fun RowScope.PlayerScoreView(result: TurnResult) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = result.player.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Box(contentAlignment = Alignment.TopEnd) {
            Text(
                text = result.newScore.toString(),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                modifier = Modifier.offset(24.dp, (-6).dp),
                text = "${if (result.scoreChange < 0) "" else "+"}${result.scoreChange}",
                color = if (result.scoreChange > 0) Color.Green else if (result.scoreChange < 0) Color.Red else Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}