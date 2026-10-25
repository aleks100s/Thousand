package com.alextos.thousand.presentation.menu.statistics

import com.alextos.thousand.application.theme.AppDimens
import com.alextos.thousand.presentation.components.AppIconBadge
import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.AppColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alextos.thousand.common.Screen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*

@Composable
fun StatisticsScreen(
    goBack: (() -> Unit)? = null,
    openGamesStatistics: () -> Unit,
    openTurnsStatistics: () -> Unit,
    openRollsStatistics: () -> Unit,
    openDiceStatistics: () -> Unit,
    openEventsStatistics: () -> Unit,
) {
    Screen(
        modifier = Modifier,
        constrainListWidth = true,
        title = "Статистика",
        goBack = goBack,
    ) { modifier ->
        LazyColumn(
            modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(StatisticsSection.entries, key = { it.name }, contentType = { "statistics-section" }) { section ->
                StatisticsItem(section, onClick = when (section) {
                    StatisticsSection.Games -> openGamesStatistics
                    StatisticsSection.Turns -> openTurnsStatistics
                    StatisticsSection.Rolls -> openRollsStatistics
                    StatisticsSection.Dice -> openDiceStatistics
                    StatisticsSection.Events -> openEventsStatistics
                })
            }
        }
    }
}

private enum class StatisticsSection(
    val title: String,
    val description: String,
    val icon: DrawableResource,
) {
    Games("Статистика игр", "Победы, поражения и завершённые игры", Res.drawable.statistics_bar_chart),
    Turns("Статистика ходов", "Результативность ходов и средние очки", Res.drawable.statistics_format_list_bulleted),
    Rolls("Статистика бросков", "Серии бросков, рекорды и средние показатели", Res.drawable.rules_casino),
    Dice("Статистика кубиков", "Распределение значений и средний результат кубиков", Res.drawable.statistics_emoji_events),
    Events("Статистика событий", "Болты, обгоны, штрафы и другие события", Res.drawable.statistics_calendar_month),
}

@Composable
private fun StatisticsItem(section: StatisticsSection, onClick: () -> Unit) {
    val shape = AppShapes.Panel
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = shape,
        modifier = Modifier.fillMaxWidth().border(AppDimens.Border, AppColors.Outline, shape),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 88.dp)
                .background(AppColors.Surface)
                .padding(AppDimens.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconBadge(section.icon, large = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(section.title, color = AppColors.TextPrimary, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text(section.description, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(painterResource(Res.drawable.rules_chevron_right), contentDescription = null,
                modifier = Modifier.size(24.dp), tint = AppColors.TextPrimary)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun StatisticsPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            StatisticsScreen(goBack = {}, openGamesStatistics = {}, openTurnsStatistics = {},
                openRollsStatistics = {}, openDiceStatistics = {}, openEventsStatistics = {})
        }
    }
}
