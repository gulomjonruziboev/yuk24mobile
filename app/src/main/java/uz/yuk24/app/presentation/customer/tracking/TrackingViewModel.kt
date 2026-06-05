package uz.yuk24.app.presentation.customer.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.domain.model.Order
import uz.yuk24.app.domain.usecase.GetOrderByIdUseCase
import uz.yuk24.app.domain.usecase.SubmitReviewUseCase
import uz.yuk24.app.util.ApiErrorMessages
import javax.inject.Inject

sealed interface TrackingUiState {
    data object Loading : TrackingUiState
    data class Success(val order: Order) : TrackingUiState
    data class Error(val message: String) : TrackingUiState
}

sealed interface ReviewSubmitState {
    data object Idle : ReviewSubmitState
    data object Submitting : ReviewSubmitState
    data object Submitted : ReviewSubmitState
    data class Error(val message: String) : ReviewSubmitState
}

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val getOrderById: GetOrderByIdUseCase,
    private val submitReview: SubmitReviewUseCase,
    private val apiErrors: ApiErrorMessages
) : ViewModel() {

    private val _state = MutableStateFlow<TrackingUiState>(TrackingUiState.Loading)
    val state: StateFlow<TrackingUiState> = _state.asStateFlow()

    private val _review = MutableStateFlow<ReviewSubmitState>(ReviewSubmitState.Idle)
    val review: StateFlow<ReviewSubmitState> = _review.asStateFlow()

    private var pollingJob: Job? = null

    fun start(orderId: String, phone: String?, poll: Boolean = true) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                when (val res = getOrderById(orderId, phone)) {
                    is ApiResult.Success -> {
                        _state.value = TrackingUiState.Success(res.data)
                        if (res.data.status.isTerminal || !poll) break
                    }
                    is ApiResult.Error -> {
                        _state.value = TrackingUiState.Error(apiErrors.from(res))
                        if (!poll) break
                    }
                    ApiResult.NetworkError -> {
                        _state.value = TrackingUiState.Error(apiErrors.from(ApiResult.NetworkError))
                        if (!poll) break
                    }
                    ApiResult.Loading -> Unit
                }
                if (!poll) break
                delay(5_000)
            }
        }
    }

    fun submitOrderReview(orderId: String, rating: Int, comment: String?) {
        viewModelScope.launch {
            _review.value = ReviewSubmitState.Submitting
            when (val res = submitReview(orderId, rating, comment)) {
                is ApiResult.Success -> {
                    val merged = mergeDriverFromPrevious(_state.value, res.data)
                    _state.value = TrackingUiState.Success(merged)
                    _review.value = ReviewSubmitState.Submitted
                }
                is ApiResult.Error -> _review.value = ReviewSubmitState.Error(apiErrors.from(res))
                ApiResult.NetworkError -> _review.value = ReviewSubmitState.Error(apiErrors.from(ApiResult.NetworkError))
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun mergeDriverFromPrevious(current: TrackingUiState, updated: Order): Order {
        val prior = (current as? TrackingUiState.Success)?.order ?: return updated
        if (!updated.driverName.isNullOrBlank()) return updated
        return updated.copy(
            driverName = prior.driverName,
            driverPhone = prior.driverPhone ?: updated.driverPhone
        )
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
