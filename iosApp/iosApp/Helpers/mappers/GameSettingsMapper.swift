import ComposeApp

extension IOSMultiplayerManager {
    func dictionary(from gameSettings: GameSettings) -> [String: Any] {
        swiftDictionary(from: FirebaseGameSettingsMapper.shared.dictionary(from: gameSettings))
    }

    func gameSettings(from value: Any?) -> GameSettings {
        FirebaseGameSettingsMapper.shared.gameSettings(from: firebaseDictionary(from: value))
    }

    func defaultGameSettings() -> GameSettings {
        FirebaseGameSettingsMapper.shared.gameSettings(from: nil)
    }
}
