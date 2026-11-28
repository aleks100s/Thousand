package com.alextos.thousand.domain.models

import org.jetbrains.compose.resources.DrawableResource
import thousand.composeapp.generated.resources.Res
import thousand.composeapp.generated.resources.achievement_11111
import thousand.composeapp.generated.resources.achievement_22222
import thousand.composeapp.generated.resources.achievement_33333
import thousand.composeapp.generated.resources.achievement_44444
import thousand.composeapp.generated.resources.achievement_55555
import thousand.composeapp.generated.resources.achievement_66666
import thousand.composeapp.generated.resources.achievement_chain
import thousand.composeapp.generated.resources.achievement_score
import thousand.composeapp.generated.resources.achievement_zero
import thousand.composeapp.generated.resources.achievements_win

enum class AchievementImage(val resource: DrawableResource) {
    Win(Res.drawable.achievements_win),
    Ones(Res.drawable.achievement_11111),
    Twos(Res.drawable.achievement_22222),
    Threes(Res.drawable.achievement_33333),
    Fours(Res.drawable.achievement_44444),
    Fives(Res.drawable.achievement_55555),
    Sixes(Res.drawable.achievement_66666),
    Chain(Res.drawable.achievement_chain),
    Score(Res.drawable.achievement_score),
    Zero(Res.drawable.achievement_zero),
}
