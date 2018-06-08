package com.marywaft.fingerprint.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.marywaft.fingerprint.R

/**
 * View that represents button with key-value and hint.
 */
class KeyButtonView constructor(
        context: Context,
        attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    var key: Char? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view__key_button, this)
        if (attrs != null && !isInEditMode) {
            val attributes = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.KeyButtonView,
                    0,
                    0
            )
            val value = attributes.getString(R.styleable.KeyButtonView_key_button_view__value)
            setValue(value)
            val hint = attributes.getString(R.styleable.KeyButtonView_key_button_view__hint)
            setHint(hint)
        }
    }

    /**
     * Set key value.
     * @param value Value.
     */
    private fun setValue(value: String?) {
        findViewById<TextView>(R.id.view__key_button__tv_value).text = value
        key = value?.get(0)
    }

    /**
     * Set hint value.
     * @param hint Hint value.
     */
    private fun setHint(hint: String?) {
        val hintTextView = findViewById<TextView>(R.id.view__key_button__tv_hint)
        if (hint == null) {
            hintTextView.visibility = View.GONE
        } else {
            hintTextView.text = hint
            hintTextView.visibility = View.VISIBLE
        }
    }
}