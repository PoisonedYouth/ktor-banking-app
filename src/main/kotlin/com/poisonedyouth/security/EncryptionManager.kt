package com.poisonedyouth.security

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets


class EncryptionResult(
    val secretKey: ByteArray,
    val initializationVector: ByteArray,
    val ciphterText: ByteArray
)

private const val TRANSFORMATION = "AES/CBC/PKCS5PADDING"

private const val ALGORITHM = "AES"

object EncryptionManager {


    fun encrypt(password: String): EncryptionResult {

        val plainText = password.toByteArray(Charsets.UTF_8)
        val keygen = KeyGenerator.getInstance(ALGORITHM)
        keygen.init(256)

        val key = keygen.generateKey()

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)

        return EncryptionResult(
            secretKey = key.encoded,
            initializationVector = cipher.iv,
            ciphterText = cipherText
        )
    }

    fun decrypt(encryptionResult: EncryptionResult): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivSpec = IvParameterSpec(encryptionResult.initializationVector)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionResult.secretKey, ALGORITHM), ivSpec)
        val cipherText = cipher.doFinal(encryptionResult.ciphterText)

        return String(cipherText, StandardCharsets.UTF_8)
    }
}