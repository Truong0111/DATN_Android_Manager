package com.truongtq_datn_manager.dialog

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.truongtq_datn_manager.activity.MainActivity
import com.truongtq_datn_manager.databinding.DialogDoorCreateBinding
import com.truongtq_datn_manager.extensions.Constants
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.extensions.Pref
import com.truongtq_datn_manager.fragment.DoorFragment
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint
import com.truongtq_datn_manager.okhttpcrud.PostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoorCreateDialog(
    private val mainActivity: MainActivity,
    private val doorFragment: DoorFragment
) : DialogFragment() {

    private var _binding: DialogDoorCreateBinding? = null
    private val binding get() = _binding!!
    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDoorCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.doorBtnId.setOnClickListener { checkPermissionsCamera(mainActivity) }

        binding.doorBtnCreateDoor.setOnClickListener { submitCreateDoorRequest() }

        binding.doorBtnCancel.setOnClickListener { dismiss() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submitCreateDoorRequest() {
        val position = binding.doorPositionInput.text.toString()
        val idAccountCreate = Pref.getString(mainActivity, Constants.ID_ACCOUNT)

        if (position.isEmpty()) {
            Extensions.toastCall(mainActivity, "All fields are required")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val api = ApiEndpoint.Endpoint_Door_Create

            val body =
                gson.toJson(
                    mapOf(
                        "position" to position,
                        "idAccountCreate" to idAccountCreate
                    )
                )

            val postRequest = PostRequest(api, body)
            val response = postRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    Extensions.toastCall(mainActivity, "Create door success")
                    doorFragment.loadDoorToAdapter()
                    clearDoorForm()
                    dismiss()
                } else {
                    Extensions.toastCall(mainActivity, "Failed to create door")
                }
            }
        }
    }

    private fun clearDoorForm() {
        binding.doorIdInput.text?.clear()
        binding.doorPositionInput.text?.clear()
    }

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Extensions.toastCall(mainActivity, "Cancelled")
        } else {
            binding.doorIdInput.setText(result.contents)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            run {
                if (isGranted) {
                    openQrScannerAddDoor()
                } else {
                    Extensions.toastCall(mainActivity, "Camera permission is required")
                }
            }
        }

    private fun checkPermissionsCamera(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openQrScannerAddDoor()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Extensions.toastCall(context, "Camera permission is required")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openQrScannerAddDoor() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan QR to add new door")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }
}