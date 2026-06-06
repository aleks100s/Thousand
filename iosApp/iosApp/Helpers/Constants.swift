//
//  Constants.swift
//  iosApp
//
//  Created by Codex on 06.06.2026.
//

enum FirebasePath {
    static let users = "users"
    static let lobbies = "lobbies"
    static let games = "games"
    static let game = "game"
    static let gameCount = "gameCount"
    static let winCount = "winCount"

    static func userField(userId: String, field: String) -> String {
        "\(users)/\(userId)/\(field)"
    }
}

enum FirebaseUserKey {
    static let name = "name"
    static let platform = "platform"
    static let gameCount = "gameCount"
    static let winCount = "winCount"
    static let rating = "rating"
}

enum FirebaseLobbyKey {
    static let id = "id"
}

enum FirebasePlatform {
    static let ios = "iOS"
}
