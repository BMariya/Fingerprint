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
    // Count of signs in pin code value that were setup.
    private var count: Int = -1
    private var valueArray = arrayOfNulls<Char>(MAX_COUNT)
    private val imageViewArray = arrayOfNulls<ImageView>(MAX_COUNT)

    var value: String? = null
        get() = valueArray.filterNotNull().joinToString(separator = "")

    var onPinCodeCompleteListener: OnPinCodeCompleteListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view__pin_code, this)
        imageViewArray[0] = findViewById(R.id.view__pin_code__iv_1)
        imageViewArray[1] = findViewById(R.id.view__pin_code__iv_2)
        imageViewArray[2] = findViewById(R.id.view__pin_code__iv_3)
        imageViewArray[3] =  findViewById(R.id.view__pin_code__iv_4)
        for (imageView in imageViewArray)
            imageView?.setImageResource(R.drawable.pin_code__off)
    }

    /**
     * Add new key for pin code value, if not null and
     * count of signs less than maximum possible count.
     * @param key New sign for pin code value.
     */
    fun setPinCodeKey(key: Char?) {
        if (key == null || count == MAX_COUNT - 1) return
        valueArray[++count] = key
        imageViewArray[count]?.setImageResource(R.drawable.pin_code__on)
        if (count == MAX_COUNT - 1)
            onPinCodeCompleteListener?.onPinCodeComplete(value!!)
    }

    /**
     * Set new value for pin code.
     * @param value New pin code value.
     */
    fun setPinCode(value: String?) {
        removePinCode()
        value?.forEach { setPinCodeKey(it) }
    }

    /**
     * Remove one last key of pin code value.
     */
    fun removePinCodeKey() {
        if (count == -1) return
        valueArray[count] = null
        imageViewArray[count--]?.setImageResource(R.drawable.pin_code__off)
    }

    /**
     * Remove pin code value.
     */
    fun removePinCode() {
        while (count != -1)
            removePinCodeKey()
    }

    companion object {
        // Max count of signs in pin code value.
        private const val MAX_COUNT = 4
    }

    /**
     * Interface definition for a callback to be invoked when pin code value is completed.
     */
    interface OnPinCodeCompleteListener {
        /**
         * Called when pin code value is completed.
         * @param pinCode Pin code value.
         */
        fun onPinCodeComplete(pinCode: String)
    }
}