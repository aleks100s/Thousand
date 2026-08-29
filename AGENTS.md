# Repository Guidelines

## Project Structure & Module Organization

This repository is a Kotlin Multiplatform app targeting Android and iOS.

- `composeApp/src/commonMain`: shared Compose UI, domain logic, data models, repositories, and resources.
- `composeApp/src/androidMain`: Android entry points and platform-specific services.
- `composeApp/src/iosMain`: iOS-specific Kotlin implementations and framework integration.
- `composeApp/src/commonTest`: shared unit tests, organized to mirror the production package structure.
- `iosApp/iosApp`: the native SwiftUI app entry point, helpers, assets, and Xcode project files.
- `composeApp/schemas`: Room database schema snapshots; update them deliberately with database changes.

Prefer shared code for behavior that is platform-independent. Keep Android- and iOS-only APIs in the matching source set.

## Build, Test, and Development Commands

Run these from the repository root:

- `./gradlew :composeApp:assembleDebug` — build the Android debug APK.
- `./gradlew :composeApp:testDebugUnitTest` — run Android JVM unit tests.
- `./gradlew :composeApp:allTests` — run available multiplatform tests.
- `open iosApp/iosApp.xcodeproj` — open the iOS app in Xcode for simulator/device development.

Use the IDE run configurations for local app launches. On Windows, use the equivalent `gradlew.bat` commands.

## Coding Style & Naming Conventions

Use Kotlin and Swift standard conventions: four-space indentation, trailing commas in multiline Kotlin declarations, `PascalCase` for types, and `camelCase` for functions, properties, and variables. Name tests after the behavior they verify, such as `CalculateDiceRollScoreUseCaseTest`. Keep Compose screens focused and extract reusable UI into clearly named composables. Match existing package names under `com.alextos.thousand`.

## Testing Guidelines

Shared tests use Kotlin Test and belong in `commonTest`; place a test beside the corresponding domain/use-case package. Cover scoring, turn rules, persistence behavior, and other deterministic game logic. Run the relevant Gradle test task before submitting changes. No repository-wide coverage threshold is currently configured.

## Commit & Pull Request Guidelines

Recent commits use short, imperative, sentence-style summaries (for example, `Observe user presence` or `Change icons`). Follow that style and keep each commit focused. Pull requests should explain the user-visible or architectural change, list validation performed, link the relevant issue when available, and include screenshots or a short recording for UI changes. Call out database schema, Firebase, or platform-specific changes explicitly.

## Security & Configuration Tips

Do not add credentials, tokens, signing material, or private backend data. Review changes to `google-services.json`, `GoogleService-Info.plist`, and `iosApp/Configuration/Config.xcconfig` carefully, and use local environment/configuration mechanisms for sensitive values.
