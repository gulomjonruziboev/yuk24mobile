package uz.yuk24.app.util

import uz.yuk24.app.domain.model.LoadSize
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Canonical pricing formula for YUK 24.
 *
 *     distanceFee          = distanceKm * PRICE_PER_KM
 *     preMultSubtotal      = BASE_PRICE + distanceFee
 *     coefficientSurcharge = preMultSubtotal * (multiplier - 1)
 *     total                = preMultSubtotal * multiplier + (UNLOADING_FEE if unloading else 0)
 *                          = BASE_PRICE + distanceFee + coefficientSurcharge + unloadingFee
 *
 * Per-km is charged from km 0 (no free distance).
 *
 * Load multipliers come from [LoadSize.multiplier] only. The server `POST /api/price`
 * endpoint must mirror this formula; server result is authoritative for checkout.
 */
object PricingUtils {

    const val BASE_PRICE = 10_000.0
    const val PRICE_PER_KM = 3_000.0
    const val UNLOADING_FEE = 20_000.0

    fun distanceFee(distanceKm: Double): Double = distanceKm * PRICE_PER_KM

    fun preMultSubtotal(distanceKm: Double): Double = BASE_PRICE + distanceFee(distanceKm)

    fun coefficientSurcharge(distanceKm: Double, multiplier: Double): Double =
        preMultSubtotal(distanceKm) * (multiplier - 1.0)

    fun coefficientSurcharge(distanceKm: Double, loadSize: LoadSize): Double =
        coefficientSurcharge(distanceKm, loadSize.multiplier)

    fun unloadingFee(unloading: Boolean): Double = if (unloading) UNLOADING_FEE else 0.0

    fun calculate(distanceKm: Double, loadSize: LoadSize, unloading: Boolean): Double {
        val price = preMultSubtotal(distanceKm) * loadSize.multiplier + unloadingFee(unloading)
        return (price * 100.0).roundToLong() / 100.0
    }

    fun calculateOrZero(distanceKm: Double?, loadSize: LoadSize?, unloading: Boolean): Double {
        if (distanceKm == null || loadSize == null) return 0.0
        return calculate(distanceKm, loadSize, unloading)
    }

    /** Lower-bound price for a given load size (0 km, no unloading): BASE_PRICE * multiplier. */
    fun minPrice(loadSize: LoadSize): Double = BASE_PRICE * loadSize.multiplier

    fun format(price: Double): String {
        val rounded = price.roundToLong()
        return "${"%,d".format(Locale.US, rounded)} UZS"
    }

    fun formatPlain(price: Double): String {
        val rounded = price.roundToLong()
        return "%,d".format(Locale.US, rounded)
    }
}
