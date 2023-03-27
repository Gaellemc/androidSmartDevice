package fr.isen.monteil.androidsmartdevice

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.isen.monteil.androidsmartdevice.databinding.ScanCellBinding

class ScanAdapter(val devices: ArrayList<BluetoothDevice>) : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val binding = ScanCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.textPlay.text = devices[position].toString()
    }

    class ScanViewHolder(binding: ScanCellBinding) : RecyclerView.ViewHolder(binding.root) {
        val textPlay: TextView = binding.textView
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



