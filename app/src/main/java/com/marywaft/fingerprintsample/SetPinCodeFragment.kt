package com.marywaft.fingerprintsample

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.marywaft.fingerprint.Injection
import com.marywaft.fingerprint.crypto.IFingerprintManager
import com.marywaft.fingerprint.crypto.IKeyManager
import com.marywaft.fingerprint.crypto.Status
import com.marywaft.fingerprint.data.IDataSource
import com.marywaft.fingerprint.data.ISettings
import com.marywaft.fingerprint.view.KeyboardView
import com.marywaft.fingerprint.view.PinCodeView
import kotlinx.android.synthetic.main.fragment__set_pin_code.view.*

class SetPinCodeFragment : Fragment() {
    private lateinit var dataSource: IDataSource
    private lateinit var settings: ISettings
    private lateinit var fingerprintManager: IFingerprintManager
    private lateinit var keyManager: IKeyManager

    private enum class State {Init, Confirm}
    private lateinit var state: State

    private var initPinCode: String? = null

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
    ): View = inflater.inflate(R.layout.fragment__set_pin_code, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subtitleTextView = view.fragment__set_pin_code__tv_subtitle

        pinCodeView = view.fragment__set_pin_code__pcv
        pinCodeView.onPinCodeCompleteListener = onPinCodeCompleteListener

        keyboardView = view.fragment__set_pin_code__kv
        keyboardView.setFingerprintEnabled(false)
        keyboardView.onKeyboardClickListener = onKeyboardClickListener

        if (savedInstanceState == null) {
            updateView(State.Init)
        } else {
            initPinCode = savedInstanceState.getString(KEY__INIT_PIN_CODE)
            val pinCode = savedInstanceState.getString(KEY__PIN_CODE)

            val state = savedInstanceState.getSerializable(KEY__STATE) as State
            updateView(state, pinCode)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY__INIT_PIN_CODE, initPinCode)
        outState.putString(KEY__PIN_CODE, pinCodeView.value)
        outState.putSerializable(KEY__STATE, state)
    }

    private fun updateView(state: State, pinCode: String? = null) {
        this.state = state
        pinCodeView.setPinCode(pinCode)
        when (this.state) {
            State.Init ->
                subtitleTextView.setText(R.string.fragment__subtitle__set_pin_code)
            State.Confirm ->
                subtitleTextView.setText(R.string.fragment__subtitle__confirm_pin_code)
        }
    }


    private val onPinCodeCompleteListener = object: PinCodeView.OnPinCodeCompleteListener {
        //todo: check fast user on new callback...
        override fun onPinCodeComplete(pinCode: String) {
            when (state) {
                State.Init -> {
                    initPinCode = pinCode
                    updateView(State.Confirm)
                }
                State.Confirm -> {
                    if (initPinCode != pinCode) {
                        showDialogPinCodeInvalid()
                        return
                    }
                    dataSource.putPinCode(pinCode)
                    if (fingerprintManager.equalsStatus(Status.Available)) {
                        savePinCodeEncodedByFingerprint(pinCode)
                        showDialogUseFingerprint()
                        return
                    }
                    showPinCodeSetted()
                }
            }
        }
    }

    private val onKeyboardClickListener = object: KeyboardView.OnKeyboardClickListener {
        override fun onKeyButtonClick(key: Char?): Unit = pinCodeView.setPinCodeKey(key)

        override fun onFingerprintClick(): Unit = showDialogSignInByFingerprint()

        override fun onBackClick(): Unit = pinCodeView.removePinCodeKey()
    }

    private fun showPinCodeSetted(): Unit =
            Toast.makeText(context!!, "Pin Code Setted", Toast.LENGTH_SHORT).show()

    private fun showFingerprintEnabled(): Unit =
            Toast.makeText(context!!, "Fingerprint Enabled", Toast.LENGTH_SHORT).show()

    private fun showFingerprintDisabled(): Unit =
            Toast.makeText(context!!, "Fingerprint Disabled", Toast.LENGTH_SHORT).show()


    private fun showDialogPinCodeInvalid() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog__title__pin_code_invalid)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog__button__positive_ok) { _, _ ->
                    when (state) {
                        State.Init -> {}
                        State.Confirm -> {
                            initPinCode = null
                            updateView(State.Init)
                        }
                    }
                }
                .show()
    }

    private fun showDialogUseFingerprint() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog__title__use_fingerprint)
                .setIcon(R.drawable.ic_fingerprint__red)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog__button__positive_yes) { _, _ ->
                    settings.setUseFingerprintToAuth(true)
                    showPinCodeSetted()
                    showFingerprintEnabled()
                }
                .setNegativeButton(R.string.dialog__button__negative) { _, _ ->
                    settings.setUseFingerprintToAuth(false)
                    showPinCodeSetted()
                    showFingerprintDisabled()
                }
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
        private const val KEY__STATE = "_key__state"
        private const val KEY__INIT_PIN_CODE = "_key__init_pin_code"
        private const val KEY__PIN_CODE = "_key__pin_code"

        fun create(): SetPinCodeFragment = SetPinCodeFragment()
    }
}