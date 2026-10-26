# Main-menu hero artwork

The scene uses the user-provided `table.png` and `main_die_1.png`–`main_die_5.png` from `composeApp/src/commonMain/composeResources/drawable/`. The table was replaced by the user with a transparent version; the generated `_transparent` variant is no longer used and was removed.

`MenuHero` uses five different image sizes, vertical amplitudes, phases, and animation durations. Dice render above the table and their travel ranges reach over the felt. Animation values are read in `graphicsLayer`; the infinite transition exists only while the screen lifecycle is resumed.

The parent layout measures the menu first, then gives the illustration the remaining portrait area or the left landscape half. The logo header is shown only in portrait.
