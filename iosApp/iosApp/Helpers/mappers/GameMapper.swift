import ComposeApp

extension IOSMultiplayerManager {
    func dictionary(from game: Game) -> [String: Any] {
        var result: [String: Any] = [
            "id": game.id,
            "startedAt": game.startedAt.toEpochMilliseconds(),
            "settings": dictionary(from: game.settings),
            "players": game.players.map { player in
                [
                    "id": player.id,
                    "user": dictionary(from: player.user),
                    "currentScore": player.currentScore,
                    "isWinner": player.isWinner,
                    "boltCount": player.boltCount,
                    "hasPassedStartLimit": player.hasPassedStartLimit
                ]
            }
        ]

        if let finishedAt = game.finishedAt {
            result["finishedAt"] = finishedAt.toEpochMilliseconds()
        }

        return result
    }

    func game(from value: Any?, fallbackId: String) -> Game? {
        guard let dictionary = value as? [String: Any] else {
            return nil
        }

        return Game(
            id: int64(from: dictionary["id"]) ?? Int64(fallbackId) ?? 0,
            startedAt: instant(from: dictionary["startedAt"]),
            finishedAt: optionalInstant(from: dictionary["finishedAt"]),
            settings: gameSettings(from: dictionary["settings"]),
            players: gamePlayers(from: dictionary["players"])
        )
    }

    func gamePlayers(from value: Any?) -> [Player] {
        if let players = value as? [[String: Any]] {
            return players.map(gamePlayer(from:))
        }

        if let players = value as? [Any] {
            return players.compactMap { $0 as? [String: Any] }.map(gamePlayer(from:))
        }

        if let players = value as? [String: [String: Any]] {
            return players.values.map(gamePlayer(from:))
        }

        return []
    }

    func gamePlayer(from dictionary: [String: Any]) -> Player {
        let userDictionary = dictionary["user"] as? [String: Any]
        let user = userDictionary.map(user(from:)) ?? lobbyPlayer(from: dictionary)

        return Player(
            id: int64(from: dictionary["id"]) ?? 0,
            user: user,
            currentScore: int32(from: dictionary["currentScore"]) ?? 0,
            isWinner: dictionary["isWinner"] as? Bool ?? false,
            boltCount: int32(from: dictionary["boltCount"]) ?? 0,
            hasPassedStartLimit: dictionary["hasPassedStartLimit"] as? Bool ?? false
        )
    }
}
