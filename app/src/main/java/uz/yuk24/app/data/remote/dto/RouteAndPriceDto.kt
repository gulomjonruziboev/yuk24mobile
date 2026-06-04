package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RouteRequest(
    val start: List<Double>,
    val end: List<Double>
)

@Serializable
data class RouteResponse(
    val distanceKm: Double,
    val durationMin: Double,
    val geometry: JsonElement? = null
)

@Serializable
data class PriceRequest(
    val distanceKm: Double,
    val loadSize: String,
    val unloading: Boolean
)

@Serializable
data class PriceResponse(
    val price: Double
)

@Serializable
data class HealthResponse(
    val status: String? = null,
    val ok: Boolean? = null
)

@Serializable
data class ReviewRequest(
    val rating: Int,
    val comment: String? = null
)
