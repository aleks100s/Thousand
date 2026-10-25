package com.alextos.thousand.presentation.components

import com.alextos.thousand.domain.models.GameSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class GameSettingOptionTest {
    @Test
    fun toggleCopiesSettingsAndChangesOnlySelectedOption() {
        val source = GameSettings(isVirtualDiceEnabled = false, isNotificationEnabled = false, isBarrel2Active = false)
        val original = source.copy()
        GameSettingOption.entries.forEach { option ->
            val result = option.update(source, !option.checked(source))
            assertNotSame(source, result)
            assertEquals(original, source)
            assertEquals(!option.checked(source), option.checked(result))
            assertEquals(source.isVirtualDiceEnabled, result.isVirtualDiceEnabled)
            GameSettingOption.entries.filter { it != option }.forEach { other ->
                assertEquals(other.checked(source), other.checked(result), "$option changed $other")
            }
        }
    }

    @Test
    fun settingsUseExactlyTheSameSymbolsAsRules() {
        assertEquals(GameRuleSymbol.StartingLimit.icon, GameSettingOption.StartingLimit.icon)
        listOf(GameSettingOption.Barrel1, GameSettingOption.Barrel2, GameSettingOption.Barrel3).forEach {
            assertEquals(GameRuleSymbol.Barrels.icon, it.icon)
        }
        assertEquals(GameRuleSymbol.Bolts.icon, GameSettingOption.TripleBolt.icon)
        assertEquals(GameRuleSymbol.Overtake.icon, GameSettingOption.Overtake.icon)
    }
}
