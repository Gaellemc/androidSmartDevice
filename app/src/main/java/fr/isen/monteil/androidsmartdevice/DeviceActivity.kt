package fr.isen.monteil.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.LightingColorFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import fr.isen.monteil.androidsmartdevice.databinding.ActivityDeviceBinding
import java.util.*


@SuppressLint("MissingPermission")
class DeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceBinding
    private lateinit var device: BluetoothDevice
    private lateinit var gatt: BluetoothGatt

    private var progressStatus = 0
    private lateinit var mHandler: Handler
    private var mScrollSpeed = 3

    private var currentSelectedAmpoule: ImageView? = null


    private val service = UUID.fromString("0000feed-cc7a-482a-984a-7f2ed5b3e58f")
    private val characteristicLed = UUID.fromString("0000abcd-8e22-4541-9d4c-21edae82ed19")
    private val characteristicButton = UUID.fromString("00001234-8e22-4541-9d4c-21edae82ed19")


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
                runOnUiThread {
                    displayTP()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread {
                    hideTP()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val characteristicButton3 = gatt?.getService(service)?.getCharacteristic(characteristicButton)
                gatt?.setCharacteristicNotification(characteristicButton3, true)
                characteristicButton3?.descriptors?.forEach { descriptor ->
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic.uuid == characteristicButton) {
                val value = characteristic.value
                val clicks = value[0].toInt()
                runOnUiThread {
                    binding.num.text = clicks.toString()
                }
            }
        }


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        device = intent.getParcelableExtra<BluetoothDevice>("device")!!
        gatt = device.connectGatt(this, false, gattCallback)

        mHandler = Handler()
        mHandler = Handler(Looper.getMainLooper())
        mHandler.postDelayed(ProgressRunnable, 100)

        changePageClick()   //pour tester sans la carte STM32


        binding.ampoule1.setOnClickListener {
            if (currentSelectedAmpoule == binding.ampoule1) {
                binding.ampoule1.clearColorFilter()
                currentSelectedAmpoule = null

                val service = gatt.getService(service)
                val characteristicLed = service.getCharacteristic(characteristicLed)
                characteristicLed.setValue(byteArrayOf(0x00))
                gatt.writeCharacteristic(characteristicLed)

            } else {

                resetAllAmpoules()
                val colorFilter = LightingColorFilter(Color.YELLOW, 1)
                binding.ampoule1.colorFilter = colorFilter
                currentSelectedAmpoule = binding.ampoule1

                val service = gatt.getService(service)
                val characteristicLed = service.getCharacteristic(characteristicLed)
                characteristicLed.setValue(byteArrayOf(0x01))
                gatt.writeCharacteristic(characteristicLed)

            }

        }

        binding.ampoule2.setOnClickListener {
            if (currentSelectedAmpoule == binding.ampoule2) {
                binding.ampoule2.clearColorFilter()
                currentSelectedAmpoule = null

                val service = gatt.getService(service)
                val characteristicLed = service.getCharacteristic(characteristicLed)
                characteristicLed.setValue(byteArrayOf(0x00))
                gatt.writeCharacteristic(characteristicLed)
            } else {
                resetAllAmpoules()
                val colorFilter = LightingColorFilter(Color.YELLOW, 1)
                binding.ampoule2.colorFilter = colorFilter
                currentSelectedAmpoule = binding.ampoule2

                val service = gatt.getService(service)
                val characteristicLed = service.getCharacteristic(characteristicLed)
                characteristicLed.setValue(byteArrayOf(0x02))
                gatt.writeCharacteristic(characteristicLed)
            }

        }

        binding.ampoule3.setOnClickListener {
            if (currentSelectedAmpoule == binding.ampoule3) {
                binding.ampoule3.clearColorFilter()
                currentSelectedAmpoule = null

                val service = gatt.getService(service)
                val characteristicLed = service.getCharacteristic(characteristicLed)
                characteristicLed.setValue(byteArrayOf(0x00))
                gatt.writeCharacteristic(characteristicLed)
            } else {
                resetAllAmpoules()
                val colorFilter = LightingColorFilter(Color.YELLOW, 1)
                binding.ampoule3.colorFilter = colorFilter
                currentSelectedAmpoule = binding.ampoule3

                val service = gatt.getService(service)
                val characteristicLed = service.getCharacteristic(characteristicLed)
                characteristicLed.setValue(byteArrayOf(0x03))
                gatt.writeCharacteristic(characteristicLed)
            }

        }
    }



    private fun displayTP() {
        binding.ConnectionMessage.visibility = View.GONE
        binding.progressBarConnexion.visibility = View.GONE
        binding.Titre.visibility = View.VISIBLE
        binding.text1.visibility = View.VISIBLE
        binding.Description2.visibility = View.VISIBLE
        binding.checkBox.visibility = View.VISIBLE
        binding.Nombre.visibility = View.VISIBLE
        binding.num.visibility = View.VISIBLE
        binding.ampoule1.visibility = View.VISIBLE
        binding.ampoule2.visibility = View.VISIBLE
        binding.ampoule3.visibility = View.VISIBLE
    }

    private fun hideTP() {
        binding.ConnectionMessage.visibility = View.VISIBLE
        binding.progressBarConnexion.visibility = View.VISIBLE
        binding.Titre.visibility = View.GONE
        binding.text1.visibility = View.GONE
        binding.Description2.visibility = View.GONE
        binding.checkBox.visibility = View.GONE
        binding.Nombre.visibility = View.GONE
        binding.num.visibility = View.GONE
        binding.ampoule1.visibility = View.GONE
        binding.ampoule2.visibility = View.GONE
        binding.ampoule3.visibility = View.GONE
    }

    private fun resetAllAmpoules() {
        binding.ampoule1.clearColorFilter()
        binding.ampoule2.clearColorFilter()
        binding.ampoule3.clearColorFilter()
        currentSelectedAmpoule = null
    }

    private val ProgressRunnable = object : Runnable {
        override fun run() {
            progressStatus += mScrollSpeed
            binding.progressBarConnexion.progress = progressStatus
            if (progressStatus >= 100) {
                progressStatus = 0

            }
            mHandler.postDelayed(this, 50)
        }
    }


    fun changePageClick() {
        binding.changePage.setOnClickListener{
            binding.ConnectionMessage.visibility = View.GONE
            binding.progressBarConnexion.visibility = View.GONE
            binding.Titre.visibility = View.VISIBLE
            binding.text1.visibility = View.VISIBLE
            binding.Description2.visibility = View.VISIBLE
            binding.checkBox.visibility = View.VISIBLE
            binding.Nombre.visibility = View.VISIBLE
            binding.num.visibility = View.VISIBLE
            binding.ampoule1.visibility = View.VISIBLE
            binding.ampoule2.visibility = View.VISIBLE
            binding.ampoule3.visibility = View.VISIBLE
            binding.changePage.visibility = View.GONE
        }
    }
}


