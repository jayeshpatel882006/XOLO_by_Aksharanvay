# XOLO by AKSHARANVAY

A premium, tactile tic-tac-toe game for Android. Built natively in Kotlin with
custom Canvas rendering, a pure-Kotlin game engine, and a small MVVM layer.

> **A Little Game. A Lot of Play.**

---

## Build status

| Item | Result |
| --- | --- |
| Unit tests | **32 passed, 0 failed** |
| Release build | **BUILD SUCCESSFUL** (R8 + resource shrinking) |
| APK signature | **Verified** — APK Signature Scheme v2 |
| APK size | **323,288 bytes (~0.31 MiB)** — well under the 10 MB limit |
| Minimum Android | **7.0 (API 24)** |
| Target / compile SDK | 34 (Android 14) |
| Device test | Not run — no ADB device or emulator was attached to this machine |

---

## Download / install

The signed release APK is at:

```
dist/XOLO-1.0.0-release.apk
```

SHA-256:

```
d0f03a89ead07a75aa2e06dc8d3ca973b18d133349858a664e3a42db706508c9
```

Install with ADB:

```bash
adb install -r dist/XOLO-1.0.0-release.apk
```

Launch:

```bash
adb shell am start -n com.aksharanvay.xolo/.MainActivity
```

> Requires a connected device with USB debugging enabled (`adb devices` must
> list it). No device was available during this build, so on-device behaviour
> was not verified.

---

## Features

- **Two modes** — pass-and-play with a friend, or play the computer.
- **Three computer levels**
  - *Easy* — relaxed, random legal moves.
  - *Medium* — takes wins, blocks losses, prefers the centre.
  - *Hard* — full minimax with alpha-beta pruning. Provably never loses.
- **Choose your mark** in computer mode (X moves first, O moves second).
- **Score tracking** kept separately for friend mode and computer mode, and
  persisted across launches.
- **Persistent settings** — sound, vibration, theme, and difficulty.
- **Light / dark / system theme** with a full palette swap, including
  Canvas-drawn surfaces.
- **Original sound design** — six tiny synthesised WAV effects (tap, place X,
  place O, win, draw, start). No external audio libraries.
- **Haptic feedback** with graceful fallback on devices without a vibrator.
- **Animated splash, home, game, settings and about screens.**
- **Accessibility** — content descriptions, live turn announcements, 48dp touch
  targets, and screen-reader-friendly custom controls.
- **Fully offline.** No ads, no analytics, no network permission, no tracking.

---

## Screens

| Screen | Purpose |
| --- | --- |
| Splash | Animated logo, wordmark and tagline. |
| Home | Brand hero, mode cards, settings and about entry points. |
| Game | Score strip, turn card, tactile board, restart and new game. |
| Setup overlay | Mark choice and difficulty for computer mode. |
| Settings | Sound, vibration, theme, difficulty, score reset. |
| About | Brand story, version, credits. |

---

## Architecture

```
app/src/main/java/com/aksharanvay/xolo/
├── MainActivity.kt            Activity, navigation, theme selection
├── XoloViewModel.kt           Game lifecycle, AI scheduling, persistence
├── game/                      Pure Kotlin rules (no Android imports)
│   ├── Mark.kt  GameStatus.kt  GameState.kt
│   ├── GameEngine.kt          Win detection, move validation
│   ├── MoveOutcome.kt  Difficulty.kt  GameMode.kt
├── ai/
│   ├── XoloAi.kt  AiFactory.kt
│   ├── EasyAi.kt  MediumAi.kt  HardAi.kt
├── data/
│   ├── KeyValueStore.kt  SharedPreferences-backed storage
│   ├── SettingsRepository.kt  ScoreRepository.kt
│   ├── XoloSettings.kt  ThemeMode.kt
├── audio/
│   ├── SoundManager.kt  Haptics.kt  FeedbackController.kt
└── ui/
    ├── XoloNavigator.kt
    ├── state/XoloUiState.kt   UI state + one-shot events
    ├── screen/                Splash, Home, Game, Settings, About
    └── view/                  Custom Canvas views
        ├── GlyphPainter.kt    Dimensional X/O rendering
        ├── BoardView.kt       The board slab, wells and pieces
        ├── TactileButton.kt   Layered, springy cards
        ├── SegmentedControl.kt  ScoreStripView.kt
        ├── ToggleRowView.kt   XoloSwitch.kt
        ├── MarkGlyphView.kt   ThinkingIndicator.kt
        ├── AmbientPlaygroundView.kt  XoloDialog.kt
        └── XoloPalette.kt     Theme-aware colours for Canvas code
```

