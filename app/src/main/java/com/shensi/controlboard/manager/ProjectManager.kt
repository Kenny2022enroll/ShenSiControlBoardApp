package com.shensi.controlboard.manager

import android.content.Context
import com.shensi.controlboard.data.Project
import com.shensi.controlboard.data.ProjectFile
import java.io.File
import java.util.Date

class ProjectManager(private val context: Context) {
    
    companion object {
        private const val PROJECTS_DIR = "ShenSiProjects"
        private const val MAIN_FILE_NAME = "main.py"
    }
    
    private val projectsDir: File by lazy {
        File(context.getExternalFilesDir(null), PROJECTS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    fun createProject(name: String, description: String = ""): Project {
        val projectDir = File(projectsDir, name)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }
        
        val mainFile = File(projectDir, MAIN_FILE_NAME)
        if (!mainFile.exists()) {
            mainFile.writeText(getDefaultPythonCode())
        }
        
        return Project(
            name = name,
            description = description,
            filePath = projectDir.absolutePath,
            createdAt = Date(),
            modifiedAt = Date()
        )
    }
    
    fun getAllProjects(): List<Project> {
        val projects = mutableListOf<Project>()
        
        projectsDir.listFiles()?.forEach { dir ->
            if (dir.isDirectory) {
                val mainFile = File(dir, MAIN_FILE_NAME)
                if (mainFile.exists()) {
                    projects.add(
                        Project(
                            name = dir.name,
                            filePath = dir.absolutePath,
                            createdAt = Date(dir.lastModified()),
                            modifiedAt = Date(mainFile.lastModified())
                        )
                    )
                }
            }
        }
        
        return projects.sortedByDescending { it.modifiedAt }
    }
    
    fun getProject(projectId: Long): Project? {
        // For simplicity, using project name as ID
        return getAllProjects().find { it.id == projectId }
    }
    
    fun getProjectByName(name: String): Project? {
        val projectDir = File(projectsDir, name)
        if (!projectDir.exists()) return null
        
        val mainFile = File(projectDir, MAIN_FILE_NAME)
        if (!mainFile.exists()) return null
        
        return Project(
            name = name,
            filePath = projectDir.absolutePath,
            createdAt = Date(projectDir.lastModified()),
            modifiedAt = Date(mainFile.lastModified())
        )
    }
    
    fun saveProjectFile(project: Project, fileName: String, content: String): Boolean {
        return try {
            val projectDir = File(project.filePath)
            val file = File(projectDir, fileName)
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getProjectFile(project: Project, fileName: String): ProjectFile? {
        return try {
            val file = File(project.filePath, fileName)
            if (file.exists()) {
                ProjectFile(
                    name = fileName,
                    content = file.readText(),
                    isMain = fileName == MAIN_FILE_NAME
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getProjectFiles(project: Project): List<ProjectFile> {
        val files = mutableListOf<ProjectFile>()
        val projectDir = File(project.filePath)
        
        projectDir.listFiles { file -> file.extension == "py" }?.forEach { file ->
            files.add(
                ProjectFile(
                    name = file.name,
                    content = file.readText(),
                    isMain = file.name == MAIN_FILE_NAME
                )
            )
        }
        
        return files.sortedWith(compareBy({ !it.isMain }, { it.name }))
    }
    
    fun deleteProject(project: Project): Boolean {
        return try {
            val projectDir = File(project.filePath)
            projectDir.deleteRecursively()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getMainFileContent(project: Project): String {
        return getProjectFile(project, MAIN_FILE_NAME)?.content ?: getDefaultPythonCode()
    }
    
    private fun getDefaultPythonCode(): String {
        return """# 盛思掌控板 Python 程序
# ShenSi Control Board Python Program

from mpython import *
import time

# 显示欢迎信息
oled.fill(0)
oled.DispChar('欢迎使用', 30, 20)
oled.DispChar('掌控板!', 35, 35)
oled.show()

# 主循环
while True:
    # 检测按键A
    if button_a.value() == 0:
        rgb[0] = (255, 0, 0)  # 红色
        rgb.write()
        print("按键A被按下")
        time.sleep(0.2)
    
    # 检测按键B  
    if button_b.value() == 0:
        rgb[0] = (0, 255, 0)  # 绿色
        rgb.write()
        print("按键B被按下")
        time.sleep(0.2)
    
    # 关闭LED
    rgb[0] = (0, 0, 0)
    rgb.write()
    
    time.sleep(0.1)
"""
    }
    
    fun getTemplateProjects(): List<Project> {
        return listOf(
            Project(
                name = "LED闪烁",
                description = "基础LED控制示例",
                filePath = "",
                isTemplate = true
            ),
            Project(
                name = "按键检测",
                description = "按键输入检测示例",
                filePath = "",
                isTemplate = true
            ),
            Project(
                name = "传感器读取",
                description = "传感器数据读取示例",
                filePath = "",
                isTemplate = true
            ),
            Project(
                name = "OLED显示",
                description = "OLED屏幕显示示例",
                filePath = "",
                isTemplate = true
            )
        )
    }
}

