package com.alextos.thousand.domain.models

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class UserReaction(
    val id: String = Uuid.random().toString(),
    val authorId: String = "",
    val author: String,
    val reaction: String,
)
