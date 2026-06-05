package uz.yuk24.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object AppLanguage {
    const val UZ = "uz"
    const val RU = "ru"
    const val EN = "en"

    val supported = setOf(UZ, RU, EN)

    /** Map device locale to app language; unknown languages default to Uzbek. */
    fun fromDevice(locale: Locale = Locale.getDefault()): String =
        when (locale.language.lowercase(Locale.ROOT)) {
            "ru" -> RU
            "en" -> EN
            "uz" -> UZ
            else -> UZ
        }

    fun apply(code: String) {
        val tag = if (code in supported) code else UZ
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
