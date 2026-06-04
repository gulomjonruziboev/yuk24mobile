package uz.yuk24.app.domain.model

/**
 * Lightweight (lat, lng) pair for raw geometry — used when we don't have a
 * label or want to avoid pulling in any map-SDK type at the domain layer.
 */
data class LatLng(
    val lat: Double,
    val lng: Double
)
