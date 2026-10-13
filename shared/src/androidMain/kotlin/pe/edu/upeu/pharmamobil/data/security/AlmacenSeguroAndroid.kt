package pe.edu.upeu.pharmamobil.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import pe.edu.upeu.pharmamobil.domain.security.AlmacenSeguro

class AlmacenSeguroAndroid(
    context: Context
) : AlmacenSeguro {

    private val preferencias =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun guardarToken(token: String) {

        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.ENCRYPT_MODE, claveDelKeystore())

        val iv = cifrador.iv
        val cifrado = cifrador.doFinal(token.toByteArray(Charsets.UTF_8))

        val empaquetado = Base64.encodeToString(iv + cifrado, Base64.NO_WRAP)

        preferencias.edit().putString(CLAVE_TOKEN, empaquetado).apply()
    }

    override fun leerToken(): String? {

        val empaquetado = preferencias.getString(CLAVE_TOKEN, null) ?: return null

        return try {
            val bytes = Base64.decode(empaquetado, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, TAMANIO_IV)
            val cifrado = bytes.copyOfRange(TAMANIO_IV, bytes.size)

            val descifrador = Cipher.getInstance(TRANSFORMACION)
            descifrador.init(
                Cipher.DECRYPT_MODE,
                claveDelKeystore(),
                GCMParameterSpec(TAMANIO_TAG_BITS, iv)
            )

            String(descifrador.doFinal(cifrado), Charsets.UTF_8)
        } catch (_: Exception) {
            borrarToken()
            null
        }
    }

    override fun borrarToken() {
        preferencias.edit().remove(CLAVE_TOKEN).apply()
    }

    private fun claveDelKeystore(): SecretKey {

        val keystore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        (keystore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)
            ?.let { return it.secretKey }

        val generador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        generador.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )

        return generador.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "pharmamobil_token"
        const val PREFS = "pharmamobil_seguro"
        const val CLAVE_TOKEN = "token"
        const val TRANSFORMACION = "AES/GCM/NoPadding"
        const val TAMANIO_IV = 12
        const val TAMANIO_TAG_BITS = 128
    }
}
