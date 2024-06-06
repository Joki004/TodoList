package com.example.todolist.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.todolist.database.TaskDatabase
import com.example.todolist.database.dao.TaskDao
import com.example.todolist.database.entities.Task

class TaskRepository(application: Application) {
    private val taskDao: TaskDao
    val allTasks: LiveData<List<Task>>

    init {
        val database = TaskDatabase.getInstance(application)
        taskDao = database.taskDao()
        allTasks = taskDao.getAllTasks()
    }

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }
}
