package uz.yuk24.app.data.repository

import kotlinx.serialization.json.Json
import uz.yuk24.app.BuildConfig
import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.data.remote.api.OrsApiService
import uz.yuk24.app.data.remote.api.PublicApiService
import uz.yuk24.app.data.remote.dto.OrsDirectionsRequest
import uz.yuk24.app.data.remote.dto.toGeoJsonElement
import uz.yuk24.app.data.remote.dto.PriceRequest
import uz.yuk24.app.data.remote.dto.PriceResponse
import uz.yuk24.app.data.remote.dto.RouteRequest
import uz.yuk24.app.data.remote.dto.RouteResponse
import uz.yuk24.app.data.remote.safeApiCall
import uz.yuk24.app.domain.model.LatLng
import uz.yuk24.app.util.RouteGeometryParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    private val api: PublicApiService,
    private val orsApi: OrsApiService,
    private val json: Json
) {
    suspend fun getRoute(
        pickup: Pair<Double, Double>,
        delivery: Pair<Double, Double>
    ): ApiResult<RouteResponse> = safeApiCall {
        api.getRoute(
            RouteRequest(
                start = listOf(pickup.first, pickup.second),
                end = listOf(delivery.first, delivery.second)
            )
        )
    }

    suspend fun getPrice(
        distanceKm: Double,
        loadSize: String,
        unloading: Boolean
    ): ApiResult<PriceResponse> = safeApiCall {
        api.getPrice(PriceRequest(distanceKm, loadSize, unloading))
    }

    /** Map polyline from backend `geometry` only. */
    fun mapGeometry(route: RouteResponse): List<LatLng> =
        RouteGeometryParser.parse(route.geometry)

    /**
     * Map polyline: backend geometry first, then direct ORS (same as legacy app) when empty.
     * Distance/duration for pricing always come from [getRoute], not from ORS.
     */
    suspend fun resolveMapGeometry(
        route: RouteResponse,
        pickup: Pair<Double, Double>,
        delivery: Pair<Double, Double>
    ): List<LatLng> {
        val fromBackend = mapGeometry(route)
        if (fromBackend.size >= 2) return fromBackend
        return fetchOrsMapGeometry(pickup, delivery)
    }

    /**
     * Road-following polyline: backend route + ORS fallback for map shape.
     */
    suspend fun getRoadGeometry(
        pickup: Pair<Double, Double>,
        delivery: Pair<Double, Double>
    ): ApiResult<List<LatLng>> = when (val route = getRoute(pickup, delivery)) {
        is ApiResult.Success -> ApiResult.Success(resolveMapGeometry(route.data, pickup, delivery))
        is ApiResult.Error -> route
        ApiResult.NetworkError -> ApiResult.NetworkError
        ApiResult.Loading -> ApiResult.Loading
    }

    private suspend fun fetchOrsMapGeometry(
        pickup: Pair<Double, Double>,
        delivery: Pair<Double, Double>
    ): List<LatLng> {
        if (BuildConfig.ORS_API_KEY.isBlank()) return emptyList()
        val result = safeApiCall {
            orsApi.getDirections(
                OrsDirectionsRequest(
                    coordinates = listOf(
                        listOf(pickup.second, pickup.first),
                        listOf(delivery.second, delivery.first)
                    )
                )
            )
        }
        if (result !is ApiResult.Success) return emptyList()
        return RouteGeometryParser.parse(result.data.toGeoJsonElement(json))
    }
}
