package com.marywaft.fingerprint.view

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import com.marywaft.fingerprint.R
import android.os.Vibrator

/**
 * View that represents 10-sign keyboard with back and fingerprint buttons.
 */
class KeyboardView constructor(
        context: Context,
        attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val _keyButtonCount = 10

    private val arrayKeyButtonView = arrayOfNulls<KeyButtonView>(_keyButtonCount)
    private var fingerprintImageView: ImageView
    private var backImageView: ImageView

    var onKeyboardClickListener: OnKeyboardClickListener? = null

    private val vibe: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val onClickListener = OnClickListener { view ->
        when (view) {
            is KeyButtonView -> {
                vibrate()
                onKeyboardClickListener?.onKeyButtonClick(view.key)
            }
            is ImageView -> {
                when (view.id) {
                    R.id.view__keyboard__iv_fingerprint ->
                        onKeyboardClickListener?.onFingerprintClick()
                    R.id.view__keyboard__iv_back ->
                        onKeyboardClickListener?.onBackClick()
                }
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view__keyboard, this)

        arrayKeyButtonView[0] = findViewById(R.id.view__keyboard__kbv_0)
        arrayKeyButtonView[1] = findViewById(R.id.view__keyboard__kbv_1)
        arrayKeyButtonView[2] = findViewById(R.id.view__keyboard__kbv_2)
        arrayKeyButtonView[3] = findViewById(R.id.view__keyboard__kbv_3)
        arrayKeyButtonView[4] = findViewById(R.id.view__keyboard__kbv_4)
        arrayKeyButtonView[5] = findViewById(R.id.view__keyboard__kbv_5)
        arrayKeyButtonView[6] = findViewById(R.id.view__keyboard__kbv_6)
        arrayKeyButtonView[7] = findViewById(R.id.view__keyboard__kbv_7)
        arrayKeyButtonView[8] = findViewById(R.id.view__keyboard__kbv_8)
        arrayKeyButtonView[9] = findViewById(R.id.view__keyboard__kbv_9)
        fingerprintImageView = findViewById(R.id.view__keyboard__iv_fingerprint)
        backImageView = findViewById(R.id.view__keyboard__iv_back)

        for (keyButtonView in arrayKeyButtonView) {
            keyButtonView?.setOnClickListener(onClickListener)
        }
        backImageView.setOnClickListener(onClickListener)

        setFingerprintEnabled(false)
    }

    /**
     * Set visibility of fingerprint button.
     * @param enabled If true set visible.
     */
    fun setFingerprintEnabled(enabled: Boolean) {
        if (enabled) {
            fingerprintImageView.setOnClickListener(onClickListener)
            fingerprintImageView.visibility = View.VISIBLE
            return
        }
        fingerprintImageView.setOnClickListener(null)
        fingerprintImageView.visibility = View.INVISIBLE
    }

    private fun vibrate() {
        val vibrateTimeInMillis = 30L
        if (vibe != null && vibe.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibe.vibrate(VibrationEffect.createOneShot(vibrateTimeInMillis,
                        VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                vibe.vibrate(vibrateTimeInMillis)
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when a keyboard button is clicked.
     */
    interface OnKeyboardClickListener {
        /**
         * Called when a key button has been clicked.
         * @param key Button key value.
         */
        fun onKeyButtonClick(key: Char?)

        /**
         * Called when a fingerprint button has been clicked.
         */
        fun onFingerprintClick()

        /**
         * Called when a back button has been clicked.
         */
        fun onBackClick()
    }
}