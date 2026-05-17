package com.alextos.thousand.presentation.other.game_rules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.domain.models.DieValue
import com.alextos.thousand.presentation.game.components.SingleDieView
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameRulesScreen(
    onGoBack: () -> Unit,
) {
    Screen(
        modifier = Modifier,
        title = "Правила игры",
        goBack = onGoBack,
    ) { modifier ->
        GameRulesContent(modifier = modifier.fillMaxSize())
    }
}

@Composable
fun GameRulesContent(
    modifier: Modifier = Modifier,
) {
    val viewModel: GameRulesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GameRulesTableOfContents(
                items = state.tableOfContents,
                onItemClick = { item ->
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(item.itemIndex + 1)
                    }
                },
            )
        }
        items(state.items) { item ->
            when (item) {
                is GameRulesItem.TextRule -> GameRuleItem(item)
                is GameRulesItem.DiceCombinations -> DiceCombinationsRulesBlock(item)
                is GameRulesItem.RerollRules -> RerollRulesBlock(item)
            }
        }
    }
}

@Composable
private fun GameRulesTableOfContents(
    items: List<GameRulesTableOfContentsItem>,
    onItemClick: (GameRulesTableOfContentsItem) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Оглавление",
                style = MaterialTheme.typography.titleMedium,
            )
            items.forEach { item ->
                Text(
                    text = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onItemClick(item)
                        }
                        .padding(vertical = 6.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun GameRuleItem(rule: GameRulesItem.TextRule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = rule.title,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = rule.description,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DiceCombinationsRulesBlock(item: GameRulesItem.DiceCombinations) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            item.groups.forEach { group ->
                DiceCombinationGroup(group)
            }
        }
    }
}

@Composable
private fun RerollRulesBlock(item: GameRulesItem.RerollRules) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = item.intro,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RerollExampleView(item.example)

            item.paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RerollExampleView(example: RerollExample) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = example.title,
            style = MaterialTheme.typography.titleSmall,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            example.dice.forEach { die ->
                RerollExampleDie(
                    dieValue = die.dieValue,
                    borderColor = die.state.borderColor,
                )
            }
        }
        Text(
            text = example.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RerollExampleDie(
    dieValue: DieValue,
    borderColor: Color,
) {
    SingleDieView(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .size(48.dp)
            .border(
                border = BorderStroke(2.dp, borderColor),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(4.dp),
        dieValue = dieValue,
    )
}

@Composable
private fun DiceCombinationGroup(group: DiceCombinationGroup) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Кубик ${group.dieValue.value}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        group.rows.forEach { row ->
            DiceCombinationRow(row)
        }
    }
}

@Composable
private fun DiceCombinationRow(row: DiceCombinationRow) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            row.dice.forEach { dieValue ->
                SingleDieView(
                    modifier = Modifier.size(24.dp),
                    dieValue = dieValue,
                )
            }
        }

        Text(
            text = "${row.score} очков",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private val RerollDieBorderColor = Color(0xFF2E7D32)
private val ScoreDieBorderColor = Color(0xFFC62828)

private val RerollExampleDieState.borderColor: Color
    get() = when (this) {
        RerollExampleDieState.Scored -> ScoreDieBorderColor
        RerollExampleDieState.Reroll -> RerollDieBorderColor
    }
