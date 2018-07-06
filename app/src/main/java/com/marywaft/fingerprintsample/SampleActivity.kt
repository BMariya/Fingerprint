package com.marywaft.fingerprintsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity__sample.*

class SampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__sample)

        activity__sample__btn_set_pin_code.setOnClickListener { showSetPinCodeFragment() }
        activity__sample__btn_check_pin_code.setOnClickListener { showCheckPinCodeFragment() }
    }

    private fun showSetPinCodeFragment() {
        var fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG__SET_PIN_CODE) as SetPinCodeFragment?
        if (fragment == null) {
            fragment = SetPinCodeFragment.create()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.activity__sample__fl_content, fragment, FRAGMENT_TAG__SET_PIN_CODE)
                    .commit()
        }
    }

    private fun showCheckPinCodeFragment() {
        var fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG__CHECK_PIN_CODE) as CheckPinCodeFragment?
        if (fragment == null) {
            fragment = CheckPinCodeFragment.create()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.activity__sample__fl_content, fragment, FRAGMENT_TAG__CHECK_PIN_CODE)
                    .commit()
        }
    }

    companion object {
        private const val FRAGMENT_TAG__SET_PIN_CODE = "_fragment_tag__set_pin_code"
        private const val FRAGMENT_TAG__CHECK_PIN_CODE = "_fragment_tag__check_pin_code"
    }
}