package uz.yuk24.app.data.repository

import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.data.remote.api.PublicApiService
import uz.yuk24.app.data.remote.dto.CreateOrderRequest
import uz.yuk24.app.data.remote.dto.HealthResponse
import uz.yuk24.app.data.remote.dto.OrderDto
import uz.yuk24.app.data.remote.dto.ReviewRequest
import uz.yuk24.app.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val api: PublicApiService
) {
    suspend fun health(): ApiResult<HealthResponse> = safeApiCall { api.health() }

    suspend fun createOrder(body: CreateOrderRequest): ApiResult<OrderDto> =
        safeApiCall { api.createOrder(body) }

    suspend fun getOrderById(id: String, phone: String?): ApiResult<OrderDto> =
        safeApiCall { api.getOrderById(id, phone) }

    suspend fun getOrdersByPhone(phone: String): ApiResult<List<OrderDto>> =
        safeApiCall { api.getOrdersByPhone(phone) }

    suspend fun submitReview(id: String, rating: Int, comment: String?): ApiResult<OrderDto> =
        safeApiCall { api.submitReview(id, ReviewRequest(rating, comment)) }
}
