# YUK 24 Android — Session Notes

Summary of work done in this development session (June 2026).

---

## Goals

1. Connect the customer Android app to the **existing** deployed backend (no new server in this repo).
2. Draw the map route **along streets**, not as a straight line between pickup and delivery.
3. Align **LoadSize** pricing with the backend and keep the price UI **customer-only** (no platform/driver revenue split).
4. Keep secrets out of git by ignoring `.env`.

---

## Backend connection

| Item | Detail |
|------|--------|
| Production API | `https://yuk24-backend.onrender.com/` |
| Config | Default `BASE_URL` in [`app/build.gradle.kts`](app/build.gradle.kts) |
| Override (optional) | `yuk24.base.url.debug` in `local.properties` for local backend (e.g. `http://10.0.2.2:5000/` on emulator) |
| ORS key | **Not** in the Android app — set `ORS_API_KEY` on **Render** (same value as web `VITE_ORS_API_KEY`) |

**Already wired endpoints** (unchanged paths):

- `GET /api/health`
- `POST /api/route`, `POST /api/price`
- `POST /api/orders`, `GET /api/orders/by-phone`, `GET /api/orders/:id`, `POST /api/orders/:id/review`

Contract reference: [`BACKEND_API_FOR_FRONTEND.txt`](BACKEND_API_FOR_FRONTEND.txt).

**Phone / release:** HTTPS to Render works on a real device without LAN or cleartext setup.

---

## Street-following map polyline

**Map polyline (legacy + backend):**

- **Distance / duration / price:** always from backend `POST /api/route` and `POST /api/price`.
- **Map shape:** backend `geometry` if present; else direct **OpenRouteService** on device (`OrsApiService` + `BuildConfig.ORS_API_KEY`), same as the pre-rebuild app.
- [`RouteGeometryParser.kt`](app/src/main/java/uz/yuk24/app/util/RouteGeometryParser.kt) parses `LineString`, `Feature`, or `FeatureCollection`.
- `RouteRepository.resolveMapGeometry()` / `GetRouteUseCase.resolveMapGeometry()` — used from `BookingViewModel.refreshRoute()`.
- One route request on pin change caches **distance/duration** for the price step.

**Local ORS key (not in git):** add to `local.properties`:

```properties
ors.api.key=your-openrouteservice-jwt
```

Or set env `ORS_API_KEY` when building. Without a key and with backend `geometry: null`, the map falls back to a straight line.

**Map UI:** [`Step1MapScreen.kt`](app/src/main/java/uz/yuk24/app/presentation/customer/booking/Step1MapScreen.kt) unchanged — still draws road geometry when `geometry.size >= 2`, else straight line fallback.

**Important:** A straight line on the map means `routeGeometry` is empty — either `geometry` is `null` from the API, or the parser did not recognize the JSON shape. The app now unwraps ORS `FeatureCollection`; if the line is still straight, check `POST /api/route` returns non-null `geometry`.

---

## LoadSize pricing + price UI cleanup

**Multipliers unchanged** (must match backend):

| Tier | API key | Multiplier |
|------|---------|------------|
| XSMALL | `xsmall` | 1.0 |
| SMALL | `small` | 1.2 |
| MEDIUM | `medium` | 1.5 |
| LARGE | `large` | 2.0 |
| XLARGE | `xlarge` | 2.5 |

**Formula** (client mirrors server; server `POST /api/price` is authoritative):

```
total = (BASE_PRICE + distanceKm × PRICE_PER_KM) × multiplier + unloadingFee
BASE_PRICE = 10,000 UZS
PRICE_PER_KM = 3,000 UZS
unloadingFee = 20,000 UZS if unloading
```

**Code updates:**

- [`LoadSize.kt`](app/src/main/java/uz/yuk24/app/domain/model/LoadSize.kt) — KDoc: multipliers are the single client source and must match the API.
- [`PricingUtils.kt`](app/src/main/java/uz/yuk24/app/util/PricingUtils.kt) — `coefficientSurcharge(distanceKm, loadSize: LoadSize)` overload.
- [`PriceBreakdownCard.kt`](app/src/main/java/uz/yuk24/app/presentation/common/components/PriceBreakdownCard.kt) — customer-only breakdown; `PriceBreakdownData.fromQuote(...)` factory; uses `LoadSize` instead of a loose `multiplier` field.
- [`Step4PriceScreen.kt`](app/src/main/java/uz/yuk24/app/presentation/customer/booking/Step4PriceScreen.kt) — builds breakdown via `fromQuote()`.

**Platform / driver split (60% / 40%):** Described in [`YUK24_Android_Full_Prompt.md`](YUK24_Android_Full_Prompt.md) but **never implemented** in Kotlin. Confirmed no `platform_share` / `driver_share` UI in source; breakdown shows base, distance, load tier, unloading, and total only.

**Step 2 min prices** (0 km, no unloading): 10k / 12k / 15k / 20k / 25k UZS via `LoadSizeLabels.minPriceText`.

---

## Security / git

- [`.gitignore`](.gitignore) — added `.env` so MongoDB URI, JWT secret, and API keys are not committed.
- Do **not** put `VITE_ORS_API_KEY` or backend secrets in `local.properties` or Gradle.

If `.env` was already tracked:

```bash
git rm --cached .env
```

---

## Files touched (summary)

| Area | Files |
|------|--------|
| API URL | `app/build.gradle.kts` |
| Map / route | `RouteRepository.kt`, `RouteGeometryParser.kt`, `BookingViewModel.kt`, `Step1MapScreen.kt` (comments) |
| Network cleanup | `NetworkModule.kt` (removed ORS) |
| Pricing UI | `LoadSize.kt`, `PricingUtils.kt`, `PriceBreakdownCard.kt`, `Step4PriceScreen.kt` |
| Git | `.gitignore` |

**Not in this repo:** Node/Express backend (`yuk24-backend` on Render).

---

## Verification checklist

1. `GET https://yuk24-backend.onrender.com/api/health` → `ok: true`
2. `POST /api/route` with two coordinates → `geometry` is a LineString (after `ORS_API_KEY` on Render)
3. Rebuild app; map shows road path between pickup and delivery
4. Step 4: total matches API; no platform/driver % cards
5. Full flow: price → place order → my orders → tracking

---

## What was explicitly out of scope

- Creating or modifying the `yuk24-backend` server in this repository
- Changing load multipliers or the pricing formula
- Driver/admin app screens or JWT flows (customer MVP only)
