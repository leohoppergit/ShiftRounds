package de.nulide.shiftcal.crypt

import de.nulide.shiftcal.crypt.CommonCrypt.Companion.generateSecureRandom
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


class AES {

    companion object {
        const val AES_GCM_KEY_SIZE_BYTES: Int = 32 // byte = 256 bit
        const val AES_GCM_IV_SIZE_BYTES: Int = 12 // byte = 96 bit
        const val AES_GCM_TAG_SIZE_BITS: Int = 128 // bit = 16 byte


        fun encryptWithAes(msg: String, aesKeyString: String): String? {
            val aesKey = Base64Helper.from(aesKeyString)
            if (aesKey.size != AES_GCM_KEY_SIZE_BYTES) {
                throw RuntimeException("Bad AES key size:" + aesKey.size)
            }
            try {
                val ivBytes: ByteArray = generateSecureRandom(AES_GCM_IV_SIZE_BYTES)
                val gcmSpec = GCMParameterSpec(AES_GCM_TAG_SIZE_BITS, ivBytes)
                val secretKeySpec = SecretKeySpec(aesKey, "AES")

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec)
                val ctBytes = cipher.doFinal(msg.toByteArray())

                return Base64Helper.to(CommonCrypt.concatByteArrays(ivBytes, ctBytes))
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }
            return null
        }

        fun decryptWithAes(encMsg: String, aesKeyString: String): String? {
            val msgBytes = Base64Helper.from(encMsg)
            val aesKey = Base64Helper.from(aesKeyString)
            try {
                val ivBytes: ByteArray = Arrays.copyOfRange(msgBytes, 0, AES_GCM_IV_SIZE_BYTES)
                val ctBytes: ByteArray =
                    Arrays.copyOfRange(msgBytes, AES_GCM_IV_SIZE_BYTES, msgBytes.size)

                val gcmSpec = GCMParameterSpec(AES_GCM_TAG_SIZE_BITS, ivBytes)
                val secretKeySpec = SecretKeySpec(aesKey, "AES")

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec)
                return String(cipher.doFinal(ctBytes))
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }
            return null
        }
    }
}