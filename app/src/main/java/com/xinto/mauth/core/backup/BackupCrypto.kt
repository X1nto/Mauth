package com.xinto.mauth.core.backup

import org.bouncycastle.crypto.PBEParametersGenerator
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.generators.SCrypt
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {

    private const val KEY_LENGTH = 32
    private const val SALT_LENGTH = 16
    private const val NONCE_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128

    private const val ARGON2_MEMORY_KIB = 19456
    private const val ARGON2_ITERATIONS = 2
    private const val ARGON2_PARALLELISM = 1

    private val AAD = "mauth-backup:1".toByteArray(Charsets.UTF_8)

    private val random = SecureRandom()

    fun encrypt(plaintext: ByteArray, password: CharArray): EncryptedBlob {
        val salt = randomBytes(SALT_LENGTH)
        val nonce = randomBytes(NONCE_LENGTH)
        val key = deriveArgon2id(password, salt, ARGON2_MEMORY_KIB, ARGON2_ITERATIONS, ARGON2_PARALLELISM)

        try {
            return EncryptedBlob(
                ciphertext = gcm(Cipher.ENCRYPT_MODE, key, nonce, plaintext, AAD),
                params = Argon2Params(
                    memoryKib = ARGON2_MEMORY_KIB,
                    iterations = ARGON2_ITERATIONS,
                    parallelism = ARGON2_PARALLELISM,
                    salt = salt,
                    nonce = nonce
                )
            )
        } finally {
            key.fill(0)
        }
    }

    /**
     * @return Ciphertext carrying the tag at its end.
     */
    fun encryptRaw(plaintext: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray {
        return gcm(Cipher.ENCRYPT_MODE, key, nonce, plaintext, aad = null)
    }

    /**
     * @return Ciphertext carrying the tag at its end.
     */
    fun encryptRaw(plaintext: ByteArray, nonce: ByteArray, key: ByteArray, aad: ByteArray): ByteArray {
        return gcm(Cipher.ENCRYPT_MODE, key, nonce, plaintext, aad)
    }

    fun decrypt(ciphertext: ByteArray, params: Argon2Params, password: CharArray): ByteArray {
        val key = deriveArgon2id(
            password = password,
            salt = params.salt,
            memoryKib = params.memoryKib,
            iterations = params.iterations,
            parallelism = params.parallelism
        )

        try {
            return gcm(Cipher.DECRYPT_MODE, key, params.nonce, ciphertext, AAD)
        } finally {
            key.fill(0)
        }
    }

    /**
     * @param ciphertext must already have the tag appended.
     */
    fun decryptRaw(ciphertext: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray {
        return gcm(Cipher.DECRYPT_MODE, key, nonce, ciphertext, aad = null)
    }

    /**
     * @param ciphertext must already have the tag appended.
     */
    fun decryptRaw(ciphertext: ByteArray, nonce: ByteArray, key: ByteArray, aad: ByteArray): ByteArray {
        return gcm(Cipher.DECRYPT_MODE, key, nonce, ciphertext, aad)
    }

    /**
     * @param prk must already be the key material rather than a secret to extract from
     */
    fun hkdfExpand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val generator = HKDFBytesGenerator(SHA256Digest()).apply {
            init(HKDFParameters.skipExtractParameters(prk, info))
        }
        return ByteArray(length).apply {
            generator.generateBytes(this, 0, size)
        }
    }

    /**
     * AES-256-CBC with the MAC taken over the IV and ciphertext.
     *
     * @return null when the MAC does not match
     */
    fun decryptCbcHmac(
        iv: ByteArray,
        ciphertext: ByteArray,
        mac: ByteArray,
        encKey: ByteArray,
        macKey: ByteArray
    ): ByteArray? {
        val hmac = Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(macKey, "HmacSHA256"))
            update(iv)
        }

        if (!MessageDigest.isEqual(hmac.doFinal(ciphertext), mac))
            return null

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(encKey, "AES"), IvParameterSpec(iv))
        }
        return cipher.doFinal(ciphertext)
    }

    fun sha1(input: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-1").digest(input)
    }

    fun sha256(input: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(input)
    }

    fun randomBytes(size: Int): ByteArray {
        return ByteArray(size).also(random::nextBytes)
    }

    fun deriveScrypt(
        password: CharArray,
        salt: ByteArray,
        n: Int,
        r: Int,
        p: Int
    ): ByteArray {
        val passwordBytes = PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password)
        try {
            return SCrypt.generate(passwordBytes, salt, n, r, p, KEY_LENGTH)
        } finally {
            passwordBytes.fill(0)
        }
    }

    fun derivePbkdf2(
        password: CharArray,
        salt: ByteArray,
        iterations: Int,
        hmac: Pbkdf2Hmac,
        keyLength: Int = KEY_LENGTH
    ): ByteArray {
        val digest = when (hmac) {
            Pbkdf2Hmac.SHA1 -> SHA1Digest()
            Pbkdf2Hmac.SHA256 -> SHA256Digest()
        }
        val generator = PKCS5S2ParametersGenerator(digest)
        val passwordBytes = PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password)
        try {
            generator.init(passwordBytes, salt, iterations)
            val keyParameter = generator.generateDerivedParameters(keyLength * 8) as KeyParameter
            return keyParameter.key
        } finally {
            passwordBytes.fill(0)
        }
    }

    fun deriveArgon2id(
        password: CharArray,
        salt: ByteArray,
        memoryKib: Int,
        iterations: Int,
        parallelism: Int,
        keyLength: Int = KEY_LENGTH
    ): ByteArray {
        val parameters = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withSalt(salt)
            .withMemoryAsKB(memoryKib)
            .withIterations(iterations)
            .withParallelism(parallelism)
            .build()

        val generator = Argon2BytesGenerator().apply {
            init(parameters)
        }

        return ByteArray(keyLength).apply {
            generator.generateBytes(password, this)
        }
    }

    private fun gcm(
        mode: Int,
        key: ByteArray,
        nonce: ByteArray,
        input: ByteArray,
        aad: ByteArray?
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_LENGTH_BITS, nonce))
        }
        if (aad != null) {
            cipher.updateAAD(aad)
        }
        return cipher.doFinal(input)
    }

    class EncryptedBlob(
        val ciphertext: ByteArray,
        val params: Argon2Params
    )

    enum class Pbkdf2Hmac {
        SHA1,
        SHA256
    }
}

class Argon2Params(
    val memoryKib: Int,
    val iterations: Int,
    val parallelism: Int,
    val salt: ByteArray,
    val nonce: ByteArray,
)
