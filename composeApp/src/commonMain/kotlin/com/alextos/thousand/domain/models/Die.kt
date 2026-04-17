package com.alextos.thousand.domain.models

data class Die(
    val id: Long = 0,
    val order: Int,
    val value: DieValue
)