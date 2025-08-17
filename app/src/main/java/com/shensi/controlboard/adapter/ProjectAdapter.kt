package com.shensi.controlboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shensi.controlboard.R
import com.shensi.controlboard.data.Project
import java.text.SimpleDateFormat
import java.util.*

class ProjectAdapter(
    private val onProjectClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {
    
    private var projects = listOf<Project>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    fun updateProjects(newProjects: List<Project>) {
        projects = newProjects
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(projects[position])
    }
    
    override fun getItemCount(): Int = projects.size
    
    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textProjectName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textProjectDescription)
        private val dateTextView: TextView = itemView.findViewById(R.id.textProjectDate)
        
        fun bind(project: Project) {
            nameTextView.text = project.name
            descriptionTextView.text = project.description.ifEmpty { "无描述" }
            dateTextView.text = "修改时间: ${dateFormat.format(project.modifiedAt)}"
            
            itemView.setOnClickListener {
                onProjectClick(project)
            }
        }
    }
}

