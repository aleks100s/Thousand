package com.alextos.thousand.presentation.components

import org.jetbrains.compose.resources.DrawableResource
import thousand.composeapp.generated.resources.*

/** The same rule always uses the same Material Symbol in rules and settings. */
enum class GameRuleSymbol(val icon: DrawableResource) {
    Goal(Res.drawable.rules_target),
    PlayerTurn(Res.drawable.rules_play_arrow),
    Combinations(Res.drawable.rules_casino),
    Reroll(Res.drawable.rules_refresh),
    StartingLimit(Res.drawable.rules_flag),
    Barrels(Res.drawable.rules_database),
    Overtake(Res.drawable.rules_trending_up),
    Bolts(Res.drawable.rules_gavel),
    Pit(Res.drawable.rules_trip_origin),
    Contents(Res.drawable.rules_description),
}
