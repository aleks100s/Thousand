package com.alextos.thousand.domain.models

enum class AchievementId(val storageId: String) {
    FirstWin("first_win");

    companion object {
        fun fromStorageId(value: String): AchievementId? =
            entries.firstOrNull { it.storageId == value }
    }
}
