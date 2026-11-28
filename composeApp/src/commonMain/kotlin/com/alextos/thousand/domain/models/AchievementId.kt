package com.alextos.thousand.domain.models

enum class AchievementId(val storageId: String, val image: AchievementImage) {
    MultiplayerWin("multiplayer_win", AchievementImage.Win),
    BotWin("bot_win", AchievementImage.Win),
    WinWithinTenTurns("win_within_ten_turns", AchievementImage.Win),
    WinOnFirstTurn("win_on_first_turn", AchievementImage.Win),
    TurnScore200("turn_score_200", AchievementImage.Score),
    TurnScore300("turn_score_300", AchievementImage.Score),
    TurnScore400("turn_score_400", AchievementImage.Score),
    TurnScore500("turn_score_500", AchievementImage.Score),
    TurnScore1000("turn_score_1000", AchievementImage.Score),
    GameScoreMinus1000("game_score_minus_1000", AchievementImage.Score),
    GameScore2000("game_score_2000", AchievementImage.Score),
    WinAfterPit("win_after_pit", AchievementImage.Win),
    TwoPitFalls("two_pit_falls", AchievementImage.Score),
    RollChain5("roll_chain_5", AchievementImage.Chain),
    RollChain10("roll_chain_10", AchievementImage.Chain),
    FinishWithoutZero("finish_without_zero", AchievementImage.Zero),
    FiveTwos("five_twos", AchievementImage.Twos),
    FiveThrees("five_threes", AchievementImage.Threes),
    FiveFours("five_fours", AchievementImage.Fours),
    FiveFives("five_fives", AchievementImage.Fives),
    FiveSixes("five_sixes", AchievementImage.Sixes),
    FiveOnes("five_ones", AchievementImage.Ones),
    ;

    companion object {
        fun fromStorageId(value: String): AchievementId? =
            entries.firstOrNull { it.storageId == value }
    }
}
