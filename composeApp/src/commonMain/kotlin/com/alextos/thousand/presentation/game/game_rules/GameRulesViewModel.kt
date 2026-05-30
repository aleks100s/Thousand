package com.alextos.thousand.presentation.game.game_rules

import androidx.lifecycle.ViewModel
import com.alextos.thousand.domain.usecase.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRulesViewModel(
    private val calculateDiceRollScoreUseCase: CalculateDiceRollScoreUseCase,
) : ViewModel() {
    private val items = createRulesItems()

    private val _state = MutableStateFlow(
        GameRulesState(
            items = items,
            tableOfContents = createTableOfContents(items),
        )
    )
    val state: StateFlow<GameRulesState> = _state.asStateFlow()

    private fun createTableOfContents(items: List<GameRulesItem>): List<GameRulesTableOfContentsItem> {
        return items.mapIndexed { index, item ->
            GameRulesTableOfContentsItem(
                title = item.title,
                itemIndex = index,
            )
        }
    }

    private fun createRulesItems(): List<GameRulesItem> {
        return listOf(
            GameRulesItem.TextRule(
                title = "Цель игры",
                description = "Игроки по очереди бросают кубики и набирают очки. Побеждает тот, кто первым набирает 1000 очков."
            ),
            GameRulesItem.TextRule(
                title = "Ход игрока",
                description = "Каждый ход начинается с броска сразу 5 кубиков. После того, как кубики упадут, бросок анализируется на наличие очковых комбинаций.\n" +
                        "Очки приносят кубики достоинством 1 (10 очков) и 5 (5 очков), а также 3 и более кубика одного достоинства, выпавшие в текущем броске.\n" +
                        "При этом 3 кубика дают очки по номиналу кубика умноженного на 10 (т.е. три четверки дадут 40 очков, а три единицы 100 (обратите внимание, что 1 во всей игре считается за 10 очков), четыре на 20, пять на 100.\n"
            ),
            GameRulesItem.DiceCombinations(
                title = "Комбинации кубиков",
                description = "Очки считаются отдельно для каждого достоинства кубика в текущем броске.",
                groups = createDiceCombinationGroups(),
            ),
            GameRulesItem.RerollRules(
                title = "Правила переброса",
                intro = "Если бросок принес игроку хотя бы 5 очков (т.е. выпала не \"нулевая\" комбинация), то все кубики, которые принесли очки откладываются, а оставшиеся кубики игрок может бросать повторно.",
                example = createRerollExample(),
                paragraphs = createRerollRuleParagraphs(),
            ),
            GameRulesItem.TextRule(
                title = "Стартовый лимит",
                description = "Если правило включено, очки начинают засчитываться только после прохождения стартового порога в 50 очков.\n" +
                        "После того как игрок выполнил минимальную норму ограничение снимается и он может записывать любое количество очков.",
            ),
            GameRulesItem.TextRule(
                title = "Бочки",
                description = "В игре существует понятие бочек (в классическом варианте существует две бочки: первая с 200 по 300 очков, вторая с 600 до 700 очков). Если игрок \"сидит в бочке\", т.е. имеет сумму очков между границами бочки, то он обязан набрать столько очков, чтобы сразу вылезти на верх бочки.\n" +
                        "К примеру, если игрок имеет 225 очков (т.е. сидит в 1-й бочке), то он обязан набрать не менее 300 (верхняя граница бочки) минус 225 (текущее количество очков), т.е 75 очков.\n" +
                        "Самое плохое, что можно сделать, сесть на самое дно бочки и ждать когда выпадет много очков. Значительно проще преодолеть бочку в два прыжка, при этом первый нужно сделать как можно дальше (в идеале за центр бочки).\n"
            ),
            GameRulesItem.TextRule(
                title = "Обгоны",
                description = "Если один из игроков \"обгоняет\" другого, т.е. до хода он имел меньшую сумму очков, а после хода большую (но не равную, при которой обгон не засчитывается!), то с игрока, которого он обгоняет списывается 50 очков.\n" +
                        "Если игрок \"обгоняет\" сразу нескольких игроков, то очки списываются с каждого из них."
            ),
            GameRulesItem.TextRule(
                title = "Болты и штрафы",
                description = "Обычно за запись \"нулевой\" суммы очков, т.е. если игрок не набирает за ход ничего, он получает болт. Болты опасны тем, что при накоплении их определенного количества, с игрока списывается оговоренная сумма очков (в классическом варианте 100).\n" +
                        "В виду того, что когда игрок находится на \"в бочке\" или еще \"не открыл игру\" он ограничен в возможности записывать мелкие суммы очков, болты в таких ситуациях обычно не засчитываются. Иногда этого и не делают, но тогда игра может неоправданно затянуться.",
            ),
            GameRulesItem.TextRule(
                title = "Яма",
                description = "В игре существует яма. Если игрок набирает 555 очков (сам или \"с помощью\" других игроков (обгон, болты), то он попадает на \"яму\" и с него списываются все очки (т.е. его счет становиться нулевым). После самосвала игрок не должен будет открывать игру, но от этого ему, думаю, будет не легче…",
            ),
        )
    }

    private fun createDiceCombinationGroups(): List<DiceCombinationGroup> {
        return DieValue.entries.map { dieValue ->
            DiceCombinationGroup(
                dieValue = dieValue,
                rows = (1..5).map { count ->
                    val dice = List(count) { dieValue }
                    DiceCombinationRow(
                        dice = dice,
                        score = calculateDiceRollScoreUseCase(
                            dice.map { Die(value = it) }
                        ).score,
                    )
                },
            )
        }
    }

    private fun createRerollExample(): RerollExample {
        return RerollExample(
            title = "Пример: пятерка принесла очки, остальные 4 кубика можно перебросить.",
            dice = listOf(
                RerollExampleDie(
                    dieValue = DieValue.FIVE,
                    state = RerollExampleDieState.Scored,
                ),
                RerollExampleDie(
                    dieValue = DieValue.TWO,
                    state = RerollExampleDieState.Reroll,
                ),
                RerollExampleDie(
                    dieValue = DieValue.THREE,
                    state = RerollExampleDieState.Reroll,
                ),
                RerollExampleDie(
                    dieValue = DieValue.FOUR,
                    state = RerollExampleDieState.Reroll,
                ),
                RerollExampleDie(
                    dieValue = DieValue.SIX,
                    state = RerollExampleDieState.Reroll,
                ),
            ),
            description = "Зеленая рамка - можно перебросить. Красная рамка - кубик уже принес очки.",
        )
    }

    private fun createRerollRuleParagraphs(): List<String> {
        return listOf(
            "Новый бросок, таким образом, осуществляется с меньшим количеством кубиков, а значит имеет меньше шансов принести очки. При этом очки за новые выпавшие комбинации суммируются с ранее набранными.",
            "Обратите внимание, что если все пять кубиков участвовали в комбинации (за один или несколько последовательных бросков), то к броску становятся доступны снова все пять кубиков, т.е. игрок, теоретически, может за один бросок набрать любое количество очков. При этом бросок нужно будет делать обязательно!",
            "Если при очередном броске выпадает \"нулевая\" комбинация, то игрок получает 0 очков и ему вписывается \"болт\" (подробнее читайте дальше) при этом все набранные очки за текущий ход полностью \"сгорают\" (очки набранные ранее и записанные в таблицу, естественно, остаются), а ход автоматически переходит к следующему игроку.",
            "Чтобы избежать этого игрок должен сказать \"хватит\", он набрал достаточное количество очков. Правда это иногда не дозволяется, так как если игрок еще не \"открыл игру\" или \"сидит в бочке\", то у него есть ограничения на минимальную сумму очков, которые он обязан набрать (подробнее читаем дальше).",
        )
    }

    private val GameRulesItem.title: String
        get() = when (this) {
            is GameRulesItem.TextRule -> title
            is GameRulesItem.DiceCombinations -> title
            is GameRulesItem.RerollRules -> title
        }
}
