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

    override fun toString(): String {
        return if (user.name.count() > 10) {
            "${user.name.substring(0, 8)}…"
        } else {
            user.name
        }
    }
}
