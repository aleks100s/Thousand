package com.alextos.thousand.presentation.game.game_results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameResultsScreen(
    onGoBack: () -> Unit,
) {
    val viewModel: GameResultsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GameResultsAction.LoadResults)
    }

    Screen(
        modifier = Modifier,
        title = state.title,
        goBack = onGoBack,
    ) { modifier ->
        if (state.isLoading) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ScoreChartCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        series = state.scoreSeries,
                    )
                }

                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "Статистика игроков",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                items(state.playerStatistics) { player ->
                    PlayerStatisticsCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        statistics = player,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreChartCard(
    modifier: Modifier = Modifier,
    series: List<PlayerScoreSeries>,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Динамика счета",
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
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val maxScore = series.flatMap { it.points }.maxOrNull()?.coerceAtLeast(100) ?: 100
    val maxIndex = series.maxOfOrNull { it.points.lastIndex }?.coerceAtLeast(1) ?: 1

    Canvas(modifier = modifier) {
        val leftPadding = 12.dp.toPx()
        val rightPadding = 12.dp.toPx()
        val topPadding = 12.dp.toPx()
        val bottomPadding = 20.dp.toPx()
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding

        repeat(5) { index ->
            val y = topPadding + chartHeight * index / 4f
            drawLine(
                color = axisColor,
                start = Offset(leftPadding, y),
                end = Offset(size.width - rightPadding, y),
                strokeWidth = 1.dp.toPx(),
            )
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
                style = Stroke(width = 3.dp.toPx()),
            )

            playerSeries.points.forEachIndexed { pointIndex, score ->
                val x = leftPadding + chartWidth * pointIndex / maxIndex
                val y = topPadding + chartHeight * (1f - score.toFloat() / maxScore)
                drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
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
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
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
                            shape = RoundedCornerShape(5.dp),
                        )
                )
                Text(
                    text = playerSeries.playerName,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun PlayerStatisticsCard(
    modifier: Modifier = Modifier,
    statistics: GameResultsPlayerStatistics,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = statistics.playerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            HorizontalDivider()

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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = gameValue.toScoreText(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (shouldShowDelta) {
                Text(
                    text = "(${delta.toSignedScoreText()})",
                    color = deltaColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun chartColors(): List<Color> {
    return listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF2E7D32),
        Color(0xFFC62828),
        Color(0xFFEF6C00),
        Color(0xFF6A1B9A),
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

private val PositiveDeltaColor = Color(0xFF2E7D32)
private val NegativeDeltaColor = Color(0xFFC62828)
