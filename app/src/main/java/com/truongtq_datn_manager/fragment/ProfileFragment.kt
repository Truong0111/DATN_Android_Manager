package com.truongtq_datn_manager.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint
import com.truongtq_datn_manager.extensions.Constants
import com.truongtq_datn_manager.activity.MainActivity
import com.truongtq_datn_manager.databinding.FragmentProfileBinding
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.extensions.Pref
import com.truongtq_datn_manager.okhttpcrud.GetRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment(private val mainActivity: MainActivity) : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.btnChangePassword.setOnClickListener { openChangePasswordDialog() }
        initValueProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openChangePasswordDialog() {
        ChangePasswordDialogFragment(mainActivity).show(
            childFragmentManager,
            "ChangePasswordDialog"
        )
    }

    private var isGetInfo: Boolean = false

    private fun initValueProfile() {
        if (isGetInfo) return

        lifecycleScope.launch(Dispatchers.IO) {
            val idAccount = Pref.getString(mainActivity, Constants.ID_ACCOUNT)
            val getAccountApi = "${ApiEndpoint.Endpoint_Account}/${idAccount}"
            val getRequest = GetRequest(getAccountApi)
            val response = getRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    val info = Extensions.extractJson(response.body!!.string())
                    binding.profileName.text =
                        buildString {
                            append(Extensions.removePreAndSuffix(info.get("firstName").toString()))
                            append(" ")
                            append(Extensions.removePreAndSuffix(info.get("lastName").toString()))
                        }

                    binding.profileEmail.text =
                        Extensions.removePreAndSuffix(info.get("email").toString())
                    binding.profilePhoneNumber.text =
                        Extensions.removePreAndSuffix(info.get("phoneNumber").toString())
                    binding.profileRefId.text =
                        Extensions.removePreAndSuffix(info.get("refId").toString())

                    isGetInfo = true
                } else {
                    Extensions.toastCall(context, "Failed to load account")
                }
            }
        }
    }

}