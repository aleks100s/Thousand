package com.alextos.thousand.domain.models

data class Player(
    val id: Long = 0,
    val user: User,
    val currentScore: Int = 0,
    val isWinner: Boolean = false
) {
    override fun toString(): String {
        return user.name
    }
}
