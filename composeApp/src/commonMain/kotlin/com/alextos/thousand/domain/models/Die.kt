package com.alextos.thousand.domain.models

data class Die(
    val id: Long = 0,
    val value: DieValue
) {
    override fun toString(): String {
        return value.value.toString()
    }
}