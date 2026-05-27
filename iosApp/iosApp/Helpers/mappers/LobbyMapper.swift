import ComposeApp

extension IOSMultiplayerManager {
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
            "id": lobby.id,
            "game": lobby.game
        ]
    }

    func lobby(from value: Any?) -> Lobby? {
        guard let dictionary = value as? [String: Any] else {
            return nil
        }

        return Lobby(
            settings: gameSettings(from: dictionary["settings"]),
            players: lobbyPlayers(from: dictionary["players"]),
            host: dictionary["host"] as? String ?? "",
            id: dictionary["id"] as? String ?? "",
            game: dictionary["game"] as? String ?? ""
        )
    }

    func lobbyPlayers(from value: Any?) -> [ComposeApp.User] {
        if let players = value as? [[String: Any]] {
            return players.map(lobbyPlayer(from:))
        }

        if let players = value as? [Any] {
            return players.compactMap { $0 as? [String: Any] }.map(lobbyPlayer(from:))
        }

        if let players = value as? [String: [String: Any]] {
            return players.values.map(lobbyPlayer(from:))
        }

        return []
    }
}
