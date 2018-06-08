package com.marywaft.fingerprint.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.marywaft.fingerprint.R

/**
 * View that represents 4-sign pin code.
 */
class PinCodeView constructor(
        context: Context,
        attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    // Max count of signs in pin code value.
    private val _maxCount = 4

    // Count of signs in pin code value that were setup.
    private var count: Int = -1
    private var arrayValue = arrayOfNulls<Char>(_maxCount)
    private val arrayImageView = arrayOfNulls<ImageView>(_maxCount)

    var value: String? = null
        get() = arrayValue.filterNotNull().joinToString(separator = "")

    var onPinCodeCompleteListener: OnPinCodeCompleteListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view__pin_code, this)
        arrayImageView[0] = findViewById(R.id.view__pin_code__iv_1)
        arrayImageView[1] = findViewById(R.id.view__pin_code__iv_2)
        arrayImageView[2] = findViewById(R.id.view__pin_code__iv_3)
        arrayImageView[3] =  findViewById(R.id.view__pin_code__iv_4)
        for (imageView in arrayImageView)
            imageView?.setImageResource(R.drawable.pin_code__off)
    }

    /**
     * Add new key for pin code value, if not null and
     * count of signs less than maximum possible count.
     * @param key New sign for pin code value.
     */
    fun setPinCodeKey(key: Char?) {
        if (key == null || count == _maxCount - 1) return
        arrayValue[++count] = key
        arrayImageView[count]?.setImageResource(R.drawable.pin_code__on)
        if (count == _maxCount - 1)
            onPinCodeCompleteListener?.onPinCodeComplete(value!!)
    }

    /**
     * Set new value for pin code.
     * @param value New pin code value.
     */
    fun setPinCode(value: String?) {
        removePinCode()
        if (value == null) return
        for (key in value)
            setPinCodeKey(key)
    }

    /**
     * Remove one last key of pin code value.
     */
    fun removePinCodeKey() {
        if (count == -1) return
        arrayValue[count] = null
        arrayImageView[count--]?.setImageResource(R.drawable.pin_code__off)
    }

    /**
     * Remove pin code value.
     */
    fun removePinCode() {
        while (count != -1)
            removePinCodeKey()
    }

    /**
     * Interface definition for a callback to be invoked when pin code value is completed.
     */
    interface OnPinCodeCompleteListener {
        /**
         * Called when pin code value is completed.
         * @param value Pin code value.
         */
        fun onPinCodeComplete(value: String)
    }
}