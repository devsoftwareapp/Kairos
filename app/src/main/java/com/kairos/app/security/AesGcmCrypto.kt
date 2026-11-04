package com.kairos.app.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-GCM primitive only. Production key wrapping, Keystore policy,
 * recovery and time-lock protocol must be implemented and security-reviewed
 * before storing real user content.
 */
object AesGcmCrypto {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val NONCE_BYTES = 12
    private const val TAG_BITS = 128

    data class SealedData(val nonce: ByteArray, val ciphertext: ByteArray)

    fun generateContentKey(): SecretKey =
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    fun encrypt(key: SecretKey, plaintext: ByteArray, aad: ByteArray? = null): SealedData {
        val nonce = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
        aad?.let(cipher::updateAAD)
        return SealedData(nonce, cipher.doFinal(plaintext))
    }

    fun decrypt(key: SecretKey, sealed: SealedData, aad: ByteArray? = null): ByteArray {
        require(sealed.nonce.size == NONCE_BYTES) { "Invalid GCM nonce" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, sealed.nonce))
        aad?.let(cipher::updateAAD)
        return cipher.doFinal(sealed.ciphertext)
    }
}
