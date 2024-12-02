package com.truongtq_datn_manager.activity

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.truongtq_datn_manager.extensions.Constants
import com.truongtq_datn_manager.databinding.ActivityMainBinding
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.extensions.Pref
import com.truongtq_datn_manager.extensions.BiometricPromptManager
import com.truongtq_datn_manager.fragment.DoorFragment
import com.truongtq_datn_manager.fragment.ProfileFragment
import com.truongtq_datn_manager.fragment.QrFragment
import com.truongtq_datn_manager.fragment.SettingFragment
import com.truongtq_datn_manager.fragment.TicketFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var biometricPromptManager: BiometricPromptManager
    private var currentButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        biometricPromptManager = BiometricPromptManager(this)

        if (savedInstanceState == null) {
            showFragment(QrFragment(this))
        }

        //Check time token expired
        val timeTokenExpired = Pref.getLong(this, Constants.TOKEN_BIOMETRIC_TIME)

        if (timeTokenExpired < Date().time) {
            Extensions.changeIntent(this, LoginActivity::class.java)
            return
        }

        setupEventListeners()
    }

    override fun onStart() {
        super.onStart()
        if (biometricPromptManager.isBiometricEnabled()) {
            return
        } else {
            showBiometric(
                description = "Authenticate to next login",
                successAction = {
                    biometricPromptManager.enableBiometric()
                    val tokenBiometric =
                        Pref.getString(this@MainActivity, Constants.TOKEN_BIOMETRIC)
                    Extensions.saveAuthToken(this@MainActivity, tokenBiometric)
                    Extensions.toastCall(this@MainActivity, "Set biometric success")
                },
                failAction = {

                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val timeTokenExpired = Pref.getLong(this, Constants.TOKEN_BIOMETRIC_TIME)
        if (timeTokenExpired < Date().time) {
            Extensions.changeIntent(this, LoginActivity::class.java)
            return
        }
    }

    private fun setupEventListeners() {
        currentButton = binding.mainBtnQrCode
        binding.mainBtnQrCode.isSelected = true

        binding.mainBtnQrCode.setOnClickListener { showQrFragment() }
        binding.mainBtnProfile.setOnClickListener { showProfileFragment() }
        binding.mainBtnDoor.setOnClickListener { showDoorFragment() }
        binding.mainBtnTickets.setOnClickListener { showTicketFragment() }
        binding.mainBtnSetting.setOnClickListener { showSettingFragment() }
    }

    private fun updateButtonSelection(selectedButton: Button) {
        currentButton?.isSelected = false
        selectedButton.isSelected = true
        currentButton = selectedButton
    }

    fun showBiometric(
        title: String = "Authenticate Biometric",
        description: String,
        successAction: (() -> Unit)? = null,
        failAction: (() -> Unit)? = null,
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            val result = biometricPromptManager.promptResults.first()
            when (result) {
                is BiometricPromptManager.BiometricResult.HardwareUnavailable,
                is BiometricPromptManager.BiometricResult.FeatureUnavailable,
                is BiometricPromptManager.BiometricResult.AuthenticationNotSet,
                is BiometricPromptManager.BiometricResult.AuthenticationFailed,
                is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                    failAction?.invoke()
                }

                is BiometricPromptManager.BiometricResult.AuthenticationSucceed -> {
                    successAction?.invoke()
                }
            }
        }

        biometricPromptManager.showBiometricPrompt(
            title,
            description
        )
    }

    private fun showFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)

        currentFragment?.let {
            supportFragmentManager.beginTransaction()
                .detach(it)
                .commit()
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .addToBackStack(null)
            .setReorderingAllowed(true)
            .commit()

        supportFragmentManager.beginTransaction()
            .attach(fragment)
            .commit()
    }

    //QR functions
    private fun showQrFragment() {
        showFragment(QrFragment(this))
        updateButtonSelection(binding.mainBtnQrCode)
    }

    //Profile Functions
    private fun showProfileFragment() {
        showFragment(ProfileFragment(this))
        updateButtonSelection(binding.mainBtnProfile)
    }

    //Door functions
    private fun showDoorFragment() {
        showFragment(DoorFragment(this))
        updateButtonSelection(binding.mainBtnDoor)
    }


    //Ticket functions
    private fun showTicketFragment() {
        showFragment(TicketFragment(this))
        updateButtonSelection(binding.mainBtnTickets)
    }

    //Setting Function
    private fun showSettingFragment() {
        showFragment(SettingFragment(this))
        updateButtonSelection(binding.mainBtnSetting)
    }


}