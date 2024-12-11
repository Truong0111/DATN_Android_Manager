package com.truongtq_datn.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.truongtq_datn.databinding.FragmentQrBinding
import com.truongtq_datn.extensions.Constants
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.extensions.Pref
import com.truongtq_datn.okhttpcrud.ApiEndpoint
import com.truongtq_datn.okhttpcrud.PostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrFragment(private val mainActivity: Activity) : Fragment() {

    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!
    private var gson = Gson()
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.qrBtnScan.setOnClickListener { checkPermissionsCamera(mainActivity) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        _binding = null
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            run {
                if (isGranted) {
                    openQRScannerToAccessDoor()
                } else {
                    Extensions.toastCall(mainActivity, "Camera permission is required")
                }
            }
        }

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Extensions.toastCall(mainActivity, "Cancelled")
        } else {
            requestAccessDoor(result.contents)
        }
    }

    private fun checkPermissionsCamera(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openQRScannerToAccessDoor()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Extensions.toastCall(context, "Camera permission is required")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openQRScannerToAccessDoor() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan QR to access door")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }

    private fun requestAccessDoor(token: String) {
        job = lifecycleScope.launch(Dispatchers.IO) {
            val parts = token.split("::")
            val idAccount = Pref.getString(mainActivity, Constants.ID_ACCOUNT)

            val requestBody = gson.toJson(mapOf("token" to parts[1]))

            val accessDoorApi =
                "${ApiEndpoint.Endpoint_Door}?idDoor=${parts[0]}&idAccount=${idAccount}";
            val postRequest = PostRequest(accessDoorApi, requestBody)
            val response = postRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response == null) {
                    Extensions.toastCall(mainActivity, "Failed to request access door!")
                    return@withContext
                }

                val responseBody = response.body!!.string()

                if (response.isSuccessful) {
                    Extensions.toastCall(
                        mainActivity,
                        Extensions.extractJson(responseBody).get("message").toString()
                    )
                } else {
                    Extensions.toastCall(
                        mainActivity,
                        Extensions.extractJson(responseBody).get("message").toString()
                    )
                }
            }
        }
    }
}