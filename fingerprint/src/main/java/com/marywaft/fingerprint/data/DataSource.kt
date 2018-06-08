package com.marywaft.fingerprint.data

import android.content.Context
import com.marywaft.fingerprint.extensions.md5

/**
 * Class stores pin code data.
 */
internal class DataSource private constructor(context: Context) : IDataSource {
    private val _pinCodeFileName = "_com_marywaft_fingerprint__pin_code"
    private val _pinCodeKey = "_key__pin_code"
    private val _encodedPinCodeKey = "_key__encoded_pin_code"

    private val sharedPrefs = context.getSharedPreferences(_pinCodeFileName, Context.MODE_PRIVATE)

    companion object {
        @Volatile private var INSTANCE: IDataSource? = null

        fun getInstance(context: Context): IDataSource =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: DataSource(context).also { INSTANCE = it }
                }
    }

    override fun putPinCode(pinCode: String?) {
        if (pinCode == null) return
        sharedPrefs.edit().putString(_pinCodeKey, pinCode.md5()).apply()
    }

    override fun checkPinCode(pinCode: String?): Boolean =
            if (pinCode == null) false
            else pinCode.md5() == sharedPrefs.getString(_pinCodeKey, null)


    override fun putEncodedPinCode(encodedPinCode: String?) {
        if (encodedPinCode == null) return
        sharedPrefs.edit().putString(_encodedPinCodeKey, encodedPinCode).apply()
    }

    override fun getEncodedPinCode(): String? =
            sharedPrefs.getString(_encodedPinCodeKey, null)

    override fun containsEncodedPinCode(): Boolean = sharedPrefs.contains(_encodedPinCodeKey)

    override fun removeEncodedPinCode(): Unit = sharedPrefs.edit().remove(_encodedPinCodeKey).apply()
}

/**
 * Interface for accessing pin code data source.
 */
interface IDataSource {
    /**
     * Put pin code value to data source, if not null.
     * @param pinCode Value.
     */
    fun putPinCode(pinCode: String?)

    /**
     * Check pin code that stores with input value.
     * @param pinCode Value to compare.
     * @return true if values are equals, false - otherwise or if input value is null.
     */
    fun checkPinCode(pinCode: String?): Boolean

    /**
     * Put encoded pin code value to data source, if not null.
     * @param encodedPinCode Encoded pin code value.
     */
    fun putEncodedPinCode(encodedPinCode: String?)

    /**
     * Retrieve an encoded pin code value.
     * @return Returns encoded pin code value, if it exists or null.
     */
    fun getEncodedPinCode(): String?

    /**
     * Checks whether the data contains an encoded pin code.
     * @return Returns true if it exists.
     */
    fun containsEncodedPinCode(): Boolean

    /**
     * Remove an encoded pin code value.
     */
    fun removeEncodedPinCode()
}