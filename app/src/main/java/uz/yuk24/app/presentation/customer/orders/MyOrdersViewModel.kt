package uz.yuk24.app.presentation.customer.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.yuk24.app.data.local.DataStoreManager
import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.domain.model.Order
import uz.yuk24.app.domain.usecase.GetOrdersByPhoneUseCase
import uz.yuk24.app.util.ApiErrorMessages
import uz.yuk24.app.util.PhoneUtils
import javax.inject.Inject

sealed interface MyOrdersUiState {
    data object Idle : MyOrdersUiState
    data object Loading : MyOrdersUiState
    data class Loaded(val orders: List<Order>) : MyOrdersUiState
    data class Error(val message: String) : MyOrdersUiState
}

@HiltViewModel
class MyOrdersViewModel @Inject constructor(
    private val getOrdersByPhone: GetOrdersByPhoneUseCase,
    private val dataStore: DataStoreManager,
    private val apiErrors: ApiErrorMessages
) : ViewModel() {

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _state = MutableStateFlow<MyOrdersUiState>(MyOrdersUiState.Idle)
    val state: StateFlow<MyOrdersUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = dataStore.lastPhone.first()
            if (!saved.isNullOrBlank()) {
                _phone.value = PhoneUtils.stripLocal(saved)
                load(_phone.value)
            }
        }
    }

    fun setPhone(value: String) {
        _phone.value = PhoneUtils.stripLocal(value)
    }

    fun load(localDigits: String = _phone.value) {
        if (!PhoneUtils.isComplete(localDigits)) return
        viewModelScope.launch {
            _state.value = MyOrdersUiState.Loading
            val normalized = PhoneUtils.normalize(localDigits)
            when (val res = getOrdersByPhone(normalized)) {
                is ApiResult.Success -> {
                    _state.value = MyOrdersUiState.Loaded(res.data)
                    dataStore.setLastPhone(normalized)
                }
                is ApiResult.Error -> _state.value = MyOrdersUiState.Error(apiErrors.from(res))
                ApiResult.NetworkError -> _state.value = MyOrdersUiState.Error(apiErrors.from(ApiResult.NetworkError))
                ApiResult.Loading -> Unit
            }
        }
    }
}
