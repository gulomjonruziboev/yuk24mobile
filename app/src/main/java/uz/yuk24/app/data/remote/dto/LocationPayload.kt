package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationPayload(
    val label: String,
    val coords: List<Double>
)
