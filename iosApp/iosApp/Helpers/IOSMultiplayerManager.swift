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
        let hostPlayer = UserProfile(
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

    func joinLobby(id: String) async throws {
        guard let currentUser = Auth.auth().currentUser else {
            return
        }

        let reference = Database.database().reference()
            .child("lobbies")
            .child(id)
        
        let lobby: Lobby = try await withCheckedThrowingContinuation { continuation in
            reference.observeSingleEvent(of: .value, with: { [weak self] snapshot in
                guard let lobby = self?.lobby(from: snapshot.value) else {
                    let error = NSError(
                        domain: "IOSMultiplayerManager",
                        code: 1,
                        userInfo: [NSLocalizedDescriptionKey: "Failed to join lobby."]
                    )
                    continuation.resume(throwing: error)
                    return
                }

                let currentPlayer = UserProfile(
                    id: currentUser.uid,
                    name: currentUser.displayName ?? "Без имени"
                )
                guard lobby.players.contains(where: { $0.id == currentPlayer.id }) == false else {
                    let error = NSError(
                        domain: "IOSMultiplayerManager",
                        code: 1,
                        userInfo: [NSLocalizedDescriptionKey: "Failed to join lobby."]
                    )
                    continuation.resume(throwing: error)
                    return
                }

                var players = lobby.players
                players.append(currentPlayer)
                lobby.players = players
                
                continuation.resume(returning: lobby)
            }, withCancel: { error in
                continuation.resume(throwing: error)
            })
        }
        
        try await reference.setValue(dictionary(from: lobby))
    }

    func connectToLobby(id: String) -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameSettingsFlowBridge()
        let reference = Database.database().reference().child("lobbies").child(id)

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self, let lobby = self.lobby(from: snapshot.value) else {
                bridge.closeWithError(message: "Failed to connect")
                return
            }

            bridge.emit(lobby: lobby)
        }, withCancel: { error in
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }

    func disconnectFromLobby(id: String) async throws {
        guard let currentUser = Auth.auth().currentUser else {
            return
        }

        let reference = Database.database().reference()
            .child("lobbies")
            .child(id)
        let snapshot = try await reference.getData()
        let data = snapshot.value as? [String: [String: Any]]
        guard let lobby = lobby(from: data?[id] ?? [:]) else {
            return
        }

        if lobby.host == currentUser.uid {
            try await reference.removeValue()
        } else {
            lobby.players = lobby.players.filter { $0.id != currentUser.uid }
            try await reference.setValue(dictionary(from: lobby))
        }
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
    func currentPlayer() -> UserProfile {
        let currentUser = Auth.auth().currentUser
        return UserProfile(
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

    func players(from value: Any?) -> [UserProfile] {
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

    func player(from dictionary: [String: Any]) -> UserProfile {
        UserProfile(
            id: dictionary["id"] as? String ?? "",
            name: dictionary["name"] as? String ?? "Без имени"
        )
    }
}
