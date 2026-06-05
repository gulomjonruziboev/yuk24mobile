package uz.yuk24.app.domain.usecase

import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.data.remote.dto.CreateOrderRequest
import uz.yuk24.app.data.remote.dto.PriceResponse
import uz.yuk24.app.data.remote.dto.RouteResponse
import uz.yuk24.app.data.repository.OrderRepository
import uz.yuk24.app.data.repository.RouteRepository
import uz.yuk24.app.domain.model.LatLng
import uz.yuk24.app.domain.model.LocationPoint
import uz.yuk24.app.domain.model.Order
import javax.inject.Inject

class GetRouteUseCase @Inject constructor(
    private val repo: RouteRepository
) {
    suspend operator fun invoke(
        pickup: LocationPoint,
        delivery: LocationPoint
    ): ApiResult<RouteResponse> = repo.getRoute(
        pickup = pickup.lat to pickup.lng,
        delivery = delivery.lat to delivery.lng
    )

    fun mapGeometry(route: RouteResponse): List<LatLng> = repo.mapGeometry(route)

    suspend fun resolveMapGeometry(
        route: RouteResponse,
        pickup: LocationPoint,
        delivery: LocationPoint
    ): List<LatLng> = repo.resolveMapGeometry(
        route = route,
        pickup = pickup.lat to pickup.lng,
        delivery = delivery.lat to delivery.lng
    )
}

class GetRoadGeometryUseCase @Inject constructor(
    private val repo: RouteRepository
) {
    suspend operator fun invoke(
        pickup: LocationPoint,
        delivery: LocationPoint
    ): ApiResult<List<LatLng>> = repo.getRoadGeometry(
        pickup = pickup.lat to pickup.lng,
        delivery = delivery.lat to delivery.lng
    )
}

class GetPriceUseCase @Inject constructor(
    private val repo: RouteRepository
) {
    suspend operator fun invoke(
        distanceKm: Double,
        loadSize: String,
        unloading: Boolean
    ): ApiResult<PriceResponse> = repo.getPrice(distanceKm, loadSize, unloading)
}

class CreateOrderUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(body: CreateOrderRequest): ApiResult<Order> {
        return when (val res = repo.createOrder(body)) {
            is ApiResult.Success -> ApiResult.Success(Order.fromDto(res.data))
            is ApiResult.Error -> res
            is ApiResult.NetworkError -> ApiResult.NetworkError
            is ApiResult.Loading -> ApiResult.Loading
        }
    }
}

class GetOrderByIdUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(id: String, phone: String?): ApiResult<Order> {
        val first = repo.getOrderById(id, phone)
        val res = if (
            first is ApiResult.Error &&
            first.code == 403 &&
            !phone.isNullOrBlank()
        ) {
            repo.getOrderById(id, phone = null)
        } else {
            first
        }
        return when (res) {
            is ApiResult.Success -> ApiResult.Success(Order.fromDto(res.data))
            is ApiResult.Error -> res
            is ApiResult.NetworkError -> ApiResult.NetworkError
            is ApiResult.Loading -> ApiResult.Loading
        }
    }
}

class GetOrdersByPhoneUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(phone: String): ApiResult<List<Order>> {
        return when (val res = repo.getOrdersByPhone(phone)) {
            is ApiResult.Success -> ApiResult.Success(res.data.map { Order.fromDto(it) })
            is ApiResult.Error -> res
            is ApiResult.NetworkError -> ApiResult.NetworkError
            is ApiResult.Loading -> ApiResult.Loading
        }
    }
}

class SubmitReviewUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(id: String, rating: Int, comment: String?): ApiResult<Order> {
        return when (val res = repo.submitReview(id, rating, comment)) {
            is ApiResult.Success -> ApiResult.Success(Order.fromDto(res.data))
            is ApiResult.Error -> res
            is ApiResult.NetworkError -> ApiResult.NetworkError
            is ApiResult.Loading -> ApiResult.Loading
        }
    }
}

class HealthCheckUseCase @Inject constructor(
    private val repo: OrderRepository
) {
    suspend operator fun invoke(): ApiResult<*> = repo.health()
}
