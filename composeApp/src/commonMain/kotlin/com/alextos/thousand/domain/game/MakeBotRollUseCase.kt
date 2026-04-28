package com.alextos.thousand.domain.game

import com.alextos.thousand.domain.models.RollAbility
import kotlinx.coroutines.delay

class MakeBotRollUseCase {
    suspend operator fun invoke(
        rollAbility: RollAbility
    ): Boolean {
        delay(1000L)
        return when (rollAbility) {
            RollAbility.REQUIRED -> true
            RollAbility.AVAILABLE_4, RollAbility.AVAILABLE_3 -> true
            RollAbility.AVAILABLE_2, RollAbility.AVAILABLE_1 -> false
            RollAbility.UNAVAILABLE -> false
        }
    }
}