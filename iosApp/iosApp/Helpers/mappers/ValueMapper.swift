import ComposeApp
import FirebaseDatabase
import Foundation

extension DataSnapshot {
    var firebaseDictionary: [String: Any]? {
        normalizedFirebaseDictionary(from: value)
    }
}

extension IOSMultiplayerManager {
    func firebaseDictionary(from value: Any?) -> [String: Any]? {
        normalizedFirebaseDictionary(from: value)
    }

    func swiftDictionary(from value: [AnyHashable: Any]) -> [String: Any] {
        normalizedFirebaseDictionary(from: value) ?? [:]
    }

    func firebaseValue(from value: Any) -> Any {
        normalizedFirebaseValue(from: value)
    }
}

private func normalizedFirebaseDictionary(from value: Any?) -> [String: Any]? {
    if let dictionary = value as? [String: Any] {
        return dictionary.mapValues { normalizedFirebaseValue(from: $0) }
    }

    if let dictionary = value as? [AnyHashable: Any] {
        return Dictionary(
            uniqueKeysWithValues: dictionary.compactMap { key, value in
                guard let key = key as? String else {
                    return nil
                }

                return (key, normalizedFirebaseValue(from: value))
            }
        )
    }

    return nil
}

private func normalizedFirebaseValue(from value: Any) -> Any {
    if let boolean = value as? KotlinBoolean {
        return boolean.boolValue
    }

    if let number = value as? NSNumber,
       CFGetTypeID(number) == CFBooleanGetTypeID() {
        return number.boolValue
    }

    if let dictionary = normalizedFirebaseDictionary(from: value) {
        return dictionary
    }

    if let array = value as? [Any] {
        return array.map { normalizedFirebaseValue(from: $0) }
    }

    return value
}
