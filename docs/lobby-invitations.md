# Lobby invitations

The lobby's **Поделиться ссылкой** button opens the platform share sheet with
`thousand://lobby/<public-code>`. This is an installed-app URL scheme, not an
HTTPS universal link; it does not redirect to an app store when the app is absent.

- Android: BROWSABLE VIEW intent, including cold launch and `onNewIntent`.
- iOS: CFBundleURLTypes and SwiftUI `onOpenURL`.
- Invitations wait through startup and onboarding. The multiplayer screen keeps
  the pending code in SavedStateHandle until login and a join attempt complete.
- The existing `joinLobby(publicCode)` resolves the backend lobby key. It retains
  the existing authorization, membership and missing-lobby checks.
- A newer invitation replaces the previous navigation target. Replayed route
  events are ignored; malformed or unrelated URLs are not accepted.
- Test links can use nonnumeric codes such as `InviteQA_20261025` to exercise the
  missing-lobby flow without joining a real four-digit lobby.

Tests: `LobbyLinksTest` and `MultiplayerInvitationTest` cover parsing, queue
replacement, login, restored pending invitations, duplicate delivery and retry.
