package com.marywaft.fingerprint.data

import android.content.Context
import com.marywaft.fingerprint.extensions.md5

/**
 * Class stores pin code data.
 */
internal class DataSource private constructor(context: Context) : IDataSource {
    private val sharedPrefs = context.getSharedPreferences(FILE_NAME__PIN_CODE, Context.MODE_PRIVATE)

    override fun putPinCode(pinCode: String): Unit =
            sharedPrefs.edit().putString(KEY__PIN_CODE, pinCode.md5()).apply()

    override fun checkPinCode(pinCode: String): Boolean =
            pinCode.md5() == sharedPrefs.getString(KEY__PIN_CODE, null)

    override fun putEncodedPinCode(encodedPinCode: String?): Unit =
            sharedPrefs.edit().putString(KEY__ENCODED_PIN_CODE, encodedPinCode).apply()

    override fun getEncodedPinCode(defValue: String?): String? =
            sharedPrefs.getString(KEY__ENCODED_PIN_CODE, defValue)

    override fun containsEncodedPinCode(): Boolean =
            sharedPrefs.contains(KEY__ENCODED_PIN_CODE)

    override fun removeEncodedPinCode(): Unit =
            sharedPrefs.edit().remove(KEY__ENCODED_PIN_CODE).apply()

    companion object {
        private const val FILE_NAME__PIN_CODE = "__pin_code"
        private const val KEY__PIN_CODE = "_key__pin_code"
        private const val KEY__ENCODED_PIN_CODE = "_key__encoded_pin_code"

        @Volatile private var INSTANCE: IDataSource? = null

        fun getInstance(context: Context): IDataSource =
                INSTANCE ?: synchronized(lock = this) {
                    INSTANCE ?: DataSource(context).also { INSTANCE = it }
                }
    }
}

/**
 * Interface for accessing pin code data source.
 */
interface IDataSource {
    /**
     * Put pin code value to data source.
     * @param pinCode Value.
     */
    fun putPinCode(pinCode: String)

    /**
     * Check pin code that stores with input value.
     * @param pinCode Value to compare.
     * @return true if values are equals, false - otherwise.
     */
    fun checkPinCode(pinCode: String): Boolean

    /**
     * Put encoded pin code value to data source.
     * @param encodedPinCode Encoded pin code value.
     */
    fun putEncodedPinCode(encodedPinCode: String?)

    /**
     * Retrieve an encoded pin code value.
     * @return Returns encoded pin code value, if it exists or defValue.
     */
    fun getEncodedPinCode(defValue: String? = null): String?

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