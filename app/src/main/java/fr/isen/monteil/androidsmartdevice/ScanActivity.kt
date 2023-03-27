package fr.isen.monteil.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import fr.isen.monteil.androidsmartdevice.databinding.ActivityScanBinding

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.requestPermissions


class ScanActivity : AppCompatActivity() {

    lateinit var binding: ActivityScanBinding

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val REQUEST_ALL_PERMISSIONS = 1001


    private lateinit var scanAdapter: ScanAdapter

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                scanBLEDevice()
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = ScanAdapter(arrayListOf())

        scanAdapter = ScanAdapter(ArrayList<BluetoothDevice>())
        binding.list.adapter = scanAdapter





        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Snackbar.make(
                binding.root,
                "Le Bluetooth n'est pas pris en charge sur cet appareil",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            if (bluetoothAdapter?.isEnabled == true) {
                val device = mutableListOf<String>()
                scanDeviceWithPermissions()
                ScanPlayStop()
                //Toast.makeText(this, "Bluetooth activé", Toast.LENGTH_LONG).show()
                Snackbar.make(binding.root, "Bluetooth activé", Snackbar.LENGTH_LONG).show()

            } else {
                handleBLENotAvailable()
                //Toast.makeText(this, "Bluetooth doit être activé", Toast.LENGTH_LONG).show()
                Snackbar.make(binding.root, "Le Bluetooth doit être activé", Snackbar.LENGTH_LONG)
                    .show()
            }
        }


    }




    @RequiresApi(Build.VERSION_CODES.S)
    private fun scanDeviceWithPermissions(){
        if (allPermissionGranted()){
            scanBLEDevice()
        }else{
            requestPermissionLauncher.launch(getAllPermission())
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun allPermissionGranted(): Boolean{
        val allPermissions = getAllPermission()
        return allPermissions.all {
            it
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getAllPermission(): Array<String>{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN
        )
        }else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
         }
    }


    private fun handleBLENotAvailable() {
        binding.textPlay.text = getString(R.string.ble_scan_missing)
    }

    private fun scanBLEDevice() {
        val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter()?.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let {
                    scanAdapter.addDevice(it)
                    updateDeviceList()
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Snackbar.make(binding.root, "Le scan a échoué : $errorCode", Snackbar.LENGTH_LONG).show()
            }
        }

        fun startScan() {
            //bluetoothLeScanner?.startScan(scanCallback)
        }

        fun stopScan() {
            //bluetoothLeScanner?.stopScan(scanCallback)
        }

        ScanPlayStop()
    }



    private fun initDeviceList() {
        scanAdapter = ScanAdapter(ArrayList())
        binding.list.apply {
            layoutManager = LinearLayoutManager(this@ScanActivity)
            adapter = scanAdapter
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateDeviceList() {
        scanAdapter.notifyDataSetChanged()
    }


    private fun ScanPlayStop() {
        binding.imagePlayButton.setOnClickListener {
            binding.textPlay.text = getString(R.string.textPause)
            binding.textPause.text = getString(R.string.textPause)
            binding.imagePlayButton.setVisibility(View.GONE)
            binding.imagePauseButton.setVisibility(View.VISIBLE)
            binding.progressBar.setVisibility(View.VISIBLE)
            //startScan()
        }
        binding.imagePauseButton.setOnClickListener {
            binding.textPlay.text = getString(R.string.textPlay)
            binding.textPause.text = getString(R.string.textPlay)
            binding.imagePlayButton.setVisibility(View.VISIBLE)
            binding.imagePauseButton.setVisibility(View.GONE)
            binding.progressBar.setVisibility(View.GONE)
            //stopScan()
        }
    }
}