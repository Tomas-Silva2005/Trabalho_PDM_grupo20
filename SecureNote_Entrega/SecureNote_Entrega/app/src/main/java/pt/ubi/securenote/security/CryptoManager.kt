package pt.ubi.securenote.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    companion object {
        private const val KEY_ALIAS = "SecureNote_AES_Key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFIX = "SECURENOTE_ENC_V1:"
        private const val IV_SIZE = 12
    }

    private fun getOrCreateKey(): SecretKey {

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey != null) {
            return existingKey as SecretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setKeySize(256)
            .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    fun encrypt(text: String): String {

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val encryptedBytes = cipher.doFinal(
            text.toByteArray(Charsets.UTF_8)
        )

        val combined = cipher.iv + encryptedBytes

        val encoded = Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )

        return PREFIX + encoded
    }

    fun decrypt(text: String): String {

        // Notas antigas ainda em texto simples
        if (!text.startsWith(PREFIX)) {
            return text
        }

        val encodedData = text.removePrefix(PREFIX)

        val combined = Base64.decode(
            encodedData,
            Base64.NO_WRAP
        )

        require(combined.size > IV_SIZE) {
            "Dados encriptados inválidos."
        }

        val iv = combined.copyOfRange(0, IV_SIZE)

        val encryptedBytes = combined.copyOfRange(
            IV_SIZE,
            combined.size
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, iv)
        )

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(
            decryptedBytes,
            Charsets.UTF_8
        )
    }
}