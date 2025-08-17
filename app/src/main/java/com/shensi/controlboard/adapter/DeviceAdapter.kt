package com.shensi.controlboard.adapter

import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shensi.controlboard.R

class DeviceAdapter(
    private val onDeviceClick: (UsbDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    
    private var devices = listOf<UsbDevice>()
    private var connectedDevice: UsbDevice? = null
    
    fun updateDevices(newDevices: List<UsbDevice>) {
        devices = newDevices
        notifyDataSetChanged()
    }
    
    fun setConnectedDevice(device: UsbDevice?) {
        connectedDevice = device
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textDeviceName)
        private val infoTextView: TextView = itemView.findViewById(R.id.textDeviceInfo)
        private val statusImageView: ImageView = itemView.findViewById(R.id.imageDeviceStatus)
        
        fun bind(device: UsbDevice) {
            val deviceName = "${device.manufacturerName ?: "Unknown"} ${device.productName ?: "Device"}"
            nameTextView.text = deviceName
            
            val deviceInfo = "VID: ${String.format("%04X", device.vendorId)} " +
                    "PID: ${String.format("%04X", device.productId)}"
            infoTextView.text = deviceInfo
            
            val isConnected = device == connectedDevice
            statusImageView.setImageResource(
                if (isConnected) R.drawable.ic_usb_connected 
                else R.drawable.ic_usb_disconnected
            )
            
            itemView.setOnClickListener {
                if (!isConnected) {
                    onDeviceClick(device)
                }
            }
            
            itemView.alpha = if (isConnected) 1.0f else 0.7f
        }
    }
}

