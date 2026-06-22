package com.alextos.thousand.domain.service

interface GamePresenceObserverDelegate {
    fun userDidLeaveGame()
    fun userDidReturnToGame()
}
