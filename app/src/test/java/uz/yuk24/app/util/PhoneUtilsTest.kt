package uz.yuk24.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.net.URLDecoder

class PhoneUtilsTest {

    @Test
    fun fromNavArg_percentEncodedPlus() {
        assertEquals("+998976053110", PhoneUtils.fromNavArg("%2B998976053110"))
    }

    @Test
    fun fromNavArg_literalPlus_notCorrupted() {
        assertEquals("+998976053110", PhoneUtils.fromNavArg("+998976053110"))
        // URLDecoder incorrectly turns '+' into space (the bug we fixed)
        assertNotEquals(
            "+998976053110",
            URLDecoder.decode("+998976053110", Charsets.UTF_8.name())
        )
    }

    @Test
    fun fromNavArg_blank_returnsEmpty() {
        assertEquals("", PhoneUtils.fromNavArg(""))
        assertEquals("", PhoneUtils.fromNavArg("   "))
    }
}
