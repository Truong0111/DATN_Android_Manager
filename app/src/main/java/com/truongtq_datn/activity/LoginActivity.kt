package com.truongtq_datn.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.truongtq_datn.okhttpcrud.ApiEndpoint
import com.truongtq_datn.extensions.Constants
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.databinding.ActivityLoginBinding
import com.truongtq_datn.extensions.Pref
import com.truongtq_datn.extensions.BiometricPromptManager
import com.truongtq_datn.model.LoginForm
import com.truongtq_datn.okhttpcrud.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var biometricPromptManager: BiometricPromptManager
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEventListeners()
    }

    private fun setupEventListeners() {
        binding.loginBtnRegister.setOnClickListener {
            Extensions.changeIntent(this, RegisterActivity::class.java)
        }

        binding.loginBtnLogin.setOnClickListener {
            loginClicked(
                binding.loginInputUsername.text.toString(),
                binding.loginInputPassword.text.toString()
            )
        }

        binding.imgBtnBiometric.setOnClickListener {
            showBiometricOnLogin()
        }
    }

    private fun showBiometricOnLogin() {
        biometricPromptManager = BiometricPromptManager(this)

        lifecycleScope.launch {
            biometricPromptManager.promptResults.collect { result ->
                when (result) {
                    is BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                        //Todo OAuth2
                    }

                    is BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                        //Todo OAuth2
                    }

                    is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                        //Todo OAuth2
                    }

                    is BiometricPromptManager.BiometricResult.AuthenticationSucceed -> {
                        //Save biometric
                        val token = Extensions.getAuthToken(this@LoginActivity)
                        if (token != null) {
                            setLifeTime()
                            Extensions.toastCall(applicationContext, "Login successful.")
                            Extensions.changeIntent(this@LoginActivity, MainActivity::class.java)
                            return@collect
                        }
                    }

                    is BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                        //Todo authenticate again or OAuth2
                    }

                    is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                        //Todo authenticate again or OAuth2
                    }
                }
            }
        }

        if (biometricPromptManager.isBiometricEnabled()) {
            biometricPromptManager.showBiometricPrompt(
                "Authenticate Biometric",
                "Login by biometric"
            )
        } else {
            Extensions.toastCall(this, "You don't register biometric.\nPlease login by password")
        }
    }

    private fun loginClicked(usernameInput: String, passwordInput: String) {
        if (binding.loginInputUsername.text.toString()
                .isEmpty() || binding.loginInputPassword.text.toString().isEmpty()
        ) {
            Extensions.toastCall(this, "Please enter username and password")
            return
        }
        Extensions.showLoadingFragment(supportFragmentManager)

        lifecycleScope.launch(Dispatchers.IO) {
            val loginApi = ApiEndpoint.Endpoint_Account_Login
            val loginForm = LoginForm(usernameInput, passwordInput, Constants.TYPE_APP)

            val requestBody = gson.toJson(loginForm)
            val postRequest = PostRequest(loginApi, requestBody)
            val response = postRequest.execute(false)

            withContext(Dispatchers.Main) {
                Extensions.hideLoadingFragment(supportFragmentManager)
                if (response == null) {
                    Extensions.toastCall(applicationContext, "Login failed")
                    return@withContext
                }

                val responseBody = response.body!!.string()

                if (response.isSuccessful) {
                    val token = Extensions.extractJson(responseBody).get("token")
                    Pref.setString(
                        this@LoginActivity,
                        Constants.JWT,
                        Extensions.removePreAndSuffix(token.toString())
                    )

                    val claimToken = Extensions.decodeJWT(token.toString())
                    val idAccount: String = claimToken.first.toString()
                    Pref.setString(this@LoginActivity, Constants.ID_ACCOUNT, idAccount)
                    Pref.setString(
                        this@LoginActivity,
                        Constants.PASSWORD,
                        Extensions.sha256(passwordInput)
                    )

                    val tokenBiometric = Extensions.sha256(idAccount)
                    Extensions.saveAuthToken(this@LoginActivity, tokenBiometric)

                    setLifeTime()

                    Extensions.toastCall(applicationContext, "Login successful.")
                    Extensions.changeIntent(this@LoginActivity, MainActivity::class.java);
                } else {
                    Extensions.toastCall(
                        applicationContext,
                        Extensions.extractJson(responseBody).get("message")
                            .toString()
                    )
                }
            }
        }
    }

    private fun setLifeTime() {
        val time = Date().time + 2 * 60 * 60 * 1000
        Pref.setLong(this@LoginActivity, Constants.TOKEN_BIOMETRIC_TIME, time)
    }
}