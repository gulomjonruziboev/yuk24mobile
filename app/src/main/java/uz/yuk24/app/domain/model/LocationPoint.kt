package uz.yuk24.app.domain.model

import uz.yuk24.app.data.remote.dto.LocationPayload

data class LocationPoint(
    val label: String,
    val lat: Double,
    val lng: Double
) {
    fun toPayload(): LocationPayload = LocationPayload(label = label, coords = listOf(lat, lng))

    companion object {
        fun fromPayload(payload: LocationPayload?): LocationPoint? {
            if (payload == null || payload.coords.size < 2) return null
            return LocationPoint(
                label = payload.label,
                lat = payload.coords[0],
                lng = payload.coords[1]
            )
        }
    }
}
