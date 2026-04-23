//
//  DefaultDeviceShakeObserver.swift
//  iosApp
//
//  Created by Alexander on 23.04.2026.
//

import ComposeApp

final class DefaultShakeDeviceObserver: ShakeDeviceObserver {
    var delegate: ShakeDeviceObserverDelegate?

    func shake() {
        delegate?.deviceDidShake()
    }
}
