package com.example.todolist
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.database.entities.Task
class TaskAdapter : ListAdapter<Task, TaskAdapter.TaskHolder>(DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val currentTask = getItem(position)
        holder.textViewTitle.text = currentTask.title
        holder.textViewDescription.text = currentTask.description
    }

    class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.text_view_title)
        val textViewDescription: TextView = itemView.findViewById(R.id.text_view_description)
    }
}