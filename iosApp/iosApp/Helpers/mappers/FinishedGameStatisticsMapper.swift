import ComposeApp
import FirebaseDatabase

extension IOSMultiplayerRepository {
    func finishedGameStatisticsUpdates(for game: RemoteGame) -> [String: Any] {
        var updates: [String: Any] = [:]

        game.players.forEach { player in
            let userId = player.user.id
            if userId.isEmpty {
                return
            }

            updates["users/\(userId)/totalGamesCount"] = ServerValue.increment(1)
            if player.isWinner {
                updates["users/\(userId)/winCount"] = ServerValue.increment(1)
            }
        }

        return updates
    }
}
