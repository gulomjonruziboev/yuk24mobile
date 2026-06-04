package uz.yuk24.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.domain.usecase.HealthCheckUseCase
import javax.inject.Inject

data class SplashState(
    val ready: Boolean = false,
    val serverError: String? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val healthCheck: HealthCheckUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val healthDeferred = async { runCatching { healthCheck() }.getOrNull() }
            delay(1500)
            val result = healthDeferred.await()
            val err = when (result) {
                is ApiResult.Error -> result.message
                ApiResult.NetworkError -> "network"
                else -> null
            }
            _state.value = SplashState(ready = true, serverError = err)
        }
    }
}
