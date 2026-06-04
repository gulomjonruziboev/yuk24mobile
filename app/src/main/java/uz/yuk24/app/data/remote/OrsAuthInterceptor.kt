package uz.yuk24.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import uz.yuk24.app.BuildConfig

/** Adds OpenRouteService `Authorization` header when [BuildConfig.ORS_API_KEY] is set. */
class OrsAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = BuildConfig.ORS_API_KEY
        val request = if (key.isNotBlank()) {
            chain.request().newBuilder()
                .header("Authorization", key)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
