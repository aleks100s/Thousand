# Lobby invitations

The lobby's **Поделиться ссылкой** button opens the platform share sheet with
`https://thousand-688b0.web.app/lobby/<public-code>`. Firebase Hosting serves a fallback
page with the lobby code and an open-in-game button using the original
`thousand://lobby/<public-code>` scheme.

Before distributing Android builds, replace the placeholder fingerprint in
`hosting/public/.well-known/assetlinks.json` with the SHA-256 of the app signing
certificate and redeploy Hosting. For Google Play, use the Play app signing certificate.

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
