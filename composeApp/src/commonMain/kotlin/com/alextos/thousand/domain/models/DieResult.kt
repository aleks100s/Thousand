package com.alextos.thousand.domain.models

data class DieResult(
    val id: Int,
    val playerID: Int,
    val rollID: Int,
    val order: Int,
    val die: Die
)
