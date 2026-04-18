package com.alextos.thousand.domain.models

enum class RollAbility {
    UNAVAILABLE,
    AVAILABLE_1,
    AVAILABLE_2,
    AVAILABLE_3,
    AVAILABLE_4,
    REQUIRED;

    val count: Int
        get() = ordinal
}