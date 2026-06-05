package uz.yuk24.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration
import uz.yuk24.app.data.local.DataStoreManager
import uz.yuk24.app.util.AppLanguage
import javax.inject.Inject

@HiltAndroidApp
class YukApplication : Application() {

    @Inject
    lateinit var dataStore: DataStoreManager

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName
        applyStoredOrDeviceLocale()
    }

    private fun applyStoredOrDeviceLocale() {
        runBlocking {
            val saved = dataStore.appLanguage.first()
            val code = saved?.takeIf { it in AppLanguage.supported }
                ?: AppLanguage.fromDevice().also { detected ->
                    dataStore.setAppLanguage(detected)
                }
            AppLanguage.apply(code)
        }
    }
}
