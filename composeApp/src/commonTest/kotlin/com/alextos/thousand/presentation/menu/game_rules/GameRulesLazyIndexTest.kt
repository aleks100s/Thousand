package com.alextos.thousand.presentation.menu.game_rules

import com.alextos.thousand.domain.usecase.game.CalculateDiceRollScoreUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GameRulesLazyIndexTest {
    @Test
    fun contentsTargetsAccountForExpandedCombinationAndRerollRows() {
        val state = GameRulesViewModel(CalculateDiceRollScoreUseCase()).state.value
        val targets = state.tableOfContents.associate { it.title to it.itemIndex + 1 }
        assertEquals(1, targets["Цель игры"])
        assertEquals(3, targets["Комбинации кубиков"])
        assertEquals(10, targets["Правила переброса"])
        assertEquals(16, targets["Стартовый лимит"])
        assertEquals(20, targets["Яма"])
    }
}
