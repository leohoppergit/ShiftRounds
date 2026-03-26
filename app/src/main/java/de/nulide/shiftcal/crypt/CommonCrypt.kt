package de.nulide.shiftcal.crypt

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom


class CommonCrypt {

    companion object {
        fun generateSecureRandom(lengthInBytes: Int): ByteArray {
            val random = SecureRandom()
            val randomBytes = ByteArray(lengthInBytes)
            random.nextBytes(randomBytes)
            return randomBytes
        }

        fun concatByteArrays(vararg arrays: ByteArray?): ByteArray {
            val out = ByteArrayOutputStream()
            for (array in arrays) {
                try {
                    out.write(array)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return out.toByteArray()
        }
    }
}