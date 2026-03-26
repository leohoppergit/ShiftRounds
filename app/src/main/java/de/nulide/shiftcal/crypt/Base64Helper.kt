package de.nulide.shiftcal.crypt

import java.util.Base64

class Base64Helper {

    companion object {

        fun to(bytes: ByteArray): String {
            return Base64.getEncoder().encodeToString(bytes)
        }

        fun from(string: String): ByteArray {
            return Base64.getDecoder().decode(string)
        }

    }
}