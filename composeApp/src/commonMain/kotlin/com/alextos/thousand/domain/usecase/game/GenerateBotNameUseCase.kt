package com.alextos.thousand.domain.usecase.game

import kotlin.random.Random

class GenerateBotNameUseCase {
    operator fun invoke(): String {
        val template = nameTemplates.random()
        return "${template.adjective} ${template.noun}"
    }

    private data class BotNameTemplate(
        val noun: String,
        val adjective: String,
    )

    private companion object {
        val nameTemplates = listOf(
            BotNameTemplate(noun = "Пельмень", adjective = "Бесстрашный"),
            BotNameTemplate(noun = "Кактус", adjective = "Гламурный"),
            BotNameTemplate(noun = "Бобр", adjective = "Реактивный"),
            BotNameTemplate(noun = "Носок", adjective = "Великий"),
            BotNameTemplate(noun = "Арбуз", adjective = "Загадочный"),
            BotNameTemplate(noun = "Хомяк", adjective = "Легендарный"),
            BotNameTemplate(noun = "Чайник", adjective = "Агрессивный"),
            BotNameTemplate(noun = "Огурец", adjective = "Космический"),
            BotNameTemplate(noun = "Дракон", adjective = "Ленивый"),
            BotNameTemplate(noun = "Пингвин", adjective = "Хаотичный"),
            BotNameTemplate(noun = "Кабачок", adjective = "Неукротимый"),
            BotNameTemplate(noun = "Енот", adjective = "Деловой"),
            BotNameTemplate(noun = "Батон", adjective = "Эпический"),
            BotNameTemplate(noun = "Кубик", adjective = "Турбо"),
            BotNameTemplate(noun = "Тостер", adjective = "Всемогущий"),
            BotNameTemplate(noun = "Пончик", adjective = "Свирепый"),
            BotNameTemplate(noun = "Игрок", adjective = "Квантовый"),
            BotNameTemplate(noun = "Жираф", adjective = "Секретный"),
            BotNameTemplate(noun = "Сырок", adjective = "Бессмертный"),
            BotNameTemplate(noun = "Тапок", adjective = "Героический"),
        )
    }

    private fun List<BotNameTemplate>.random(): BotNameTemplate {
        return this[Random.nextInt(size)]
    }
}
