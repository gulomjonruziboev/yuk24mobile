# YUK 24 — Android App (Kotlin + Jetpack Compose)
## COMPLETE AI DEVELOPMENT PROMPT — Design + Backend Integration

---

## ═══════════════════════════════════════════
## SECTION 0: WHAT THIS APP IS
## ═══════════════════════════════════════════

**YUK 24** is an on-demand cargo/load transport booking app for **Uzbekistan**.
Customers book a truck to move cargo between two points in the city (Tashkent-centric).
They pick load weight category, toggle optional unloading help, enter phone number,
see a price estimate, pay cash, and then track their order status.

There are **3 user types** in the system:
- **Customer** — books orders (no JWT, phone-based)
- **Driver** — accepts and fulfills orders (JWT auth)
- **Admin** — manages the platform (JWT auth)

**Phase 1 (this prompt)** covers the **Customer app flow** end-to-end, plus the **Driver app**.
Admin is optional/later.

---

## ═══════════════════════════════════════════
## SECTION 1: TECH STACK
## ═══════════════════════════════════════════

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture (UseCases + Repositories) |
| Navigation | Jetpack Navigation Compose (single Activity) |
| State | StateFlow + ViewModel + UiState sealed classes |
| DI | Hilt |
| Networking | Retrofit 2 + OkHttp 3 + Kotlin serialization (or Gson) |
| Maps | Google Maps SDK for Android (or OSMDroid for OpenStreetMap parity with web) |
| Location | FusedLocationProviderClient |
| Geocoding | Backend `/api/route` → prefer server-side; fallback Geocoder API |
| Push | Firebase Cloud Messaging (FCM) — for future order status push |
| Secure Storage | EncryptedSharedPreferences (driver/admin JWT) |
| Image loading | Coil |
| Build Variants | `debug` (http://10.0.2.2:5000) / `release` (https://your-api.com) |

---

## ═══════════════════════════════════════════
## SECTION 2: BRAND & DESIGN SYSTEM
## ═══════════════════════════════════════════

### 2.1 Color Palette (match website exactly)

```
Primary (Blue CTA):       #2563EB   — main buttons ("Narxni hisoblash", "To'lash")
Primary Dark:             #1D4ED8
Price Card Background:    #1E40AF   — the dark blue card showing price breakdown
Price Card Surface:       #1E3A8A   — slightly darker blue inner sections
Surface White:            #FFFFFF
Surface Light:            #F9FAFB
Border/Outline:           #E5E7EB
Text Primary:             #111827
Text Secondary:           #6B7280
Selected Truck Card:      #EFF6FF border #2563EB (blue tint fill + blue border)
Unselected Truck Card:    #FFFFFF border #E5E7EB
Green Map Pin:            standard green (#22C55E accent)
Red Map Pin:              standard red (#EF4444 accent)
Unloading "Ha":           #EFF6FF background, #2563EB border + text (selected)
Unloading "Yo'q":         #F9FAFB background, #E5E7EB border (unselected)
```

### 2.2 Typography

Use **system font** (Roboto on Android) — do not load custom web fonts.

```
Screen Title:     MaterialTheme.typography.titleLarge  (22sp, SemiBold)
Section Label:    MaterialTheme.typography.labelMedium (12sp, Medium, #6B7280)
Body:             MaterialTheme.typography.bodyMedium  (14sp, Regular)
Price Large:      MaterialTheme.typography.headlineMedium (28sp, Bold, White)
Price Detail Row: MaterialTheme.typography.bodySmall   (13sp, Regular, White/80%)
Button Text:      MaterialTheme.typography.labelLarge  (14sp, SemiBold)
Truck Weight:     MaterialTheme.typography.bodySmall   (12sp)
Truck Price:      MaterialTheme.typography.bodySmall   (12sp, #2563EB)
```

### 2.3 Shape & Spacing

```
Cards:              RoundedCornerShape(12.dp), elevation 1dp
Truck selector cards: RoundedCornerShape(8.dp), size ~72dp × 100dp
CTA Buttons:        RoundedCornerShape(8.dp), height 52dp, full width
Input Fields:       OutlinedTextField, RoundedCornerShape(8.dp)
Toggle Buttons:     RoundedCornerShape(8.dp), side by side equal width
Screen Padding:     16dp horizontal, 20dp top
Card Padding:       16dp all sides
Item spacing:       12dp between sections, 8dp between cards
```

### 2.4 Language

The app is in **Uzbek (O'zbek)** by default. All UI strings must be in Uzbek:

```
"Transport buyurtma qilish"   — screen title / booking title
"Yuk og'irligi"               — Load weight (section label)
"Olish joyi"                  — Pickup location
"Yetkazish joyi"              — Delivery location
"Tushirish yordami kerakmi?"  — Unloading help needed?
"Yo'q, rahmat"                — No, thanks
"Ha, iltimos"                 — Yes, please
"Telefon raqam"               — Phone number
"Narxni hisoblash"            — Calculate price
"Taxminiy narx"               — Estimated price
"Asosiy narx"                 — Base price
"Masofa"                      — Distance
"Yuk koeffitsienti"           — Load coefficient
"Tushirish yordami"           — Unloading fee
"Jami"                        — Total
"Platforma ulushi (60%)"      — Platform share
"Haydovchi ulushi (40%)"      — Driver share
"To'lash"                     — Pay / Checkout
"Naqd pul"                    — Cash
"Buyurtmalarim"               — My Orders
"Holati"                      — Status
```

Add Russian (RU) as secondary locale option (strings.xml + values-ru/).

---

## ═══════════════════════════════════════════
## SECTION 3: APP NAVIGATION STRUCTURE
## ═══════════════════════════════════════════

The app uses **step-by-step screens** (not one long page like the website).
Each booking step is a separate Compose screen/destination.

```
NavGraph:

CUSTOMER FLOW:
  SplashScreen
    └─► RoleSelectScreen (Customer / Driver / Admin)
          │
          ▼ [Customer]
  Step1_MapScreen          ← Pick location on map + confirm pickup & delivery
          │
          ▼
  Step2_LoadSizeScreen     ← Choose truck / load weight
          │
          ▼
  Step3_UnloadingScreen    ← Unloading help toggle + phone + name
          │
          ▼
  Step4_PriceScreen        ← See price breakdown (calls /api/route + /api/price)
          │
          ▼
  Step5_PaymentScreen      ← Cash payment confirmation → POST /api/orders
          │
          ▼
  OrderSuccessScreen       ← Order created, shows orderId
          │
          ▼
  OrderTrackingScreen      ← Poll GET /api/orders/:id, show status timeline

  MyOrdersScreen           ← GET /api/orders/by-phone (accessible from bottom nav)
  OrderDetailScreen        ← Single order detail + review

DRIVER FLOW:
  DriverLoginScreen        ← POST /api/auth/driver/login
  DriverHomeScreen         ← GET /api/driver/orders/available
  DriverOrderDetailScreen  ← Accept / Decline
  DriverActiveJobScreen    ← Picked-up → Delivered flow + location updates
  DriverProfileScreen      ← GET /api/driver/me
```

Bottom navigation bar (Customer app):
- 🏠 Bosh sahifa (Home → Step1_MapScreen)
- 📋 Buyurtmalarim (My Orders → MyOrdersScreen)
- 👤 Profil (Profile → simple phone/settings)

---

## ═══════════════════════════════════════════
## SECTION 4: SCREEN-BY-SCREEN SPECIFICATION
## ═══════════════════════════════════════════

---

### SCREEN 1: SplashScreen

- Full white screen, centered YUK 24 logo
- Logo: "YUK" in bold dark text, "24" in bold #2563EB blue, tagline below: "Yuk tashish xizmati"
- Auto-navigate after 1.5s:
  - If driver token exists → DriverHomeScreen
  - Otherwise → RoleSelectScreen (first launch) or Step1_MapScreen (returning customer)
- Call GET /api/health on splash; if 503 → show "Server mavjud emas" snackbar

---

### SCREEN 2: RoleSelectScreen

- White background, top logo
- Tagline: "Sizning rolingizni tanlang" (Choose your role)
- Two large cards (full width, 12dp radius, 1dp border):
  - **Mijoz (Customer)**: 📦 icon, subtitle "Yuk tashishga buyurtma bering"
  - **Haydovchi (Driver)**: 🚛 icon, subtitle "Buyurtmalarni qabul qiling"
- Tapping Customer → Step1_MapScreen
- Tapping Driver → DriverLoginScreen
- Small "Admin" text link at bottom for admin login

---

### SCREEN 3: Step1_MapScreen

**Header:** "1 / 5 — Manzilni tanlang" progress indicator (LinearProgressIndicator at top, 20% filled, blue)

**Layout (Column):**

1. **Full-height Google Map** (weight 1f — takes up most of screen):
   - On launch: request ACCESS_FINE_LOCATION, then animate camera to user's position (Tashkent default if denied: lat 41.2995, lng 69.2401)
   - Two draggable markers:
     - 🔴 Red pin = Pickup (olish joyi)
     - 🟢 Green pin = Delivery (yetkazish joyi)
   - When both pins placed: draw route polyline on map (blue dashed line)
   - Map style: standard OpenStreetMap tiles (use OSMDroid to match web) OR Google Maps default

2. **Bottom Card** (white, top-rounded 16dp, elevation 4dp):
   - "Olish joyi" label + OutlinedTextField (tapping opens AddressSearchScreen)
   - Small location icon button on right → set to current GPS location
   - "Yetkazish joyi" label + OutlinedTextField (same)
   - "Davom etish →" blue pill button (disabled until both fields filled)

**Address Search (AddressSearchScreen overlay):**
- Search bar at top
- Results list from backend geocoding (call POST /api/route concept, or use OpenRouteService autocomplete bounded to UZ)
- Recent searches stored locally
- Tapping result → sets pin on map + fills field, pops back

**State passed forward:** pickup { label, coords[lat,lng] }, delivery { label, coords[lat,lng] }

---

### SCREEN 4: Step2_LoadSizeScreen

**Header:** "2 / 5 — Yuk og'irligini tanlang" + LinearProgressIndicator 40%

**Layout:**

- Section label: "Yuk og'irligi" (#6B7280, 12sp)
- **Horizontal scrollable row of 5 truck cards** (can also be 2-column grid on small screens):

  Each card (72dp wide × 110dp tall, 8dp radius):
  - Truck illustration (vector drawable, different truck size per option)
  - Weight label below truck
  - Price below weight (in blue #2563EB)
  - Selected state: blue border 2dp + light blue (#EFF6FF) background
  - Unselected: grey border 1dp + white background

  | Key | Label | Base Price | Multiplier |
  |---|---|---|---|
  | xsmall | 100 kg gacha | 10,000 UZS | ×1.0 |
  | small | 100–250 kg | 12,500 UZS | ×1.2 |
  | medium | 250–500 kg | 15,000 UZS | ×1.5 |
  | large | 500–750 kg | 17,500 UZS | ×2.0 |
  | xlarge | 750 kg–1 tonna | 20,000 UZS | ×2.5 |

- Below grid: "Asosiy narx" label on left, selected price on right in blue

- "Davom etish →" blue button (disabled until selection made)

**State passed forward:** loadSize (string key)

---

### SCREEN 5: Step3_UnloadingScreen

**Header:** "3 / 5 — Qo'shimcha ma'lumot" + LinearProgressIndicator 60%

**Layout (scrollable column, 16dp padding):**

**Section: "Tushirish yordami kerakmi?"**
- Two equal-width toggle buttons side by side (Row, fillMaxWidth):
  - Left: "✕ Yo'q, rahmat" — selected = grey fill; unselected = white
  - Right: "🖐 Ha, iltimos" — selected = blue fill + border; unselected = white
  - Default: "Ha, iltimos" selected (matches website default)
  - If "Ha" selected → adds 20,000 UZS to price

**Section: "Telefon raqam"**
- Label: "Telefon raqam"
- Row: Country code dropdown ("UZ +998" with Uzbekistan flag emoji 🇺🇿) + phone number OutlinedTextField
- Phone mask: XX XXX XX XX (format as user types)
- Validate: must be 9 digits after +998
- Pre-fill from last used phone (DataStore key: `last_phone`)

**Section: "Ismingiz" (optional)**
- OutlinedTextField for customer name
- Placeholder: "Ixtiyoriy"

- "Davom etish →" blue button (disabled until phone valid)

**State passed forward:** unloading (Boolean), customerPhone ("+998XXXXXXXXX"), customerName (String?)

---

### SCREEN 6: Step4_PriceScreen

**Header:** "4 / 5 — Narxni hisoblash" + LinearProgressIndicator 80%

**On screen entry → fire API calls:**
1. POST /api/route { start: [pickup.lat, pickup.lng], end: [delivery.lat, delivery.lng] }
   → gets distanceKm, durationMin
2. POST /api/price { distanceKm, loadSize, unloading }
   → gets authoritative price

**Loading state:** CircularProgressIndicator centered, text "Narx hisoblanmoqda..."

**Loaded state layout:**

Top half — Route summary card (white, 12dp radius, 1dp border):
- "📍 [Pickup label]" → "📍 [Delivery label]"
- Row: "🛣 2.28 km" · "⏱ ~5 min" (from API response)

Bottom half — **Price breakdown card** (background #1E40AF dark blue, white text, 12dp radius):

```
┌─────────────────────────────────────────┐  ← dark blue card
│  Taxminiy narx                          │
│  15,000 UZS          (28sp bold white)  │
│  🛣 2.28 km  ⏱ ~5 min                   │
│                                         │
│  NARX TAFSILOTLARI                      │  ← small caps label, white/60%
│  ─────────────────────────────          │
│  Asosiy narx                10,000 UZS  │
│  Masofa  2.28km·3,000/km=  6,840 UZS   │  ← white/80% text
│  Yuk koeffitsienti  100kg (×1)          │
│  Tushirish yordami         20,000 UZS   │
│  ─────────────────────────────          │
│  Jami                       36,840 UZS  │  ← bold white
│                                         │
│  ┌──────────────┐  ┌──────────────┐    │
│  │Platforma 60% │  │Haydovchi 40% │    │  ← two dark-blue sub-cards
│  │  22,104 UZS  │  │  14,736 UZS  │    │
│  └──────────────┘  └──────────────┘    │
└─────────────────────────────────────────┘
```

- Below card: "Davom etish →" blue button

**Store in ViewModel:** distanceKm, durationMin, finalPrice for order creation

---

### SCREEN 7: Step5_PaymentScreen

**Header:** "5 / 5 — To'lov usuli" + LinearProgressIndicator 100%

**Layout:**

Order summary mini-card (white, 12dp radius):
- Route: pickup → delivery
- Load: [weight label]
- Unloading: Ha / Yo'q
- Total: **[price] UZS** (bold blue)

"To'lov usuli" section label
- Single option card (selected, blue border):
  - 💵 "Naqd pul" (Cash) — "Haydovchiga to'lang" subtitle
  - Radio button selected

(Future: Card payment option — show as disabled/coming soon)

**"Buyurtma berish" (Place Order) button** — full width, blue, 52dp height
- On tap → POST /api/orders with full payload (see Section 5)
- Loading state: button shows CircularProgressIndicator
- On 201 success → navigate to OrderSuccessScreen
- On error → Snackbar with error message

---

### SCREEN 8: OrderSuccessScreen

- Centered layout, white background
- ✅ Large green checkmark icon (animated scale-in)
- "Buyurtma qabul qilindi!" (H2, bold)
- Order ID card: "Buyurtma №: **ORD-1001**" (monospace, blue)
- "Yetkazuvchingiz topilmoqda..." caption with pulsing dots animation
- Two buttons:
  - "Buyurtmani kuzatish" → OrderTrackingScreen (primary blue)
  - "Bosh sahifaga" → Step1 (ghost/outlined)

---

### SCREEN 9: OrderTrackingScreen

**Navigated to with orderId (_id) + customerPhone**

**Polling:** GET /api/orders/:id?phone={phone} every **5 seconds**
Stop polling when status is `delivered` or `cancelled`

**Layout:**

Top section — Route card (white):
- Pickup and delivery addresses
- Driver info (if assigned): name, phone (tap to call)

**Status Timeline** (vertical stepper):

```
● queue      "Haydovchi kutilmoqda"    ← filled circle if reached
● process    "Haydovchi yo'lda"
● pickedUp   "Yuk olindi"
● delivered  "Yetkazildi ✓"
```

Each step: circle (filled=completed in blue, empty=pending grey) + label + timestamp if available

If status = `cancelled`: show red "Bekor qilindi" pill + cancelReason

**Status → display mapping:**
- `queue` → "Haydovchi qidirilmoqda..." (pulsing)
- `process` → "Haydovchi yo'lda" (blue)
- `pickedUp` → "Yuk olib ketildi" (blue)
- `delivered` → "Yetkazildi!" (green)
- `cancelled` → "Bekor qilindi" (red)

After `delivered` → show "Baholash" (Rate) button → RatingBottomSheet

**RatingBottomSheet:**
- 5 star rating (tap to select, gold stars)
- Optional comment TextField
- "Yuborish" button → POST /api/orders/:id/review { rating, comment }
- After submit: "Rahmat!" confirmation + dismiss

---

### SCREEN 10: MyOrdersScreen

**Top bar:** "Buyurtmalarim"
**Input:** phone number field + "Yuklash" button → GET /api/orders/by-phone?phone=

**List:**
- Each order card (white, 12dp radius, 1dp border):
  - Left: status color indicator bar (3dp wide):
    - queue = grey, process = blue, pickedUp = blue, delivered = green, cancelled = red
  - Order ID: "ORD-1001" (bold)
  - Route: "[Pickup] → [Delivery]" (1 line, ellipsis)
  - Price: "[X] UZS" (blue, right-aligned)
  - Status chip (rounded pill): text + color
  - Date (caption, grey)
- Tap → OrderDetailScreen

**Empty state:** 📦 illustration + "Buyurtmalar topilmadi"
**Loading:** shimmer skeleton cards

---

## ═══════════════════════════════════════════
## SECTION 5: BACKEND API INTEGRATION
## ═══════════════════════════════════════════

### 5.1 Base URL Configuration

```kotlin
// BuildConfig (build.gradle)
debug:   BASE_URL = "http://10.0.2.2:5000/"   // emulator → host localhost
release: BASE_URL = "https://your-api.com/"

// All endpoints under /api/
// Full path example: http://10.0.2.2:5000/api/health
```

### 5.2 Retrofit Setup

```kotlin
// Single OkHttpClient
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor(tokenProvider))   // adds Bearer if token exists
    .addInterceptor(HttpLoggingInterceptor())          // debug only
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

// Two Retrofit instances or one with dynamic auth:
// - PublicApiService (no auth)
// - DriverApiService (requires Bearer token)
// - AdminApiService (requires Bearer token)
```

### 5.3 AuthInterceptor

```kotlin
class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.getToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else chain.request()
        return chain.proceed(request)
    }
}
```

### 5.4 Data Models (Kotlin data classes)

```kotlin
// --- Order creation ---
data class CreateOrderRequest(
    val customerPhone: String,      // "+998XXXXXXXXX"
    val customerName: String?,
    val pickup: LocationPayload,
    val delivery: LocationPayload,
    val loadSize: String,           // "xsmall"|"small"|"medium"|"large"|"xlarge"
    val unloading: Boolean,
    val price: Double,              // server will recalculate if differs >0.02
    val distanceKm: Double,
    val durationMin: Double
)

data class LocationPayload(
    val label: String,
    val coords: List<Double>        // [lat, lng]
)

// --- Order response ---
data class Order(
    val _id: String,
    val orderId: String,            // "ORD-1001"
    val customerPhone: String,
    val customerName: String?,
    val pickup: LocationPayload,
    val delivery: LocationPayload,
    val loadSize: String,
    val unloading: Boolean,
    val price: Double,
    val distanceKm: Double,
    val durationMin: Double,
    val status: String,             // "queue"|"process"|"pickedUp"|"delivered"|"cancelled"
    val cancelReason: String?,
    val driverId: DriverInfo?,
    val review: Review?,
    val createdAt: String,
    val completedAt: String?
)

data class DriverInfo(
    val username: String,
    val name: String,
    val phone: String?
)

data class Review(
    val rating: Int,
    val comment: String?
)

// --- Price & Route ---
data class RouteRequest(val start: List<Double>, val end: List<Double>)
data class RouteResponse(val distanceKm: Double, val durationMin: Double, val geometry: Any?)

data class PriceRequest(val distanceKm: Double, val loadSize: String, val unloading: Boolean)
data class PriceResponse(val price: Double)

// --- Review ---
data class ReviewRequest(val rating: Int, val comment: String?)

// --- Driver auth ---
data class DriverLoginRequest(val username: String, val password: String)
data class DriverLoginResponse(val token: String, val user: DriverUser)
data class DriverUser(val id: String, val username: String, val name: String, val active: Boolean)

// --- Driver location update ---
data class LocationUpdateRequest(val lat: Double, val lng: Double)

// --- Available order (driver) ---
// Same Order model; status will be "queue"
```

### 5.5 API Service Interfaces

```kotlin
interface PublicApiService {
    @GET("api/health")
    suspend fun health(): HealthResponse

    @POST("api/route")
    suspend fun getRoute(@Body body: RouteRequest): RouteResponse

    @POST("api/price")
    suspend fun getPrice(@Body body: PriceRequest): PriceResponse

    @POST("api/orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): Order

    @GET("api/orders/by-phone")
    suspend fun getOrdersByPhone(@Query("phone") phone: String): List<Order>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String,
        @Query("phone") phone: String? = null
    ): Order

    @POST("api/orders/{id}/review")
    suspend fun submitReview(@Path("id") id: String, @Body body: ReviewRequest): Order
}

interface DriverApiService {
    @POST("api/auth/driver/login")
    suspend fun login(@Body body: DriverLoginRequest): DriverLoginResponse

    @GET("api/driver/orders/available")
    suspend fun getAvailableOrders(): List<Order>

    @POST("api/driver/orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") id: String): Order

    @POST("api/driver/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Order

    @POST("api/driver/orders/{id}/picked-up")
    suspend fun pickUpOrder(@Path("id") id: String): Order

    @POST("api/driver/orders/{id}/delivered")
    suspend fun deliverOrder(@Path("id") id: String): Order

    @GET("api/driver/me")
    suspend fun getProfile(): DriverProfile

    @PATCH("api/driver/location")
    suspend fun updateLocation(@Body body: LocationUpdateRequest): LocationUpdateResponse
}
```

### 5.6 Error Handling

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// In Repository:
suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val message = parseErrorMessage(errorBody) ?: e.message()
        if (e.code() == 401) clearStoredToken()
        ApiResult.Error(e.code(), message)
    } catch (e: IOException) {
        ApiResult.NetworkError
    }
}
```

HTTP error codes to handle:
- **400** → Show validation error message from `details` array
- **401** → Clear token → redirect to login (driver/admin)
- **403** → "Ruxsat yo'q" (for phone mismatch on order detail)
- **404** → "Topilmadi"
- **429** → "Juda ko'p so'rovlar, biroz kuting" — use exponential backoff
- **500** → "Server xatoligi" + retry button

### 5.7 Customer Booking Flow (API sequence)

```
Step 1: User selects pickup + delivery (no API call yet)
Step 2: User selects load size (no API call yet)
Step 3: User enters phone + unloading (no API call yet)
Step 4: Screen opens →
    → POST /api/route { start: [pickup.lat, pickup.lng], end: [delivery.lat, delivery.lng] }
       gets distanceKm, durationMin
    → POST /api/price { distanceKm, loadSize, unloading }
       gets authoritative price
Step 5: User taps "Buyurtma berish" →
    → POST /api/orders {
         customerPhone, customerName,
         pickup: { label, coords: [lat, lng] },
         delivery: { label, coords: [lat, lng] },
         loadSize, unloading,
         price,        ← from Step 4 API response
         distanceKm,   ← from Step 4 API response
         durationMin   ← from Step 4 API response
      }
    ← 201 { _id, orderId, status: "queue", ... }

Tracking: Poll GET /api/orders/:id?phone={phone} every 5s
           Stop when status = "delivered" | "cancelled"
```

### 5.8 Phone Normalization

```kotlin
fun normalizePhone(input: String): String {
    // Remove all non-digits
    val digits = input.filter { it.isDigit() }
    return when {
        digits.startsWith("998") && digits.length == 12 -> "+$digits"
        digits.length == 9 -> "+998$digits"
        else -> "+998$digits"
    }
}
// Store and send normalized form: "+998901234567"
// Display with mask: +998 (90) 123-45-67
```

### 5.9 Polling Implementation

```kotlin
// In ViewModel:
private var pollingJob: Job? = null

fun startPolling(orderId: String, phone: String) {
    pollingJob = viewModelScope.launch {
        while (isActive) {
            val result = orderRepository.getOrderById(orderId, phone)
            if (result is ApiResult.Success) {
                _orderState.value = result.data
                if (result.data.status in listOf("delivered", "cancelled")) {
                    break  // Stop polling when terminal
                }
            }
            delay(5_000)  // 5 second interval
        }
    }
}

override fun onCleared() {
    pollingJob?.cancel()
    super.onCleared()
}
```

---

## ═══════════════════════════════════════════
## SECTION 6: DRIVER APP
## ═══════════════════════════════════════════

### DriverLoginScreen
- Logo + "Haydovchi tizimga kirish"
- Username + Password fields (password toggle visibility)
- "Kirish" button → POST /api/auth/driver/login
- Store token in EncryptedSharedPreferences key: `driver_jwt`
- On 403 (inactive): "Hisobingiz faol emas" error dialog

### DriverHomeScreen
- TopAppBar: "YUK 24 Haydovchi" + logout icon
- Pull-to-refresh + auto-refresh every 10s: GET /api/driver/orders/available
- **AvailableOrderCard** (white, 12dp radius):
  - Load size chip (blue pill): "250–500 kg"
  - Route: "[Pickup] → [Delivery]"
  - Distance: "3.4 km"
  - Price: "**52,000 UZS**" (bold blue, right-aligned)
  - Two buttons: "Rad etish" (outlined grey) | "Qabul qilish" (filled blue)
- Bottom nav: 🏠 Asosiy | 📋 Faol buyurtma | 👤 Profil

### DriverActiveJobScreen
- Appears when driver has accepted an order
- Map showing pickup pin + delivery pin + route polyline
- Vertical status stepper (same as customer tracking)
- Current step highlighted with blue
- Action button changes per status:
  - After accept → "Olish joyiga yetib keldim" (I've arrived at pickup) → POST /picked-up
  - After picked-up → "Yetkazib berdim" (Delivered) → POST /delivered
- Location update: PATCH /api/driver/location every 30 seconds while job is active
  - Use FusedLocationProviderClient
  - Foreground service for background location (Android 10+ compliance)

### DriverProfileScreen
- GET /api/driver/me
- Shows: name, username, vehicleInfo
- Stats: completed orders, cancelled orders, avg delivery time
- "Chiqish" (Logout) button → clear token → DriverLoginScreen

---

## ═══════════════════════════════════════════
## SECTION 7: PRICING FORMULA (CLIENT-SIDE ESTIMATE)
## ═══════════════════════════════════════════

Use for offline estimates before API call. Always use API response as final.

```kotlin
object PricingUtils {
    // From backend src/utils/pricing.js
    private const val BASE_KM = 5.0
    private const val BASE_PRICE = 10_000.0      // UZS (frontend values)
    private const val PRICE_PER_KM = 3_000.0     // UZS per km
    private const val UNLOADING_FEE = 20_000.0   // UZS

    private val MULTIPLIERS = mapOf(
        "xsmall" to 1.0,
        "small"  to 1.2,
        "medium" to 1.5,
        "large"  to 2.0,
        "xlarge" to 2.5
    )

    fun calculate(distanceKm: Double, loadSize: String, unloading: Boolean): Double {
        val multiplier = MULTIPLIERS[loadSize] ?: 1.0
        val distComponent = maxOf(0.0, distanceKm - BASE_KM) * PRICE_PER_KM + BASE_PRICE
        var price = distComponent * multiplier
        if (unloading) price += UNLOADING_FEE
        return Math.round(price * 100.0) / 100.0
    }

    fun format(price: Double): String {
        // "15,000 UZS"
        return "${"%,.0f".format(price)} UZS"
    }
}
```

Load size display names:
```kotlin
val LOAD_SIZE_LABELS = mapOf(
    "xsmall" to "100 kg gacha",
    "small"  to "100–250 kg",
    "medium" to "250–500 kg",
    "large"  to "500–750 kg",
    "xlarge" to "750 kg–1 tonna"
)
val LOAD_SIZE_BASE_PRICES = mapOf(
    "xsmall" to "10,000 UZS",
    "small"  to "12,500 UZS",
    "medium" to "15,000 UZS",
    "large"  to "17,500 UZS",
    "xlarge" to "20,000 UZS"
)
```

---

## ═══════════════════════════════════════════
## SECTION 8: LOCAL STORAGE & STATE PERSISTENCE
## ═══════════════════════════════════════════

Use **DataStore (Preferences)** for all persistent app state:

```kotlin
// Keys
val LAST_PHONE = stringPreferencesKey("last_phone")
val DRIVER_TOKEN = stringPreferencesKey("driver_jwt")   // encrypted
val LAST_ORDER_ID = stringPreferencesKey("last_order_id")
val APP_LANGUAGE = stringPreferencesKey("app_language") // "uz" | "ru"
```

Use **EncryptedSharedPreferences** for driver token:
```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
val encryptedPrefs = EncryptedSharedPreferences.create(
    context, "yuk24_secure", masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

**Booking state** (across steps): pass via ViewModel shared between steps OR Navigation arguments:
- Use a single `BookingViewModel` scoped to the NavGraph
- Each step updates its field in the shared ViewModel
- On app kill mid-booking: save partial state to DataStore and restore

---

## ═══════════════════════════════════════════
## SECTION 9: PERMISSIONS
## ═══════════════════════════════════════════

Declare in AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<!-- Driver only: -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

Runtime permission flow:
- On Step1_MapScreen: request `ACCESS_FINE_LOCATION`
  - Granted → animate to user location
  - Denied → use Tashkent default + show "Joylashuvga ruxsat bering" info banner
- Driver active job: request background location + start foreground service

Cleartext HTTP (dev only):
```xml
<!-- debug/AndroidManifest.xml -->
<application android:usesCleartextTraffic="true" ... />
```

---

## ═══════════════════════════════════════════
## SECTION 10: BUILD VARIANTS & CONFIG
## ═══════════════════════════════════════════

```groovy
// build.gradle (app)
buildTypes {
    debug {
        buildConfigField "String", "BASE_URL", '"http://10.0.2.2:5000/"'
        buildConfigField "String", "MAPS_API_KEY", '"YOUR_DEBUG_KEY"'
    }
    release {
        buildConfigField "String", "BASE_URL", '"https://your-api.com/"'
        buildConfigField "String", "MAPS_API_KEY", '"YOUR_RELEASE_KEY"'
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
    }
}

// Product flavors (optional):
flavorDimensions += "role"
productFlavors {
    customer { dimension "role" }
    driver   { dimension "role" }
}
```

---

## ═══════════════════════════════════════════
## SECTION 11: MAP INTEGRATION
## ═══════════════════════════════════════════

**Option A (Recommended for web parity): OSMDroid** — uses OpenStreetMap tiles, same as the web app's Leaflet.

**Option B: Google Maps SDK** — smoother Android experience.

Either way implement:

1. **Map Composable** wrapping `MapView` (OSMDroid) or `GoogleMap` (Google Maps)
2. **Two markers**: draggable red (pickup) + green (delivery)
3. **Route polyline**: drawn after route API call, blue dashed line
4. **Camera animation**: `animateCamera` to fit both markers in view with padding
5. **Map tap**: tapping map sets the currently-active pin (pickup or delivery)
6. **Geocoding**: use backend `/api/route` for route; for address search use OpenRouteService geocode API bounded to Uzbekistan:
   ```
   GET https://api.openrouteservice.org/geocode/search
     ?api_key={KEY}&text={query}&boundary.country=UZ&size=5
   ```
   **Never hardcode ORS key in release APK** — proxy through your backend or use backend `/api/route`.

---

## ═══════════════════════════════════════════
## SECTION 12: MVVM ARCHITECTURE LAYOUT
## ═══════════════════════════════════════════

```
app/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── PublicApiService.kt
│   │   │   └── DriverApiService.kt
│   │   ├── dto/              ← API request/response models
│   │   └── RemoteDataSource.kt
│   ├── local/
│   │   ├── DataStoreManager.kt
│   │   └── EncryptedPrefsManager.kt
│   └── repository/
│       ├── OrderRepository.kt
│       ├── RouteRepository.kt
│       └── DriverRepository.kt
├── domain/
│   ├── model/                ← Clean domain models
│   │   ├── Order.kt
│   │   ├── BookingState.kt
│   │   └── Driver.kt
│   └── usecase/
│       ├── CreateOrderUseCase.kt
│       ├── GetOrderStatusUseCase.kt
│       ├── CalculatePriceUseCase.kt
│       └── GetAvailableOrdersUseCase.kt
├── presentation/
│   ├── customer/
│   │   ├── booking/
│   │   │   ├── BookingViewModel.kt     ← Shared across steps
│   │   │   ├── Step1MapScreen.kt
│   │   │   ├── Step2LoadSizeScreen.kt
│   │   │   ├── Step3UnloadingScreen.kt
│   │   │   ├── Step4PriceScreen.kt
│   │   │   └── Step5PaymentScreen.kt
│   │   ├── tracking/
│   │   │   ├── TrackingViewModel.kt
│   │   │   └── OrderTrackingScreen.kt
│   │   └── orders/
│   │       ├── MyOrdersViewModel.kt
│   │       └── MyOrdersScreen.kt
│   ├── driver/
│   │   ├── DriverLoginScreen.kt
│   │   ├── DriverHomeScreen.kt
│   │   ├── DriverViewModel.kt
│   │   └── DriverActiveJobScreen.kt
│   └── common/
│       ├── components/       ← Reusable Composables
│       └── theme/            ← Color.kt, Theme.kt, Type.kt
├── di/
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
└── MainActivity.kt           ← Single activity, NavHost
```

---

## ═══════════════════════════════════════════
## SECTION 13: STRINGS (strings.xml excerpt)
## ═══════════════════════════════════════════

```xml
<!-- values/strings.xml (Uzbek default) -->
<string name="app_name">YUK 24</string>
<string name="tagline">Yuk tashish xizmati</string>
<string name="step_map_title">Manzilni tanlang</string>
<string name="step_load_title">Yuk og\'irligini tanlang</string>
<string name="step_info_title">Qo\'shimcha ma\'lumot</string>
<string name="step_price_title">Narxni hisoblash</string>
<string name="step_payment_title">To\'lov usuli</string>
<string name="pickup_label">Olish joyi</string>
<string name="delivery_label">Yetkazish joyi</string>
<string name="unloading_question">Tushirish yordami kerakmi?</string>
<string name="unloading_yes">Ha, iltimos</string>
<string name="unloading_no">Yo\'q, rahmat</string>
<string name="phone_label">Telefon raqam</string>
<string name="name_label">Ismingiz</string>
<string name="calculate_price">Narxni hisoblash</string>
<string name="place_order">Buyurtma berish</string>
<string name="cash_payment">Naqd pul</string>
<string name="order_success">Buyurtma qabul qilindi!</string>
<string name="track_order">Buyurtmani kuzatish</string>
<string name="my_orders">Buyurtmalarim</string>
<string name="base_price">Asosiy narx</string>
<string name="distance_label">Masofa</string>
<string name="load_coefficient">Yuk koeffitsienti</string>
<string name="unloading_fee">Tushirish yordami</string>
<string name="total">Jami</string>
<string name="platform_share">Platforma ulushi (60%%)</string>
<string name="driver_share">Haydovchi ulushi (40%%)</string>
<string name="estimated_price">Taxminiy narx</string>
<string name="status_queue">Haydovchi qidirilmoqda</string>
<string name="status_process">Haydovchi yo\'lda</string>
<string name="status_picked_up">Yuk olib ketildi</string>
<string name="status_delivered">Yetkazildi!</string>
<string name="status_cancelled">Bekor qilindi</string>
<string name="error_network">Internet aloqasi yo\'q</string>
<string name="error_server">Server xatoligi</string>
<string name="retry">Qayta urinish</string>
```

---

## ═══════════════════════════════════════════
## SECTION 14: NON-FUNCTIONAL REQUIREMENTS
## ═══════════════════════════════════════════

| Area | Requirement |
|---|---|
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 35 |
| Security | HTTPS only in release; EncryptedSharedPreferences for tokens; no secrets in APK |
| Accessibility | TalkBack labels on all interactive elements; min 48dp touch targets |
| Performance | Debounce address search input 500ms; skeleton loaders on all async screens |
| Offline | Cached last order list; retry queue for failed order creation; clear error messages |
| Location | Handle permission denied gracefully; Tashkent default fallback |
| Polling | Max 5s interval; stop on terminal status; cancel on screen exit |
| Rate limiting | On 429: exponential backoff (1s, 2s, 4s); show "Biroz kuting..." message |
| Driver background | Foreground service for location (Android 10+ policy compliant) |

---

## ═══════════════════════════════════════════
## SECTION 15: IMPLEMENTATION ORDER (BACKLOG)
## ═══════════════════════════════════════════

**Sprint 1 — Customer MVP**
1. Project setup: Hilt + Retrofit + Compose + Navigation
2. Theme/Design system (colors, typography, shapes)
3. SplashScreen + RoleSelectScreen
4. Step1_MapScreen (map + location permission + address search)
5. Step2_LoadSizeScreen
6. Step3_UnloadingScreen (phone input + unloading toggle)
7. Step4_PriceScreen (route + price API calls)
8. Step5_PaymentScreen + POST /api/orders
9. OrderSuccessScreen
10. OrderTrackingScreen (polling)
11. MyOrdersScreen

**Sprint 2 — Driver MVP**
12. DriverLoginScreen + JWT storage
13. DriverHomeScreen (available orders)
14. DriverActiveJobScreen (status updates + location PATCH)
15. Driver foreground service for background location

**Sprint 3 — Polish**
16. Rating flow
17. Russian locale
18. Push notifications (FCM)
19. Offline cache + retry
20. ProGuard + release build

---

*End of YUK 24 Android App Prompt — v1.0*
*Next update: add Admin panel screens if needed.*
