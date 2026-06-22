package com.alextos.thousand.domain.service

interface GamePresenceObserver {
    var delegate: GamePresenceObserverDelegate?

    fun notifyUserLeftGame()
    fun notifyUserReturnedToGame()
}
