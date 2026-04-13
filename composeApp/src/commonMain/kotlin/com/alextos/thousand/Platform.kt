package com.alextos.thousand

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform