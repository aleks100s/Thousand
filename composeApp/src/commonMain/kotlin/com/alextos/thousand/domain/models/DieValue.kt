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
            return ordinal + 1
        }
}