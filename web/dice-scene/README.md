# Offline dice scene

From this directory run `npm ci && npm run build` to type-check, bundle and update
`composeApp/src/commonMain/composeResources/files/dice-scene/index.html`.
Commit the source, lockfile and generated HTML together. Mobile builds do not need Node.

`npm test` checks that each requested face ends on top. `npm run dev` opens the
browser-only preview; the native app uses exactly the same scene/JSON bridge.

`window.DiceScene.receive(JSON.stringify(configuration))` accepts an active flag,
CSS background color, normalized bounds and an optional request (presentationId,
values, animate). Events ready/rollStarted/rollFinished/error go to DiceNative on
Android and webkit.messageHandlers.diceScene on iOS. In a browser they are exposed
as `diceSceneEvent` CustomEvents. New IDs replace previous animations; resizing
and repeated configurations never replay a roll. Opposite die faces sum to seven.

The artifact is self-contained; no CDN, network access, textures or local file URLs.
Rendering stops when a roll settles. WebGL context loss is reported to the native
fallback rather than silently recreating a scene with stale state.
