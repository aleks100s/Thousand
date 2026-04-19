package com.alextos.thousand.data.service

import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.service.ShakeDeviceObserverDelegate

class ShakeDeviceObserverImpl: ShakeDeviceObserver {
    override var delegate: ShakeDeviceObserverDelegate? = null

    override fun shake() {
        delegate?.deviceDidShake()
    }
}