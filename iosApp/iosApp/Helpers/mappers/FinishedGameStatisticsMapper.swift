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

            updates[FirebasePath.userField(userId: userId, field: FirebasePath.gameCount)] = ServerValue.increment(1)
            if player.isWinner {
                updates[FirebasePath.userField(userId: userId, field: FirebasePath.winCount)] = ServerValue.increment(1)
            }
        }

        return updates
    }
}
