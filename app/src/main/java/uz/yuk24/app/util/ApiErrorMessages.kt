package uz.yuk24.app.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.yuk24.app.R
import uz.yuk24.app.data.remote.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiErrorMessages @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun from(result: ApiResult<*>): String = when (result) {
        is ApiResult.Error -> when (result.code) {
            400 -> result.message.ifBlank { context.getString(R.string.error_validation) }
            403 -> context.getString(R.string.error_forbidden)
            404 -> context.getString(R.string.error_not_found)
            429 -> context.getString(R.string.error_rate_limited)
            in 500..599 -> context.getString(R.string.error_server)
            else -> result.message.ifBlank { context.getString(R.string.error_unknown) }
        }
        ApiResult.NetworkError -> context.getString(R.string.error_network)
        else -> context.getString(R.string.error_unknown)
    }
}
