package com.alextos.thousand.domain.models

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** The latest completed turn, or turn zero at the start of a new round. */
data class RemoteAchievementTurn(
    val roundId: String,
    val number: Int,
    val nextPlayerIndex: Int,
    val hasCompleteNumbering: Boolean = true,
    val playerId: String? = null,
    val total: Int = 0,
    val pitPlayerIds: Set<String> = emptySet(),
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        internal fun newRound() = RemoteAchievementTurn(
            roundId = Uuid.random().toString(),
            number = 0,
            nextPlayerIndex = 0,
        )
    }
}
