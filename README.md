# NVILE for Android

Native Android app for NVILE: detect signals, see why markets move, forecast the outcome, and
know when to act. Every figure carries its evidence class: ● Verified, ◦ Signal, ◇ Estimate,
◈ Prediction.

Built with Kotlin and Jetpack Compose. Screens follow the **NVILE Mobile App** design canvas.

## Screens

| Tab | Screens |
| --- | --- |
| Welcome | Animated hero forecast, word-by-word headline, early access / sign in |
| Onboarding | Role picker, market tracking (up to 10) |
| Today | Daily brief, verdict-moved banner, tracked markets with sparklines, top catalyst, track record |
| Signals | Search and stage filters, ranked opportunity cards → Market detail → Why it is moving → Decision Center |
| Ask | Question composer with Standard/Deep depth, suggestions → staged analysis → verdict answer |
| Ideas | Score an idea (buyer, regions, horizon) → score ring, verdict, 8-part Opportunity Report |
| You | Plan, integrations, alerts, track record, sign out |

## Design system

- **Ground:** near-black ink with a slow, drifting mint aurora (`AuroraBackground`).
- **Surfaces:** frosted glass cards with a light-catching hairline rim (`GlassCard`).
- **Type:** Space Grotesk (display and numerals), Instrument Sans (reading), IBM Plex Mono
  (labels and evidence marks). All bundled under the SIL Open Font License (`licenses/`).
- **Colour:** one luminous mint accent (`#4FE3C1`) for momentum, amber (`#E0A94F`) for
  verdicts under pressure.
- **Motion:** spring physics throughout. Charts draw themselves, numbers count up and roll,
  horizons morph the forecast curve, the tab bar capsule glides, controls give haptic ticks.

Tokens live in `app/src/main/java/com/nvile/app/ui/theme`, components in `ui/components`.

## Data

`data/SeedRepository.kt` serves a demo dataset behind the `NvileRepository` interface. AI
Workflow Platforms carries the full report from the design; the other markets are
illustrative. The engines API (`Nvile/trends-intelligence`) is internal (NestJS → Python with a
service key), so the app will reach it through a public gateway. Implement
`NvileRepository` against that gateway to go live; the UI needs no changes.

## Build

Requirements: JDK 17 and the Android SDK (API 35).

```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # onto a connected device or emulator
```

CI (`.github/workflows/android.yml`) builds debug and release APKs and runs lint on every
push; download the APKs from the run's **nvile-apks** artifact. The release build is signed
with the debug key for review only — add a real upload key before publishing to Google Play.
