package com.alextos.thousand.presentation.components

import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.AppColors
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alextos.thousand.common.ListScreenContent
import com.alextos.thousand.domain.models.GameSettings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import thousand.composeapp.generated.resources.*

private val SettingsCream = AppColors.TextPrimary
private val SettingsBody = AppColors.TextSecondary
private val SettingsShape = AppShapes.Panel

@Composable
fun GameSettingsView(
    singlePlayer: Boolean = true,
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
    modifier: Modifier = Modifier,
    continueText: String = "",
    isContinuing: Boolean = false,
    onContinue: (() -> Unit)? = null,
) {
    val options = GameSettingOption.entries.filter { option ->
        when (option) {
            GameSettingOption.Shake -> singlePlayer && settings.isVirtualDiceEnabled
            GameSettingOption.Notifications -> singlePlayer
            else -> true
        }
    }
    ListScreenContent(modifier) {
        LazyColumn(
            Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (singlePlayer) {
                item(key = "mode", contentType = "mode") { GameModeCard(settings, onSettingsChange) }
            }
            items(options, key = { it.name }, contentType = { "setting" }) { option ->
                GameSettingsCard(
                    title = option.title, description = option.description, icon = option.icon,
                    checked = option.checked(settings),
                    onCheckedChange = { onSettingsChange(option.update(settings, it)) },
                )
            }
            if (onContinue != null) {
                item(key = "continue", contentType = "action") {
                    AppButton(
                        Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(top = 10.dp),
                        enabled = !isContinuing, onClick = onContinue,
                    ) {
                        if (isContinuing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        else Text(continueText)
                    }
                }
            }
        }
    }
}

@Composable
internal fun GameSettingsCard(
    title: String,
    description: String,
    icon: DrawableResource,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    value: String? = null,
) {
    val action = if (onCheckedChange != null) Modifier.toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange) else Modifier
    Row(
        Modifier.widthIn(max = 760.dp).fillMaxWidth().appPanel()
            .then(action).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIconBadge(icon)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = SettingsCream, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(description, color = SettingsBody, style = MaterialTheme.typography.bodySmall)
        }
        if (onCheckedChange != null) {
            Switch(
                checked = checked, onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, checkedTrackColor = AppColors.Accent,
                    checkedBorderColor = AppColors.Accent, uncheckedThumbColor = AppColors.TextSecondary,
                    uncheckedTrackColor = AppColors.SurfaceMuted, uncheckedBorderColor = AppColors.OutlineSubtle,
                ),
            )
        } else {
            Text(value.orEmpty(), color = if (checked) AppColors.Positive else SettingsBody,
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GameModeCard(settings: GameSettings, onSettingsChange: (GameSettings) -> Unit) {
    Column(
        Modifier.widthIn(max = 760.dp).fillMaxWidth().appPanel().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(painterResource(if (settings.isVirtualDiceEnabled) Res.drawable.sports_esports_24px else GameRuleSymbol.Combinations.icon),
                contentDescription = null, tint = AppColors.Highlight)
            Text(if (settings.isVirtualDiceEnabled) "В игровом режиме вам не понадобятся настоящие кубики - вы сможете играть прямо на телефоне."
                else "Режим ассистента позволяет вести счет реальной игры в приложении",
                color = SettingsBody, style = MaterialTheme.typography.bodySmall)
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf(true, false).forEachIndexed { index, virtual ->
                SegmentedButton(
                    selected = settings.isVirtualDiceEnabled == virtual,
                    onClick = { onSettingsChange(settings.copy(isVirtualDiceEnabled = virtual)) },
                    shape = SegmentedButtonDefaults.itemShape(index, 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = AppColors.Accent, activeContentColor = AppColors.OnAccent,
                        inactiveContainerColor = Color.Transparent, inactiveContentColor = SettingsCream),
                    label = { Text(if (virtual) "Игровой режим" else "Режим ассистента") },
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
internal fun GameSettingsPreview() {
    com.alextos.thousand.application.theme.ThousandTheme {
        com.alextos.thousand.application.theme.AppBackground {
            com.alextos.thousand.common.Screen(Modifier, title = "Настройки игры", goBack = {}) { modifier ->
                GameSettingsView(singlePlayer = false, settings = GameSettings(), onSettingsChange = {}, modifier = modifier,
                    continueText = "Перейти в лобби", onContinue = {})
            }
        }
    }
}
