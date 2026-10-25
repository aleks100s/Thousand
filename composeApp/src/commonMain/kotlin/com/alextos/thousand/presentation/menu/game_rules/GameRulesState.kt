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
    val title: String
    data class TextRule(
        override val title: String,
        val description: String,
    ) : GameRulesItem

    data class DiceCombinations(
        override val title: String,
        val description: String,
        val groups: List<DiceCombinationGroup>,
    ) : GameRulesItem

    data class RerollRules(
        override val title: String,
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

/** Number of independently composed rows contributed by this rules section. */
internal val GameRulesItem.lazyItemCount: Int
    get() = when (this) {
        is GameRulesItem.TextRule -> 1
        is GameRulesItem.DiceCombinations -> 1 + groups.size
        is GameRulesItem.RerollRules -> 2 + paragraphs.size
    }

internal fun rulesTableOfContents(items: List<GameRulesItem>): List<GameRulesTableOfContentsItem> {
    var offset = 0
    return items.map { item ->
        GameRulesTableOfContentsItem(item.title, offset).also { offset += item.lazyItemCount }
    }
}
