package com.alextos.thousand.presentation.menu.play_game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.GameSettings
import com.alextos.thousand.presentation.components.GameSettingOption
import com.alextos.thousand.presentation.components.GameSettingsCard

@Composable
fun GameSettingsSheet(
    settings: GameSettings,
    isNotificationEnabled: Boolean,
    onNotificationEnabledChange: (Boolean) -> Unit,
) {
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item(key = "title", contentType = "title") { Text("Настройки игры", style = MaterialTheme.typography.titleLarge) }
        item(key = "notifications", contentType = "setting") {
            val option = GameSettingOption.Notifications
            GameSettingsCard("Уведомления", option.description, option.icon, isNotificationEnabled, onNotificationEnabledChange)
        }
        items(GameSettingOption.entries.drop(2), key = { it.name }, contentType = { "rule" }) { option ->
            GameSettingsCard(option.title, option.description, option.icon, option.checked(settings),
                value = if (option.checked(settings)) "Включено" else "Отключено")
        }
    }
}
