package uz.yuk24.app.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.yuk24.app.R
import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.data.remote.api.OrsApiService
import uz.yuk24.app.data.remote.safeApiCall
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves map coordinates to a driver-readable place name (street, district, city).
 */
@Singleton
class AddressGeocoder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orsApi: OrsApiService
) {
    suspend fun resolveLabel(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        resolveWithAndroidGeocoder(lat, lng)
            ?: resolveWithOrs(lat, lng)
            ?: context.getString(R.string.address_unknown)
    }

    private fun resolveWithAndroidGeocoder(lat: Double, lng: Double): String? {
        if (!Geocoder.isPresent()) return null
        val locales = listOf(Locale("uz", "UZ"), Locale("ru", "UZ"), Locale.getDefault())
        for (locale in locales) {
            try {
                @Suppress("DEPRECATION")
                val list = Geocoder(context, locale).getFromLocation(lat, lng, 1)
                val formatted = list?.firstOrNull()?.let { formatAddress(it) }
                if (!formatted.isNullOrBlank()) return formatted
            } catch (_: IOException) {
            } catch (_: IllegalArgumentException) {
            }
        }
        return null
    }

    private suspend fun resolveWithOrs(lat: Double, lng: Double): String? {
        val result = safeApiCall {
            orsApi.reverseGeocode(lat = lat, lon = lng)
        }
        if (result !is ApiResult.Success) return null
        val props = result.data.features?.firstOrNull()?.properties ?: return null
        return props.label?.takeIf { it.isNotBlank() }
            ?: props.name?.takeIf { it.isNotBlank() }
    }

    private fun formatAddress(address: Address): String? {
        address.getAddressLine(0)?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
        val parts = listOfNotNull(
            address.thoroughfare?.trim(),
            address.subThoroughfare?.trim(),
            address.featureName?.trim(),
            address.locality?.trim(),
            address.subAdminArea?.trim(),
            address.adminArea?.trim()
        ).filter { it.isNotBlank() }.distinct()
        return parts.joinToString(", ").takeIf { it.isNotBlank() }
    }
}
