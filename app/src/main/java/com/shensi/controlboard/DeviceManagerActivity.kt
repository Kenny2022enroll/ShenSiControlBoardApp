package com.shensi.controlboard

import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.shensi.controlboard.adapter.DeviceAdapter
import com.shensi.controlboard.manager.USBManager

class DeviceManagerActivity : AppCompatActivity(), USBManager.ConnectionListener {
    
    private lateinit var deviceRecyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var usbManager: USBManager
    private lateinit var buttonScan: MaterialButton
    private lateinit var buttonTest: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_manager)
        
        setupToolbar()
        initializeViews()
        initializeUSBManager()
        scanForDevices()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "设备管理"
        }
    }
    
    private fun initializeViews() {
        deviceRecyclerView = findViewById(R.id.recyclerViewDevices)
        buttonScan = findViewById(R.id.buttonScanDevices)
        buttonTest = findViewById(R.id.buttonTestConnection)
        
        deviceAdapter = DeviceAdapter { device ->
            connectToDevice(device)
        }
        
        deviceRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DeviceManagerActivity)
            adapter = deviceAdapter
        }
        
        buttonScan.setOnClickListener {
            scanForDevices()
        }
        
        buttonTest.setOnClickListener {
            testConnection()
        }
    }
    
    private fun initializeUSBManager() {
        usbManager = USBManager(this)
        usbManager.setConnectionListener(this)
    }
    
    private fun scanForDevices() {
        val devices = usbManager.getAvailableDevices()
        deviceAdapter.updateDevices(devices)
        
        if (devices.isEmpty()) {
            Toast.makeText(this, "未找到可用设备", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "找到 ${devices.size} 个设备", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun connectToDevice(device: UsbDevice) {
        usbManager.requestPermissionAndConnect(device)
    }
    
    private fun testConnection() {
        if (!usbManager.isConnected()) {
            Toast.makeText(this, "请先连接设备", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Send a simple test command
        val testCode = """
print("Hello from ShenSi Control Board!")
import time
for i in range(3):
    print(f"Test {i+1}")
    time.sleep(0.5)
print("Test completed!")
        """.trimIndent()
        
        val success = usbManager.uploadPythonCode(testCode)
        if (success) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "测试代码已发送",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // USBManager.ConnectionListener implementation
    override fun onConnected(device: UsbDevice) {
        runOnUiThread {
            Toast.makeText(this, "已连接到: ${device.productName}", Toast.LENGTH_SHORT).show()
            buttonTest.isEnabled = true
            deviceAdapter.setConnectedDevice(device)
        }
    }
    
    override fun onDisconnected() {
        runOnUiThread {
            Toast.makeText(this, "设备已断开连接", Toast.LENGTH_SHORT).show()
            buttonTest.isEnabled = false
            deviceAdapter.setConnectedDevice(null)
        }
    }
    
    override fun onConnectionError(error: String) {
        runOnUiThread {
            Toast.makeText(this, "连接错误: $error", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDataReceived(data: ByteArray) {
        val receivedText = String(data)
        runOnUiThread {
            // Could display in a console view
            Snackbar.make(
                findViewById(android.R.id.content),
                "收到数据: $receivedText",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        usbManager.cleanup()
    }
}

