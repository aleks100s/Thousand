package com.alextos.thousand.data.service

import com.alextos.thousand.domain.service.NativeAuthenticatorDelegate
import com.alextos.thousand.domain.service.NativeAuthenticatorService

class AndroidAuthenticatorService: NativeAuthenticatorService {
    override var delegate: NativeAuthenticatorDelegate? = null
}