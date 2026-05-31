package com.alextos.thousand.presentation.menu.game_rules

import com.alextos.thousand.domain.models.DieValue

data class GameRulesState(
    val items: List<GameRulesItem> = emptyList(),
    val tableOfContents: List<GameRulesTableOfContentsItem> = emptyList(),
)

data class GameRulesTableOfContentsItem(
    val title: String,
    val itemIndex: Int,
)

sealed interface GameRulesItem {
    data class TextRule(
        val title: String,
        val description: String,
    ) : GameRulesItem

    data class DiceCombinations(
        val title: String,
        val description: String,
        val groups: List<DiceCombinationGroup>,
    ) : GameRulesItem

    data class RerollRules(
        val title: String,
        val intro: String,
        val example: RerollExample,
        val paragraphs: List<String>,
    ) : GameRulesItem
}

data class DiceCombinationGroup(
    val dieValue: DieValue,
    val rows: List<DiceCombinationRow>,
)

data class DiceCombinationRow(
    val dice: List<DieValue>,
    val score: Int,
)

data class RerollExample(
    val title: String,
    val dice: List<RerollExampleDie>,
    val description: String,
)

data class RerollExampleDie(
    val dieValue: DieValue,
    val state: RerollExampleDieState,
)

enum class RerollExampleDieState {
    Scored,
    Reroll,
}
