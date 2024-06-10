package com.example.todolist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.todolist.database.entities.Task
import com.example.todolist.repository.TaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    val allTasks: LiveData<List<Task>>
    val tasksByCompletionTimeDESC: LiveData<List<Task>>
    val pendingTasks: LiveData<List<Task>>
    val completedTasks: LiveData<List<Task>>
    val urgentTasks: LiveData<List<Task>>
    val tasksForCurrentDay: LiveData<List<Task>>
    init {
        repository = TaskRepository(application)
        allTasks = repository.allTasks
        tasksByCompletionTimeDESC = repository.tasksByCompletionTimeDESC
        pendingTasks = repository.pendingTasks
        completedTasks = repository.completedTasks
        urgentTasks = repository.urgentTasks
        tasksForCurrentDay = repository.tasksForCurrentDay
        insertSampleData()
    }

    fun insert(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    fun update(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
    private fun insertSampleData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sampleTasks = listOf(
            Task(
                title = "Sample Task 1",
                description = "This is a sample task description 1",
                creationTime = System.currentTimeMillis(),
                completionTime = dateFormat.parse("2024-06-10 10:00")!!,
                isCompleted = false,
                notificationEnabled = false,
                categoryId = null,
                hasAttachments = false
            ),
            Task(
                title = "Sample Task 2",
                description = "This is a sample task description 2",
                creationTime = System.currentTimeMillis(),
                completionTime = dateFormat.parse("2024-06-09 03:00")!!,
                isCompleted = false,
                notificationEnabled = false,
                categoryId = null,
                hasAttachments = true
            ),
            Task(
                title = "Sample Task 3",
                description = "This is a sample task description 3",
                creationTime = System.currentTimeMillis(),
                completionTime = dateFormat.parse("2024-06-11 05:00")!!,
                isCompleted = true,
                notificationEnabled = false,
                categoryId = null,
                hasAttachments = false
            )
        )
        sampleTasks.forEach { task ->
            insert(task)
        }
    }
}
