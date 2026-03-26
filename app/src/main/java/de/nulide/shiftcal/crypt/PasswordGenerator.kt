package de.nulide.shiftcal.crypt

import java.security.SecureRandom


class PasswordGenerator {

    companion object {
        fun genNewPW(): String {
            val key = ByteArray(32)
            SecureRandom().nextBytes(key)
            return Base64Helper.to(key)
        }

    }
}