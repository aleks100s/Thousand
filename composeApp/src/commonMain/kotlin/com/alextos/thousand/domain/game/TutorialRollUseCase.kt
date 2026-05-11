package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue

class TutorialRollUseCase {
    private var index = 0

    operator fun invoke(): TutorialRoll {
        val roll = tutorialRolls.getOrNull(index) ?: TutorialRoll.Empty
        index += 1
        return roll
    }

    fun reset() {
        index = 0
    }

    private companion object {
        val tutorialRolls = listOf(
            tutorialRoll(
                DieValue.FIVE,
                DieValue.TWO,
                DieValue.THREE,
                DieValue.FOUR,
                DieValue.SIX,
                nextAction = TutorialNextAction.Reroll,
                advice = "Пока набрано меньше 50 очков, стартовый лимит не пройден. Лучше перебросить 4 кубика."
            ),
            tutorialRoll(
                DieValue.TWO,
                DieValue.THREE,
                DieValue.FOUR,
                DieValue.SIX,
                nextAction = TutorialNextAction.FinishTurn,
                advice = "Выпала нулевая комбинация: очки за ход сгорели, остается только закончить ход."
            ),
            tutorialRoll(
                DieValue.FIVE,
                DieValue.FIVE,
                DieValue.FIVE,
                DieValue.TWO,
                DieValue.THREE,
                nextAction = TutorialNextAction.FinishTurn
            ),
            tutorialRoll(
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                DieValue.TWO,
                DieValue.THREE,
                nextAction = TutorialNextAction.FinishTurn,
                advice = "100 очков уже открывают игру и позволяют обогнать бота. Два кубика рискованны, лучше записать очки."
            ),
            tutorialRoll(
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                DieValue.TWO,
                nextAction = TutorialNextAction.FinishTurn
            ),
            tutorialRoll(
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                DieValue.TWO,
                nextAction = TutorialNextAction.FinishTurn,
                advice = "Один кубик скорее всего не принесет очки. Лучше закончить ход: так ты обгонишь бота и попадешь в бочку."
            ),
            tutorialRoll(
                DieValue.FIVE,
                DieValue.FIVE,
                DieValue.FIVE,
                DieValue.TWO,
                DieValue.THREE,
                nextAction = TutorialNextAction.FinishTurn
            ),
            tutorialRoll(
                DieValue.TWO,
                DieValue.TWO,
                DieValue.TWO,
                DieValue.FOUR,
                DieValue.SIX,
                nextAction = TutorialNextAction.Reroll,
                advice = "Ты остаешься в бочке 200-299. Закончить ход нельзя, нужно перебросить оставшиеся 2 кубика."
            ),
            tutorialRoll(
                DieValue.THREE,
                DieValue.FOUR,
                nextAction = TutorialNextAction.FinishTurn,
                advice = "Нулевая комбинация сожгла ход. Так выглядит невыход из бочки и первый болт подряд."
            ),
            tutorialRoll(
                DieValue.TWO,
                DieValue.TWO,
                DieValue.TWO,
                DieValue.FOUR,
                DieValue.SIX,
                nextAction = TutorialNextAction.Reroll
            ),
            tutorialRoll(
                DieValue.THREE,
                DieValue.FOUR,
                nextAction = TutorialNextAction.FinishTurn
            ),
            tutorialRoll(
                DieValue.TWO,
                DieValue.THREE,
                DieValue.FOUR,
                DieValue.SIX,
                DieValue.TWO,
                nextAction = TutorialNextAction.FinishTurn,
                advice = "Это второй пустой ход подряд. Очков нет, поэтому нужно завершить ход и получить второй болт."
            ),
            tutorialRoll(
                DieValue.THREE,
                DieValue.THREE,
                DieValue.THREE,
                DieValue.THREE,
                DieValue.THREE,
                nextAction = TutorialNextAction.Reroll
            ),
            tutorialRoll(
                DieValue.FIVE,
                DieValue.TWO,
                DieValue.THREE,
                DieValue.FOUR,
                DieValue.SIX,
                nextAction = TutorialNextAction.FinishTurn
            ),
            tutorialRoll(
                DieValue.TWO,
                DieValue.THREE,
                DieValue.FOUR,
                DieValue.SIX,
                DieValue.TWO,
                nextAction = TutorialNextAction.FinishTurn,
                advice = "Это третий пустой ход подряд: после завершения хода будет штраф -100 очков."
            ),
            tutorialRoll(
                DieValue.FIVE,
                DieValue.FIVE,
                DieValue.FIVE,
                DieValue.TWO,
                DieValue.THREE,
                nextAction = TutorialNextAction.FinishTurn
            ),
            tutorialRoll(
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                DieValue.ONE,
                nextAction = TutorialNextAction.Reroll,
                advice = "Все 5 кубиков дали очки. По правилам они снова становятся доступны, поэтому нужно перебросить."
            ),
            tutorialRoll(
                DieValue.FIVE,
                DieValue.TWO,
                DieValue.THREE,
                DieValue.FOUR,
                DieValue.SIX,
                nextAction = TutorialNextAction.FinishTurn,
                advice = "Победные очки уже набраны. 4 кубика можно перебросить, но лучше не рисковать и закончить игру."
            ),
        )

        fun tutorialRoll(
            vararg dice: DieValue,
            nextAction: TutorialNextAction,
            advice: String? = null,
        ): TutorialRoll {
            return TutorialRoll(
                dice = dice.map { value ->
                    Die(value = value)
                },
                nextAction = nextAction,
                advice = advice,
            )
        }
    }
}

data class TutorialRoll(
    val dice: List<Die>,
    val nextAction: TutorialNextAction?,
    val advice: String?,
) {
    companion object {
        val Empty = TutorialRoll(
            dice = emptyList(),
            nextAction = null,
            advice = null,
        )
    }
}

enum class TutorialNextAction {
    Reroll,
    FinishTurn,
}
