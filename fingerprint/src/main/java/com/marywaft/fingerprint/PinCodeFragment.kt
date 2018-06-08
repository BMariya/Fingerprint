package com.marywaft.fingerprint

import android.annotation.TargetApi
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.marywaft.fingerprint.crypto.IFingerprintManager
import com.marywaft.fingerprint.crypto.IKeyManager
import com.marywaft.fingerprint.crypto.Status
import com.marywaft.fingerprint.data.IDataSource
import com.marywaft.fingerprint.data.ISettings
import com.marywaft.fingerprint.view.PinCodeView
import com.marywaft.fingerprint.view.KeyboardView

class PinCodeFragment : Fragment() {
    companion object {
        private const val STATE_ARG_KEY = "_key__state_arg"

        fun createSetPinCode(): PinCodeFragment {
            return create(State.SetPinCode)
        }

        fun createCheckPinCode(): PinCodeFragment {
            return create(State.CheckPinCode)
        }

        private fun create(state: State) : PinCodeFragment {
            val pinCodeFragment = PinCodeFragment()
            val args = Bundle()
            args.putSerializable(STATE_ARG_KEY, state)
            pinCodeFragment.arguments = args
            return pinCodeFragment
        }
    }

    private val _stateKey = "_key__state"
    private val _pinCodeKey = "_key__pin_code"
    private val _pinCodeConfirmKey = "_key__pin_code_confirm"

    private lateinit var dataSource: IDataSource
    private lateinit var settings: ISettings
    private lateinit var fingerprintManager: IFingerprintManager
    private lateinit var keyManager: IKeyManager

    private var fingerprintListener: FingerprintListener? = null

    private lateinit var callback: PinCodeInteractionListener

    private enum class State {SetPinCode, SetPinCodeConfirm, CheckPinCode}
    private lateinit var state: State

    private var pinCode: String? = null
    private var pinCodeConfirm: String? = null

    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var pinCodeView: PinCodeView
    private lateinit var keyboardView: KeyboardView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataSource = Injection.provideDataSource(context)
        settings = Injection.provideSettings(context)

        fingerprintManager = Injection.provideFingerprintManager(context)
        keyManager = Injection.provideKeyManager()

        callback = context as PinCodeInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment__pin_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTextView = view.findViewById(R.id.fragment__pin_code__tv_title)
        subtitleTextView = view.findViewById(R.id.fragment__pin_code__tv_subtitle)

        pinCodeView = view.findViewById(R.id.fragment__pin_code__pcv)
        pinCodeView.onPinCodeCompleteListener = onPinCodeCompleteListener

        keyboardView = view.findViewById(R.id.fragment__pin_code__kv)
        keyboardView.onKeyboardClickListener = onKeyboardClickListener

        if (savedInstanceState == null) {
            arguments?.let {
                val state = it.getSerializable(STATE_ARG_KEY) as State
                setState(state, pinCode, pinCodeConfirm)
            }
        } else {
            val pinCode = savedInstanceState.getString(_pinCodeKey)
            val pinCodeConfirm = savedInstanceState.getString(_pinCodeConfirmKey)

            val state = savedInstanceState.getSerializable(_stateKey) as State
            setState(state, pinCode, pinCodeConfirm)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(_pinCodeKey, pinCode)
        outState.putString(_pinCodeConfirmKey, pinCodeView.value)
        outState.putSerializable(_stateKey, state)
    }

