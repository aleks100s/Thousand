//
//  IOSMultiplayerManager.swift
//  iosApp
//
//  Created by Codex on 23.05.2026.
//

import ComposeApp
import FirebaseAuth
import FirebaseDatabase

final class IOSMultiplayerManager: MultiplayerManager {
    func createLobby(gameSettings: GameSettings) async throws -> String {
        let gameID = String(Int.random(in: 1000...10000))
        let currentUser = Auth.auth().currentUser
        let host = currentUser?.uid ?? ""
        let hostPlayer = Lobby.Player(
            id: currentUser?.uid ?? "",
            name: currentUser?.displayName ?? "Без имени"
        )
        let lobby = Lobby(
            settings: gameSettings,
            players: [hostPlayer],
            host: host,
            id: gameID
        )

        try await Database.database().reference().child("lobbies").child(gameID)
            .setValue(dictionary(from: lobby))
        return gameID
    }

    func connectToLobby(id: String) -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameSettingsFlowBridge()
        let reference = Database.database().reference().child("lobbies").child(id)

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self, let lobby = self.lobby(from: snapshot.value) else {
                return
            }

            let currentPlayer = self.currentPlayer()
            guard !currentPlayer.id.isEmpty else {
                bridge.emit(lobby: lobby)
                return
            }

            if lobby.players.contains(where: { $0.id == currentPlayer.id }) {
                lobby.players.first(where: { $0.id == currentPlayer.id })?.name = currentPlayer.name
                bridge.emit(lobby: lobby)
                return
            }

            var players = lobby.players
            players.append(currentPlayer)
            lobby.players = players

            reference.setValue(self.dictionary(from: lobby)) { error, _ in
                if let error {
                    bridge.closeWithError(message: error.localizedDescription)
                    return
                }
                bridge.emit(lobby: lobby)
            }
        }, withCancel: { error in
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }
    
    func userLobbies() -> any Kotlinx_coroutines_coreFlow {
        let bridge = LobbyListFlowBridge()
        let currentUserId = Auth.auth().currentUser?.uid ?? ""
        let reference = Database.database().reference().child("lobbies")

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self else {
                return
            }
            guard !currentUserId.isEmpty else {
                bridge.emit(lobbies: [])
                return
            }

            let lobbies = snapshot.children.allObjects.compactMap { child -> Lobby? in
                guard let childSnapshot = child as? DataSnapshot,
                      let lobby = self.lobby(from: childSnapshot.value),
                      lobby.players.contains(where: { $0.id == currentUserId }) else {
                    return nil
                }

                if lobby.id.isEmpty {
                    lobby.id = childSnapshot.key
                }

                return lobby
            }

            bridge.emit(lobbies: lobbies)
        }, withCancel: { error in
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }
}

private extension IOSMultiplayerManager {
    func currentPlayer() -> Lobby.Player {
        let currentUser = Auth.auth().currentUser
        return Lobby.Player(
            id: currentUser?.uid ?? "",
            name: currentUser?.displayName ?? "Без имени"
        )
    }

    func dictionary(from lobby: Lobby) -> [String: Any] {
        [
            "settings": dictionary(from: lobby.settings),
            "players": lobby.players.map {
                [
                    "id": $0.id,
                    "name": $0.name
                ]
            },
            "host": lobby.host,
            "id": lobby.id
        ]
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
            "isOvertakeFineActive": gameSettings.isOvertakeFineActive
        ]
    }

    func lobby(from value: Any?) -> Lobby? {
        guard let dictionary = value as? [String: Any] else {
            return nil
        }

        return Lobby(
            settings: gameSettings(from: dictionary["settings"]),
            players: players(from: dictionary["players"]),
            host: dictionary["host"] as? String ?? "",
            id: dictionary["id"] as? String ?? ""
        )
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

    func players(from value: Any?) -> [Lobby.Player] {
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

    func player(from dictionary: [String: Any]) -> Lobby.Player {
        Lobby.Player(
            id: dictionary["id"] as? String ?? "",
            name: dictionary["name"] as? String ?? "Без имени"
        )
    }
}
