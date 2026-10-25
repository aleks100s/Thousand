package com.alextos.thousand.presentation.menu.game_rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alextos.thousand.common.Screen
import com.alextos.thousand.presentation.components.SingleDieView
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import thousand.composeapp.generated.resources.*

private val RulesCream = Color(0xFFF5F0DF)
private val RulesBody = Color(0xFFCADBE7)
private val RulesGold = Color(0xFFFFC56A)
private val RulesOutline = Color(0xFF087F98)

@Composable
fun GameRulesScreen(onGoBack: () -> Unit) {
    Screen(modifier = Modifier, title = "Правила игры", goBack = onGoBack) { modifier ->
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "contents") {
            GameRulesTableOfContents(state.tableOfContents) { item ->
                coroutineScope.launch { lazyListState.animateScrollToItem(item.itemIndex + 1) }
            }
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
private fun RulesPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.widthIn(max = 760.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Color(0xB300202A)).border(1.5.dp, RulesOutline, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun RulesIcon(icon: DrawableResource, prominent: Boolean = false, compact: Boolean = false) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier.size(if (compact) 34.dp else 42.dp).clip(shape)
            .background(if (prominent) Brush.verticalGradient(listOf(Color(0xFFFFD88E), Color(0xFFFFB851)))
                else Brush.verticalGradient(listOf(Color(0xFF07333F), Color(0xFF01222D))))
            .border(1.dp, if (prominent) Color(0xFFFFDF9D) else Color(0xFF0B5364), shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(if (compact) 24.dp else 26.dp),
            tint = if (prominent) Color(0xFF08212A) else RulesGold)
    }
}

@Composable
private fun RulesHeading(title: String, icon: DrawableResource = title.ruleIcon(), prominent: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RulesIcon(icon, prominent)
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
        RulesHeading("Оглавление", Res.drawable.rules_description, prominent = true)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(Color(0x66001A24)).border(1.dp, Color(0xFF0A5367), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) { onItemClick(item) }.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RulesIcon(item.title.ruleIcon(), compact = true)
                    Text(item.title, modifier = Modifier.weight(1f), color = RulesCream,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Icon(painterResource(Res.drawable.rules_chevron_right), contentDescription = null,
                        modifier = Modifier.size(24.dp), tint = Color(0xFF7EA5B8))
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
private fun DiceCombinationsRulesBlock(item: GameRulesItem.DiceCombinations) {
    RulesPanel {
        RulesHeading(item.title)
        RulesText(item.description)
        item.groups.forEach { DiceCombinationGroup(it) }
    }
}

@Composable
private fun RerollRulesBlock(item: GameRulesItem.RerollRules) {
    RulesPanel {
        RulesHeading(item.title)
        RulesText(item.intro)
        RerollExampleView(item.example)
        item.paragraphs.forEach { RulesText(it) }
    }
}

@Composable
private fun RerollExampleView(example: RerollExample) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x660A3540)).padding(10.dp),
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x660A3540)).padding(10.dp),
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
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x66001E25)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

private fun String.ruleIcon(): DrawableResource = when (this) {
    "Цель игры" -> Res.drawable.rules_target
    "Ход игрока" -> Res.drawable.rules_play_arrow
    "Комбинации кубиков" -> Res.drawable.rules_casino
    "Правила переброса" -> Res.drawable.rules_refresh
    "Стартовый лимит" -> Res.drawable.rules_flag
    "Бочки" -> Res.drawable.rules_database
    "Обгоны" -> Res.drawable.rules_trending_up
    "Болты и штрафы" -> Res.drawable.rules_gavel
    "Яма" -> Res.drawable.rules_trip_origin
    else -> Res.drawable.rules_description
}

private val RerollExampleDieState.borderColor: Color
    get() = when (this) {
        RerollExampleDieState.Scored -> Color(0xFFFF8585)
        RerollExampleDieState.Reroll -> Color(0xFF78DF98)
    }
