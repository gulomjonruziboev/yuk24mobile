package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrsGeocodeResponse(
    val features: List<OrsGeocodeFeature>? = null
)

@Serializable
data class OrsGeocodeFeature(
    val properties: OrsGeocodeProperties? = null
)

@Serializable
data class OrsGeocodeProperties(
    val label: String? = null,
    val name: String? = null
)
