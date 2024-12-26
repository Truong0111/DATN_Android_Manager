package com.truongtq_datn.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.truongtq_datn.okhttpcrud.ApiEndpoint
import com.truongtq_datn.databinding.ActivityRegisterBinding
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.model.Account
import com.truongtq_datn.okhttpcrud.PostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : ComponentActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEventListeners()
    }

    private fun setupEventListeners() {
        binding.registerBtnLogin.setOnClickListener {
            loginClicked()
        }

        binding.registerBtnRegister.setOnClickListener {
            registerClicked(
                binding.registerInputFirstName.text.toString(),
                binding.registerInputLastName.text.toString(),
                binding.registerInputEmail.text.toString(),
                binding.registerInputPassword.text.toString(),
                binding.registerInputPhoneNumber.text.toString(),
                binding.registerInputRefId.text.toString()
            )
        }
    }

    private fun loginClicked() {
        Extensions.changeIntent(this, LoginActivity::class.java)
    }

    private fun registerClicked(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        refId: String
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val loginApi = ApiEndpoint.Endpoint_Account_Register
            val account =
                Account(firstName, lastName, email, password, phoneNumber, refId, emptyList())

            val requestBody = gson.toJson(account)

            Extensions.toastCall(this@RegisterActivity, "Registering...")

            val postRequest = PostRequest(loginApi, requestBody)
            val response = postRequest.execute(false)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    Extensions.toastCall(this@RegisterActivity, "Register successful.")
                    Extensions.changeIntent(this@RegisterActivity, LoginActivity::class.java)
                } else {
                    Extensions.toastCall(this@RegisterActivity, "Register failed.")
                }
            }
        }
    }
}