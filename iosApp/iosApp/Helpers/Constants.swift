//
//  Constants.swift
//  iosApp
//
//  Created by Codex on 06.06.2026.
//

enum FirebasePath {
    static let lobbies = "lobbies"
    static let games = "games"
    static let game = "game"
}

enum FirebaseUserKey {
    static let name = "name"
    static let platform = "platform"
}

enum FirebaseLobbyKey {
    static let id = "id"
}

enum FirebaseGameKey {
    static let onlinePlayerIds = "onlinePlayerIds"
    static let reaction = "reaction"
    static let currentRoll = "currentRoll"
    static let finishedAt = "finishedAt"
}

enum FirebasePlatform {
    static let ios = "iOS"
}
