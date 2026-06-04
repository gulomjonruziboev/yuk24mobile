package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** ORS driving directions request. Coordinates are `[longitude, latitude]`. */
@Serializable
data class OrsDirectionsRequest(
    val coordinates: List<List<Double>>
)

/** ORS GeoJSON response (FeatureCollection with route geometry). */
@Serializable
data class OrsDirectionsResponse(
    val type: String? = null,
    val features: List<OrsFeature>? = null
)

@Serializable
data class OrsFeature(
    val type: String? = null,
    val geometry: JsonElement? = null
)

fun OrsDirectionsResponse.toGeoJsonElement(json: Json): JsonElement =
    json.encodeToJsonElement(OrsDirectionsResponse.serializer(), this)
