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
    private func databaseOperation<T>(_ operation: String, _ perform: () async throws -> T) async throws -> T {
        do {
            return try await perform()
        } catch is CancellationError {
            throw CancellationError()
        } catch {
            logFirebaseError("Database.\(operation)", error)
            throw error
        }
    }

    func createLobby(gameSettings: GameSettings, player: ComposeApp.User) async throws -> String {
        let gameID = LobbyCode.shared.generate()
        let currentUser = Auth.auth().currentUser
        let host = currentUser?.uid ?? ""
        let key = UUID().uuidString
        let lobby = Lobby(
            id: gameID,
            key: key,
            settings: gameSettings,
            players: [player],
            host: host,
            game: ""
        )

        try await databaseOperation("createLobby") {
            try await Database.database().reference()
                .child(FirebasePath.lobbies)
                .child(key)
                .setValue(dictionary(from: lobby))
        }
        return key
    }

    func joinLobby(id: String, player: ComposeApp.User) async throws -> String {
        guard let currentUser = Auth.auth().currentUser else {
            return ""
        }

        let lobbies = Database.database().reference()
            .child(FirebasePath.lobbies)
        let query = lobbies
            .queryOrdered(byChild: FirebaseLobbyKey.id)
            .queryEqual(toValue: id)
        
        let lobby: (key: String, update: Lobby?) = try await withCheckedThrowingContinuation { continuation in
            query.observeSingleEvent(of: .value, with: { [weak self] snapshot in
                guard let self,
                      let childSnapshot = snapshot.children.allObjects.first as? DataSnapshot,
                      let lobby = self.lobby(from: childSnapshot.firebaseDictionary, key: childSnapshot.key) else {
                    let error = NSError(
                        domain: "IOSMultiplayerRepository",
                        code: 1,
                        userInfo: [NSLocalizedDescriptionKey: "Лобби не найдено"]
                    )
                    logFirebaseError("Database.joinLobby", error)
                    continuation.resume(throwing: error)
                    return
                }

                let key = childSnapshot.key
                
                if lobby.players.contains(where: { $0.id == currentUser.uid }) {
                    continuation.resume(returning: (key, nil))
                    return
                }

                lobby.players.append(player)
                
                continuation.resume(returning: (key, lobby))
            }, withCancel: { error in
                logFirebaseError("Database.joinLobby.find", error)
                continuation.resume(throwing: error)
            })
        }
        
        if let update = lobby.update {
            try Task.checkCancellation()
            try await databaseOperation("joinLobby.write") {
                try await lobbies.child(lobby.key).setValue(dictionary(from: update))
            }
        }
        return lobby.key
    }

    func connectToLobby(key: String) -> any Kotlinx_coroutines_coreFlow {
        let bridge = GameSettingsFlowBridge()
        let reference = Database.database().reference().child(FirebasePath.lobbies).child(key)

        let handle = reference.observe(.value, with: { [weak self] snapshot in
            guard snapshot.exists() else {
                bridge.closeMissingLobby()
                return
            }
            guard let self, let lobby = self.lobby(from: snapshot.firebaseDictionary, key: snapshot.key) else {
                NSLog("[ThousandFirebase] Database.connectToLobby failed: invalid lobby data")
                bridge.closeWithError(message: "Failed to connect")
                return
            }

            bridge.emit(lobby: lobby)
        }, withCancel: { error in
            logFirebaseError("Database.connectToLobby", error)
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow(removeObserver: {
            reference.removeObserver(withHandle: handle)
        })
    }

    func disconnectFromLobby(key: String) async throws {
        guard let userId = Auth.auth().currentUser?.uid else {
            return
        }

        let reference = Database.database().reference()
            .child(FirebasePath.lobbies)
            .child(key)
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            reference.runTransactionBlock({ data in
                guard let code = data.childData(byAppendingPath: FirebaseLobbyKey.id).value as? String,
                      !code.isEmpty,
                      let hostId = data.childData(byAppendingPath: "host").value as? String,
                      !hostId.isEmpty else {
                    // The first snapshot may be empty until Firebase checks it against the server.
                    return TransactionResult.success(withValue: data)
                }

                if hostId == userId {
                    data.value = nil
                    return TransactionResult.success(withValue: data)
                }

                let entries = data.childData(byAppendingPath: "players").children.allObjects
                    .compactMap { $0 as? MutableData }
                    .filter { $0.childData(byAppendingPath: "id").value as? String == userId }
                if !entries.isEmpty {
                    for entry in entries {
                        entry.value = nil
                    }
                    data.childData(byAppendingPath: "lastActivityAt").value = ServerValue.timestamp()
                }
                return TransactionResult.success(withValue: data)
            }, andCompletionBlock: { error, _, _ in
                if let error {
                    logFirebaseError("Database.disconnectFromLobby", error)
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume()
                }
            }, withLocalEvents: false)
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
            logFirebaseError("Database.userLobbies", error)
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }
    
    func startGame(key: String) async throws {
        let lobbyReference = Database.database().reference()
            .child(FirebasePath.lobbies)
            .child(key)

        let snapshot = try await databaseOperation("startGame.readLobby") {
            try await lobbyReference.getData()
        }
        let data = snapshot.value as? [String: [String: Any]]
        guard let lobby = lobby(from: data?[key] ?? [:], key: key) else {
            let error = NSError(
                domain: "IOSMultiplayerRepository",
                code: 1,
                userInfo: [NSLocalizedDescriptionKey: "Failed to start game."]
            )
            logFirebaseError("Database.startGame.readLobby", error)
            throw error
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
            messagesToShow: [],
            reaction: nil,
            onlinePlayerIds: [],
            finishedAt: nil,
            achievementTurn: nil
        ).startAchievementRound()

        try await databaseOperation("startGame.writeGame") {
            try await Database.database().reference()
                .child(FirebasePath.games)
                .child(gameID)
                .setValue(dictionary(from: game))
        }
        try await databaseOperation("startGame.linkLobby") {
            try await lobbyReference.child(FirebasePath.game).setValue(gameID)
        }
        try await databaseOperation("startGame.removeLobby") {
            try await lobbyReference.removeValue()
        }
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
            logFirebaseError("Database.userGames", error)
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
                NSLog("[ThousandFirebase] Database.observeGame failed: missing or invalid game data")
                bridge.closeWithError(message: "Failed to fetch game")
                return
            }

            bridge.emit(game: game)
        }, withCancel: { error in
            logFirebaseError("Database.observeGame", error)
            bridge.closeWithError(message: error.localizedDescription)
        })

        return bridge.flow
    }
    
    func updateGame(game: RemoteGame) async throws {
        try await databaseOperation("updateGame") {
            try await Database.database().reference()
                .child(FirebasePath.games)
                .child(game.key)
                .updateChildValues(updateDictionary(from: game))
        }
    }

    func sendReaction(gameKey: String, reaction: UserReaction) async throws {
        try await databaseOperation("sendReaction") {
            try await Database.database().reference()
                .child(FirebasePath.games)
                .child(gameKey)
                .child(FirebaseGameKey.reaction)
                .setValue(swiftDictionary(from: FirebaseGameMapper.shared.reactionDictionary(from: reaction)))
        }
    }

    func updatePlayerOnlineStatus(gameKey: String, isOnline: Bool) async throws {
        guard let user = Auth.auth().currentUser else {
            return
        }
        
        try await databaseOperation("updatePlayerOnlineStatus") {
            try await Database.database().reference()
                .child(FirebasePath.games)
                .child(gameKey)
                .child(FirebaseGameKey.onlinePlayerIds)
                .child(user.uid)
                .setValue(isOnline)
        }
    }
    
    func saveGameResult(game: RemoteGame) async throws -> FinishedGameSaveResult? {
        let reference = Database.database().reference()
        let gameReference = reference.child(FirebasePath.games).child(game.key)
        let snapshot = try await databaseOperation("saveGameResult.read") {
            try await gameReference.getData()
        }
        let previousPlayerIndex = snapshot.childSnapshot(forPath: "currentPlayerIndex").value as? Int ?? -1
        guard game.isFinished() else {
            return nil
        }

        if previousPlayerIndex < 0 {
            guard let savedGame = self.game(from: snapshot.value, key: game.key),
                  savedGame.isFinished() else {
                NSLog("[ThousandFirebase] Database.saveGameResult.read failed: finished game is missing")
                return nil
            }
            return FinishedGameSaveResult(game: savedGame, newlyFinished: false)
        }

        var updates = updateDictionary(from: game)
        updates[FirebaseGameKey.finishedAt] = ServerValue.timestamp()
        do {
            try await databaseOperation("saveGameResult.write") {
                try await gameReference.updateChildValues(updates)
            }
        } catch {
            let latest = try await databaseOperation("saveGameResult.readAfterWriteFailure") {
                try await gameReference.getData()
            }
            guard let savedGame = self.game(from: latest.value, key: game.key),
                  savedGame.isFinished(),
                  savedGame.finishedAt != nil else {
                throw error
            }
            return FinishedGameSaveResult(game: savedGame, newlyFinished: false)
        }

        let confirmed = try await databaseOperation("saveGameResult.confirm") {
            try await gameReference.getData()
        }
        guard let savedGame = self.game(from: confirmed.value, key: game.key),
              savedGame.isFinished() else {
            NSLog("[ThousandFirebase] Database.saveGameResult.confirm failed: finished game is missing")
            return nil
        }
        return FinishedGameSaveResult(game: savedGame, newlyFinished: true)
    }

    func deleteGame(key: String) async throws {
        try await databaseOperation("deleteGame") {
            try await Database.database().reference()
                .child(FirebasePath.games)
                .child(key)
                .removeValue()
        }
    }

}
