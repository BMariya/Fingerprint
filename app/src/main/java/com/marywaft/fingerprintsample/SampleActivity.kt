package com.marywaft.fingerprintsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.marywaft.fingerprint.PinCodeFragment

class SampleActivity : AppCompatActivity(), PinCodeFragment.PinCodeInteractionListener {
    private val _pinCodeFragmentTag = "_fragment_tag__pin_code"

    private var pinCodeFragment: PinCodeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__sample)

        pinCodeFragment = supportFragmentManager.findFragmentByTag(_pinCodeFragmentTag) as PinCodeFragment?
        if (pinCodeFragment == null) {
            pinCodeFragment = PinCodeFragment.createCheckPinCode()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.activity__sample__content, pinCodeFragment, _pinCodeFragmentTag)
                    .commit()
        }
    }

    override fun onPinCodeSetUp() {
        Toast.makeText(this, "onPinCodeSetUp", Toast.LENGTH_LONG).show()
    }
    override fun onPinCodeChecked() {
        Toast.makeText(this, "onPinCodeChecked", Toast.LENGTH_LONG).show()
    }

    override fun onFingerprintEnabled() {
        Toast.makeText(this, "onFingerprintEnabled", Toast.LENGTH_LONG).show()
    }

    override fun onFingerprintDisabled() {
        Toast.makeText(this, "onFingerprintDisabled", Toast.LENGTH_LONG).show()
    }
}