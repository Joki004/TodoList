package com.example.todolist.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.todolist.database.TaskDatabase
import com.example.todolist.database.dao.TaskDao
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
import com.example.todolist.database.entities.Task

class TaskRepository(application: Application) {
    private val taskDao: TaskDao
    val allTasks: LiveData<List<Task>>
    val tasksByCompletionTimeDESC: LiveData<List<Task>>
    val pendingTasks: LiveData<List<Task>>
    val completedTasks: LiveData<List<Task>>
    val urgentTasks: LiveData<List<Task>>
    val tasksForCurrentDay: LiveData<List<Task>>
    val categories: LiveData<List<Category>>

    val getUrgentTasksForCurrentDay : LiveData<List<Task>>
    val getPendingTasksForCurrentDay : LiveData<List<Task>>
    val getCompletedTasksForCurrentDay : LiveData<List<Task>>
    init {
        val database = TaskDatabase.getInstance(application)
        taskDao = database.taskDao()
        allTasks = taskDao.getAllTasks()
        tasksByCompletionTimeDESC = taskDao.getAllTasksOrderByCompletionTimeDESC()
        pendingTasks = taskDao.getPendingTasks()
        completedTasks = taskDao.getCompletedTasks()
        urgentTasks = taskDao.getUrgentTasks()
        tasksForCurrentDay = taskDao.getTasksForCurrentDay()
        getUrgentTasksForCurrentDay = taskDao.getUrgentTasksForCurrentDay()
        getPendingTasksForCurrentDay = taskDao.getPendingTasksForCurrentDay()
        getCompletedTasksForCurrentDay = taskDao.getCompletedTasksForCurrentDay()
        categories = taskDao.getAllCategories()
    }
    fun getTotalTaskCount(): LiveData<Int> {
        return taskDao.getTotalTaskCount()
    }

    fun getCompletedTaskCount(): LiveData<Int> {
        return taskDao.getCompletedTaskCount()
    }
    fun getTasksByCategory(categoryId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByCategory(categoryId)
    }
    fun getTasksByCategoryForCurrentDay(categoryId: Int):LiveData<List<Task>>{
        return taskDao.getTasksByCategoryForCurrentDay(categoryId)
    }

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }
    suspend fun insertCategory(category: Category){
        taskDao.insertCategory(category)
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }

    suspend fun addAttachment(attachment: Attachment) {
        taskDao.insertAttachment(attachment)
    }

    fun getAttachmentsForTask(taskId: Int): LiveData<List<Attachment>> {
        return taskDao.getAttachmentsForTask(taskId)
    }

     suspend fun getLastTaskId(): Int{
        return taskDao.getLastTaskId()
    }
    suspend fun getLastCategoryId(): Int {
        return taskDao.getLastCategoryId()
    }
   suspend fun getCategoryId(name : String): Int {
        return taskDao.getCategoryId(name)
    }


}
