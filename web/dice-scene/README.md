# Offline dice scene

From this directory run `npm ci && npm run build` to type-check, bundle and update
`composeApp/src/commonMain/composeResources/files/dice-scene/index.html`.
Commit the source, lockfile and generated HTML together. Mobile builds do not need Node.

`npm test` checks that each requested face ends on top. `npm run dev` opens the
browser-only preview; the native app uses exactly the same scene/JSON bridge.

`window.DiceScene.receive(JSON.stringify(configuration))` accepts an active flag,
CSS background color, normalized bounds, promptText and an optional request (presentationId,
values, animate, scoringIndices, scoreText). Kotlin supplies the formatted roll
score (for example, "15 очков") and zero-based scoringIndices: all
ones, fives and groups of at least three equal values. A soft turquoise silhouette
appears around these dice only after settling, and disappears on the next roll.
The score and the smaller "продолжить или сохранить?" prompt appear below the dice
after settling. Their space is reserved inside the supplied bounds; resizing
repositions them without replaying the roll. Text is assigned with textContent.
Kotlin supplies "ход противника" as promptText during another player's turn;
changing this label does not replay the roll.
Events ready/rollStarted/rollFinished/error go to DiceNative on
Android and webkit.messageHandlers.diceScene on iOS. In a browser they are exposed
as `diceSceneEvent` CustomEvents. New IDs replace previous animations; resizing
and repeated configurations never replay a roll. Opposite die faces sum to seven.

The artifact is self-contained; no CDN, network access, external textures or local file URLs.
The dark lime felt uses generated color and bump tiles; the wooden perimeter uses
a separate canvas with grain and rounded, oval rails, redrawn only on resize.
The table casts an outer shadow; the raised rim casts a shadow onto the felt,
with highlights and dice shadows matching a light source on the left.
Rendering stops when a roll settles. WebGL context loss is reported to the native
fallback rather than silently recreating a scene with stale state.
