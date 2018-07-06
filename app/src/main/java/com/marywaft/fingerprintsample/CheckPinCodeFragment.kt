package com.marywaft.fingerprintsample

import android.annotation.TargetApi
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.marywaft.fingerprint.Injection
import com.marywaft.fingerprint.crypto.*
import com.marywaft.fingerprint.data.IDataSource
import com.marywaft.fingerprint.data.ISettings
import com.marywaft.fingerprint.view.KeyboardView
import com.marywaft.fingerprint.view.PinCodeView
import kotlinx.android.synthetic.main.fragment__check_pin_code.view.*

class CheckPinCodeFragment : Fragment() {
    private lateinit var dataSource: IDataSource
    private lateinit var settings: ISettings
    private lateinit var fingerprintManager: IFingerprintManager
    private lateinit var keyManager: IKeyManager

    private lateinit var subtitleTextView: TextView
    private lateinit var pinCodeView: PinCodeView
    private lateinit var keyboardView: KeyboardView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataSource = Injection.provideDataSource(context)
        settings = Injection.provideSettings(context)

        fingerprintManager = Injection.provideFingerprintManager(context)
        keyManager = Injection.provideKeyManager()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment__check_pin_code, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subtitleTextView = view.fragment__check_pin_code__tv_subtitle

        pinCodeView = view.fragment__check_pin_code__pcv
        pinCodeView.onPinCodeCompleteListener = onPinCodeCompleteListener

        keyboardView = view.fragment__check_pin_code__kv
        keyboardView.onKeyboardClickListener = onKeyboardClickListener

        if (savedInstanceState == null) {
            updateView()
        } else {
            val pinCode = savedInstanceState.getString(KEY__PIN_CODE)
            updateView(pinCode)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY__PIN_CODE, pinCodeView.value)
    }

    override fun onStart() {
        super.onStart()
        startFingerprintListening()
    }

    override fun onStop() {
        super.onStop()
        stopFingerprintListening()
    }

    private fun updateView(pinCode: String? = null) {
        pinCodeView.setPinCode(pinCode)
        if (fingerprintManager.equalsStatus(Status.Available) && settings.getUseFingerprintToAuth()
                && dataSource.containsEncodedPinCode())
        {
            subtitleTextView.setText(R.string.fragment__subtitle__sign_in_pin_code_or_touch_sensor)
            keyboardView.setFingerprintEnabled(true)
        } else {
            subtitleTextView.setText(R.string.fragment__subtitle__sign_in_pin_code)
            keyboardView.setFingerprintEnabled(false)
        }
    }

    private fun startFingerprintListening() {
        if (fingerprintManager.equalsStatus(Status.Available) && settings.getUseFingerprintToAuth()
            && dataSource.containsEncodedPinCode())
        {
            val cryptoObject = keyManager.getCryptoObject()
            if (cryptoObject != null) {
                fingerprintManager.startAuthListening(cryptoObject, fingerprintAuthCallback)
            } else {
                dataSource.removeEncodedPinCode()
                keyboardView.setFingerprintEnabled(false)
                Toast.makeText(context, R.string.message__fingerprint_new_enrolled, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopFingerprintListening() {
        fingerprintManager.stopAuthListening()
    }

    private val onPinCodeCompleteListener = object: PinCodeView.OnPinCodeCompleteListener {
        //todo: check fast user on new callback...
        override fun onPinCodeComplete(pinCode: String) {
            if (!dataSource.checkPinCode(pinCode)) {
                showDialogPinCodeInvalid()
                return
            }
            // if code removed (new fingerprint enrolled) save it
            if (fingerprintManager.equalsStatus(Status.Available) && !dataSource.containsEncodedPinCode()) {
                savePinCodeEncodedByFingerprint(pinCode)
            }
            showPinCodeChecked()
        }
    }

    private val onKeyboardClickListener = object: KeyboardView.OnKeyboardClickListener {
        override fun onKeyButtonClick(key: Char?): Unit = pinCodeView.setPinCodeKey(key)

        override fun onFingerprintClick(): Unit = showDialogSignInByFingerprint()

        override fun onBackClick(): Unit = pinCodeView.removePinCodeKey()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private val fingerprintAuthCallback = object: FingerprintAuthCallback {
        override fun onAuthenticationMessage(messageString: CharSequence) {
            Toast.makeText(context, messageString, Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationSucceeded(cryptoObject: FingerprintManager.CryptoObject) {
            val encoded = dataSource.getEncodedPinCode()
            val decoded = keyManager.decode(encoded!!, cryptoObject.cipher!!)
            //TODO: check fast user on new callback...
            pinCodeView.setPinCode(decoded)
        }

        override fun onAuthenticationFailed() {
            Toast.makeText(context, R.string.message__fingerprint_try_again, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPinCodeChecked(): Unit =
            Toast.makeText(context!!, "Pin Code Checked", Toast.LENGTH_SHORT).show()

    private fun showDialogPinCodeInvalid() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog__title__pin_code_invalid)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog__button__positive_ok) { _, _ -> updateView() }
                .show()
    }

    private fun showDialogSignInByFingerprint() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog__title__sign_in_by_fingerprint)
                .setMessage(R.string.dialog__subtitle__touch_sensor)
                .setIcon(R.drawable.ic_fingerprint__red)
                .setCancelable(true)
                .setNeutralButton(R.string.dialog__button__neutral) { _, _ -> }
                .show()
    }

    private fun savePinCodeEncodedByFingerprint(pinCode: String) {
        val encoded = keyManager.encode(pinCode)
        dataSource.putEncodedPinCode(encoded)
    }

    companion object {
        private const val KEY__PIN_CODE = "_key__pin_code"

        fun create(): CheckPinCodeFragment = CheckPinCodeFragment()
    }
}