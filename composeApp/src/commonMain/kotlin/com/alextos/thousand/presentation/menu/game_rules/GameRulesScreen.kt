package com.alextos.thousand.presentation.menu.game_rules

import com.alextos.thousand.presentation.components.AppIconBadge
import com.alextos.thousand.presentation.components.AppPanel
import com.alextos.thousand.application.theme.AppShapes
import com.alextos.thousand.application.theme.AppColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.SingleDieView
import com.alextos.thousand.presentation.components.PanelSegment
import com.alextos.thousand.presentation.components.GameRuleSymbol
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.*

private val RulesCream = AppColors.TextPrimary
private val RulesBody = AppColors.TextSecondary
private val RulesGold = AppColors.Highlight
private val RulesOutline = AppColors.Outline

@Composable
fun GameRulesScreen(onGoBack: () -> Unit) {
    Screen(modifier = Modifier, constrainListWidth = true, title = "Правила игры", goBack = onGoBack) { modifier ->
        GameRulesContent(modifier.fillMaxSize())
    }
}

@Composable
fun GameRulesContent(modifier: Modifier = Modifier) {
    val viewModel: GameRulesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    GameRulesList(modifier, state)
}

@Composable
internal fun GameRulesList(modifier: Modifier = Modifier, state: GameRulesState) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "contents", contentType = "contents") {
            Column {
                GameRulesTableOfContents(state.tableOfContents) { item ->
                    coroutineScope.launch { lazyListState.animateScrollToItem(item.itemIndex + 1) }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        state.items.forEach { section ->
            when (section) {
                is GameRulesItem.TextRule -> item(key = "rule-${section.title}", contentType = "text-rule") {
                    Column { GameRuleItem(section); Spacer(Modifier.height(12.dp)) }
                }
                is GameRulesItem.DiceCombinations -> {
                    item(key = "rule-${section.title}", contentType = "rule-intro") {
                        PanelSegment(first = true, last = section.groups.isEmpty(), padding = 12.dp, gap = 12.dp) {
                            RulesHeading(section.title)
                            RulesText(section.description)
                        }
                        if (section.groups.isEmpty()) Spacer(Modifier.height(12.dp))
                    }
                    items(count = section.groups.size,
                        key = { "rule-${section.title}-group-${section.groups[it].dieValue}" },
                        contentType = { "combination-group" },
                    ) { index ->
                        val last = index == section.groups.lastIndex
                        Column {
                            PanelSegment(first = false, last = last, padding = 12.dp, gap = 12.dp) {
                                DiceCombinationGroup(section.groups[index])
                            }
                            if (last) Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                is GameRulesItem.RerollRules -> {
                    item(key = "rule-${section.title}", contentType = "rule-intro") {
                        PanelSegment(first = true, last = false, padding = 12.dp, gap = 12.dp) {
                            RulesHeading(section.title)
                            RulesText(section.intro)
                        }
                    }
                    item(key = "rule-${section.title}-example", contentType = "reroll-example") {
                        Column {
                            PanelSegment(first = false, last = section.paragraphs.isEmpty(), padding = 12.dp, gap = 12.dp) {
                                RerollExampleView(section.example)
                            }
                            if (section.paragraphs.isEmpty()) Spacer(Modifier.height(12.dp))
                        }
                    }
                    items(count = section.paragraphs.size,
                        key = { "rule-${section.title}-paragraph-$it" }, contentType = { "rule-paragraph" },
                    ) { index ->
                        val last = index == section.paragraphs.lastIndex
                        Column {
                            PanelSegment(first = false, last = last, padding = 12.dp, gap = 12.dp) { RulesText(section.paragraphs[index]) }
                            if (last) Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RulesPanel(content: @Composable ColumnScope.() -> Unit) {
    AppPanel(Modifier.widthIn(max = 760.dp), content = content)
}

@Composable
private fun RulesIcon(icon: DrawableResource, compact: Boolean = false) {
    AppIconBadge(icon, compact = compact)
}

@Composable
private fun RulesHeading(title: String, icon: DrawableResource = title.ruleIcon()) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RulesIcon(icon)
        Text(title, modifier = Modifier.weight(1f), color = RulesCream,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RulesText(text: String) {
    Text(text, color = RulesBody, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
}

@Composable
private fun GameRulesTableOfContents(items: List<GameRulesTableOfContentsItem>, onItemClick: (GameRulesTableOfContentsItem) -> Unit) {
    RulesPanel {
        RulesHeading("Оглавление", GameRuleSymbol.Contents.icon)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().clip(AppShapes.Row)
                        .background(AppColors.SurfaceInset).border(1.dp, AppColors.Outline, AppShapes.Row)
                        .clickable(role = Role.Button) { onItemClick(item) }.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RulesIcon(item.title.ruleIcon(), compact = true)
                    Text(item.title, modifier = Modifier.weight(1f), color = RulesCream,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Icon(painterResource(Res.drawable.rules_chevron_right), contentDescription = null,
                        modifier = Modifier.size(24.dp), tint = AppColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun GameRuleItem(rule: GameRulesItem.TextRule) {
    RulesPanel {
        RulesHeading(rule.title)
        RulesText(rule.description)
    }
}

@Composable
private fun RerollExampleView(example: RerollExample) {
    Column(
        Modifier.fillMaxWidth().clip(AppShapes.Row).background(AppColors.SurfaceInset).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(example.title, color = RulesCream, style = MaterialTheme.typography.titleSmall)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val dieSize = ((maxWidth - 24.dp) / 5).coerceAtMost(48.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                example.dice.forEach { die ->
                    SingleDieView(
                        modifier = Modifier.size(dieSize).border(2.dp, die.state.borderColor, RoundedCornerShape(25)).padding(4.dp),
                        dieValue = die.dieValue,
                    )
                }
            }
        }
        Text(example.description, color = RulesBody, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DiceCombinationGroup(group: DiceCombinationGroup) {
    Column(
        Modifier.fillMaxWidth().clip(AppShapes.Row).background(AppColors.SurfaceInset).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Кубик ${group.dieValue.value}", color = RulesGold,
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        group.rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    row.dice.forEach { dieValue -> SingleDieView(Modifier.size(24.dp), dieValue) }
                }
                Text("${row.score} очков", color = RulesCream, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(AppShapes.Inset).background(AppColors.SurfaceInset).padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

private fun String.ruleIcon(): DrawableResource = when (this) {
    "Цель игры" -> GameRuleSymbol.Goal.icon
    "Ход игрока" -> GameRuleSymbol.PlayerTurn.icon
    "Комбинации кубиков" -> GameRuleSymbol.Combinations.icon
    "Правила переброса" -> GameRuleSymbol.Reroll.icon
    "Стартовый лимит" -> GameRuleSymbol.StartingLimit.icon
    "Бочки" -> GameRuleSymbol.Barrels.icon
    "Обгоны" -> GameRuleSymbol.Overtake.icon
    "Болты и штрафы" -> GameRuleSymbol.Bolts.icon
    "Яма" -> GameRuleSymbol.Pit.icon
    else -> GameRuleSymbol.Contents.icon
}

private val RerollExampleDieState.borderColor: Color
    get() = when (this) {
        RerollExampleDieState.Scored -> AppColors.Scoring
        RerollExampleDieState.Reroll -> AppColors.Danger
    }
