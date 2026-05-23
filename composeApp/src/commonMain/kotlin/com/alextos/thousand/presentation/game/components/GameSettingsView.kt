package com.alextos.thousand.presentation.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.casino_24px
import thousand.composeapp.generated.resources.info_24px
import thousand.composeapp.generated.resources.mobile_hand_24px
import thousand.composeapp.generated.resources.mobile_vibrate_24px
import thousand.composeapp.generated.resources.notifications_24px
import thousand.composeapp.generated.resources.notifications_off_24px
import thousand.composeapp.generated.resources.sports_esports_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingsView(
    singlePlayer: Boolean = true,
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
) {
    var settingInfo by remember { mutableStateOf<GameSettingInfo?>(null) }

    fun update(block: GameSettings.() -> Unit) {
        onSettingsChange(settings.copy().apply(block))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (singlePlayer) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (settings.isVirtualDiceEnabled) {
                            Res.drawable.sports_esports_24px
                        } else {
                            Res.drawable.casino_24px
                        },
                    ),
                    contentDescription = null,
                )

                val text = if (settings.isVirtualDiceEnabled) {
                    "В игровом режиме вам не понадобятся настоящие кубики - вы сможете играть прямо на телефоне."
                } else {
                    "Режим ассистента позволяет вести счет реальной игры в приложении"
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                listOf(true, false).forEachIndexed { index, isEnabled ->
                    SegmentedButton(
                        selected = settings.isVirtualDiceEnabled == isEnabled,
                        onClick = {
                            update { isVirtualDiceEnabled = isEnabled }
                        },
                        label = {
                            Text(if (isEnabled) "Игровой режим" else "Режим ассистента")
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = 2,
                        ),
                    )
                }
            }

            AnimatedVisibility(settings.isVirtualDiceEnabled) {
                GameSettingsItemView(
                    text = "Бросать кубики по тряске устройства",
                    resource = if (settings.isShakeEnabled) Res.drawable.mobile_vibrate_24px else Res.drawable.mobile_hand_24px,
                    checked = settings.isShakeEnabled,
                    onCheckedChange = {
                        update { isShakeEnabled = it }
                    },
                )
            }

            GameSettingsItemView(
                text = "Уведомления во время игры",
                resource = if (settings.isNotificationEnabled) Res.drawable.notifications_24px else Res.drawable.notifications_off_24px,
                checked = settings.isNotificationEnabled,
                onCheckedChange = {
                    update { isNotificationEnabled = it }
                },
            )
        }

        GameSettingsItemView(
            text = "Открытие игры с $STARTING_LIMIT очков",
            resource = Res.drawable.info_24px,
            checked = settings.hasStartLimit,
            onCheckedChange = {
                update { hasStartLimit = it }
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Открытие игры",
                    description = "Игрок начинает набирать очки только после хода на $STARTING_LIMIT очков или больше.",
                )
            },
        )

        GameSettingsItemView(
            text = "Первая бочка $BARREL_1",
            resource = Res.drawable.info_24px,
            checked = settings.isBarrel1Active,
            onCheckedChange = {
                update { isBarrel1Active = it }
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Первая бочка",
                    description = "Если счет игрока остается в диапазоне $BARREL_1, ход не засчитывается и игрок получает болт.",
                )
            },
        )

        GameSettingsItemView(
            text = "Вторая бочка $BARREL_2",
            resource = Res.drawable.info_24px,
            checked = settings.isBarrel2Active,
            onCheckedChange = {
                update { isBarrel2Active = it }
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Вторая бочка",
                    description = "Если счет игрока остается в диапазоне $BARREL_2, ход не засчитывается и игрок получает болт.",
                )
            },
        )

        GameSettingsItemView(
            text = "Третья бочка $BARREL_3",
            resource = Res.drawable.info_24px,
            checked = settings.isBarrel3Active,
            onCheckedChange = {
                update { isBarrel3Active = it }
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Третья бочка",
                    description = "Если счет игрока остается в диапазоне $BARREL_3, ход не засчитывается и игрок получает болт.",
                )
            },
        )

        GameSettingsItemView(
            text = "Штраф $BOLT_FINE очков за 3 болта",
            resource = Res.drawable.info_24px,
            checked = settings.isTripleBoltFineActive,
            onCheckedChange = {
                update { isTripleBoltFineActive = it }
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Штраф за 3 болта",
                    description = "Если игрок получает третий болт подряд, его счет уменьшается на $BOLT_FINE очков.",
                )
            },
        )

        GameSettingsItemView(
            text = "Штраф за обгон $OVERTAKE_FINE очков",
            resource = Res.drawable.info_24px,
            checked = settings.isOvertakeFineActive,
            onCheckedChange = {
                update { isOvertakeFineActive = it }
            },
            onInfoClick = {
                settingInfo = GameSettingInfo(
                    title = "Штраф за обгон",
                    description = "Когда игрок обгоняет соперника по счету, соперник теряет $OVERTAKE_FINE очков.",
                )
            },
        )
    }

    settingInfo?.let { info ->
        ModalBottomSheet(
            onDismissRequest = {
                settingInfo = null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GameSettingsItemView(
    text: String,
    resource: DrawableResource,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onInfoClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingContent = {
            if (onInfoClick == null) {
                Icon(
                    painter = painterResource(resource),
                    contentDescription = text,
                )
            } else {
                Icon(
                    painter = painterResource(resource),
                    contentDescription = "Подробнее: $text",
                    modifier = Modifier.clickable(onClick = onInfoClick),
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(checked.not())
            },
    )
}

private data class GameSettingInfo(
    val title: String,
    val description: String,
)
