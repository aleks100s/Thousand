package com.alextos.thousand.presentation.other

import kotlinx.serialization.Serializable

sealed interface OtherRoute {
    @Serializable
    data object Other : OtherRoute

    @Serializable
    data object Rules : OtherRoute

    @Serializable
    data object Tutorial : OtherRoute

    @Serializable
    data object Statistics : OtherRoute

    @Serializable
    data object Users : OtherRoute
}
