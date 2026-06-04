package uz.yuk24.app.domain.model

data class BookingState(
    val pickup: LocationPoint? = null,
    val delivery: LocationPoint? = null,
    val loadSize: LoadSize? = null,
    val unloading: Boolean = true,
    val customerPhone: String = "",
    val customerName: String = "",
    val distanceKm: Double? = null,
    val durationMin: Double? = null,
    val finalPrice: Double? = null
) {
    val hasRoute: Boolean get() = pickup != null && delivery != null
    val canCalculatePrice: Boolean get() = hasRoute && loadSize != null
    val canPlaceOrder: Boolean get() = canCalculatePrice
        && customerPhone.length >= 9
        && finalPrice != null
        && distanceKm != null
        && durationMin != null
}
