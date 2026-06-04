package uz.yuk24.app.data.repository

import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.data.remote.api.PublicApiService
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
    private val api: PublicApiService
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

    /**
     * Road-following polyline via backend `POST /api/route` (ORS runs on server).
     * Empty list when geometry is null or the request fails — map falls back to straight line.
     */
    suspend fun getRoadGeometry(
        pickup: Pair<Double, Double>,
        delivery: Pair<Double, Double>
    ): ApiResult<List<LatLng>> = when (val route = getRoute(pickup, delivery)) {
        is ApiResult.Success -> ApiResult.Success(RouteGeometryParser.parse(route.data.geometry))
        is ApiResult.Error -> route
        ApiResult.NetworkError -> ApiResult.NetworkError
        ApiResult.Loading -> ApiResult.Loading
    }
}
