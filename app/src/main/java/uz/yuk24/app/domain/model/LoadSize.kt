package uz.yuk24.app.domain.model

/**
 * Cargo weight tier sent to the API as [key] (`POST /api/price`, `POST /api/orders`).
 *
 * [multiplier] is the single source of truth for load pricing on the client and must
 * match the backend formula: `(BASE_PRICE + distanceKm * PRICE_PER_KM) * multiplier + unloading`.
 *
 * Backend contract: xsmall=1.0, small=1.2, medium=1.5, large=2.0, xlarge=2.5.
 */
enum class LoadSize(val key: String, val multiplier: Double) {
    XSMALL("xsmall", 1.0),
    SMALL("small", 1.2),
    MEDIUM("medium", 1.5),
    LARGE("large", 2.0),
    XLARGE("xlarge", 2.5);

    companion object {
        fun fromKey(key: String?): LoadSize? = entries.firstOrNull { it.key == key }
    }
}
