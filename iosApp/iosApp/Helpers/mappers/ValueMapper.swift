import ComposeApp
import Foundation

extension IOSMultiplayerManager {
    func instant(from value: Any?) -> KotlinInstant {
        optionalInstant(from: value) ?? KotlinInstant.companion.fromEpochMilliseconds(
            epochMilliseconds: Int64(Date().timeIntervalSince1970 * 1000)
        )
    }

    func optionalInstant(from value: Any?) -> KotlinInstant? {
        if let milliseconds = int64(from: value) {
            return KotlinInstant.companion.fromEpochMilliseconds(epochMilliseconds: milliseconds)
        }

        if let dictionary = value as? [String: Any],
           let epochSeconds = int64(from: dictionary["epochSeconds"]) {
            return KotlinInstant.companion.fromEpochSeconds(
                epochSeconds: epochSeconds,
                nanosecondAdjustment: int32(from: dictionary["nanosecondsOfSecond"]) ?? 0
            )
        }

        if let string = value as? String {
            return KotlinInstant.companion.parseOrNull(input: string)
        }

        return nil
    }

    func int32(from value: Any?) -> Int32? {
        if let value = value as? Int32 {
            return value
        }
        if let value = value as? Int {
            return Int32(value)
        }
        if let value = value as? Int64 {
            return Int32(value)
        }
        if let value = value as? Double {
            return Int32(value)
        }
        if let value = value as? String {
            return Int32(value)
        }
        return nil
    }

    func int64(from value: Any?) -> Int64? {
        if let value = value as? Int64 {
            return value
        }
        if let value = value as? Int {
            return Int64(value)
        }
        if let value = value as? Int32 {
            return Int64(value)
        }
        if let value = value as? Double {
            return Int64(value)
        }
        if let value = value as? String {
            return Int64(value)
        }
        return nil
    }
}
