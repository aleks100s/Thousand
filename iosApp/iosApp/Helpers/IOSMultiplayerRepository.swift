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
        let currentUser = Auth.auth().currentUser
        let hostPlayer = GameSettings.Player(
            id: currentUser?.uid ?? "",
            name: currentUser?.displayName ?? "Без имени"
        )

        gameSettings.host = currentUser?.uid
        gameSettings.players = [hostPlayer]

        try await Database.database().reference().child("lobbies").child(gameID)
            .setValue(dictionary(from: gameSettings))
        return gameID
    }

    func connectToLobby(id: String) -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameSettingsFlowBridge()
        let reference = Database.database().reference().child("lobbies").child(id)

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self, let gameSettings = self.gameSettings(from: snapshot.value) else {
                return
            }

            let currentPlayer = self.currentPlayer()
            guard !currentPlayer.id.isEmpty else {
                bridge.emit(lobby: self.lobby(from: gameSettings))
                return
            }

            if gameSettings.players.contains(where: { $0.id == currentPlayer.id }) {
                gameSettings.players.first(where: { $0.id == currentPlayer.id })?.name = currentPlayer.name
                bridge.emit(lobby: self.lobby(from: gameSettings))
                return
            }

            var players = gameSettings.players
            players.append(currentPlayer)
            gameSettings.players = players

            reference.setValue(self.dictionary(from: gameSettings)) { error, _ in
                guard error == nil else {
                    return
                }
                bridge.emit(lobby: self.lobby(from: gameSettings))
            }
        })

        return bridge.flow
    }
}

private extension IOSMultiplayerRepository {
    func lobby(from gameSettings: GameSettings) -> Lobby {
        Lobby(
            settings: gameSettings,
            isCurrentPlayerHost: Auth.auth().currentUser?.uid == gameSettings.host
        )
    }

    func currentPlayer() -> GameSettings.Player {
        let currentUser = Auth.auth().currentUser
        return GameSettings.Player(
            id: currentUser?.uid ?? "",
            name: currentUser?.displayName ?? "Без имени"
        )
    }

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
            "isOvertakeFineActive": gameSettings.isOvertakeFineActive,
            "host": gameSettings.host ?? "",
            "players": gameSettings.players.map {
                [
                    "id": $0.id,
                    "name": $0.name
                ]
            }
        ]
    }

    func gameSettings(from value: Any?) -> GameSettings? {
        guard let dictionary = value as? [String: Any] else {
            return nil
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
            isOvertakeFineActive: dictionary["isOvertakeFineActive"] as? Bool ?? true,
            host: dictionary["host"] as? String,
            players: players(from: dictionary["players"])
        )
    }

    func players(from value: Any?) -> [GameSettings.Player] {
        if let players = value as? [[String: Any]] {
            return players.map(player(from:))
        }

        if let players = value as? [Any] {
            return players.compactMap { $0 as? [String: Any] }.map(player(from:))
        }

        if let players = value as? [String: [String: Any]] {
            return players.values.map(player(from:))
        }

        return []
    }

    func player(from dictionary: [String: Any]) -> GameSettings.Player {
        GameSettings.Player(
            id: dictionary["id"] as? String ?? "",
            name: dictionary["name"] as? String ?? "Без имени"
        )
    }
}
