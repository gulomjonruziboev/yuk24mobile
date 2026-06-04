package uz.yuk24.app.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Wraps a suspending API call into an [ApiResult].
 *
 * Handles HTTP error codes per spec §5.6:
 * - 400 -> validation error (parses `details` array if present)
 * - 401/403 -> permission / token issues
 * - 404 -> not found
 * - 429 -> rate limited
 * - 500 -> server error
 */
suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        val errorBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val parsedMessage = parseErrorMessage(errorBody) ?: e.message()
        ApiResult.Error(e.code(), parsedMessage)
    } catch (_: IOException) {
        ApiResult.NetworkError
    } catch (e: Exception) {
        ApiResult.Error(-1, e.message ?: "Unknown error")
    }
}

private fun parseErrorMessage(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return runCatching {
        val root: JsonElement = errorJson.parseToJsonElement(body)
        val obj = root.jsonObject
        val message = obj["message"]?.jsonPrimitive?.contentOrNullSafe()
        val error = obj["error"]?.jsonPrimitive?.contentOrNullSafe()
        message ?: error
    }.getOrNull()
}

private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
    runCatching { content }.getOrNull()
