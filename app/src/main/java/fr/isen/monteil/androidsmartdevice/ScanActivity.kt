package fr.isen.monteil.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import fr.isen.monteil.androidsmartdevice.databinding.ActivityScanBinding

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi


class ScanActivity : AppCompatActivity() {

    lateinit var binding: ActivityScanBinding


    private lateinit var scanAdapter: ScanAdapter

    private val bluetoothAdapter: BluetoothAdapter? by
    lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                scanBLEDevice()
            }
        }

  /*  //Scan Bluetooth fonction corrigée
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())*/

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = ScanAdapter(ArrayList(), { device ->
        }, HashMap<String, Int>())

        scanAdapter = ScanAdapter(ArrayList(), { device ->
        }, HashMap<String, Int>())

        binding.list.adapter = scanAdapter

        //bluetoothAdapter?.bluetoothLeScanner

        if (bluetoothAdapter == null) {
            Snackbar.make(
                binding.root,
                "Le Bluetooth n'est pas pris en charge sur cet appareil",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            if (bluetoothAdapter?.isEnabled == true) {
                //val device = mutableListOf<String>()
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

    lateinit var leScanCallback : ScanCallback

    private fun scanBLEDevice() {
        leScanCallback = object : ScanCallback() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let {
                    scanAdapter.addDevice(it)
                    scanAdapter.notifyDataSetChanged()
                    updateDeviceList()
                    val rssi = result.rssi
                    scanAdapter.rssiValues[it.address] = rssi
                }
            }
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Snackbar.make(binding.root, "Le scan a échoué : $errorCode", Snackbar.LENGTH_LONG).show()
            }
        }
        ScanPlayStop()
    }

    fun startScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
        }
    }

    fun stopScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStop(){
        super.onStop()
        if(bluetoothAdapter?.isEnabled == true && allPermissionGranted()){
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }


    private fun initDeviceList() {
        val rssiValues = HashMap<String, Int>()
        scanAdapter = ScanAdapter(ArrayList(), { device ->
            val intent = Intent(this, DeviceActivity::class.java)
            intent.putExtra("device", device)
            intent.putExtra("rssi", rssiValues[device.address])
            startActivity(intent)
        }, rssiValues)
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
        initDeviceList()
        binding.imagePlayButton.setOnClickListener {
            binding.textPlay.text = getString(R.string.textPause)
            binding.textPause.text = getString(R.string.textPause)
            binding.imagePlayButton.setVisibility(View.GONE)
            binding.imagePauseButton.setVisibility(View.VISIBLE)
            binding.progressBar.setVisibility(View.VISIBLE)
            startScan()
        }
        binding.imagePauseButton.setOnClickListener {
            binding.textPlay.text = getString(R.string.textPlay)
            binding.textPause.text = getString(R.string.textPlay)
            binding.imagePlayButton.setVisibility(View.VISIBLE)
            binding.imagePauseButton.setVisibility(View.GONE)
            binding.progressBar.setVisibility(View.GONE)
            stopScan()
        }
    }





        /*   //Fonction scan Bluetooth corrigée
           @SuppressLint("MissingPermission")
           private fun scanBLeDevice() {
               if (!scanning) { // Stops scanning after a pre-defined scan period.
                   handler.postDelayed({
                       scanning = false
                       bluetoothLeScanner?.stopScan(leScanCallback)
                       ScanPlayStop()
                   }, SCAN_PERIOD)
                   scanning = true
                   bluetoothLeScanner?.startScan(leScanCallback)
               } else {
                   scanning = false
                   bluetoothLeScanner?.stopScan(leScanCallback)
               }
               ScanPlayStop()
           }

           @RequiresApi(Build.VERSION_CODES.S)
           override fun onStop(){
               super.onStop()
               if(bluetoothAdapter?.isEnabled == true && allPermissionGranted()){
                   scanning = false
                   bluetoothLeScanner?.stopScan(leScanCallback)
               }
           }

           // Device scan callback.
           private val leScanCallback: ScanCallback = object : ScanCallback() {
               @SuppressLint("NotifyDataSetChanged")
               override fun onScanResult(callbackType: Int, result: ScanResult) {
                   super.onScanResult(callbackType, result)
                   (binding.list.adapter as? ScanAdapter)?.addDevice(result.device)
                   binding.list.adapter?.notifyDataSetChanged()
               }
           }

           companion object{
               // Stops scanning after 10 seconds.
               private val SCAN_PERIOD: Long = 10000
           }*/

}




