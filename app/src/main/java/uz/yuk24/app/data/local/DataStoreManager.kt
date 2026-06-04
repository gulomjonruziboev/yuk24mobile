package uz.yuk24.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "yuk24_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.appDataStore

    private object Keys {
        val LAST_PHONE = stringPreferencesKey("last_phone")
        val LAST_ORDER_ID = stringPreferencesKey("last_order_id")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val LAST_CUSTOMER_NAME = stringPreferencesKey("last_customer_name")
        val RECENT_SEARCHES = stringSetPreferencesKey("recent_searches")
    }

    val lastPhone: Flow<String?> = dataStore.data.map { it[Keys.LAST_PHONE] }
    val lastOrderId: Flow<String?> = dataStore.data.map { it[Keys.LAST_ORDER_ID] }
    val appLanguage: Flow<String?> = dataStore.data.map { it[Keys.APP_LANGUAGE] }
    val lastCustomerName: Flow<String?> = dataStore.data.map { it[Keys.LAST_CUSTOMER_NAME] }
    val recentSearches: Flow<Set<String>> = dataStore.data.map { it[Keys.RECENT_SEARCHES] ?: emptySet() }

    suspend fun setLastPhone(value: String) = dataStore.edit { it[Keys.LAST_PHONE] = value }
    suspend fun setLastOrderId(value: String) = dataStore.edit { it[Keys.LAST_ORDER_ID] = value }
    suspend fun setAppLanguage(value: String) = dataStore.edit { it[Keys.APP_LANGUAGE] = value }
    suspend fun setLastCustomerName(value: String) = dataStore.edit { it[Keys.LAST_CUSTOMER_NAME] = value }

    suspend fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        dataStore.edit { prefs ->
            val existing = prefs[Keys.RECENT_SEARCHES].orEmpty().toMutableSet()
            existing.add(query.trim())
            prefs[Keys.RECENT_SEARCHES] = existing.take(10).toSet()
        }
    }

    suspend fun lastPhoneOrEmpty(): String = lastPhone.first().orEmpty()
}
