package com.alextos.thousand.presentation.models

import com.alextos.thousand.domain.models.Game
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Game.toUi(): GameUi {
    val winner = players.firstOrNull { it.isWinner }
    return GameUi(
        id = id,
        title = "Игра #$id",
        opponents = if (isFinished()) {
            players.joinToString(separator = " \uD83C\uDD9A ") { "${if (it == winner) "\uD83C\uDFC6 " else ""}${it.user.name}" }
        } else {
            players.joinToString(separator = " \uD83C\uDD9A ") { "${it.user.name}: ${it.currentScore}" }
        },
        finishedAt = "${finishedAt?.formatForUi()} \uD83C\uDFC1",
        isFinished = isFinished(),
        isVirtualDiceEnabled,
        isNotificationEnabled
    )
}

private fun Instant.formatForUi(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    return DateFormatter.format(dateTime)
}

private val DateFormatter = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
}
