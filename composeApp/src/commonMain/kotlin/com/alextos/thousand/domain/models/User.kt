package com.alextos.thousand.domain.models

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class User(
    val id: String = Uuid.random().toString(),
    val name: String,
    val kind: UserKind = UserKind.LocalUser,
    val color: PlayerColor = PlayerColor.defaultFor(id, kind),
    val avatar: PlayerAvatar = PlayerAvatar.Neutral
)
