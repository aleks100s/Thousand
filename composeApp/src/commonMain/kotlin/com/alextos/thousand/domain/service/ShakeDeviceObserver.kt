package com.alextos.thousand.domain.service

interface ShakeDeviceObserver {
    var delegate: ShakeDeviceObserverDelegate?

    fun shake()
}