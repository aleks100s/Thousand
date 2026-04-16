package com.alextos.thousand.presentation.game.game_score

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.DiceRoll
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.Game
import com.alextos.thousand.domain.models.Player
import com.alextos.thousand.domain.models.Turn
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.person_24px
import thousand.composeapp.generated.resources.trophy_24px

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
            Box(modifier = modifier.fillMaxSize()) {
                LoadingIndicator()
            }
        } else {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                item {
                    GameHeaderView(game)
                }

                items(state.turns) { turn ->
                    TurnHeaderView(player = game.players.first { it.user == turn.user })
                    TurnView(turn)
                }
            }
        }
    }
}

@Composable
private fun GameHeaderView(game: Game) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        game.players.forEach { player ->
            PlayerView(player)

            if (game.players.firstOrNull() == player) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Red)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    text = "vs"
                )
            }
        }
    }
}

@Composable
private fun TurnHeaderView(player: Player) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Ходит $player")

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
private fun PlayerView(player: Player) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(if (player.isWinner) Res.drawable.trophy_24px else Res.drawable.person_24px),
            contentDescription = null,
            tint = if (player.isWinner) Color.Yellow else MaterialTheme.colorScheme.onBackground
        )

        Text(text = player.toString())
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
            Row(modifier = Modifier.fillMaxWidth()) {
                (1..5).forEach { _ ->
                    EmptyCell()
                }

                ResultCell(
                    result = turn.total,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DiceRollView(roll: DiceRoll) {
    Row(modifier = Modifier.fillMaxWidth()) {
        roll.dice.forEach {
            DieView(modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally), die = it)
        }

        (roll.dice.count()..4).forEach { _ ->
            EmptyCell()
        }

        ResultCell(
            result = roll.result,
            tint = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun DieView(modifier: Modifier, die: Die) {
    Text(
        text = die.value.value.toString(),
        modifier = modifier.padding(vertical = 12.dp)
    )
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