package com.example.todolist.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.database.entities.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskAdapter(private val isDaily: Boolean = false, private val onTaskClick: (Int) -> Unit) : ListAdapter<Task, TaskAdapter.TaskHolder>(
    DIFF_CALLBACK
) {
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
        val layout = if (isDaily) R.layout.daily_task else R.layout.all_tasks
        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return TaskHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val currentTask = getItem(position)
        holder.bind(currentTask, onTaskClick)

    }

    class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textViewTitle: TextView = itemView.findViewById(R.id.task_title)
        val textViewDescription: TextView = itemView.findViewById(R.id.task_description)
        val taskTime: TextView = itemView.findViewById(R.id.task_time)
        val taskAttachments: ImageView = itemView.findViewById(R.id.task_attachment)
        val taskStatus: TextView = itemView.findViewById(R.id.task_status)
        fun bind(task: Task, onTaskClick: (Int) -> Unit) {
            textViewTitle.text = task.title
            textViewDescription.text = if (task.description.length >= 20) "${
                task.description.substring(
                    0,
                    20
                )
            }..." else task.description
            taskTime.text = formatDate(task.completionTime)
            taskStatus.text = getStatusText(task.isCompleted, task.completionTime)

            if (getStatusText(task.isCompleted, task.completionTime) == "Completed") {
                taskStatus.setTextColor(Color.GREEN)
            } else if (getStatusText(task.isCompleted, task.completionTime) == "Late") {
                taskStatus.setTextColor(Color.RED)
            } else {
                taskStatus.setTextColor(Color.BLACK)
            }

            taskTime.setTextColor(if (isDueSoon(task.completionTime)) Color.RED else Color.BLACK)
            taskAttachments.visibility = if (task.hasAttachments) View.VISIBLE else View.INVISIBLE

            itemView.setOnClickListener {
                onTaskClick(task.id)
            }

        }


        private fun formatDate(timestamp: Date): String {
            val currentDate = Calendar.getInstance()
            val taskDate = Calendar.getInstance()
            taskDate.time = timestamp

            return if (currentDate.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
                currentDate.get(Calendar.DAY_OF_YEAR) == taskDate.get(Calendar.DAY_OF_YEAR)
            ) {
                val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
                "Today " + timeFormat.format(timestamp)
            } else {
                val dateFormat = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault())
                dateFormat.format(timestamp)
            }
        }

        private fun isDueSoon(completionTime: Date): Boolean {
            val currentTime = System.currentTimeMillis()
            val twoHoursInMillis = 2 * 60 * 60 * 1000
            return (completionTime.time - currentTime) <= twoHoursInMillis
        }

        private fun getStatusText(isCompleted: Boolean, completionTime: Date): String {
            return if (isCompleted) {
                "Completed"
            } else if (isLate(completionTime)) {
                "late"
            } else "Not completed yet"
        }

        private fun isLate(completionTime: Date): Boolean {
            val currentTime = System.currentTimeMillis()
            return currentTime > completionTime.time
        }
    }

}