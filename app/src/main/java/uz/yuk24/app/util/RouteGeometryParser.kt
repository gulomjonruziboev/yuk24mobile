package uz.yuk24.app.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import uz.yuk24.app.domain.model.LatLng

/**
 * Parses GeoJSON from `POST /api/route` into map points.
 * Supports bare LineString, Feature, and FeatureCollection (ORS default).
 * Coordinates are `[lng, lat]` per GeoJSON; we expose `(lat, lng)` for the UI.
 */
object RouteGeometryParser {

    fun parse(geometry: JsonElement?): List<LatLng> {
        if (geometry == null || geometry !is JsonObject) return emptyList()
        return when (geometry["type"]?.jsonPrimitive?.content) {
            "LineString" -> parseLineString(geometry)
            "Feature" -> geometry["geometry"]?.let { parse(it) }.orEmpty()
            "FeatureCollection" -> {
                val features = geometry["features"]?.jsonArray ?: return emptyList()
                val first = features.firstOrNull() as? JsonObject ?: return emptyList()
                parse(first)
            }
            else -> emptyList()
        }
    }

    private fun parseLineString(obj: JsonObject): List<LatLng> {
        val coords = obj["coordinates"] ?: return emptyList()
        if (coords !is JsonArray) return emptyList()
        return coords.mapNotNull { point ->
            if (point !is JsonArray || point.size < 2) return@mapNotNull null
            val lng = point[0].jsonPrimitive.content.toDoubleOrNull() ?: return@mapNotNull null
            val lat = point[1].jsonPrimitive.content.toDoubleOrNull() ?: return@mapNotNull null
            LatLng(lat = lat, lng = lng)
        }
    }
}
