package com.alextos.thousand.presentation.game.play_game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import com.alextos.thousand.domain.models.Game

@Composable
fun GameRulesView(
    game: Game,
    isNotificationEnabled: Boolean,
    onNotificationEnabledChange: (Boolean) -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Настройки игры",
            style = MaterialTheme.typography.titleLarge,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SwitchRow(
                title = "Уведомления",
                checked = isNotificationEnabled,
                onCheckedChange = onNotificationEnabledChange,
            )
            RuleRow(
                title = "Открытие игры",
                value = if (game.hasStartLimit) "$STARTING_LIMIT очков" else "Отключено",
            )
            RuleRow(
                title = "Первая бочка",
                value = if (game.isBarrel1Active) "$BARREL_1" else "Отключено",
            )
            RuleRow(
                title = "Вторая бочка",
                value = if (game.isBarrel2Active) "$BARREL_2" else "Отключено",
            )
            RuleRow(
                title = "Третья бочка",
                value = if (game.isBarrel3Active) "$BARREL_3" else "Отключено",
            )
            RuleRow(
                title = "Штраф за 3 болта",
                value = if (game.isTripleBoltFineActive) "$BOLT_FINE" else "Отключено",
            )
            RuleRow(
                title = "Штраф за обгон",
                value = if (game.isOvertakeFineActive) "$OVERTAKE_FINE" else "Отключено",
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun RuleRow(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value)
    }
}
