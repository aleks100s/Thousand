package com.alextos.thousand.domain.models

data class Player(
    val id: Long,
    val user: User,
    val currentScore: Int,
    val isWinner: Boolean
) {
    override fun toString(): String {
        return user.name
    }
}
