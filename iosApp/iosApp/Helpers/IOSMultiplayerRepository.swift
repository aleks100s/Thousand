//
//  IOSMultiplayerRepository.swift
//  iosApp
//
//  Created by Codex on 23.05.2026.
//

import ComposeApp
import FirebaseAuth
import FirebaseDatabase

final class IOSMultiplayerRepository: MultiplayerRepository {
    func createLobby(gameSettings: GameSettings) async throws -> String {
        let gameID = String(Int.random(in: 1000...10000))
        try await Database.database().reference().child("games").child(gameID)
            .setValue([
                "isNotificationEnabled": gameSettings.isNotificationEnabled,
                "isVirtualDiceEnabled": gameSettings.isVirtualDiceEnabled,
                "isShakeEnabled": gameSettings.isShakeEnabled,
                "hasStartLimit": gameSettings.hasStartLimit,
                "isBarrel1Active": gameSettings.isBarrel1Active,
                "isBarrel2Active": gameSettings.isBarrel2Active,
                "isBarrel3Active": gameSettings.isBarrel3Active,
                "isTripleBoltFineActive": gameSettings.isTripleBoltFineActive,
                "isOvertakeFineActive": gameSettings.isOvertakeFineActive,
                "host": Auth.auth().currentUser?.uid ?? ""
            ])
        return gameID
    }
}