    override fun onStart() {
        super.onStart()
        if (state == State.CheckPinCode && dataSource.containsEncodedPinCode()) {
            startFingerprintListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (state == State.CheckPinCode) {
            stopFingerprintListening()
        }
    }

    private fun setState(newState: State, newPinCode: String? = null, newPinCodeConfirm: String? = null) {
        state = newState
        pinCode = newPinCode
        pinCodeConfirm = newPinCodeConfirm
        when (state) {
            State.SetPinCode -> {
                titleTextView.setText(R.string.fragment__title__sign_in)
                subtitleTextView.setText(R.string.fragment__subtitle__set_pin_code)
                pinCodeView.setPinCode(pinCodeConfirm)
                keyboardView.setFingerprintEnabled(false)
            }
            State.SetPinCodeConfirm -> {
                titleTextView.setText(R.string.fragment__title__sign_in)
                subtitleTextView.setText(R.string.fragment__subtitle__confirm_pin_code)
                pinCodeView.setPinCode(pinCodeConfirm)
                keyboardView.setFingerprintEnabled(false)
            }
            State.CheckPinCode -> {
                titleTextView.setText(R.string.fragment__title__pin_code)
                if (fingerprintManager.getStatus() == Status.Available
                        && settings.getUseFingerprintToAuth()
                        && dataSource.containsEncodedPinCode()) {
                    subtitleTextView.setText(R.string.fragment__subtitle__sign_in_pin_code_or_touch_sensor)
                    keyboardView.setFingerprintEnabled(true)
                } else {
                    subtitleTextView.setText(R.string.fragment__subtitle__sign_in_pin_code)
                    keyboardView.setFingerprintEnabled(false)
                }
                pinCodeView.setPinCode(pinCodeConfirm)
            }
        }
    }

    private fun startFingerprintListening() {
        if (fingerprintManager.getStatus() == Status.Available
                && settings.getUseFingerprintToAuth()) {
            val cryptoObject = keyManager.getCryptoObject()
            if (cryptoObject != null) {
                fingerprintListener = FingerprintListener(context!!)
                fingerprintListener?.startAuth(cryptoObject)
            } else {
                dataSource.removeEncodedPinCode()
                //todo: скрыть отпечаток
                Toast.makeText(context, R.string.message__fingerprint_new_enrolled, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopFingerprintListening() {
        fingerprintListener?.cancel()
    }

    private val onPinCodeCompleteListener = object: PinCodeView.OnPinCodeCompleteListener {
        //todo: при быстром вводе сработал новый callback...
        override fun onPinCodeComplete(value: String) {
            when (state) {
                State.SetPinCode ->
                    setState(State.SetPinCodeConfirm, value)
                State.SetPinCodeConfirm -> {
                    if (pinCode != value) {
                        showDialogPinCodeInvalid()
                        return
                    }
                    dataSource.putPinCode(value)
                    if (fingerprintManager.getStatus() == Status.Available) {
                        savePinCodeEncodedByFingerprint(value)
                        showDialogUseFingerprint()
                        return
                    }
                    callback.onPinCodeSetUp()
                    callback.onPinCodeChecked()
                }
                State.CheckPinCode -> {
                    if (!dataSource.checkPinCode(value)) {
                        showDialogPinCodeInvalid()
                        return
                    }
                    // if code removed (new fingerprint enrolled) save it
                    if (fingerprintManager.getStatus() == Status.Available
                            && !dataSource.containsEncodedPinCode()) {
                        savePinCodeEncodedByFingerprint(value)
                    }
                    callback.onPinCodeChecked()
                }
            }
        }
    }

    private val onKeyboardClickListener = object: KeyboardView.OnKeyboardClickListener {
        override fun onKeyButtonClick(key: Char?): Unit = pinCodeView.setPinCodeKey(key)

        override fun onFingerprintClick(): Unit = showDialogSignInByFingerprint()

        override fun onBackClick(): Unit = pinCodeView.removePinCodeKey()
    }

    private fun showDialogPinCodeInvalid() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog__title__pin_code_invalid)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog__button__positive_ok, { _, _ ->
                    when (state) {
                        State.SetPinCode -> {}
                        State.SetPinCodeConfirm ->
                            setState(State.SetPinCode)
                        State.CheckPinCode ->
                            setState(State.CheckPinCode)
                    }
                })
                .show()
    }

    private fun showDialogUseFingerprint() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog__title__use_fingerprint)
                .setIcon(R.drawable.ic_fingerprint__red)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog__button__positive_yes, { _, _ ->
                    settings.setUseFingerprintToAuth(true)

                    callback.onPinCodeSetUp()
                    callback.onPinCodeChecked()

                    callback.onFingerprintEnabled()
                })
                .setNegativeButton(R.string.dialog__button__negative, { _, _ ->
                    settings.setUseFingerprintToAuth(false)

                    callback.onPinCodeSetUp()
                    callback.onPinCodeChecked()

                    callback.onFingerprintDisabled()
                })
                .show()
    }

    private fun showDialogSignInByFingerprint() {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog__title__sign_in_by_fingerprint)
                .setMessage(R.string.dialog__subtitle__touch_sensor)
                .setIcon(R.drawable.ic_fingerprint__red)
                .setCancelable(true)
                .setNeutralButton(R.string.dialog__button__neutral, { _, _ -> })
                .show()
    }

    private fun savePinCodeEncodedByFingerprint(pin: String) {
        val encoded = keyManager.encode(pin)
        dataSource.putEncodedPinCode(encoded)
    }


    @TargetApi(Build.VERSION_CODES.M)
    internal inner class FingerprintListener constructor(
            private val context: Context
    ) : FingerprintManager.AuthenticationCallback() {
        private var cancellationSignal: CancellationSignal? = null

        fun startAuth(cryptoObject: FingerprintManager.CryptoObject) {
            cancellationSignal = CancellationSignal()
            val manager = fingerprintManager.getFingerprintManager()
            manager!!.authenticate(cryptoObject, cancellationSignal, 0,this, null)
        }

        fun cancel() {
            cancellationSignal?.cancel()
            cancellationSignal = null
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            Toast.makeText(context, helpString, Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
            val cipher = result.cryptoObject.cipher
            val encoded = dataSource.getEncodedPinCode()
            val decoded = keyManager.decode(encoded!!, cipher!!)
            //TODO: то же..., ассинхронно
            pinCodeView.setPinCode(decoded)
        }

        override fun onAuthenticationFailed() {
            Toast.makeText(context, R.string.message__fingerprint_try_again, Toast.LENGTH_SHORT).show()
        }
    }


    interface PinCodeInteractionListener {

        /**
         * Вызывается при успешной установки пин кода
         */
        fun onPinCodeSetUp()

        /**
         * Вызывается при успешной проверке введенного пин кода
         */
        fun onPinCodeChecked()

        /**
         * Вызывыется при включении входа по отпечатку
         */
        fun onFingerprintEnabled()

        /**
         * Вызывается при отключении функции входа по отпечатку
         */
        fun onFingerprintDisabled()
    }
}