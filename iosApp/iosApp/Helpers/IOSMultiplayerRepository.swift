//
//  IOSMultiplayerRepository.swift
//  iosApp
//
//  Created by Codex on 23.05.2026.
//

import ComposeApp
import FirebaseAuth
import FirebaseDatabase
import Foundation

final class IOSMultiplayerRepository: MultiplayerRepository {
    func createLobby(gameSettings: GameSettings) async throws -> String {
        let gameID = String(Int.random(in: 1000...10000))
        let currentUser = Auth.auth().currentUser
        let host = currentUser?.uid ?? ""
        let hostPlayer = currentPlayer()
        let key = UUID().uuidString
        let lobby = Lobby(
            id: gameID,
            key: key,
            settings: gameSettings,
            players: [hostPlayer],
            host: host,
            game: ""
        )

        try await Database.database().reference()
            .child(FirebasePath.lobbies)
            .child(key)
            .setValue(dictionary(from: lobby))
        return key
    }

    func joinLobby(id: String) async throws -> String {
        guard let currentUser = Auth.auth().currentUser else {
            return ""
        }

        let lobbies = Database.database().reference()
            .child(FirebasePath.lobbies)
        let query = lobbies
            .queryOrdered(byChild: FirebaseLobbyKey.id)
            .queryEqual(toValue: id)
        
        let lobby: (key: String, lobby: Lobby)? = try await withCheckedThrowingContinuation { continuation in
            query.observeSingleEvent(of: .value, with: { [weak self] snapshot in
                guard let childSnapshot = snapshot.children.allObjects.first as? DataSnapshot,
                      let lobby = self?.lobby(from: childSnapshot.firebaseDictionary, key: childSnapshot.key) else {
                    let error = NSError(
                        domain: "IOSMultiplayerRepository",
                        code: 1,
                        userInfo: [NSLocalizedDescriptionKey: "Failed to join lobby."]
                    )
                    continuation.resume(throwing: error)
                    return
                }

                let key = childSnapshot.key
                
                guard let currentPlayer = self?.currentPlayer(), lobby.players.contains(where: { $0.id == currentUser.uid }) == false else {
                    continuation.resume(returning: nil)
                    return
                }

                var players = lobby.players
                players.append(currentPlayer)
                lobby.players = players
                
                continuation.resume(returning: (key, lobby))
            }, withCancel: { error in
                continuation.resume(throwing: error)
            })
        }
        
        guard let lobby else {
            return ""
        }
        
        try await lobbies
            .child(lobby.key)
            .setValue(dictionary(from: lobby.lobby))
        return lobby.key
    }

    func connectToLobby(key: String) -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameSettingsFlowBridge()
        let reference = Database.database().reference().child(FirebasePath.lobbies).child(key)

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self, let lobby = self.lobby(from: snapshot.firebaseDictionary, key: snapshot.key) else {
                bridge.closeWithError(message: "Failed to connect")
                return
            }

            bridge.emit(lobby: lobby)
        }, withCancel: { error in
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }

    func disconnectFromLobby(key: String) async throws {
        guard let currentUser = Auth.auth().currentUser else {
            return
        }

        let reference = Database.database().reference()
            .child(FirebasePath.lobbies)
            .child(key)
        let snapshot = try await reference.getData()
        let data = snapshot.value as? [String: [String: Any]]
        guard let lobby = lobby(from: data?[key] ?? [:], key: key) else {
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
        let reference = Database.database().reference().child(FirebasePath.lobbies)

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
                      let lobby = self.lobby(from: childSnapshot.firebaseDictionary, key: childSnapshot.key),
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
    
    func startGame(key: String) async throws {
        let lobbyReference = Database.database().reference()
            .child(FirebasePath.lobbies)
            .child(key)

        let snapshot = try await lobbyReference.getData()
        let data = snapshot.value as? [String: [String: Any]]
        guard let lobby = lobby(from: data?[key] ?? [:], key: key) else {
            throw NSError(
                domain: "IOSMultiplayerRepository",
                code: 1,
                userInfo: [NSLocalizedDescriptionKey: "Failed to start game."]
            )
        }

        let gameID = UUID().uuidString
        let players = lobby.players.shuffled().enumerated().map { index, user in
            Player(
                id: Int64(index),
                user: user,
                currentScore: 0,
                isWinner: false,
                boltCount: 0,
                hasPassedStartLimit: false
            )
        }
        let game = RemoteGame(
            id: Int64(lobby.id) ?? 0,
            settings: lobby.settings,
            players: players,
            host: Auth.auth().currentUser?.uid ?? "",
            key: gameID,
            currentPlayerIndex: 0,
            currentTurn: [],
            currentRoll: nil,
            rollAbility: RollAbility.required,
            buttons: [GameButton.rollTheDice],
            messagesToShow: []
        )

        try await Database.database().reference()
            .child(FirebasePath.games)
            .child(gameID)
            .setValue(dictionary(from: game))

        try await lobbyReference.child(FirebasePath.game).setValue(gameID)
        try await lobbyReference.removeValue()
    }
    
    func userGames() -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameListFlowBridge()
        let currentUserId = Auth.auth().currentUser?.uid ?? ""
        let reference = Database.database().reference().child(FirebasePath.games)

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self else {
                return
            }
            guard !currentUserId.isEmpty else {
                bridge.emit(games: [])
                return
            }

            let games = snapshot.children.allObjects.compactMap { child -> RemoteGame? in
                guard let childSnapshot = child as? DataSnapshot,
                      let game = self.game(from: childSnapshot.firebaseDictionary, key: childSnapshot.key),
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
    
    func observeGame(key: String) -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameFlowBridge()
        guard Auth.auth().currentUser != nil else {
            return bridge.flow
        }

        let reference = Database.database().reference()
            .child(FirebasePath.games)
            .child(key)

        reference.observe(.value, with: { [weak self] snapshot in
            guard let self, let game = self.game(from: snapshot.firebaseDictionary, key: snapshot.key) else {
                bridge.closeWithError(message: "Failed to fetch game")
                return
            }

            bridge.emit(game: game)
        }, withCancel: { error in
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }
    
    func updateGame(game: RemoteGame) async throws {
        let reference = Database.database().reference()
        try await reference
            .child(FirebasePath.games)
            .child(game.key)
            .setValue(dictionary(from: game))
        
        if game.isFinished() {
            let updates = finishedGameStatisticsUpdates(for: game)
            if !updates.isEmpty {
                try await reference.updateChildValues(updates)
            }
        }
    }
    
    func deleteGame(key: String) async throws {
        try await Database.database().reference()
            .child(FirebasePath.games)
            .child(key)
            .removeValue()
    }

    func userInfo(userId: String) async throws -> RemoteUserInfo? {
        guard !userId.isEmpty else {
            return nil
        }

        let snapshot = try await Database.database().reference()
            .child(FirebasePath.users)
            .child(userId)
            .getData()

        return remoteUserInfo(from: snapshot.firebaseDictionary, userId: userId)
    }
}
