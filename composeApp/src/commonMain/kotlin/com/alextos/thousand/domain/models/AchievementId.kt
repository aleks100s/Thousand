package com.alextos.thousand.domain.models

enum class AchievementId(val storageId: String, val image: AchievementImage) {
    FirstWin("first_win", AchievementImage.Win);

    companion object {
        fun fromStorageId(value: String): AchievementId? =
            entries.firstOrNull { it.storageId == value }
    }
}
