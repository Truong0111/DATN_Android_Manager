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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.truongtq_datn.databinding.FragmentQrBinding
import com.truongtq_datn.extensions.Extensions
import kotlinx.coroutines.Job

class QrFragment(private val mainActivity: Activity) : Fragment() {

    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!
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
            Extensions.toastCall(mainActivity, result.contents)
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
}