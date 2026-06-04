package uz.yuk24.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds a Bearer token from [tokenProvider] when present. Not used by the customer
 * flow today (customer endpoints are unauthenticated), but kept here so the
 * Driver/Admin flows can reuse this scaffolding when added later — per spec §5.3.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
