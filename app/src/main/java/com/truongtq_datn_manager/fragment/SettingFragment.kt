package com.truongtq_datn_manager.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.truongtq_datn_manager.extensions.Constants
import com.truongtq_datn_manager.activity.LoginActivity
import com.truongtq_datn_manager.activity.MainActivity
import com.truongtq_datn_manager.databinding.FragmentSettingBinding
import com.truongtq_datn_manager.extensions.BiometricPromptManager
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.extensions.Pref

class SettingFragment(private val mainActivity: MainActivity) : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var biometricPromptManager: BiometricPromptManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        biometricPromptManager = mainActivity.biometricPromptManager
        binding.settingSwitchBiometric.isChecked = biometricPromptManager.isBiometricEnabled()
        binding.settingSwitchBiometric.setOnClickListener {
            if (binding.settingSwitchBiometric.isChecked) {
                mainActivity.showBiometric(
                    true,
                    description = "Authenticate to next login",
                    startAction = {
                        if (biometricPromptManager.isBiometricEnabled()) {
                            mainActivity.isBiometricEnabled = true
                            return@showBiometric
                        }
                    },
                    successAction = {
                        biometricPromptManager.enableBiometric()
                        val tokenBiometric =
                            Pref.getString(mainActivity, Constants.TOKEN_BIOMETRIC)
                        Extensions.saveAuthToken(mainActivity, tokenBiometric)
                        Extensions.toastCall(mainActivity, "Set biometric success")
                    },
                    failAction = {
                        Extensions.toastCall(mainActivity, "Set biometric failed")
                    },
                )
            } else {
                showDialog()
            }
        }

        binding.settingBtnLogout.setOnClickListener { logout() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog() {
        val dialog = MaterialAlertDialogBuilder(mainActivity)
            .setTitle("Confirm")
            .setMessage("Are you sure to continue")
            .setPositiveButton("Yes") { _, _ ->
                mainActivity.showBiometric(
                    false,
                    description = "Please authenticate to disable biometric",
                    startAction = {

                    },
                    successAction = {
                        biometricPromptManager.disableBiometric()
                        Pref.remove(mainActivity, Constants.ID_ACCOUNT)
                    },
                    failAction = {

                    },
                )
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                binding.settingSwitchBiometric.isChecked = true
                dialogInterface.dismiss()
            }
            .setOnDismissListener {
                binding.settingSwitchBiometric.isChecked = true
            }

        dialog.show()
    }

    private fun logout() {
        Pref.remove(mainActivity, Constants.TOKEN_BIOMETRIC_TIME)
        startActivity(Intent(activity, LoginActivity::class.java))
    }
}