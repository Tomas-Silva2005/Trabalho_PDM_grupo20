package pt.ubi.securenote

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ubi.securenote.security.CryptoManager

class CryptoManagerTest {

    private val cryptoManager = CryptoManager()

    @Test
    fun encryptTest() {
        val originalText = "A minha nota secreta"

        val encryptedText = cryptoManager.encrypt(originalText)

        assertTrue(encryptedText.startsWith("SECURENOTE_ENC_V1:"))
        assertTrue(encryptedText != originalText)
    }

    @Test
    fun decryptTest() {
        val originalText = "A minha nota secreta"

        val encryptedText = cryptoManager.encrypt(originalText)
        val decryptedText = cryptoManager.decrypt(encryptedText)

        assertEquals(originalText, decryptedText)
    }

    @Test
    fun tamperedDataTest() {
        val originalText = "A minha nota secreta"

        val encryptedText = cryptoManager.encrypt(originalText)

        val prefix = "SECURENOTE_ENC_V1:"
        val encodedData = encryptedText.removePrefix(prefix)

        val combined = Base64.decode(
            encodedData,
            Base64.NO_WRAP
        )

        // Alterar o último byte dos dados encriptados
        combined[combined.lastIndex] =
            (combined.last().toInt() xor 1).toByte()

        val tamperedText = prefix + Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )

        var exceptionThrown = false

        try {
            cryptoManager.decrypt(tamperedText)
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertTrue(
            "A desencriptação devia falhar quando os dados são alterados",
            exceptionThrown
        )
    }

    @Test
    fun differentEncryptionTest() {
        val originalText = "A mesma nota"

        val firstEncryption = cryptoManager.encrypt(originalText)
        val secondEncryption = cryptoManager.encrypt(originalText)

        assertTrue(
            "Duas encriptações do mesmo texto devem produzir resultados diferentes",
            firstEncryption != secondEncryption
        )
    }
}
