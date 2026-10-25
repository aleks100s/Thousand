package com.alextos.thousand.presentation.menu.game_score

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alextos.thousand.presentation.components.SingleDieView

private val HistoryMint = Color(0xFF89F1D1)
private val HistoryCream = Color(0xFFF5EBD9)
private val HistoryMuted = Color(0xFFB9CCE4)
private val HistoryLine = Color(0xFF24505C)

@Composable
internal fun TurnHistoryCard(turn: TurnHistoryUi) {
    HistoryPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "ХОД ${turn.number}",
                color = Color(0xFF08212A),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFD48B), Color(0xFFFFB950))))
                    .border(1.dp, Color(0xFFFFDB94), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            )
            Text(turn.playerName, color = HistoryCream, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        turn.rolls.forEachIndexed { index, roll -> HistoryRollRow(index + 1, roll) }
        turn.effects.forEach { message ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF382A12), Color(0xCC201B12))))
                    .border(1.dp, Color(0xFFFFA51F), RoundedCornerShape(12.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("!", color = Color(0xFF25200F), fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                    modifier = Modifier.size(22.dp).background(Color(0xFFFFC35F), CircleShape))
                Text(message, color = Color(0xFFFFC35F), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            }
        }
        HorizontalDivider(color = HistoryLine)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HistoryTotal("Итог хода", turn.total.toString(), turn.total > 0, Modifier.weight(1f))
            Box(Modifier.padding(horizontal = 8.dp).width(1.dp).height(28.dp).background(HistoryLine))
            HistoryTotal("Общий счёт", turn.playerScore?.toString() ?: "—", (turn.playerScore ?: 0) > 0, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HistoryRollRow(number: Int, roll: HistoryRollUi) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color(0x99204C59), Color(0x3300202A)))).padding(6.dp),
    ) {
        val dieSize = ((maxWidth - 102.dp) / 5).coerceIn(16.dp, 48.dp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(number.toString(), textAlign = TextAlign.Center, color = HistoryCream,
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.size(24.dp).background(Color(0xFF234B58), CircleShape).wrapContentHeight(Alignment.CenterVertically))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                roll.dice.forEach { die ->
                    Box(Modifier.size(dieSize).then(if (die.scoring) Modifier.border(1.5.dp, HistoryMint, RoundedCornerShape(25)) else Modifier).padding(3.dp)) {
                        SingleDieView(Modifier.fillMaxSize(), die.value)
                    }
                }
            }
            Text(roll.score.toString(), color = if (roll.score > 0) HistoryMint else HistoryCream,
                fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center,
                modifier = Modifier.width(54.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xAA052C29))
                    .border(1.dp, Color(0xFF15574C), RoundedCornerShape(9.dp)).padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun HistoryTotal(label: String, value: String, positive: Boolean, modifier: Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.Center) {
        Text(label, color = HistoryMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.CenterVertically))
        Text(value, color = if (positive) HistoryMint else HistoryCream,
            fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun TurnHistoryCardPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            TurnHistoryCard(
                TurnHistoryUi(
                    id = 1, number = 1, playerName = "Легендарный Хомяк",
                    rolls = listOf(
                        HistoryRollUi(listOf(6, 6, 5, 3, 2).map {
                            HistoryDieUi(com.alextos.thousand.domain.models.DieValue.entries[it - 1], it == 5)
                        }, 5),
                    ),
                    effects = listOf("Легендарный Хомяк не выбил стартовые 50 очков"),
                    total = 0, playerScore = 0, otherChanges = emptyList(),
                ),
            )
        }
    }
}
