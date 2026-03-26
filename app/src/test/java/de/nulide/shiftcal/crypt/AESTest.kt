package de.nulide.shiftcal.crypt

import org.junit.Test
import kotlin.test.assertEquals

class AESTest {

    @Test
    fun testEncryptionAndDecryption() {
        val msg = "Hello Darling"
        val key = "SecureVerySecure"
        val hashedKey = HashHelper.hashPassword(key)
        val hashedKeyBytes = Base64Helper.from(hashedKey)
        assertEquals(hashedKeyBytes.size, HashHelper.ARGON2_HASH_LENGTH)
        val encryptedMsg = AES.encryptWithAes(msg, hashedKey)
        val isNotNull = encryptedMsg != null
        assert(isNotNull)
        val decryptedMsg = AES.decryptWithAes(encryptedMsg!!, hashedKey)
        assert(decryptedMsg != null)
        assertEquals(msg, decryptedMsg)
    }
}