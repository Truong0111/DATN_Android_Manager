package com.truongtq_datn.activity

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.truongtq_datn.extensions.Constants
import com.truongtq_datn.databinding.ActivityMainBinding
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.extensions.Pref
import com.truongtq_datn.extensions.BiometricPromptManager
import com.truongtq_datn.fragment.DoorFragment
import com.truongtq_datn.fragment.ProfileFragment
import com.truongtq_datn.fragment.QrFragment
import com.truongtq_datn.fragment.SettingFragment
import com.truongtq_datn.fragment.TicketFragment
import kotlinx.coroutines.Dispatchers
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

        //Check time token expired
        val timeTokenExpired = Pref.getLong(this, Constants.TOKEN_BIOMETRIC_TIME)

        if (timeTokenExpired < Date().time) {
            Extensions.changeIntent(this, LoginActivity::class.java)
            return
        }

        setupEventListeners()
        showQrFragment()
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

    private val fragmentCache = mutableMapOf<String, Fragment>()

    private fun showFragment(fragmentTag: String, fragmentSupplier: () -> Fragment) {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(binding.fragmentContainer.id)

        if (currentFragment != null && currentFragment.tag == fragmentTag) return

        val fragment = fragmentCache[fragmentTag] ?: fragmentSupplier().also {
            fragmentCache[fragmentTag] = it
        }

        fragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment, fragmentTag)
            .addToBackStack(null)
            .setReorderingAllowed(true)
            .commit()
    }

    //QR functions
    private fun showQrFragment() {
        showFragment(Constants.FRAGMENT_TAG_QR) { QrFragment(this) }
        updateButtonSelection(binding.mainBtnQrCode)
    }

    //Profile Functions
    private fun showProfileFragment() {
        showFragment(Constants.FRAGMENT_TAG_PROFILE) { ProfileFragment(this) }
        updateButtonSelection(binding.mainBtnProfile)
    }

    //Door functions
    private fun showDoorFragment() {
        showFragment(Constants.FRAGMENT_TAG_DOOR) { DoorFragment(this) }
        updateButtonSelection(binding.mainBtnDoor)
    }


    //Ticket functions
    private fun showTicketFragment() {
        showFragment(Constants.FRAGMENT_TAG_TICKET) { TicketFragment(this) }
        updateButtonSelection(binding.mainBtnTickets)
    }

    //Setting Function
    private fun showSettingFragment() {
        showFragment(Constants.FRAGMENT_TAG_SETTING) { SettingFragment(this) }
        updateButtonSelection(binding.mainBtnSetting)
    }


}