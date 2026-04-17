package com.alextos.thousand.domain.models

enum class DieValue {
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX;

    val value: Int
        get() {
            return when (this) {
                DieValue.ONE -> 10
                else -> ordinal + 1
            }
        }
}