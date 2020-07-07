package com.test.biometrics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.MasterKeys
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            val error = when (errorCode) {
                BiometricConstants.ERROR_HW_UNAVAILABLE -> "HW_UNAVAILABLE"
                BiometricConstants.ERROR_UNABLE_TO_PROCESS -> "UNABLE_TO_PROCESS"
                BiometricConstants.ERROR_TIMEOUT -> "TIMEOUT"
                BiometricConstants.ERROR_NO_SPACE -> "NO_SPACE"
                BiometricConstants.ERROR_CANCELED -> "CANCELED"
                BiometricConstants.ERROR_LOCKOUT -> "LOCKOUT"
                BiometricConstants.ERROR_VENDOR -> "VENDOR"
                BiometricConstants.ERROR_LOCKOUT_PERMANENT -> "LOCKOUT_PERMANENT"
                BiometricConstants.ERROR_USER_CANCELED -> "USER_CANCELED"
                BiometricConstants.ERROR_NO_BIOMETRICS -> "NO_BIOMETRICS"
                BiometricConstants.ERROR_HW_NOT_PRESENT -> "HW_NOT_PRESENT"
                BiometricConstants.ERROR_NEGATIVE_BUTTON -> "NEGATIVE_BUTTON"
                BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL -> "NO_DEVICE_CREDENTIAL"
                else -> "UNKNOWN"
            }
            "onAuthenticationError: $errorCode ($error) | $errString".writeResult()
        }

        override fun onAuthenticationFailed() {
            "onAuthenticationFailed".writeResult()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            "onAuthenticationSucceeded: $result".writeResult()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button1).setOnClickListener { showWithoutCipher() }
        findViewById<Button>(R.id.button2).setOnClickListener { showWithCipher() }

        val canAuthenticate = BiometricManager.from(this)
            .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        val hasBiometrics = hasBiometrics(this)
        "canAuthenticate=$canAuthenticate | hasBiometrics=$hasBiometrics".writeResult()
    }

    private fun hasBiometrics(context: Context): Boolean {
        val pm = context.packageManager
        val features = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setOf(
                PackageManager.FEATURE_FINGERPRINT,
                PackageManager.FEATURE_IRIS,
                PackageManager.FEATURE_FACE
            )
        } else {
            setOf(PackageManager.FEATURE_FINGERPRINT)
        }
        return features.any(pm::hasSystemFeature)
    }

    private fun showWithoutCipher() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, authenticationCallback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun showWithCipher() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, authenticationCallback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
        val cipher = getCipher()
        val crypto = BiometricPrompt.CryptoObject(cipher)
        biometricPrompt.authenticate(promptInfo, crypto)
    }

    // Source: https://arctouch.com/blog/cryptographic-keys-fingerprint-authentication-android/
    private fun getCipher(): Cipher {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val keySpec = KeyGenParameterSpec.Builder(
            masterKeyAlias, // (1)
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT // (2)
        )
            .setKeySize(256) // (3)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC) // (4)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7) // (5)
//            .setUserAuthenticationRequired(true) // (6)
            .build()
        val keygen = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, // (1)
            "AndroidKeyStore" // (2)
        ).apply { init(keySpec) } // (3)

        val key: SecretKey = keygen.generateKey() // (4)

        return Cipher.getInstance("AES/CBC/PKCS7Padding")
            .apply { init(Cipher.ENCRYPT_MODE, key) }
    }

    private fun String?.writeResult() {
        findViewById<TextView>(R.id.textView).text = this
    }

}