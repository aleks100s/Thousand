package com.alextos.thousand.data.mappers.firebase

import kotlin.time.Instant

internal fun Any?.asFirebaseMap(): Map<*, *>? =
    this as? Map<*, *>

internal fun Any?.asFirebaseMapList(): List<Map<*, *>> =
    when (this) {
        is List<*> -> mapNotNull { item -> item.asFirebaseMap() }
        is Map<*, *> -> entries
            .sortedWith(compareBy<Map.Entry<*, *>> { entry -> entry.key.toString().toIntOrNull() ?: Int.MAX_VALUE }
                .thenBy { entry -> entry.key.toString() })
            .mapNotNull { entry -> entry.value.asFirebaseMap() }
        else -> emptyList()
    }

internal fun Map<*, *>?.boolean(key: String, defaultValue: Boolean): Boolean =
    this?.get(key).asBoolean() ?: defaultValue

internal fun Map<*, *>?.int(key: String): Int? =
    this?.get(key).asInt()

internal fun Map<*, *>?.long(key: String): Long? =
    this?.get(key).asLong()

internal fun Map<*, *>?.string(key: String): String? =
    this?.get(key) as? String

private fun Any?.asBoolean(): Boolean? =
    when (this) {
        is Boolean -> this
        is Int -> toBooleanOrNull()
        is Long -> toInt().toBooleanOrNull()
        is Short -> toInt().toBooleanOrNull()
        is Byte -> toInt().toBooleanOrNull()
        is Double -> toInt().takeIf { value -> value.toDouble() == this }?.toBooleanOrNull()
        is Float -> toInt().takeIf { value -> value.toFloat() == this }?.toBooleanOrNull()
        is String -> when (lowercase()) {
            "true" -> true
            "false" -> false
            "1" -> true
            "0" -> false
            else -> null
        }
        else -> null
    }

private fun Int.toBooleanOrNull(): Boolean? =
    when (this) {
        1 -> true
        0 -> false
        else -> null
    }

private fun Any?.asInt(): Int? =
    when (this) {
        is Int -> this
        is Long -> toInt()
        is Short -> toInt()
        is Byte -> toInt()
        is Double -> toInt()
        is Float -> toInt()
        is String -> toIntOrNull()
        else -> null
    }

private fun Any?.asLong(): Long? =
    when (this) {
        is Long -> this
        is Int -> toLong()
        is Short -> toLong()
        is Byte -> toLong()
        is Double -> toLong()
        is Float -> toLong()
        is String -> toLongOrNull()
        else -> null
    }
