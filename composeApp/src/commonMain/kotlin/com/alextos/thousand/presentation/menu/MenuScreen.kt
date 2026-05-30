package com.alextos.thousand.presentation.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alextos.thousand.common.Screen

@Composable
fun MenuScreen(
    goBack: () -> Unit,
    openRules: () -> Unit,
    openTutorial: () -> Unit,
    openStatistics: () -> Unit,
    openUsers: () -> Unit,
) {
    Screen(
        modifier = Modifier,
        title = "Меню",
        goBack = goBack
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OtherTile(
                title = "Правила",
                description = "Комбинации, бочки, болты и остальные правила игры",
                onClick = openRules,
            )
            OtherTile(
                title = "Обучение",
                description = "Тестовая партия с подсказками для новых игроков",
                onClick = openTutorial,
            )
            OtherTile(
                title = "Статистика",
                description = "Игры, ходы, броски и кубики",
                onClick = openStatistics,
            )
            OtherTile(
                title = "Пользователи",
                description = "Список локальных игроков и ботов",
                onClick = openUsers,
            )
        }
    }
}

@Composable
private fun OtherTile(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
