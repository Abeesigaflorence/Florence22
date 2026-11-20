# Royo Farm Manager — Android Native Rebuild

Native Android application for Royo Farm (Uganda, currency UGX), built with Jetpack Compose, Material 3, Firebase Authentication, and Cloud Firestore with offline persistence.

## Architecture

- **UI / Design System**: Jetpack Compose adhering to Royo Farm bespoke palette:
  - Deep Forest Green (`#1E4A38`)
  - Warm Off-White (`#FAF7F1`)
  - Warm Amber (`#C4762A`)
  - Card Border (`#E7E1D6`)
  - Brick Red (`#C1372F`)
  - Serif display typography for statistics and page titles
- **Auth**: Google Sign-In via Jetpack `CredentialManager` with `GetSignInWithGoogleOption` and silent `GetGoogleIdOption`.
- **Database**: Cloud Firestore using named database ID `ai-studio-android-royofarm-9a7cecef-dd3b-458f-8236-ea77210bd822`.
- **Offline Persistence**: Enabled via `PersistentCacheSettings` in `RoyoApp.onCreate()`.
- **Analytics & Forecasting**: Linear least-squares regression over 6-month historical windows with monotonic projection clamping and confidence metrics.
- **Reporting & Export**: CSV generation and export via Android `FileProvider` with `Intent.ACTION_SEND`.

## Collections

1. `flocks` — Poultry batches (Layers and Broilers), tracking bird counts, survival rates, and age in days.
2. `productionRecords` — Daily egg collection, broken eggs, feed used in kg, and mortality (with automatic decrements on the parent flock).
3. `sales` — Sales ledger for eggs, live birds, manure, and other products with pending payment tracking.
4. `expenses` — 7 expense categories with optional flock attribution.
5. `inventoryItems` — Store inventory with reorder level alerts and total stock valuation.
6. `stockMovements` — Transactional in/out stock logs clamping quantities at zero.
7. `users` — Role-based access control (`ADMIN` / `USER`).
