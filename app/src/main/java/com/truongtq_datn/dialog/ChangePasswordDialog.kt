package com.truongtq_datn.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.truongtq_datn.databinding.DialogChangepasswordLayoutBinding
import com.truongtq_datn.extensions.Constants
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.extensions.Pref
import com.truongtq_datn.fragment.ProfileFragment
import com.truongtq_datn.okhttpcrud.ApiEndpoint
import com.truongtq_datn.okhttpcrud.PatchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangePasswordDialog(
    private val mainActivity: Activity,
    private val profileFragment: ProfileFragment
) : DialogFragment() {

    private var _binding: DialogChangepasswordLayoutBinding? = null
    private val binding get() = _binding!!
    private var gson = Gson()
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangepasswordLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        _binding = null
    }

    private fun changePassword() {
        val currentPassword = Pref.getString(mainActivity, Constants.PASSWORD)
        val oldPasswordInput = binding.oldPassword.text.toString()

        if (currentPassword != Extensions.sha256(oldPasswordInput)) {
            Extensions.toastCall(requireContext(), "Current password is not correct")
            return
        }

        val newPassword = binding.newPassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val newHashPassword = Extensions.sha256(newPassword)

        if (currentPassword == newHashPassword) {
            Extensions.toastCall(
                requireContext(),
                "New password cannot be the same as the old password"
            )
            return
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Extensions.toastCall(
                requireContext(),
                "New password and confirm password cannot be empty"
            )
            return
        }

        if (newPassword != confirmPassword) {
            Extensions.toastCall(requireContext(), "Confirm password is not correct")
            return
        }

        val accountId = Pref.getString(mainActivity, Constants.ID_ACCOUNT)

        job = lifecycleScope.launch(Dispatchers.IO) {
            val api = "${ApiEndpoint.Endpoint_Account}/$accountId"
            val requestBody = gson.toJson(mapOf("password" to newPassword))

            val patchRequest = PatchRequest(api, requestBody)
            val response = patchRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response == null) {
                    Extensions.toastCall(mainActivity, "Failed to change password")
                    return@withContext
                }

                if (response.isSuccessful) {
                    Pref.setString(mainActivity, Constants.PASSWORD, newHashPassword)
                    Extensions.toastCall(mainActivity, "Change password success")
                    dismissAllowingStateLoss()
                } else {
                    Extensions.toastCall(mainActivity, "Change password failed")
                }
            }
        }
    }
}
