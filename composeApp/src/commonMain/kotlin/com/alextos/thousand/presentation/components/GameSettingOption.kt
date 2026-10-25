package com.alextos.thousand.presentation.components

import com.alextos.thousand.domain.GameConstants.BARREL_1
import com.alextos.thousand.domain.GameConstants.BARREL_2
import com.alextos.thousand.domain.GameConstants.BARREL_3
import com.alextos.thousand.domain.GameConstants.BOLT_FINE
import com.alextos.thousand.domain.GameConstants.OVERTAKE_FINE
import com.alextos.thousand.domain.GameConstants.STARTING_LIMIT
import com.alextos.thousand.domain.models.GameSettings
import org.jetbrains.compose.resources.DrawableResource
import thousand.composeapp.generated.resources.*

enum class GameSettingOption(val title: String, val description: String, val icon: DrawableResource) {
    Shake("Бросать кубики по тряске устройства", "Бросайте виртуальные кубики встряхиванием устройства.", Res.drawable.mobile_vibrate_24px),
    Notifications("Уведомления во время игры", "Показывать игровые сообщения во время партии.", Res.drawable.notifications_24px),
    StartingLimit("Открытие игры с $STARTING_LIMIT очков", "Игрок начинает набирать очки только после хода на $STARTING_LIMIT очков или больше.", GameRuleSymbol.StartingLimit.icon),
    Barrel1("Первая бочка $BARREL_1", "Если счет игрока остается в диапазоне $BARREL_1, ход не засчитывается и игрок получает болт.", GameRuleSymbol.Barrels.icon),
    Barrel2("Вторая бочка $BARREL_2", "Если счет игрока остается в диапазоне $BARREL_2, ход не засчитывается и игрок получает болт.", GameRuleSymbol.Barrels.icon),
    Barrel3("Третья бочка $BARREL_3", "Если счет игрока остается в диапазоне $BARREL_3, ход не засчитывается и игрок получает болт.", GameRuleSymbol.Barrels.icon),
    TripleBolt("Штраф $BOLT_FINE очков за 3 болта", "Если игрок получает третий болт подряд, его счет уменьшается на $BOLT_FINE очков.", GameRuleSymbol.Bolts.icon),
    Overtake("Штраф за обгон $OVERTAKE_FINE очков", "Когда игрок обгоняет соперника по счету, соперник теряет $OVERTAKE_FINE очков.", GameRuleSymbol.Overtake.icon);

    fun checked(settings: GameSettings): Boolean = when (this) {
        Shake -> settings.isShakeEnabled
        Notifications -> settings.isNotificationEnabled
        StartingLimit -> settings.hasStartLimit
        Barrel1 -> settings.isBarrel1Active
        Barrel2 -> settings.isBarrel2Active
        Barrel3 -> settings.isBarrel3Active
        TripleBolt -> settings.isTripleBoltFineActive
        Overtake -> settings.isOvertakeFineActive
    }

    fun update(settings: GameSettings, enabled: Boolean): GameSettings = when (this) {
        Shake -> settings.copy(isShakeEnabled = enabled)
        Notifications -> settings.copy(isNotificationEnabled = enabled)
        StartingLimit -> settings.copy(hasStartLimit = enabled)
        Barrel1 -> settings.copy(isBarrel1Active = enabled)
        Barrel2 -> settings.copy(isBarrel2Active = enabled)
        Barrel3 -> settings.copy(isBarrel3Active = enabled)
        TripleBolt -> settings.copy(isTripleBoltFineActive = enabled)
        Overtake -> settings.copy(isOvertakeFineActive = enabled)
    }
}
