//
//  IOSMultiplayerManager.swift
//  iosApp
//
//  Created by Codex on 23.05.2026.
//

import ComposeApp
import FirebaseAuth
import FirebaseDatabase
import Foundation

final class IOSMultiplayerManager: MultiplayerManager {
    func createLobby(gameSettings: GameSettings) async throws -> String {
        let gameID = String(Int.random(in: 1000...10000))
        let currentUser = Auth.auth().currentUser
        let host = currentUser?.uid ?? ""
        let hostPlayer = currentPlayer()
        let lobby = Lobby(
            settings: gameSettings,
            players: [hostPlayer],
            host: host,
            id: gameID,
            game: ""
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
                guard let lobby = self?.lobby(from: snapshot.firebaseDictionary) else {
                    let error = NSError(
                        domain: "IOSMultiplayerManager",
                        code: 1,
                        userInfo: [NSLocalizedDescriptionKey: "Failed to join lobby."]
                    )
                    continuation.resume(throwing: error)
                    return
                }

                
                guard let currentPlayer = self?.currentPlayer(), lobby.players.contains(where: { $0.id == currentUser.uid }) == false else {
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
            guard let self, let lobby = self.lobby(from: snapshot.firebaseDictionary) else {
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
                      let lobby = self.lobby(
                          from: childSnapshot.firebaseDictionary,
                          fallbackId: childSnapshot.key
                      ),
                      lobby.players.contains(where: { $0.id == currentUserId }) else {
                    return nil
                }

                return lobby
            }

            bridge.emit(lobbies: lobbies)
        }, withCancel: { error in
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }
    
    func startGame(id: String) async throws {
        let lobbyReference = Database.database().reference()
            .child("lobbies")
            .child(id)

        let snapshot = try await lobbyReference.getData()
        let data = snapshot.value as? [String: [String: Any]]
        guard let lobby = lobby(from: data?[id] ?? [:]) else {
            throw NSError(
                domain: "IOSMultiplayerManager",
                code: 1,
                userInfo: [NSLocalizedDescriptionKey: "Failed to start game."]
            )
        }

        let gameID = UUID().uuidString
        let game = Game(
            id: Int64(id) ?? 0,
            startedAt: KotlinInstant.companion.fromEpochMilliseconds(
                epochMilliseconds: Int64(Date().timeIntervalSince1970 * 1000)
            ),
            finishedAt: nil,
            settings: lobby.settings,
            players: lobby.players.shuffled().map { user in
                Player(
                    id: 0,
                    user: user,
                    currentScore: 0,
                    isWinner: false,
                    boltCount: 0,
                    hasPassedStartLimit: false
                )
            }
        )

        try await Database.database().reference()
            .child("games")
            .child(gameID)
            .setValue(dictionary(from: game))

        try await lobbyReference.child("game").setValue(gameID)
        try await lobbyReference.removeValue()
    }
    
    func userGames() -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameListFlowBridge()
        let currentUserId = Auth.auth().currentUser?.uid ?? ""
        let reference = Database.database().reference().child("games")

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self else {
                return
            }
            guard !currentUserId.isEmpty else {
                bridge.emit(games: [])
                return
            }

            let games = snapshot.children.allObjects.compactMap { child -> Game? in
                guard let childSnapshot = child as? DataSnapshot,
                      let game = self.game(
                          from: childSnapshot.firebaseDictionary,
                          fallbackId: childSnapshot.key
                      ),
                      game.players.contains(where: { $0.user.id == currentUserId }) else {
                    return nil
                }

                return game
            }

            bridge.emit(games: games)
        }, withCancel: { error in
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }
}
