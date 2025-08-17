package com.shensi.controlboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.github.rosemoe.sora.editor.CodeEditor
import io.github.rosemoe.sora.langs.python.PythonLanguage
import com.shensi.controlboard.data.Project
import com.shensi.controlboard.manager.ProjectManager
import com.shensi.controlboard.manager.USBManager
import android.hardware.usb.UsbDevice

class CodeEditorActivity : AppCompatActivity(), USBManager.ConnectionListener {
    
    private lateinit var codeEditor: CodeEditor
    private lateinit var projectManager: ProjectManager
    private lateinit var usbManager: USBManager
    private var currentProject: Project? = null
    private var isModified = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_editor)
        
        setupToolbar()
        initializeManagers()
        setupCodeEditor()
        handleIntent()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun initializeManagers() {
        projectManager = ProjectManager(this)
        usbManager = USBManager(this)
        usbManager.setConnectionListener(this)
    }
    
    private fun setupCodeEditor() {
        codeEditor = findViewById(R.id.codeEditor)
        
        // Configure the editor
        codeEditor.apply {
            setEditorLanguage(PythonLanguage())
            isWordwrap = true
            setTextSize(14f)
            
            // Set up text change listener
            subscribeEvent(io.github.rosemoe.sora.event.ContentChangeEvent::class.java) { _, _ ->
                isModified = true
                updateTitle()
            }
        }
    }
    
    private fun handleIntent() {
        val action = intent.getStringExtra("action")
        when (action) {
            "new_project" -> createNewProject()
            "open_project" -> {
                val projectId = intent.getLongExtra("project_id", -1)
                if (projectId != -1L) {
                    openProject(projectId)
                } else {
                    val projectName = intent.getStringExtra("project_name")
                    if (projectName != null) {
                        openProjectByName(projectName)
                    }
                }
            }
        }
    }
    
    private fun createNewProject() {
        val builder = AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.hint = "项目名称"
        
        builder.setTitle("新建项目")
            .setView(input)
            .setPositiveButton("创建") { _, _ ->
                val projectName = input.text.toString().trim()
                if (projectName.isNotEmpty()) {
                    currentProject = projectManager.createProject(projectName)
                    loadProjectContent()
                    updateTitle()
                } else {
                    Toast.makeText(this, "项目名称不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消") { _, _ ->
                finish()
            }
            .show()
    }
    
    private fun openProject(projectId: Long) {
        currentProject = projectManager.getProject(projectId)
        if (currentProject != null) {
            loadProjectContent()
            updateTitle()
        } else {
            Toast.makeText(this, "无法打开项目", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun openProjectByName(projectName: String) {
        currentProject = projectManager.getProjectByName(projectName)
        if (currentProject != null) {
            loadProjectContent()
            updateTitle()
        } else {
            Toast.makeText(this, "无法打开项目: $projectName", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun loadProjectContent() {
        currentProject?.let { project ->
            val content = projectManager.getMainFileContent(project)
            codeEditor.setText(content)
            isModified = false
        }
    }
    
    private fun saveProject(): Boolean {
        return currentProject?.let { project ->
            val content = codeEditor.text.toString()
            val success = projectManager.saveProjectFile(project, "main.py", content)
            if (success) {
                isModified = false
                updateTitle()
                Toast.makeText(this, "项目已保存", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            }
            success
        } ?: false
    }
    
    private fun updateTitle() {
        val title = currentProject?.name ?: "新项目"
        val modifiedIndicator = if (isModified) " *" else ""
        supportActionBar?.title = "$title$modifiedIndicator"
    }
    
    private fun uploadToDevice() {
        if (!usbManager.isConnected()) {
            showDeviceSelectionDialog()
            return
        }
        
        val code = codeEditor.text.toString()
        if (code.isBlank()) {
            Toast.makeText(this, "代码不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        
        val success = usbManager.uploadPythonCode(code)
        if (success) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "代码上传成功",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showDeviceSelectionDialog() {
        val devices = usbManager.getAvailableDevices()
        if (devices.isEmpty()) {
            Toast.makeText(this, "未找到可用设备", Toast.LENGTH_SHORT).show()
            return
        }
        
        val deviceNames = devices.map { "${it.manufacturerName} ${it.productName}" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("选择设备")
            .setItems(deviceNames) { _, which ->
                usbManager.requestPermissionAndConnect(devices[which])
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_code_editor, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save -> {
                saveProject()
                true
            }
            R.id.action_upload -> {
                uploadToDevice()
                true
            }
            R.id.action_run -> {
                if (saveProject()) {
                    uploadToDevice()
                }
                true
            }
            R.id.action_format -> {
                formatCode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun formatCode() {
        // Basic Python code formatting
        val code = codeEditor.text.toString()
        val formattedCode = formatPythonCode(code)
        codeEditor.setText(formattedCode)
        Toast.makeText(this, "代码已格式化", Toast.LENGTH_SHORT).show()
    }
    
    private fun formatPythonCode(code: String): String {
        // Simple formatting - remove extra blank lines and fix indentation
        return code.lines()
            .map { it.trimEnd() }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
    }
    
    override fun onBackPressed() {
        if (isModified) {
            AlertDialog.Builder(this)
                .setTitle("未保存的更改")
                .setMessage("是否保存当前更改？")
                .setPositiveButton("保存") { _, _ ->
                    if (saveProject()) {
                        super.onBackPressed()
                    }
                }
                .setNegativeButton("不保存") { _, _ ->
                    super.onBackPressed()
                }
                .setNeutralButton("取消", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
    
    // USBManager.ConnectionListener implementation
    override fun onConnected(device: UsbDevice) {
        runOnUiThread {
            Toast.makeText(this, "设备已连接: ${device.productName}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDisconnected() {
        runOnUiThread {
            Toast.makeText(this, "设备已断开连接", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onConnectionError(error: String) {
        runOnUiThread {
            Toast.makeText(this, "连接错误: $error", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDataReceived(data: ByteArray) {
        // Handle received data if needed
        val receivedText = String(data)
        runOnUiThread {
            // Could show in a console view or log
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        usbManager.cleanup()
    }
}

