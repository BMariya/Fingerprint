package com.marywaft.fingerprint.data

import android.content.Context
import android.preference.PreferenceManager

/**
 * Class stores user preferences
 */
class Settings private constructor(context: Context) : ISettings {
    private val _useFingerprintToAuthKey = "_com_marywaft_fingerprint__key__use_fingerprint_to_auth"

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        @Volatile private var INSTANCE: ISettings? = null

        fun getInstance(context: Context): ISettings =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Settings(context).also { INSTANCE = it }
                }
    }

    override fun setUseFingerprintToAuth(enabled: Boolean): Unit =
            sharedPrefs.edit().putBoolean(_useFingerprintToAuthKey, enabled).apply()

    override fun getUseFingerprintToAuth(defValue: Boolean): Boolean =
            sharedPrefs.getBoolean(_useFingerprintToAuthKey, defValue)
}

/**
 * Interface for accessing user settings.
 */
interface ISettings {
    /**
     * Set user preference of using fingerprint to authenticate.
     * @param enabled Flag of using fingerprint.
     */
    fun setUseFingerprintToAuth(enabled: Boolean)

    /**
     * Retrieve user preference of using fingerprint to authenticate.
     * @return Returns the preference value if it exists or defValue.
     */
    fun getUseFingerprintToAuth(defValue: Boolean = false): Boolean
}