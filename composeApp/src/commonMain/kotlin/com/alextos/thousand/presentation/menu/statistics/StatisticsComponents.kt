package com.alextos.thousand.presentation.menu.statistics

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*
import kotlin.math.roundToInt

internal val StatisticsCream = Color(0xFFF5F0DF)
internal val StatisticsBody = Color(0xFFA6C5DD)
internal val StatisticsOutline = Color(0xFF087F9D)
internal val StatisticsBlue = Color(0xFF8DBDFF)
internal val StatisticsPurple = Color(0xFFB59AF8)
internal val StatisticsGold = Color(0xFFFFCE72)

internal data class StatisticsMetricUi(
    val label: String,
    val value: String,
    val icon: DrawableResource,
    val accent: Color = StatisticsBlue,
)

@Composable
internal fun StatisticsPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier.fillMaxWidth().clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xE0053040), Color(0xE0001826))))
            .border(1.5.dp, StatisticsOutline, shape).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp), content = content,
    )
}

@Composable
internal fun StatisticsSummary(
    metrics: List<StatisticsMetricUi>,
    title: String = "Все игры",
    onHistogram: (() -> Unit)? = null,
) {
    StatisticsPanel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), color = StatisticsCream,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (onHistogram != null) TextButton(onClick = onHistogram) {
                Icon(painterResource(Res.drawable.statistics_bar_chart), null, Modifier.size(18.dp), tint = StatisticsBlue)
                Spacer(Modifier.width(5.dp))
                Text("Гистограмма", color = StatisticsBlue)
            }
        }
        metrics.chunked(3).forEach { group ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                group.forEachIndexed { index, metric ->
                    if (index > 0) VerticalDivider(Modifier.height(64.dp), color = StatisticsOutline.copy(alpha = .45f))
                    BoxWithConstraints(Modifier.weight(1f)) {
                        val inline = maxWidth >= 140.dp
                        if (inline) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MetricIcon(metric)
                                MetricValue(metric)
                            }
                        } else {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                MetricIcon(metric)
                                MetricValue(metric, centered = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricIcon(metric: StatisticsMetricUi) {
    val shape = RoundedCornerShape(12.dp)
    Box(Modifier.size(40.dp).clip(shape)
        .background(Brush.verticalGradient(listOf(metric.accent.copy(alpha = .22f), Color(0xFF062532))))
        .border(1.dp, metric.accent.copy(alpha = .45f), shape), contentAlignment = Alignment.Center) {
        Icon(painterResource(metric.icon), null, Modifier.size(27.dp), tint = metric.accent)
    }
}

@Composable
private fun MetricValue(metric: StatisticsMetricUi, centered: Boolean = false) {
    Column(horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(metric.value, color = StatisticsCream, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(metric.label, color = StatisticsBody, style = MaterialTheme.typography.bodySmall,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start)
    }
}

/** Shared column widths and scroll position keep the heading aligned with every lazy row. */
@Composable
internal fun StatisticsTableRow(
    name: String,
    values: List<String>,
    header: Boolean = false,
    scrollState: ScrollState? = null,
    showAction: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val tableWidth = maxWidth.coerceAtLeast(if (values.size > 3) 640.dp else 0.dp)
        val viewport = if (scrollState != null) Modifier.horizontalScroll(scrollState) else Modifier
        Box(Modifier.fillMaxWidth().then(viewport)) {
            val shape = RoundedCornerShape(if (header) 10.dp else 14.dp)
            val action = if (onClick != null) Modifier.clickable(role = Role.Button,
                onClickLabel = "Показать распределение: $name", onClick = onClick) else Modifier
            Row(
                Modifier.width(tableWidth).heightIn(min = if (header) 44.dp else 64.dp).clip(shape)
                    .background(if (header) Color(0xE0052E3D) else Color(0xC0001E2B))
                    .border(1.dp, if (header) Color(0xFF08465A) else Color(0xFF07546B), shape)
                    .then(action).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(Modifier.weight(2.5f), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!header) {
                        Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1B3541)),
                            contentAlignment = Alignment.Center) {
                            Icon(painterResource(Res.drawable.person_24px), null, Modifier.size(20.dp), tint = Color(0xFFCBD8DF))
                        }
                    }
                    Text(name, Modifier.weight(1f), color = if (header) StatisticsBody else StatisticsCream,
                        style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
                        fontWeight = if (header) FontWeight.Bold else FontWeight.Medium)
                }
                values.forEach { value ->
                    Text(value, Modifier.weight(1f), color = if (header) StatisticsBody else StatisticsCream,
                        textAlign = TextAlign.Center,
                        style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
                        fontWeight = if (header) FontWeight.Bold else FontWeight.Normal)
                }
                if (showAction) {
                    if (header) Spacer(Modifier.width(20.dp))
                    else Icon(painterResource(Res.drawable.statistics_bar_chart), "Гистограмма", Modifier.size(20.dp), tint = StatisticsBlue)
                }
            }
        }
    }
}

@Composable
internal fun EmptyStatisticsCard(text: String) {
    StatisticsPanel { Text(text, color = StatisticsBody, style = MaterialTheme.typography.bodyMedium) }
}

internal fun Double.toStatisticsText(): String {
    val text = ((this * 10).roundToInt() / 10.0).toString()
    return text.removeSuffix(".0")
}