- **No XML data binding, no DI framework, no Compose, no Flutter.**
- Game rules are platform-free and unit tested directly on the JVM.
- Custom views read colours through `XoloPalette`, so both themes drive
  everything, including painted surfaces.

### Design tokens

Centralised in `app/src/main/res/values/`:

- `colors.xml` — raw brand palette.
- `themes.xml` — `Theme.Xolo.Light` / `Theme.Xolo.Dark` attribute mappings.
- `attrs.xml` — custom colour attributes.
- `dimens.xml` — spacing, radii, touch targets.
- `styles.xml` — typography styles.
- `integers.xml` — animation durations.

---

## Build

The project builds with the Gradle wrapper. On this machine the Windows
toolchain was used from WSL, pinned to Android Studio's bundled JDK 21.

**Windows (recommended helper):**

```bat
tools\gradle.bat :app:testDebugUnitTest
tools\gradle.bat :app:assembleRelease
```

`tools\gradle.bat` sets `JAVA_HOME` to Android Studio's JBR and forwards all
arguments to `gradlew.bat`. If your JDK/SDK paths differ, either edit that file
or set `JAVA_HOME` yourself and call `gradlew.bat` directly.

**Plain Gradle:**

```bat
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
gradlew.bat :app:testDebugUnitTest :app:assembleRelease
```

`local.properties` must point at your Android SDK:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

Outputs:

```
app/build/outputs/apk/release/app-release.apk
app/build/test-results/testDebugUnitTest/
```

---

## Testing

Run the JVM suite:

```bat
tools\gradle.bat :app:testDebugUnitTest
```

Coverage:

- **`GameEngineTest` (11)** — empty board, turn order, all 8 winning lines for
  both X and O, draws, occupied cells, out-of-range moves, no moves after a
  win, shrinking move lists, clean restart, `findWin` for every line.
- **`AiTest` (12)** — legal moves for Easy; Easy returns null when finished;
  Medium wins/blocks/centre; Hard wins/blocks/centre; Hard returns null on a
  full board; **exhaustive proof** that Hard never loses as X or O against every
  possible human reply.
- **`SettingsRepositoryTest` (3)** — defaults, round-trip persistence, invalid
  stored enums fall back safely.
- **`ScoreRepositoryTest` (6)** — zeroed start, friend/computer accumulation,
  persistence, reset-all, per-mode reset, label mapping.

Current result: **32 tests, 0 failures, 0 errors, 0 skipped.**

---

## APK verification

```bash
# size
stat -c%s dist/XOLO-1.0.0-release.apk      # 323236

# signature
apksigner verify --verbose --print-certs dist/XOLO-1.0.0-release.apk

# manifest summary
aapt dump badging dist/XOLO-1.0.0-release.apk
```

Signing certificate:

```
CN=XOLO by AKSHARANVAY, OU=AKSHARANVAY, O=AKSHARANVAY, L=Pune, ST=Maharashtra, C=IN
SHA-256: 42a89f6fd2ae183f9de92b1fc7cf9428cda6c488438ae04a488ac83187166689
```

The signing keystore (`xolo-release.jks`) and `keystore.properties` are
**gitignored** and stay local. Keep them safe: the same key is required to ship
updates. See `BRANDING.md` for regenerating signing material.

---

## Rebranding

XOLO ships with a placeholder brand that is intentionally easy to replace.
See **[BRANDING.md](BRANDING.md)** for:

- Replacing the logo and launcher icons.
- Updating brand colours for light and dark themes.
- Changing the wordmark and signature.

---

## License / ownership

XOLO is a product of **AKSHARANVAY**. The name, wordmark and visual identity are
placeholders for the brand and may be replaced.
