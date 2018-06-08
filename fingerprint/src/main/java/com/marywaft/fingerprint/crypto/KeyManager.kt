package com.marywaft.fingerprint.crypto

import android.annotation.TargetApi
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

/**
 * This class provides the functionality of a cryptographic for encryption and decryption.
 */
@TargetApi(Build.VERSION_CODES.M)
internal class KeyManager private constructor() : IKeyManager {
    private val _androidKeyStore = "AndroidKeyStore"
    private val _keyAlias = "com_marywaft_fingerprint__key_alias__pin_code"

    private lateinit var keyStore: KeyStore
    private lateinit var keyPairGenerator: KeyPairGenerator
    private lateinit var cipher: Cipher

    companion object {
        @Volatile private var INSTANCE: IKeyManager? = null

        fun getInstance(): IKeyManager =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: KeyManager().also { INSTANCE = it }
                }
    }

    override fun encode(string: String): String? {
        try {
            if (prepareKey() && initCipher(Cipher.ENCRYPT_MODE)) {
                val bytes = cipher.doFinal(string.toByteArray())
                return Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            when (e) {
                is BadPaddingException,
                is IllegalBlockSizeException -> e.printStackTrace()
                else -> throw e
            }
        }
        return null
    }

    override fun decode(encodedString: String, cipher: Cipher): String? {
        try {
            val bytes = Base64.decode(encodedString, Base64.NO_WRAP)
            return String(cipher.doFinal(bytes))
        } catch (e:  Exception) {
            when (e) {
                is BadPaddingException,
                is IllegalBlockSizeException -> e.printStackTrace()
                else -> throw e
            }
        }
        return null
    }

    override fun getCryptoObject(): FingerprintManager.CryptoObject? =
            if (prepareKey() && initCipher(Cipher.DECRYPT_MODE))
                FingerprintManager.CryptoObject(cipher)
            else null

    private fun prepareKey(): Boolean {
        return setupKeyStore() && setupCipher() && isKeyReady()
    }

    private fun setupKeyStore(): Boolean {
        try {
            keyStore = KeyStore.getInstance(_androidKeyStore)
            keyStore.load(null)
            return true
        } catch (e:  Exception) {
            when (e) {
                is IOException,
                is NoSuchAlgorithmException,
                is CertificateException,
                is KeyStoreException -> e.printStackTrace()
                else -> throw e
            }
        }
        return false
    }

    private fun setupCipher(): Boolean {
        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            return true
        } catch (e:  Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchPaddingException -> e.printStackTrace()
                else -> throw e
            }
        }
        return false
    }

    private fun isKeyReady(): Boolean {
        try {
            return keyStore.containsAlias(_keyAlias) || generateKey()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
        return false
    }

    private fun generateKey(): Boolean {
        if (setupKeyPairGenerator()) {
            try {
                keyPairGenerator.initialize(KeyGenParameterSpec.Builder(_keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                        .setUserAuthenticationRequired(true)
                        .build())
                keyPairGenerator.generateKeyPair()
                return true
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun setupKeyPairGenerator(): Boolean {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, _androidKeyStore)
            return true
        } catch (e:  Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchProviderException -> e.printStackTrace()
                else -> throw e
            }
        }
        return false
    }

    private fun initCipher(mode: Int): Boolean {
        try {
            keyStore.load(null)
            when (mode) {
                Cipher.ENCRYPT_MODE -> initEncodeCipher(mode)
                Cipher.DECRYPT_MODE -> initDecodeCipher(mode)
                else -> return false
            }
            return true
        } catch (e:  Exception) {
            when (e) {
                is KeyPermanentlyInvalidatedException -> deleteInvalidKey()
                is KeyStoreException,
                is CertificateException,
                is UnrecoverableKeyException,
                is IOException,
                is NoSuchAlgorithmException,
                is InvalidKeyException,
                is InvalidKeySpecException,
                is InvalidAlgorithmParameterException -> e.printStackTrace()
                else -> throw e
            }
        }
        return false
    }

    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class,
            UnrecoverableKeyException::class, InvalidKeyException::class)
    private fun initDecodeCipher(mode: Int) {
        val key = keyStore.getKey(_keyAlias, null) as PrivateKey
        cipher.init(mode, key)
    }

    @Throws(KeyStoreException::class, InvalidKeySpecException::class,
            NoSuchAlgorithmException::class, InvalidKeyException::class,
            InvalidAlgorithmParameterException::class)
    private fun initEncodeCipher(mode: Int) {
        val publicKey = keyStore.getCertificate(_keyAlias).publicKey
        val key = KeyFactory.getInstance(publicKey.algorithm).generatePublic(X509EncodedKeySpec(publicKey.encoded))
        val parameterSpec = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
        cipher.init(mode, key, parameterSpec)
    }

    private fun deleteInvalidKey() {
        if (setupKeyStore()) {
            try {
                keyStore.deleteEntry(_keyAlias)
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * Interface provides the functionality of a cryptographic for encryption and decryption.
 */
interface IKeyManager {
    /**
     * Encode string.
     * @param string Input string value.
     * @return Encoded string.
     */
    fun encode(string: String): String?

    /**
     * Decode encoded string.
     * @param encodedString Encoded string value.
     * @param cipher Cipher for decode encoded string.
     * @return Decoded string value.
     */
    fun decode(encodedString: String, cipher: Cipher): String?

    /**
     * Get crypto object for access to key by fingerprint.
     * @return FingerprintManager.CryptoObject.
     */
    fun getCryptoObject(): FingerprintManager.CryptoObject?
}