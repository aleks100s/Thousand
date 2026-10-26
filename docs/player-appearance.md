# Player appearance

`User.color` and `User.avatar` are stable player properties. `PlayerColor` contains a muted palette; new main users receive Gold and new local players receive a random non-gold color when saved. Existing users without a stored color use a deterministic ID-based fallback (Gold for the main user). Editing the name or avatar preserves the color.

Room schema 2 adds both fields with an automatic migration from schema 1. Firebase game and lobby user maps carry enum names, with safe defaults for older clients/data. The native account bridge writes appearance changes to `users/{uid}` with a partial update (without replacing statistics) and reuses the saved main user's appearance when entering a lobby; account identity replacement preserves it.

The three portraits share SVG sources in `design-system/avatars/`. Run `python3 scripts/generate_player_avatars.py` after changing a portrait to regenerate Compose vectors and the web module. Run `npm ci && npm run build` from `web/dice-scene` to update the bundled offline HTML.

The same appearance is used in player selection/editing, statistics tables and personal histograms, score chart legends/lines, lobby participants and game headers. Bot portraits use the same robot SVG in Compose and the web header.
