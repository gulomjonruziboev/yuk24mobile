package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val customerPhone: String,
    val customerName: String? = null,
    val pickup: LocationPayload,
    val delivery: LocationPayload,
    val loadSize: String,
    val unloading: Boolean,
    val price: Double,
    val distanceKm: Double,
    val durationMin: Double
)
