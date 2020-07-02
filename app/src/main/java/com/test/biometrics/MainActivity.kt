package com.test.biometrics

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

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

        findViewById<Button>(R.id.button).setOnClickListener { show() }
    }

    private fun show() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, authenticationCallback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun String?.writeResult() {
        findViewById<TextView>(R.id.textView).text = this
    }

}