package com.alextos.thousand.presentation.menu.achievements

import androidx.compose.runtime.Composable
import com.alextos.thousand.domain.models.AchievementId
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.achievement_multiplayer_win_description
import thousand.composeapp.generated.resources.achievement_multiplayer_win_title
import thousand.composeapp.generated.resources.achievement_bot_win_description
import thousand.composeapp.generated.resources.achievement_bot_win_title
import thousand.composeapp.generated.resources.achievement_win_within_ten_turns_description
import thousand.composeapp.generated.resources.achievement_win_within_ten_turns_title
import thousand.composeapp.generated.resources.achievement_win_on_first_turn_description
import thousand.composeapp.generated.resources.achievement_win_on_first_turn_title
import thousand.composeapp.generated.resources.achievement_turn_score_200_description
import thousand.composeapp.generated.resources.achievement_turn_score_200_title
import thousand.composeapp.generated.resources.achievement_turn_score_300_description
import thousand.composeapp.generated.resources.achievement_turn_score_300_title
import thousand.composeapp.generated.resources.achievement_turn_score_400_description
import thousand.composeapp.generated.resources.achievement_turn_score_400_title
import thousand.composeapp.generated.resources.achievement_turn_score_500_description
import thousand.composeapp.generated.resources.achievement_turn_score_500_title
import thousand.composeapp.generated.resources.achievement_turn_score_1000_description
import thousand.composeapp.generated.resources.achievement_turn_score_1000_title
import thousand.composeapp.generated.resources.achievement_game_score_minus_1000_description
import thousand.composeapp.generated.resources.achievement_game_score_minus_1000_title
import thousand.composeapp.generated.resources.achievement_game_score_2000_description
import thousand.composeapp.generated.resources.achievement_game_score_2000_title
import thousand.composeapp.generated.resources.achievement_win_after_pit_description
import thousand.composeapp.generated.resources.achievement_win_after_pit_title
import thousand.composeapp.generated.resources.achievement_two_pit_falls_description
import thousand.composeapp.generated.resources.achievement_two_pit_falls_title
import thousand.composeapp.generated.resources.achievement_roll_chain_5_description
import thousand.composeapp.generated.resources.achievement_roll_chain_5_title
import thousand.composeapp.generated.resources.achievement_roll_chain_10_description
import thousand.composeapp.generated.resources.achievement_roll_chain_10_title
import thousand.composeapp.generated.resources.achievement_finish_without_zero_description
import thousand.composeapp.generated.resources.achievement_finish_without_zero_title
import thousand.composeapp.generated.resources.achievement_five_twos_description
import thousand.composeapp.generated.resources.achievement_five_twos_title
import thousand.composeapp.generated.resources.achievement_five_threes_description
import thousand.composeapp.generated.resources.achievement_five_threes_title
import thousand.composeapp.generated.resources.achievement_five_fours_description
import thousand.composeapp.generated.resources.achievement_five_fours_title
import thousand.composeapp.generated.resources.achievement_five_fives_description
import thousand.composeapp.generated.resources.achievement_five_fives_title
import thousand.composeapp.generated.resources.achievement_five_sixes_description
import thousand.composeapp.generated.resources.achievement_five_sixes_title
import thousand.composeapp.generated.resources.achievement_five_ones_description
import thousand.composeapp.generated.resources.achievement_five_ones_title

enum class AchievementVisibility {
    Public,
    Hidden,
}

data class AchievementDefinition(
    val id: AchievementId,
    val title: StringResource,
    val description: StringResource,
    val visibility: AchievementVisibility,
)

object AchievementCatalog {
    val all = listOf(
        AchievementDefinition(
            AchievementId.MultiplayerWin,
            Res.string.achievement_multiplayer_win_title,
            Res.string.achievement_multiplayer_win_description,
            AchievementVisibility.Hidden,
        ),
        AchievementDefinition(
            AchievementId.BotWin,
            Res.string.achievement_bot_win_title,
            Res.string.achievement_bot_win_description,
            AchievementVisibility.Hidden,
        ),
        AchievementDefinition(
            AchievementId.WinWithinTenTurns,
            Res.string.achievement_win_within_ten_turns_title,
            Res.string.achievement_win_within_ten_turns_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.WinOnFirstTurn,
            Res.string.achievement_win_on_first_turn_title,
            Res.string.achievement_win_on_first_turn_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.TurnScore200,
            Res.string.achievement_turn_score_200_title,
            Res.string.achievement_turn_score_200_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.TurnScore300,
            Res.string.achievement_turn_score_300_title,
            Res.string.achievement_turn_score_300_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.TurnScore400,
            Res.string.achievement_turn_score_400_title,
            Res.string.achievement_turn_score_400_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.TurnScore500,
            Res.string.achievement_turn_score_500_title,
            Res.string.achievement_turn_score_500_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.TurnScore1000,
            Res.string.achievement_turn_score_1000_title,
            Res.string.achievement_turn_score_1000_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.GameScoreMinus1000,
            Res.string.achievement_game_score_minus_1000_title,
            Res.string.achievement_game_score_minus_1000_description,
            AchievementVisibility.Hidden,
        ),
        AchievementDefinition(
            AchievementId.GameScore2000,
            Res.string.achievement_game_score_2000_title,
            Res.string.achievement_game_score_2000_description,
            AchievementVisibility.Hidden,
        ),
        AchievementDefinition(
            AchievementId.WinAfterPit,
            Res.string.achievement_win_after_pit_title,
            Res.string.achievement_win_after_pit_description,
            AchievementVisibility.Hidden,
        ),
        AchievementDefinition(
            AchievementId.TwoPitFalls,
            Res.string.achievement_two_pit_falls_title,
            Res.string.achievement_two_pit_falls_description,
            AchievementVisibility.Hidden,
        ),
        AchievementDefinition(
            AchievementId.RollChain5,
            Res.string.achievement_roll_chain_5_title,
            Res.string.achievement_roll_chain_5_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.RollChain10,
            Res.string.achievement_roll_chain_10_title,
            Res.string.achievement_roll_chain_10_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.FinishWithoutZero,
            Res.string.achievement_finish_without_zero_title,
            Res.string.achievement_finish_without_zero_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.FiveTwos,
            Res.string.achievement_five_twos_title,
            Res.string.achievement_five_twos_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.FiveThrees,
            Res.string.achievement_five_threes_title,
            Res.string.achievement_five_threes_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.FiveFours,
            Res.string.achievement_five_fours_title,
            Res.string.achievement_five_fours_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.FiveFives,
            Res.string.achievement_five_fives_title,
            Res.string.achievement_five_fives_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.FiveSixes,
            Res.string.achievement_five_sixes_title,
            Res.string.achievement_five_sixes_description,
            AchievementVisibility.Public,
        ),
        AchievementDefinition(
            AchievementId.FiveOnes,
            Res.string.achievement_five_ones_title,
            Res.string.achievement_five_ones_description,
            AchievementVisibility.Public,
        ),
    )

    @Composable
    fun title(id: AchievementId): String =
        stringResource(all.first { it.id == id }.title)
}
