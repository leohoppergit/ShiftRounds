package de.nulide.shiftcal.crypt

import org.junit.Test
import kotlin.test.assertEquals

class HashHelperTest {

    @Test
    fun testHashSize() {
        val password = PasswordGenerator.genNewPW()
        val hash = HashHelper.hashPassword(password)
        assertEquals(Base64Helper.from(hash).size, HashHelper.ARGON2_HASH_LENGTH)
    }
}