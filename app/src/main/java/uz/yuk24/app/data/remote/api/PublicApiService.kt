package uz.yuk24.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import uz.yuk24.app.data.remote.dto.CreateOrderRequest
import uz.yuk24.app.data.remote.dto.HealthResponse
import uz.yuk24.app.data.remote.dto.OrderDto
import uz.yuk24.app.data.remote.dto.PriceRequest
import uz.yuk24.app.data.remote.dto.PriceResponse
import uz.yuk24.app.data.remote.dto.ReviewRequest
import uz.yuk24.app.data.remote.dto.RouteRequest
import uz.yuk24.app.data.remote.dto.RouteResponse

interface PublicApiService {

    @GET("api/health")
    suspend fun health(): HealthResponse

    @POST("api/route")
    suspend fun getRoute(@Body body: RouteRequest): RouteResponse

    @POST("api/price")
    suspend fun getPrice(@Body body: PriceRequest): PriceResponse

    @POST("api/orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): OrderDto

    @GET("api/orders/by-phone")
    suspend fun getOrdersByPhone(@Query("phone") phone: String): List<OrderDto>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String,
        @Query("phone") phone: String? = null
    ): OrderDto

    @POST("api/orders/{id}/review")
    suspend fun submitReview(
        @Path("id") id: String,
        @Body body: ReviewRequest
    ): OrderDto
}
