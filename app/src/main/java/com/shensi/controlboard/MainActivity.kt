package com.shensi.controlboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.shensi.controlboard.adapter.ProjectAdapter
import com.shensi.controlboard.data.Project
import com.shensi.controlboard.manager.ProjectManager
import com.shensi.controlboard.manager.USBManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var projectRecyclerView: RecyclerView
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var projectManager: ProjectManager
    private lateinit var usbManager: USBManager
    private lateinit var fabNewProject: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupToolbar()
        initializeManagers()
        setupViews()
        loadProjects()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.app_name)
    }
    
    private fun initializeManagers() {
        projectManager = ProjectManager(this)
        usbManager = USBManager(this)
    }
    
    private fun setupViews() {
        projectRecyclerView = findViewById(R.id.recyclerViewProjects)
        fabNewProject = findViewById(R.id.fabNewProject)
        
        projectAdapter = ProjectAdapter { project ->
            openProject(project)
        }
        
        projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = projectAdapter
        }
        
        fabNewProject.setOnClickListener {
            createNewProject()
        }
    }
    
    private fun loadProjects() {
        val projects = projectManager.getAllProjects()
        projectAdapter.updateProjects(projects)
    }
    
    private fun createNewProject() {
        val intent = Intent(this, CodeEditorActivity::class.java)
        intent.putExtra("action", "new_project")
        startActivity(intent)
    }
    
    private fun openProject(project: Project) {
        val intent = Intent(this, CodeEditorActivity::class.java)
        intent.putExtra("action", "open_project")
        intent.putExtra("project_id", project.id)
        startActivity(intent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_device_manager -> {
                startActivity(Intent(this, DeviceManagerActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showAboutDialog() {
        // TODO: Implement about dialog
        Snackbar.make(
            findViewById(android.R.id.content),
            "盛思掌控板编程助手 v1.0",
            Snackbar.LENGTH_LONG
        ).show()
    }
    
    override fun onResume() {
        super.onResume()
        loadProjects() // Refresh projects when returning to main activity
    }
}

