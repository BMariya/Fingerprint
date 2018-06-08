package com.marywaft.fingerprint.crypto

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
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

    companion object {
        @Volatile private var INSTANCE: IFingerprintManager? = null

        fun getInstance(context: Context): IFingerprintManager =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: FingerprintManager(context).also { INSTANCE = it }
                }
    }

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

    @TargetApi(Build.VERSION_CODES.M)
    override fun getStatus(): Status =
            if (fingerprintIsSupported) {
                if (!keyguardManager!!.isKeyguardSecure) Status.NotSecure
                if (!fingerprintManager!!.hasEnrolledFingerprints()) Status.NoFingerprints
                Status.Available
            } else Status.NotAvailable


    override fun getFingerprintManager(): FpManager? = fingerprintManager
}

/**
 * Interface that coordinates access to the fingerprint.
 */
interface IFingerprintManager {
    /**
     * Get status of fingerprint availability.
     * @return Status.
     */
    fun getStatus(): Status

    /**
     * Get fingerprint manager.
     * @return FpManager.
     */
    fun getFingerprintManager(): FpManager?
}