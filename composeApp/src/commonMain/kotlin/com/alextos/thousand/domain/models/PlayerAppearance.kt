package com.alextos.thousand.domain.models

/** Persisted names are shared by Room, multiplayer and the offline web scene. */
enum class PlayerAvatar { Neutral, Male, Female }

enum class PlayerColor(val hex: String) {
    Gold("#C6A15B"), Sage("#8DAE96"), Blue("#86A7BD"), Lavender("#AAA0C3"),
    Rose("#C29CA6"), Teal("#7FAEA9"), Terracotta("#BF9B85");

    companion object {
        fun defaultFor(id: String, kind: UserKind): PlayerColor =
            if (kind == UserKind.MainUser) Gold
            else entries.drop(1)[(id.hashCode().toUInt() % (entries.size - 1).toUInt()).toInt()]

    }
}
