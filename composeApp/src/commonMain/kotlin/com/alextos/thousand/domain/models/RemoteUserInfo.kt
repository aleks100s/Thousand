package com.alextos.thousand.domain.models

data class RemoteUserInfo(
    val id: String,
    val name: String,
    val platform: String,
    val gameCount: Int = 0,
    val winCount: Int = 0,
    var rating: Int = 0,
)
