package com.alextos.thousand.presentation.menu.game_score

import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.AppColors
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
internal fun ScoreChartCard(
    modifier: Modifier = Modifier,
    series: List<PlayerScoreSeries>,
) {
    HistoryPanel(modifier) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Динамика счёта",
                color = SummaryCream,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )

            if (series.isEmpty() || series.all { it.points.size <= 1 }) {
                Text(
                    text = "Пока нет ходов для построения графика",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                val colors = chartColors()
                ScoreChart(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    series = series,
                    colors = colors,
                )

                ChartLegend(
                    series = series,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun ScoreChart(
    modifier: Modifier = Modifier,
    series: List<PlayerScoreSeries>,
    colors: List<Color>,
) {
    val axisColor = AppColors.OutlineSubtle
    val textMeasurer = rememberTextMeasurer()
    val maxScore = series.flatMap { it.points }.maxOrNull()?.coerceAtLeast(100) ?: 100
    val maxIndex = series.maxOfOrNull { it.points.lastIndex }?.coerceAtLeast(1) ?: 1

    Canvas(modifier = modifier) {
        val leftPadding = 36.dp.toPx()
        val rightPadding = 12.dp.toPx()
        val topPadding = 12.dp.toPx()
        val bottomPadding = 26.dp.toPx()
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding

        repeat(5) { index ->
            val y = topPadding + chartHeight * index / 4f
            drawLine(
                color = axisColor,
                start = Offset(leftPadding, y),
                end = Offset(size.width - rightPadding, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            )
        }

        val axisStyle = TextStyle(color = SummaryMuted, fontSize = 10.sp)
        repeat(5) { index ->
            val label = textMeasurer.measure((maxScore * (4 - index) / 4).toString(), axisStyle)
            drawText(label, topLeft = Offset(0f, topPadding + chartHeight * index / 4f - label.size.height / 2))
        }
        val steps = minOf(4, maxIndex)
        for (index in 0..steps) {
            val turn = (maxIndex * index.toFloat() / steps).roundToInt()
            val label = textMeasurer.measure(turn.toString(), axisStyle)
            drawText(label, topLeft = Offset(leftPadding + chartWidth * turn / maxIndex - label.size.width / 2,
                topPadding + chartHeight + 6.dp.toPx()))
        }
        series.forEachIndexed { seriesIndex, playerSeries ->
            val path = Path()
            playerSeries.points.forEachIndexed { pointIndex, score ->
                val x = leftPadding + chartWidth * pointIndex / maxIndex
                val y = topPadding + chartHeight * (1f - score.toFloat() / maxScore)
                if (pointIndex == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            val color = colors[seriesIndex % colors.size]
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
            )

            playerSeries.points.forEachIndexed { pointIndex, score ->
                val x = leftPadding + chartWidth * pointIndex / maxIndex
                val y = topPadding + chartHeight * (1f - score.toFloat() / maxScore)
                drawCircle(
                    color = color,
                    radius = if (playerSeries.points.size < 40) 3.dp.toPx() else 1.5.dp.toPx(),
                    center = Offset(x, y),
                )
            }
        }
    }
}

@Composable
private fun ChartLegend(
    series: List<PlayerScoreSeries>,
    colors: List<Color>,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        series.forEachIndexed { index, playerSeries ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = colors[index % colors.size],
                            shape = AppShapes.Inset,
                        )
                )
                Text(
                    text = playerSeries.playerName,
                    color = SummaryMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun PlayerStatisticsCard(
    modifier: Modifier = Modifier,
    statistics: GameResultsPlayerStatistics,
) {
    HistoryPanel(modifier) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = statistics.playerName,
                color = SummaryCream,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text("Статистика игрока · сравнение со всеми играми", color = SummaryMuted, style = MaterialTheme.typography.labelSmall)
            HorizontalDivider(color = AppColors.OutlineSubtle)

            StatisticComparisonRow(
                title = "Средний ход",
                gameValue = statistics.gameAverageTurn,
                globalValue = statistics.globalAverageTurn,
            )

            StatisticComparisonRow(
                title = "Средний бросок",
                gameValue = statistics.gameAverageRoll,
                globalValue = statistics.globalAverageRoll,
            )
        }
    }
}

@Composable
private fun StatisticComparisonRow(
    title: String,
    gameValue: Double,
    globalValue: Double,
) {
    val delta = gameValue - globalValue
    val deltaColor = when {
        delta > 0 -> PositiveDeltaColor
        delta < 0 -> NegativeDeltaColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shouldShowDelta = delta.toRoundedTenths() != 0

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, modifier = Modifier.weight(1f), color = SummaryMuted,
                style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(gameValue.toScoreText(), color = PositiveDeltaColor,
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                if (shouldShowDelta) {
                    Text("(${delta.toSignedScoreText()})", color = deltaColor,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Text("Обычно: ${globalValue.toScoreText()}", color = SummaryMuted,
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun chartColors(): List<Color> {
    return listOf(
        AppColors.Positive,
        AppColors.Highlight,
        AppColors.ChartBlue,
        AppColors.Danger,
        AppColors.Purple,
        AppColors.ChartCyan,
    )
}

private fun Double.toScoreText(): String {
    val roundedTenths = toRoundedTenths()
    val sign = if (roundedTenths < 0) "-" else ""
    val absoluteTenths = roundedTenths.absoluteValue
    val whole = absoluteTenths / 10
    val decimal = absoluteTenths % 10
    if (decimal == 0) return "$sign$whole"
    return "$sign$whole.$decimal"
}

private fun Double.toSignedScoreText(): String {
    val text = toScoreText()
    return if (this > 0) "+$text" else text
}

private fun Double.toRoundedTenths(): Int {
    return (this * 10).roundToInt()
}

private val SummaryCream = AppColors.TextPrimary
private val SummaryMuted = AppColors.TextSecondary
private val PositiveDeltaColor = AppColors.Positive
private val NegativeDeltaColor = AppColors.Danger
