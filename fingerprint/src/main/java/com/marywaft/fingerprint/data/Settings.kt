package com.marywaft.fingerprint.data

import android.content.Context
import android.preference.PreferenceManager

/**
 * Class stores user preferences.
 */
internal class Settings private constructor(context: Context) : ISettings {
    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun setUseFingerprintToAuth(enabled: Boolean): Unit =
            sharedPrefs.edit().putBoolean(KEY__USE_FINGERPRINT_TO_AUTH, enabled).apply()

    override fun getUseFingerprintToAuth(defValue: Boolean): Boolean =
            sharedPrefs.getBoolean(KEY__USE_FINGERPRINT_TO_AUTH, defValue)

    companion object {
        private const val KEY__USE_FINGERPRINT_TO_AUTH = "_key__use_fingerprint_to_auth"

        @Volatile private var INSTANCE: ISettings? = null

        fun getInstance(context: Context): ISettings =
                INSTANCE ?: synchronized(lock = this) {
                    INSTANCE ?: Settings(context).also { INSTANCE = it }
                }
    }
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