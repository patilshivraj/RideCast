# UI design system

RideCast presentation-layer conventions for Compose UI. Implementation lives in `app/src/main/java/com/ridecast/presentation/theme/` and `presentation/components/`.

## Brand assets

| Asset | Location |
|-------|----------|
| Brand sheet (source) | `docs/branding/ridecast-brand-sheet.png` |
| Launcher master (light) | `docs/branding/ic_launcher_master_light.png` |
| Play Store icon (512×512) | `app/src/main/playstore/icon.png`, `docs/branding/ic_launcher_512.png` |
| Adaptive foreground | `app/src/main/res/drawable-{mdpi…xxxhdpi}/ic_launcher_foreground.png` |
| Adaptive background | `app/src/main/res/drawable/ic_launcher_background.xml` (cream) |
| Splash | `app/src/main/res/drawable/splash_background.xml` + `ic_splash_logo.png` |

Launcher icon crops the light square from the brand sheet (cream background, motorcycle + weather illustration + wordmark). Foreground layers scale content to the Android adaptive safe zone (center 66%); background is brand cream `#F2F1E6`.

## Color palette

Brand colours from the RideCast sheet:

| Token | Hex | Usage |
|-------|-----|--------|
| `RideCastOliveDark` | `#2F4530` | Forest green — dark accents, dark launcher variant |
| `RideCastOlive` | `#5C6B4A` | Primary olive — CTAs, selected nav |
| `RideCastGold` | `#C4A035` | Mustard gold — secondary accents, insights |
| `RideCastCream` | `#F2F1E6` | Cream — backgrounds, launcher light bg |

| Role | Light | Usage |
|------|-------|--------|
| Primary | Olive (`RideCastOlive`) | CTAs, selected nav, key accents |
| Secondary | Gold (`RideCastGold`) | Secondary accents, insights |
| Background | Cream (`RideCastCream`) | Screen backgrounds |
| Surface | Elevated white (`RideCastSurfaceElevated`) | Cards, bottom nav |
| Text | Charcoal (`RideCastCharcoal`) | Primary text |

Semantic weather colours (`WeatherClear`, `WeatherRain`, `WeatherWind`, `WeatherVisibility`, `WeatherExtreme`) are used for timeline accents, map markers, and condition banners. Map to conditions via `WeatherCondition.toSemanticColor()`.

Brand palette is applied by default (`RideCastTheme(dynamicColor = false)`). Material You dynamic colour remains available but off unless explicitly enabled.

## Typography

Material 3 `RideCastTypography` plus semantic roles in `RideCastType`:

| Role | Style object | Typical use |
|------|--------------|-------------|
| Screen title | `RideCastType.screenTitle` | Top app bar |
| Section | `RideCastType.sectionTitle` | Group headers (uppercase) |
| Card title | `RideCastType.cardTitle` | Card headings |
| Primary metric | `RideCastType.metricPrimary` | Temperature, distance |
| Secondary metric | `RideCastType.metricSecondary` | Supporting numbers |
| Label | `RideCastType.label` | Metric labels |
| Caption | `RideCastType.caption` | Hints, metadata |

## Spacing & shapes

Use `RideCastSpacing` tokens (`xs` 4dp → `xl` 32dp) instead of raw `dp` in presentation code.

Use `MaterialTheme.shapes` (from `RideCastShapes`) for corners: `medium` for chips/fields, `large` for cards.

## Shared components

| Component | Purpose |
|-----------|---------|
| `RideCastTopBar` | Consistent app bar |
| `SectionHeader` | Uppercase section labels |
| `RideMetric` | Label + value pairs |
| `RideCastPrimaryButton` | Primary CTA |
| `RideCastSurfaceCard` | Grouped content surface |
| `EmptyState` | Icon + title + message |
| `LoadingState` | Centered progress |
| `ShimmerBox` / `ShimmerTimelineList` | List loading placeholders |

## Screen patterns

- **Scaffold** `containerColor` = `MaterialTheme.colorScheme.background`
- **Immersive tabs** — Plan and Map extend edge-to-edge under the status bar; only bottom-nav padding is applied from `MainActivity`
- **Plan screen** — Google Map background with gradient scrim; floating search surfaces over the map
- **Map screen overlays** — `MapTypeOverlay` (top-right layers button + dropdown) persists map type via `SettingsViewModel`; semi-transparent `Card` matches route summary styling
- **Saved places** — Up to 15 favorites persisted in DataStore (`FavoritePlacesRepository`); bookmark icon on resolved origin/destination
- **Plan route flow** — Vertical connector with origin/destination dots; **Swap** (`SwapVert`) reverses start and end and auto-recalculates when all stops are resolved; intermediate stops (up to 5) support drag-handle reorder and auto-recalculate after reorder when a route is already calculated; **Add stop** inserts waypoints (Places autocomplete + remove); **Calculate Ride** runs route + weather fetch
- **Empty / error / loading** — use shared components, not one-off layouts per screen
- **Ride condition** (Summary) — derived from insight strings in the UI layer (`Good to ride`, `Ride with caution`, `Consider delaying`)

## Dark mode

Controlled via Settings → Dark mode (Light / Dark / System). `MainActivity` passes `darkTheme` into `RideCastTheme`; dark scheme uses warm charcoal surfaces with olive/gold accents.
