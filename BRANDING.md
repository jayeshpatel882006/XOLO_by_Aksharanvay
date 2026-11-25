# XOLO — Branding & Rebranding Guide

XOLO is designed to be re-skinned without touching gameplay code. All visual
identity lives in a small, documented set of resources.

> The current logo, wordmark, signature and palette are **placeholders** owned by
> AKSHARANVAY and may be replaced.

---

## 1. Where the brand lives

| Asset | File |
| --- | --- |
| In-app logo (splash, about) | `app/src/main/res/drawable/ic_xolo_mark.xml` |
| Adaptive launcher icon (API 26+) | `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` |
| Adaptive icon foreground | `app/src/main/res/drawable/ic_launcher_foreground.xml` |
| Adaptive icon background | `@color/xolo_launcher_background` in `values/colors.xml` |
| Legacy launcher PNGs (API 24–25) | `app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher*.png` |
| Wordmark / signature / tagline | `app/src/main/res/values/strings.xml` |
| App name (launcher label) | `app/src/main/res/values/strings.xml` → `app_name` |
| Raw palette | `app/src/main/res/values/colors.xml` |
| Theme attribute mapping | `app/src/main/res/values/themes.xml` |
| Typography | `app/src/main/res/values/styles.xml` |
| Spacing / radii / sizes | `app/src/main/res/values/dimens.xml` |

The wordmark is plain text, not an image. It is rendered from
`brand_wordmark` (`XOLO`) and `brand_signature` (`BY AKSHARANVAY`), so it can be
rebranded by editing strings alone.

---

## 2. Replace the logo

### 2a. In-app logo (vector)

1. Design a square mark on a **108 × 108 viewport** (content ideally inside the
   central 72 × 72 safe area).
2. Export as an Android **vector drawable** and replace the contents of
   `app/src/main/res/drawable/ic_xolo_mark.xml`.
3. Keep the file name `ic_xolo_mark` so the splash, home and about layouts pick
   it up automatically. If you rename it, update the `android:src` references in
   `screen_splash.xml` and `screen_about.xml`.
4. The in-app logo is shown at 78–94dp; favour bold, simple shapes that read at
   small sizes.

Minimal vector template:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- your paths here; use @color or ?attr colours -->
</vector>
```

### 2b. Adaptive launcher icon (API 26+)

1. Replace `app/src/main/res/drawable/ic_launcher_foreground.xml` with your
   foreground vector (108 × 108 viewport, 72 × 72 safe zone).
2. Set the background colour in `app/src/main/res/values/colors.xml`:

   ```xml
   <color name="xolo_launcher_background">#191827</color>
   ```

3. `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` already wire
   the foreground and background together; you normally do not need to edit
   them.

### 2c. Legacy launcher PNGs (API 24–25)

Replace the PNGs at these exact sizes (square, with round variants for the
`_round` files):

| Density | Size |
| --- | --- |
| mdpi | 48 × 48 |
| hdpi | 72 × 72 |
| xhdpi | 96 × 96 |
| xxhdpi | 144 × 144 |
| xxxhdpi | 192 × 192 |

Or regenerate the placeholder set with the bundled standard-library script
(no Pillow required):

```bash
python3 tools/generate_icons.py
```

Edit the `PAPER`, `INK`, `SOLAR` and shape code inside the script to draw your
own icon, or simply overwrite the generated PNGs.

---

## 3. Change the brand colours

The app uses **two themes** — `Theme.Xolo.Light` and `Theme.Xolo.Dark` — plus a
set of custom colour attributes. Custom Canvas views read those attributes
through `XoloPalette`, so changing the palette updates everything, including
painted surfaces.

### Step 1 — edit the raw palette

Open `app/src/main/res/values/colors.xml` and change the hex values. Key roles:

| Token | Role |
| --- | --- |
| `xolo_paper_light` / `xolo_paper_dark` | Screen background base |
| `xolo_paper_deep_light` / `xolo_paper_deep_dark` | Background gradient end |
| `xolo_surface_light` / `xolo_surface_dark` | Cards, board, dialogs |
| `xolo_surface_alt_*` | Secondary surfaces, wells |
| `xolo_text_primary_*` | Headlines and body emphasis |
| `xolo_text_secondary_*` | Supporting copy |
| `xolo_solar` / `xolo_solar_dark` | **X** colour, primary accent |
| `xolo_mint` / `xolo_mint_dark` | **O** colour, secondary accent |
| `xolo_lilac*` | Restart / settings accent |
| `xolo_butter*` | Winning line, draws, highlights |
| `xolo_board_well*` | Inset board cells |
| `xolo_shadow*` | Layered drop shadows |
| `xolo_stroke*` | Hairline borders |
| `xolo_overlay*` | Modal scrims |

### Step 2 — keep the theme mappings in sync

`themes.xml` maps each custom attribute to a raw colour. If you add a new token,
add a matching `<attr>` in `attrs.xml`, a mapping in **both** themes, and a field
in `XoloPalette.kt`. If you only change existing hex values, no other edits are
needed.

### Step 3 — adjust the launcher background

Update `xolo_launcher_background` and, if you want the splash window to match,
`xolo_boot_background`.

### Step 4 — rebuild

```bat
tools\gradle.bat :app:assembleRelease
```

---

## 4. Change the wordmark and copy

Edit `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">XOLO</string>
<string name="brand_wordmark">XOLO</string>
<string name="brand_signature">BY AKSHARANVAY</string>
<string name="tagline">A Little Game. A Lot of Play.</string>
```

- `app_name` is the label under the launcher icon.
- `brand_wordmark` is the large hero text on splash, home and about.
- `brand_signature` is the spaced “BY …” line.
- `tagline` appears on splash, home and about.

The wordmark uses the `Text.Xolo.Wordmark` style (`sans-serif-black`, wide letter
spacing). Adjust size/spacing in `styles.xml` if your brand name is longer.

---

## 5. Typography and spacing

- Fonts are the platform sans-serif family (no bundled font files, keeping the
  APK tiny). Change `android:fontFamily` in `styles.xml` to use another system
  family, or bundle a font and reference it with `@font/...`.
- Spacing, radii and component sizes are centralised in `dimens.xml`.
- Animation durations live in `integers.xml`.

---

## 6. Signing material

`xolo-release.jks` and `keystore.properties` are **gitignored** and are not part
of the source tree. To ship updates, reuse the original keystore. To generate a
fresh one:

```bash
keytool -genkeypair -v \
  -keystore xolo-release.jks \
  -alias xolo \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Your Brand, O=Your Company, C=IN"
```

Then create `keystore.properties` in the project root:

```properties
storeFile=xolo-release.jks
storePassword=your-store-password
keyAlias=xolo
keyPassword=your-key-password
```

`app/build.gradle.kts` reads that file automatically when present and signs the
release build with it.
