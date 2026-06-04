package uz.yuk24.app.domain.model

import uz.yuk24.app.data.remote.dto.OrderDto

data class Order(
    val id: String,
    val orderId: String,
    val customerPhone: String,
    val customerName: String?,
    val pickup: LocationPoint,
    val delivery: LocationPoint,
    val loadSize: LoadSize?,
    val loadSizeKey: String,
    val unloading: Boolean,
    val price: Double,
    val distanceKm: Double,
    val durationMin: Double,
    val status: OrderStatus,
    val cancelReason: String?,
    val driverName: String?,
    val driverPhone: String?,
    val rating: Int?,
    val reviewComment: String?,
    val createdAt: String,
    val completedAt: String?
) {
    companion object {
        fun fromDto(dto: OrderDto): Order = Order(
            id = dto.id,
            orderId = dto.orderId,
            customerPhone = dto.customerPhone,
            customerName = dto.customerName,
            pickup = LocationPoint.fromPayload(dto.pickup)
                ?: LocationPoint(dto.pickup.label, 0.0, 0.0),
            delivery = LocationPoint.fromPayload(dto.delivery)
                ?: LocationPoint(dto.delivery.label, 0.0, 0.0),
            loadSize = LoadSize.fromKey(dto.loadSize),
            loadSizeKey = dto.loadSize,
            unloading = dto.unloading,
            price = dto.price,
            distanceKm = dto.distanceKm,
            durationMin = dto.durationMin,
            status = OrderStatus.fromKey(dto.status),
            cancelReason = dto.cancelReason,
            driverName = dto.driverId?.name,
            driverPhone = dto.driverId?.phone,
            rating = dto.review?.rating,
            reviewComment = dto.review?.comment,
            createdAt = dto.createdAt,
            completedAt = dto.completedAt
        )
    }
}

enum class OrderStatus(val key: String) {
    QUEUE("queue"),
    PROCESS("process"),
    PICKED_UP("pickedUp"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    val isTerminal: Boolean get() = this == DELIVERED || this == CANCELLED

    companion object {
        fun fromKey(key: String?): OrderStatus = entries.firstOrNull { it.key == key } ?: QUEUE
    }
}
