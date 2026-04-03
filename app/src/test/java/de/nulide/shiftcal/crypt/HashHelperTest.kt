package de.nulide.shiftcal.crypt

import org.junit.Test
import kotlin.test.assertEquals

class PasswordGeneratorTest {

    @Test
    fun testGeneratedKeySize() {
        val key = PasswordGenerator.genNewPW()
        assertEquals(Base64Helper.from(key).size, AES.AES_GCM_KEY_SIZE_BYTES)
    }
}
