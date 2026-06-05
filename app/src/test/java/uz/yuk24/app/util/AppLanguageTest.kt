package uz.yuk24.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {

    @Test
    fun fromDevice_mapsKnownLanguages() {
        assertEquals(AppLanguage.RU, AppLanguage.fromDevice(Locale.forLanguageTag("ru")))
        assertEquals(AppLanguage.EN, AppLanguage.fromDevice(Locale.forLanguageTag("en-US")))
        assertEquals(AppLanguage.UZ, AppLanguage.fromDevice(Locale.forLanguageTag("uz")))
    }

    @Test
    fun fromDevice_defaultsUnknownToUzbek() {
        assertEquals(AppLanguage.UZ, AppLanguage.fromDevice(Locale.forLanguageTag("de")))
        assertEquals(AppLanguage.UZ, AppLanguage.fromDevice(Locale.forLanguageTag("fr")))
    }

    @Test
    fun supported_includesAllLocales() {
        assertEquals(setOf("uz", "ru", "en"), AppLanguage.supported)
    }
}
