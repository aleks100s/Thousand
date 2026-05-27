import ComposeApp

extension IOSMultiplayerManager {
    func dictionary(from gameSettings: GameSettings) -> [String: Any] {
        [
            "isNotificationEnabled": gameSettings.isNotificationEnabled,
            "isVirtualDiceEnabled": gameSettings.isVirtualDiceEnabled,
            "isShakeEnabled": gameSettings.isShakeEnabled,
            "hasStartLimit": gameSettings.hasStartLimit,
            "isBarrel1Active": gameSettings.isBarrel1Active,
            "isBarrel2Active": gameSettings.isBarrel2Active,
            "isBarrel3Active": gameSettings.isBarrel3Active,
            "isTripleBoltFineActive": gameSettings.isTripleBoltFineActive,
            "isOvertakeFineActive": gameSettings.isOvertakeFineActive
        ]
    }

    func gameSettings(from value: Any?) -> GameSettings {
        guard let dictionary = value as? [String: Any] else {
            return defaultGameSettings()
        }

        return GameSettings(
            isNotificationEnabled: dictionary["isNotificationEnabled"] as? Bool ?? true,
            isVirtualDiceEnabled: dictionary["isVirtualDiceEnabled"] as? Bool ?? true,
            isShakeEnabled: dictionary["isShakeEnabled"] as? Bool ?? true,
            hasStartLimit: dictionary["hasStartLimit"] as? Bool ?? true,
            isBarrel1Active: dictionary["isBarrel1Active"] as? Bool ?? true,
            isBarrel2Active: dictionary["isBarrel2Active"] as? Bool ?? true,
            isBarrel3Active: dictionary["isBarrel3Active"] as? Bool ?? false,
            isTripleBoltFineActive: dictionary["isTripleBoltFineActive"] as? Bool ?? true,
            isOvertakeFineActive: dictionary["isOvertakeFineActive"] as? Bool ?? true
        )
    }

    func defaultGameSettings() -> GameSettings {
        GameSettings(
            isNotificationEnabled: true,
            isVirtualDiceEnabled: true,
            isShakeEnabled: true,
            hasStartLimit: true,
            isBarrel1Active: true,
            isBarrel2Active: true,
            isBarrel3Active: false,
            isTripleBoltFineActive: true,
            isOvertakeFineActive: true
        )
    }
}
