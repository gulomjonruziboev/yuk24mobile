package uz.yuk24.app.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import uz.yuk24.app.data.remote.dto.OrsDirectionsRequest
import uz.yuk24.app.data.remote.dto.OrsDirectionsResponse
import uz.yuk24.app.data.remote.dto.OrsGeocodeResponse

interface OrsApiService {
    @POST("v2/directions/driving-car/geojson")
    suspend fun getDirections(@Body body: OrsDirectionsRequest): OrsDirectionsResponse

    @GET("geocode/reverse")
    suspend fun reverseGeocode(
        @Query("point.lat") lat: Double,
        @Query("point.lon") lon: Double,
        @Query("size") size: Int = 1,
        @Query("boundary.country") country: String = "UZ"
    ): OrsGeocodeResponse
}
