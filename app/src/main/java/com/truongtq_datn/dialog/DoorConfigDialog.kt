package com.truongtq_datn.dialog

import android.Manifest
import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.truongtq_datn.activity.MainActivity
import com.truongtq_datn.databinding.DialogDoorConfigBinding
import java.io.OutputStream
import java.util.UUID

class DoorConfigDialog(
    private val mainActivity: MainActivity
) : DialogFragment() {

    private val REQUEST_CODE_BLUETOOTH_CONNECT = 1
    private val REQUEST_CODE_BLUETOOTH_SCAN = 2
    private val REQUEST_CODE_LOCATION = 3

    private val handler = Handler(Looper.getMainLooper())

    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    private var bluetoothList: List<String> = emptyList()

    private val UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiList: List<String>

    private var isReceiverRegistered = false

    private var _binding: DialogDoorConfigBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDoorConfigBinding.inflate(inflater, container, false)

        bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager

        binding.doorConfigBtnScanBluetooth.setOnClickListener { scanBluetooth() }
        binding.doorConfigBtnConnectBluetooth.setOnClickListener { connectToDevice() }
        binding.doorConfigBtnScanWifi.setOnClickListener { checkPermissionLocation(mainActivity) }
        binding.doorConfigBtnSendWifiCre.setOnClickListener { sendWiFiCredentials() }
        binding.doorConfigBtnSendMqttCre.setOnClickListener { sendMqttCredentials() }
        binding.doorConfigBtnRegisterCard.setOnClickListener { registerRfidCard() }
        binding.doorConfigBtnRemoveCard.setOnClickListener { removeRfidCard() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.doorConfigBtnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(bluetoothReceiver)
            isReceiverRegistered = false
        }
    }

    fun setStatusText(txt: String) {
        binding.doorConfigTxtStatus.text = txt
    }

    private fun scanBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED

                || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), REQUEST_CODE_BLUETOOTH_SCAN
                )
                return
            }
        } else {
            return
        }
        setStatusText("Scanning for devices...")

        if (bluetoothAdapter.isEnabled) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            val startDiscoveryFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            val finishDiscoveryFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            requireContext().registerReceiver(bluetoothReceiver, filter)
            requireContext().registerReceiver(bluetoothReceiver, startDiscoveryFilter)
            requireContext().registerReceiver(bluetoothReceiver, finishDiscoveryFilter)

            isReceiverRegistered = true

            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }

            bluetoothAdapter.startDiscovery()

            handler.postDelayed({
                bluetoothAdapter.cancelDiscovery()
                setStatusText("Discovery finished...")
            }, 10000)

            setStatusText("Start discovery...")
        } else {
            setStatusText("Bluetooth is not enabled.")
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action.toString()
            Log.d("BluetoothDevice", action)
            when (action) {

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    setStatusText("Discovery started...")
                }

                BluetoothDevice.ACTION_FOUND -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(
                                mainActivity,
                                Manifest.permission.BLUETOOTH_SCAN
                            ) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(
                                mainActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ), REQUEST_CODE_BLUETOOTH_SCAN
                            )
                            return
                        }
                    }

                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        if (!device.name.isNullOrEmpty()) {
                            bluetoothDevices.add(device)
                        }
                        val deviceName = device.name ?: "Unknown device"
                        val deviceHardwareAddress = device.address
                        Log.d(
                            "BluetoothDevice",
                            "Device found: $deviceName [$deviceHardwareAddress]"
                        )
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (bluetoothDevices.isEmpty()) {
                        setStatusText("No device found!")
                    } else {
                        bluetoothList = bluetoothDevices.map {
                            it.name ?: "Unknown device"
                        }

                        val adapter =
                            ArrayAdapter(
                                mainActivity,
                                R.layout.simple_spinner_item,
                                bluetoothList
                            )
                        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                        binding.doorConfigSpinnerBluetoothList.adapter = adapter
                    }
                }
            }
        }
    }

    private fun connectToDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    mainActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    mainActivity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_CODE_BLUETOOTH_CONNECT
                )
                return
            }
        }

        try {
            val nameDevice = binding.doorConfigSpinnerBluetoothList.selectedItem.toString()

            val device = bluetoothDevices.find { it.name == nameDevice }
            if (device == null) {
                setStatusText("Device not found.")
                return
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            bluetoothSocket?.let { socket ->
                bluetoothAdapter.cancelDiscovery()
                socket.connect()
                setStatusText("Connected to ${device.name}")
            } ?: run {
                setStatusText("Bluetooth socket is null.")
            }
        } catch (e: SecurityException) {
            setStatusText("Permission denied: ${e.message}")
        } catch (e: Exception) {
            setStatusText("Connection failed: ${e.message}")
        }
    }

    private fun scanWiFi() {
        if (ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
            return
        }
        wifiManager.startScan()
        val scanResults = wifiManager.scanResults
        updateWifiList(scanResults)
    }

    private fun updateWifiList(scanResults: List<ScanResult>) {

        for (scanResult in scanResults) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(
                    "ScanResult",
                    "SSID: ${
                        scanResult.wifiSsid.toString()
                    }, BSSID: ${scanResult.BSSID}, RSSI: ${scanResult.level}"
                )
            } else {
                Log.d(
                    "ScanResult",
                    "SSID: ${
                        scanResult.SSID
                    }, BSSID: ${scanResult.BSSID}, RSSI: ${scanResult.level}"
                )
            }
        }

        wifiList = scanResults.map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.wifiSsid.toString().replace("\"", "")
            } else {
                it.SSID
            }
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, wifiList)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.doorConfigSpinnerWifiList.adapter = adapter
        setStatusText("Wi-Fi scan completed")
    }

    private fun sendWiFiCredentials() {
        val selectedSSID = binding.doorConfigSpinnerWifiList.selectedItem.toString()
        val password = binding.doorConfigEdtWifiPassword.text.toString()

        if (selectedSSID.isEmpty()) {
            setStatusText("SSID cannot be empty")
            return
        }

        val message = "WIFI|$selectedSSID|$password\n"
        try {
            val outputStream: OutputStream? = bluetoothSocket?.outputStream
            outputStream?.write(message.toByteArray())
            setStatusText("Wifi credentials sent!")
        } catch (e: Exception) {
            setStatusText("Failed to send: ${e.message}")
        }
    }

    private fun sendMqttCredentials() {
        val serverIP = binding.doorConfigEdtMqttIp.text.toString()
        if (serverIP.isEmpty()) {
            setStatusText("IP MQTT cannot be empty")
        }
        val message = "MQTT|$serverIP\n"
        try {
            val outputStream: OutputStream? = bluetoothSocket?.outputStream
            outputStream?.write(message.toByteArray())
            setStatusText("MQTT credentials sent!")
        } catch (e: Exception) {
            setStatusText("Failed to send: ${e.message}")
        }
    }

    private fun registerRfidCard() {
        val message = "RFID|REGISTER|\n"
        try {
            val outputStream: OutputStream? = bluetoothSocket?.outputStream
            outputStream?.write(message.toByteArray())
            setStatusText("RFID REGISTER request sent!")
        } catch (e: Exception) {
            setStatusText("Failed to send: ${e.message}")
        }
    }

    private fun removeRfidCard() {
        val message = "RFID|REMOVE|\n"
        try {
            val outputStream: OutputStream? = bluetoothSocket?.outputStream
            outputStream?.write(message.toByteArray())
            setStatusText("RFID REGISTER request sent!")
        } catch (e: Exception) {
            setStatusText("Failed to send: ${e.message}")
        }
    }

    private fun checkPermissionLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            scanWiFi()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(
                context,
                "Location permission is required to scan WiFi",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToDevice()
            } else {
                setStatusText("Permission denied for Bluetooth connect.")
            }
        }

        if (requestCode == REQUEST_CODE_BLUETOOTH_SCAN) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanBluetooth()
            } else {
                setStatusText("Permission denied for Bluetooth scan.")
            }
        }

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanWiFi()
            } else {
                setStatusText("Permission denied for wifi connection.")
            }
        }
    }

}