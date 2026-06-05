package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    @SerialName("_id") val id: String,
    val orderId: String,
    val customerPhone: String,
    val customerName: String? = null,
    val pickup: LocationPayload,
    val delivery: LocationPayload,
    val loadSize: String,
    val unloading: Boolean,
    val price: Double,
    val distanceKm: Double,
    val durationMin: Double,
    val status: String,
    val cancelReason: String? = null,
    @Serializable(with = DriverIdSerializer::class)
    val driverId: DriverInfoDto? = null,
    val review: ReviewDto? = null,
    val createdAt: String,
    val completedAt: String? = null
)

@Serializable
data class DriverInfoDto(
    val username: String? = null,
    val name: String? = null,
    val phone: String? = null
)

@Serializable
data class ReviewDto(
    val rating: Int,
    val comment: String? = null
)
