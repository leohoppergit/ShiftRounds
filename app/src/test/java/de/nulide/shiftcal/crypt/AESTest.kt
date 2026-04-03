package de.nulide.shiftcal.crypt

import org.junit.Test
import kotlin.test.assertEquals

class AESTest {

    @Test
    fun testEncryptionAndDecryption() {
        val msg = "Hello Darling"
        val key = PasswordGenerator.genNewPW()
        val keyBytes = Base64Helper.from(key)
        assertEquals(keyBytes.size, AES.AES_GCM_KEY_SIZE_BYTES)
        val encryptedMsg = AES.encryptWithAes(msg, key)
        val isNotNull = encryptedMsg != null
        assert(isNotNull)
        val decryptedMsg = AES.decryptWithAes(encryptedMsg!!, key)
        assert(decryptedMsg != null)
        assertEquals(msg, decryptedMsg)
    }
}
