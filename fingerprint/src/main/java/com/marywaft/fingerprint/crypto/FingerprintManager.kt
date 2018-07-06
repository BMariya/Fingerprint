package com.marywaft.fingerprint.crypto

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.hardware.fingerprint.FingerprintManager as FpManager

/**
 * A class that contains possible statuses of fingerprint availability.
 */
enum class Status {NotAvailable, NotSecure, NoFingerprints, Available}

/**
 * A class that coordinates access to the fingerprint.
 */
internal class FingerprintManager private constructor(context: Context) : IFingerprintManager {
    private var fingerprintManager: FpManager? = null
    private var keyguardManager: KeyguardManager? = null

    private var fingerprintIsSupported: Boolean = true

    private var fingerprintListener: FingerprintListener? = null

    init {
        // check fingerprint supporting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val service = context.getSystemService(android.content.Context.FINGERPRINT_SERVICE)
            if (service != null) {
                fingerprintManager = service as FpManager
                if (!fingerprintManager!!.isHardwareDetected) {
                    fingerprintIsSupported = false
                } else {
                    keyguardManager = context.getSystemService(android.content.Context.KEYGUARD_SERVICE)
                            as KeyguardManager
                }
            }
        } else fingerprintIsSupported = false
    }

    override fun equalsStatus(status: Status): Boolean {
        return getStatus() == status
    }

    override fun startAuthListening(cryptoObject: FpManager.CryptoObject, fingerprintAuthCallback: FingerprintAuthCallback) {
        fingerprintListener = FingerprintListener(fingerprintAuthCallback)
        fingerprintListener?.startAuth(cryptoObject)
    }

    override fun stopAuthListening() {
        fingerprintListener?.cancel()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun getStatus(): Status {
        return if (fingerprintIsSupported) {
            if (!keyguardManager!!.isKeyguardSecure) Status.NotSecure
            if (!fingerprintManager!!.hasEnrolledFingerprints()) Status.NoFingerprints
            Status.Available
        } else Status.NotAvailable
    }


    @TargetApi(Build.VERSION_CODES.M)
    private inner class FingerprintListener constructor(
            private val callback: FingerprintAuthCallback
    ) : FpManager.AuthenticationCallback() {
        private var cancellationSignal: CancellationSignal? = null

        fun startAuth(cryptoObject: android.hardware.fingerprint.FingerprintManager.CryptoObject) {
            cancellationSignal = CancellationSignal()
            fingerprintManager!!.authenticate(cryptoObject, cancellationSignal, 0,this, null)
        }

        fun cancel() {
            cancellationSignal?.cancel()
            cancellationSignal = null
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            callback.onAuthenticationMessage(messageString = errString)
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            callback.onAuthenticationMessage(messageString = helpString)
        }

        override fun onAuthenticationSucceeded(result: FpManager.AuthenticationResult) {
            callback.onAuthenticationSucceeded(result.cryptoObject)
        }

        override fun onAuthenticationFailed() {
            callback.onAuthenticationFailed()
        }
    }


    companion object {
        @Volatile private var INSTANCE: IFingerprintManager? = null

        fun getInstance(context: Context):IFingerprintManager =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: FingerprintManager(context).also { INSTANCE = it }
                }
    }
}

interface FingerprintAuthCallback {
    fun onAuthenticationMessage(messageString: CharSequence)

    fun onAuthenticationSucceeded(cryptoObject: FpManager.CryptoObject)

    fun onAuthenticationFailed()
}

/**
 * Interface that coordinates access to the fingerprint.
 */
interface IFingerprintManager {
    /**
     * Check status of fingerprint with input value.
     * @param status status to compare with
     * @return true if statuses are equals.
     */
    fun equalsStatus(status: Status): Boolean

    /**
     * Start listening user fingerprint.
     * @param cryptoObject
     * @param fingerprintAuthCallback
     */
    fun startAuthListening(cryptoObject: FpManager.CryptoObject, fingerprintAuthCallback: FingerprintAuthCallback)

    /**
     * Stop listening user fingerprint.
     */
    fun stopAuthListening()
}