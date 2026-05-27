import ComposeApp

extension IOSMultiplayerManager {
    func dictionary(from game: Game) -> [String: Any] {
        swiftDictionary(from: FirebaseGameMapper.shared.dictionary(from: game))
    }

    func game(from value: Any?, fallbackId: String) -> Game? {
        FirebaseGameMapper.shared.game(
            from: firebaseDictionary(from: value),
            fallbackId: fallbackId
        )
    }

    func gamePlayers(from value: Any?) -> [Player] {
        guard let dictionary = firebaseDictionary(from: ["players": value as Any]),
              let game = FirebaseGameMapper.shared.game(from: dictionary, fallbackId: "") else {
            return []
        }

        return game.players
    }

    func gamePlayer(from dictionary: [String: Any]) -> Player {
        FirebasePlayerMapper.shared.player(from: dictionary)
    }
}
