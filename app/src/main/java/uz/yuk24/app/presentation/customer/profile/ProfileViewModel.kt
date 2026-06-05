package uz.yuk24.app.presentation.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.yuk24.app.data.local.DataStoreManager
import uz.yuk24.app.util.AppLanguage
import javax.inject.Inject

data class ProfileState(
    val phone: String = "",
    val language: String = "uz"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val phone = dataStore.lastPhone.first().orEmpty()
            val language = dataStore.appLanguage.first()
                ?.takeIf { it in AppLanguage.supported }
                ?: AppLanguage.UZ
            _state.value = ProfileState(phone = phone, language = language)
        }
    }

    fun setLanguage(code: String) {
        if (code !in AppLanguage.supported) return
        viewModelScope.launch {
            dataStore.setAppLanguage(code)
            AppLanguage.apply(code)
            _state.value = _state.value.copy(language = code)
        }
    }
}
