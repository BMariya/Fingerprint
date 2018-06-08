package com.marywaft.fingerprint

import android.content.Context
import com.marywaft.fingerprint.crypto.FingerprintManager
import com.marywaft.fingerprint.crypto.IFingerprintManager
import com.marywaft.fingerprint.crypto.IKeyManager
import com.marywaft.fingerprint.crypto.KeyManager
import com.marywaft.fingerprint.data.DataSource
import com.marywaft.fingerprint.data.IDataSource
import com.marywaft.fingerprint.data.ISettings
import com.marywaft.fingerprint.data.Settings

/**
 * Class provides dependency injection of singleton instances.
 */
class Injection {
    companion object {
        fun provideDataSource(context: Context): IDataSource = DataSource.getInstance(context)

        fun provideSettings(context: Context): ISettings = Settings.getInstance(context)

        fun provideFingerprintManager(context: Context): IFingerprintManager =
                FingerprintManager.getInstance(context)

        fun provideKeyManager(): IKeyManager = KeyManager.getInstance()
    }
}