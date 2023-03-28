package fr.isen.monteil.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.isen.monteil.androidsmartdevice.databinding.ScanCellBinding



class ScanAdapter(val devices: ArrayList<BluetoothDevice>, var onDeviceClickListener: (BluetoothDevice) -> Unit, val rssiValues: HashMap<String, Int>) : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val binding = ScanCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return devices.size
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.textPlay.text = devices[position].address
        holder.textName.text = devices[position].name + "\n"
        holder.textRssi.text = rssiValues[devices[position].address].toString()
        holder.itemView.setOnClickListener {
            onDeviceClickListener(devices[position])
        }
    }

    class ScanViewHolder(binding: ScanCellBinding) : RecyclerView.ViewHolder(binding.root) {
        val textPlay: TextView = binding.AddressDevice
        val textName: TextView = binding.NameDevice
        val textRssi: TextView = binding.RSSI
    }

    fun clearDevices() {
        devices.clear()
    }

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

}

/*    //Fonction corrigée
    fun addDevice(device: BluetoothDevice) {
        var shouldAddDevice = true
        devices.forEachIndexed { index, bluetoothDevice ->
            if(bluetoothDevice.address == device.address) {
                devices[index] = device
                shouldAddDevice = false
            }else{
                devices.add(device)
            }
        }
    }*/






