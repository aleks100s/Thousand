package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.AppColors
import com.alextos.thousand.presentation.components.PanelSegment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alextos.thousand.presentation.components.SingleDieView

private val HistoryMint = AppColors.Positive
private val HistoryCream = AppColors.TextPrimary
private val HistoryMuted = AppColors.TextSecondary
private val HistoryLine = AppColors.OutlineSubtle

@Composable
internal fun TurnHistoryLazyRow(row: TurnHistoryRow) {
    when (row) {
        is TurnHistoryRow.Header -> PanelSegment(first = true, last = false) { TurnHistoryHeader(row.turn) }
        is TurnHistoryRow.Roll -> PanelSegment(first = false, last = false) { HistoryRollRow(row.number, row.roll) }
        is TurnHistoryRow.Effect -> PanelSegment(first = false, last = false) { TurnHistoryEffect(row.message) }
        is TurnHistoryRow.Footer -> Column {
            PanelSegment(first = false, last = true) { TurnHistoryFooter(row.turn) }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TurnHistoryHeader(turn: TurnHistoryUi) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "ХОД ${turn.number}",
            color = AppColors.OnAccent,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clip(CircleShape)
                .background(Brush.verticalGradient(listOf(AppColors.Highlight, AppColors.Highlight)))
                .border(1.dp, AppColors.Highlight, CircleShape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        )
        Text(turn.playerName, color = HistoryCream, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TurnHistoryEffect(message: String) {
    Row(
        Modifier.fillMaxWidth().clip(AppShapes.Row)
            .background(Brush.horizontalGradient(listOf(AppColors.Highlight.copy(alpha = .12f), AppColors.Highlight.copy(alpha = .12f))))
            .border(1.dp, AppColors.Highlight, AppShapes.Row).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("!", color = AppColors.OnAccent, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
            modifier = Modifier.size(22.dp).background(AppColors.Highlight, CircleShape))
        Text(message, color = AppColors.Highlight, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TurnHistoryFooter(turn: TurnHistoryUi) {
    HorizontalDivider(color = HistoryLine)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HistoryTotal("Итог хода", turn.total.toString(), turn.total > 0, Modifier.weight(1f))
        Box(Modifier.padding(horizontal = 8.dp).width(1.dp).height(28.dp).background(HistoryLine))
        HistoryTotal("Общий счёт", turn.playerScore?.toString() ?: "—", (turn.playerScore ?: 0) > 0, Modifier.weight(1f))
    }
}

@Composable
private fun HistoryRollRow(number: Int, roll: HistoryRollUi) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().clip(AppShapes.Row)
            .background(Brush.horizontalGradient(listOf(AppColors.SurfaceInset, AppColors.SurfaceInset))).padding(6.dp),
    ) {
        val dieSize = ((maxWidth - 102.dp) / 5).coerceIn(16.dp, 48.dp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(number.toString(), textAlign = TextAlign.Center, color = HistoryCream,
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.size(24.dp).background(AppColors.SurfaceMuted, CircleShape).wrapContentHeight(Alignment.CenterVertically))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                roll.dice.forEach { die ->
                    Box(Modifier.size(dieSize).then(if (die.scoring) Modifier.border(1.5.dp, AppColors.Scoring, RoundedCornerShape(25)) else Modifier).padding(3.dp)) {
                        SingleDieView(Modifier.fillMaxSize(), die.value)
                    }
                }
            }
            Text(roll.score.toString(), color = if (roll.score > 0) HistoryMint else HistoryCream,
                fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center,
                modifier = Modifier.width(54.dp).clip(AppShapes.Inset).background(AppColors.Positive.copy(alpha = .08f))
                    .border(1.dp, AppColors.Positive.copy(alpha = .35f), AppShapes.Inset).padding(vertical = 8.dp))
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
            val turn = TurnHistoryUi(
                id = 1, number = 1, playerName = "Легендарный Хомяк",
                rolls = listOf(
                    HistoryRollUi(listOf(6, 6, 5, 3, 2).map {
                        HistoryDieUi(com.alextos.thousand.domain.models.DieValue.entries[it - 1], it == 5)
                    }, 5),
                ),
                effects = listOf("Легендарный Хомяк не выбил стартовые 50 очков"),
                total = 0, playerScore = 0, otherChanges = emptyList(),
            )
            LazyColumn {
                items(listOf(turn).toHistoryRows(), key = { it.key }, contentType = { it::class }) { TurnHistoryLazyRow(it) }
            }
        }
    }
}
