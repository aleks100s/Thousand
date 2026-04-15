package com.alextos.thousand.domain.models

data class Die(
    val id: Long,
    val order: Int,
    val value: DieValue
)