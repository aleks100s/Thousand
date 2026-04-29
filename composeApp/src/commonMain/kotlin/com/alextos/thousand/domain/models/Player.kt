package com.alextos.thousand.domain.models

data class Player(
    val id: Long = 0,
    val user: User,
    var currentScore: Int = 0,
    var isWinner: Boolean = false,
    var boltCount: Int = 0,
    var hasPassedStartLimit: Boolean = false
) {
    fun isBot(): Boolean = user.kind == UserKind.Bot

    fun isMain(): Boolean = user.kind == UserKind.MainUser

    fun header(): String = if (user.name.count() > 10) {
        "${user.name.substring(0, 10)}…"
    } else {
        user.name
    }

    override fun toString() = user.name
}
