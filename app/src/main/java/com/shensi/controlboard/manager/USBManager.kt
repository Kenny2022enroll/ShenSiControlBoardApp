package com.shensi.controlboard.manager

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

class USBManager(private val context: Context) {
    
    companion object {
        private const val TAG = "USBManager"
        private const val ACTION_USB_PERMISSION = "com.shensi.controlboard.USB_PERMISSION"
        private const val BAUD_RATE = 115200
        private const val DATA_BITS = 8
        private const val STOP_BITS = UsbSerialPort.STOPBITS_1
        private const val PARITY = UsbSerialPort.PARITY_NONE
    }
    
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var currentPort: UsbSerialPort? = null
    private var connectionListener: ConnectionListener? = null
    
    interface ConnectionListener {
        fun onConnected(device: UsbDevice)
        fun onDisconnected()
        fun onConnectionError(error: String)
        fun onDataReceived(data: ByteArray)
    }
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let { connectToDevice(it) }
                        } else {
                            Log.d(TAG, "Permission denied for device $device")
                            connectionListener?.onConnectionError("USB权限被拒绝")
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (device != null && currentPort != null) {
                        disconnect()
                    }
                }
            }
        }
    }
    
    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(usbReceiver, filter)
    }
    
    fun setConnectionListener(listener: ConnectionListener) {
        this.connectionListener = listener
    }
    
    fun getAvailableDevices(): List<UsbDevice> {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        return availableDrivers.map { it.device }
    }
    
    fun requestPermissionAndConnect(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }
    
    private fun connectToDevice(device: UsbDevice) {
        try {
            val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
            if (driver == null) {
                connectionListener?.onConnectionError("未找到合适的驱动程序")
                return
            }
            
            val connection = usbManager.openDevice(driver.device)
            if (connection == null) {
                connectionListener?.onConnectionError("无法打开设备连接")
                return
            }
            
            val port = driver.ports[0]
            port.open(connection)
            port.setParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY)
            
            currentPort = port
            connectionListener?.onConnected(device)
            
            Log.d(TAG, "Connected to device: ${device.deviceName}")
            
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to device", e)
            connectionListener?.onConnectionError("连接设备时出错: ${e.message}")
        }
    }
    
    fun disconnect() {
        currentPort?.let { port ->
            try {
                port.close()
                currentPort = null
                connectionListener?.onDisconnected()
                Log.d(TAG, "Disconnected from device")
            } catch (e: IOException) {
                Log.e(TAG, "Error disconnecting from device", e)
            }
        }
    }
    
    fun isConnected(): Boolean {
        return currentPort != null
    }
    
    fun sendData(data: ByteArray): Boolean {
        return try {
            currentPort?.write(data, 1000)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error sending data", e)
            connectionListener?.onConnectionError("发送数据时出错: ${e.message}")
            false
        }
    }
    
    fun sendCommand(command: String): Boolean {
        return sendData((command + "\r\n").toByteArray())
    }
    
    fun readData(): ByteArray? {
        return try {
            val buffer = ByteArray(1024)
            val bytesRead = currentPort?.read(buffer, 1000) ?: 0
            if (bytesRead > 0) {
                buffer.copyOf(bytesRead)
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading data", e)
            null
        }
    }
    
    fun uploadPythonCode(code: String): Boolean {
        if (!isConnected()) {
            connectionListener?.onConnectionError("设备未连接")
            return false
        }
        
        try {
            // Enter raw REPL mode
            sendCommand("\x03\x03")  // Ctrl+C twice to interrupt
            Thread.sleep(100)
            sendCommand("\x01")      // Ctrl+A to enter raw REPL
            Thread.sleep(100)
            
            // Send the Python code
            sendData(code.toByteArray())
            sendCommand("\x04")      // Ctrl+D to execute
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading Python code", e)
            connectionListener?.onConnectionError("上传代码时出错: ${e.message}")
            return false
        }
    }
    
    fun cleanup() {
        disconnect()
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
    }
}

