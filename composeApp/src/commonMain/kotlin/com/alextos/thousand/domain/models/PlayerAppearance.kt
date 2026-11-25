package com.alextos.thousand.domain.models

/** Persisted names are shared by Room, multiplayer and the offline web scene. */
enum class PlayerAvatar { Neutral, Male, Female }

enum class PlayerColor(val hex: String) {
    Gold("#C6A15B"),
    Sage("#8DAE96"),
    Blue("#86A7BD"),
    Lavender("#AAA0C3"),
    Rose("#C29CA6"),
    Teal("#7FAEA9"),
    Terracotta("#BF9B85"),
    Coral("#F18B81"),
    Apricot("#F3B37A"),
    Lemon("#E5D76B"),
    Lime("#ACCB72"),
    Mint("#79D5AE"),
    Cyan("#6FCBCC"),
    Sky("#75C7EE"),
    Periwinkle("#9299E8"),
    Orchid("#D490DB");

    companion object {
        // Keep the original order and size for old records that have no saved color.
        private val legacyPalette = listOf(Sage, Blue, Lavender, Rose, Teal, Terracotta)

        fun defaultFor(id: String, kind: UserKind): PlayerColor =
            if (kind == UserKind.MainUser) Gold
            else legacyPalette[(id.hashCode().toUInt() % legacyPalette.size.toUInt()).toInt()]
    }
}
