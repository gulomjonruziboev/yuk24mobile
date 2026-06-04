package uz.yuk24.app.presentation.customer.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.yuk24.app.data.local.DataStoreManager
import uz.yuk24.app.data.remote.ApiResult
import uz.yuk24.app.data.remote.dto.CreateOrderRequest
import uz.yuk24.app.domain.model.BookingState
import uz.yuk24.app.domain.model.LatLng
import uz.yuk24.app.domain.model.LoadSize
import uz.yuk24.app.domain.model.LocationPoint
import uz.yuk24.app.domain.usecase.CreateOrderUseCase
import uz.yuk24.app.domain.usecase.GetPriceUseCase
import uz.yuk24.app.domain.usecase.GetRouteUseCase
import uz.yuk24.app.util.PhoneUtils
import uz.yuk24.app.util.PricingUtils
import uz.yuk24.app.util.RouteGeometryParser
import javax.inject.Inject

sealed interface PriceUiState {
    data object Idle : PriceUiState
    data object Loading : PriceUiState
    data class Loaded(
        val price: Double,
        val distanceKm: Double,
        val durationMin: Double
    ) : PriceUiState
    data class Error(val message: String) : PriceUiState
}

sealed interface OrderSubmitState {
    data object Idle : OrderSubmitState
    data object Loading : OrderSubmitState
    data class Success(val orderId: String, val internalId: String, val phone: String) : OrderSubmitState
    data class Error(val message: String) : OrderSubmitState
}

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getRoute: GetRouteUseCase,
    private val getPrice: GetPriceUseCase,
    private val createOrder: CreateOrderUseCase,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()

    private val _priceState = MutableStateFlow<PriceUiState>(PriceUiState.Idle)
    val priceState: StateFlow<PriceUiState> = _priceState.asStateFlow()

    private val _submitState = MutableStateFlow<OrderSubmitState>(OrderSubmitState.Idle)
    val submitState: StateFlow<OrderSubmitState> = _submitState.asStateFlow()

    /**
     * Road-following polyline from backend `POST /api/route` geometry.
     * Empty while loading, on failure, or when geometry is null — map uses straight line.
     */
    private val _routeGeometry = MutableStateFlow<List<LatLng>>(emptyList())
    val routeGeometry: StateFlow<List<LatLng>> = _routeGeometry.asStateFlow()

    private var routeJob: Job? = null

    init {
        viewModelScope.launch {
            val savedPhone = dataStore.lastPhone.first()
            val savedName = dataStore.lastCustomerName.first()
            _state.update { current ->
                current.copy(
                    customerPhone = savedPhone?.let { PhoneUtils.stripLocal(it) }.orEmpty(),
                    customerName = savedName.orEmpty()
                )
            }
        }
    }

    fun setPickup(point: LocationPoint) {
        _state.update { it.copy(pickup = point) }
        invalidatePrice()
        refreshRoute()
    }

    fun setDelivery(point: LocationPoint) {
        _state.update { it.copy(delivery = point) }
        invalidatePrice()
        refreshRoute()
    }

    fun setLoadSize(loadSize: LoadSize) {
        _state.update { it.copy(loadSize = loadSize) }
        invalidatePrice()
    }

    fun setUnloading(unloading: Boolean) {
        _state.update { it.copy(unloading = unloading) }
        invalidatePrice()
    }

    fun setPhone(localDigits: String) {
        _state.update { it.copy(customerPhone = PhoneUtils.stripLocal(localDigits)) }
    }

    fun setName(name: String) {
        _state.update { it.copy(customerName = name) }
    }

    private fun invalidatePrice() {
        _state.update { it.copy(distanceKm = null, durationMin = null, finalPrice = null) }
        if (_priceState.value is PriceUiState.Loaded || _priceState.value is PriceUiState.Error) {
            _priceState.value = PriceUiState.Idle
        }
    }

    /**
     * Single `POST /api/route` when both pins are set: updates map geometry and caches
     * distance/duration for the price step (avoids a second route call in [calculatePrice]).
     */
    private fun refreshRoute() {
        routeJob?.cancel()
        val pickup = _state.value.pickup
        val delivery = _state.value.delivery
        if (pickup == null || delivery == null) {
            _routeGeometry.value = emptyList()
            return
        }
        routeJob = viewModelScope.launch {
            when (val result = getRoute(pickup, delivery)) {
                is ApiResult.Success -> {
                    _routeGeometry.value = RouteGeometryParser.parse(result.data.geometry)
                    _state.update {
                        it.copy(
                            distanceKm = result.data.distanceKm,
                            durationMin = result.data.durationMin
                        )
                    }
                }
                else -> _routeGeometry.value = emptyList()
            }
        }
    }

    fun calculatePrice() {
        val current = _state.value
        val pickup = current.pickup ?: return
        val delivery = current.delivery ?: return
        val loadSize = current.loadSize ?: return

        viewModelScope.launch {
            _priceState.value = PriceUiState.Loading

            val distanceKm: Double
            val durationMin: Double
            if (current.distanceKm != null && current.durationMin != null) {
                distanceKm = current.distanceKm
                durationMin = current.durationMin
            } else {
                val routeResult = getRoute(pickup, delivery)
                if (routeResult !is ApiResult.Success) {
                    _priceState.value = PriceUiState.Error(errorMessage(routeResult))
                    return@launch
                }
                distanceKm = routeResult.data.distanceKm
                durationMin = routeResult.data.durationMin
                _routeGeometry.value = RouteGeometryParser.parse(routeResult.data.geometry)
                _state.update {
                    it.copy(distanceKm = distanceKm, durationMin = durationMin)
                }
            }

            val priceResult = getPrice(distanceKm, loadSize.key, current.unloading)
            if (priceResult !is ApiResult.Success) {
                _priceState.value = PriceUiState.Error(errorMessage(priceResult))
                return@launch
            }
            val finalPrice = priceResult.data.price

            _state.update {
                it.copy(
                    distanceKm = distanceKm,
                    durationMin = durationMin,
                    finalPrice = finalPrice
                )
            }
            _priceState.value = PriceUiState.Loaded(
                price = finalPrice,
                distanceKm = distanceKm,
                durationMin = durationMin
            )
        }
    }

    fun submitOrder() {
        val current = _state.value
        val pickup = current.pickup ?: return
        val delivery = current.delivery ?: return
        val loadSize = current.loadSize ?: return
        val price = current.finalPrice ?: return
        val distanceKm = current.distanceKm ?: return
        val durationMin = current.durationMin ?: return

        viewModelScope.launch {
            _submitState.value = OrderSubmitState.Loading
            val normalizedPhone = PhoneUtils.normalize(current.customerPhone)
            val body = CreateOrderRequest(
                customerPhone = normalizedPhone,
                customerName = current.customerName.takeIf { it.isNotBlank() },
                pickup = pickup.toPayload(),
                delivery = delivery.toPayload(),
                loadSize = loadSize.key,
                unloading = current.unloading,
                price = price,
                distanceKm = distanceKm,
                durationMin = durationMin
            )
            when (val res = createOrder(body)) {
                is ApiResult.Success -> {
                    dataStore.setLastPhone(normalizedPhone)
                    if (current.customerName.isNotBlank()) {
                        dataStore.setLastCustomerName(current.customerName)
                    }
                    dataStore.setLastOrderId(res.data.id)
                    _submitState.value = OrderSubmitState.Success(
                        orderId = res.data.orderId,
                        internalId = res.data.id,
                        phone = normalizedPhone
                    )
                }
                else -> _submitState.value = OrderSubmitState.Error(errorMessage(res))
            }
        }
    }

    fun resetSubmit() {
        _submitState.value = OrderSubmitState.Idle
    }

    fun resetBooking() {
        routeJob?.cancel()
        _state.value = BookingState()
        _priceState.value = PriceUiState.Idle
        _submitState.value = OrderSubmitState.Idle
        _routeGeometry.value = emptyList()
    }

    fun estimatedPrice(): Double = PricingUtils.calculateOrZero(
        distanceKm = _state.value.distanceKm,
        loadSize = _state.value.loadSize,
        unloading = _state.value.unloading
    )

    private fun errorMessage(result: ApiResult<*>): String = when (result) {
        is ApiResult.Error -> when (result.code) {
            400 -> result.message.ifBlank { "Ma'lumotlar to'liq emas" }
            403 -> "Ruxsat yo'q"
            404 -> "Topilmadi"
            429 -> "Juda ko'p so'rovlar, biroz kuting"
            in 500..599 -> "Server xatoligi"
            else -> result.message
        }
        ApiResult.NetworkError -> "Internet aloqasi yo'q"
        else -> "Noma'lum xato"
    }
}
