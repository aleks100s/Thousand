package com.alextos.thousand.domain.models

data class Player(
    val id: Long = 0,
    val user: User,
    var currentScore: Int = 0,
    var isWinner: Boolean = false
) {
    override fun toString(): String {
        return user.name
    }
}
