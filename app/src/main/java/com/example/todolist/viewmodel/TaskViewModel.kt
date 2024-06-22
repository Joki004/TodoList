package com.example.todolist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
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
    val getUrgentTasksForCurrentDay: LiveData<List<Task>>
    val categories: LiveData<List<Category>>
    val getPendingTasksForCurrentDay: LiveData<List<Task>>
    val getCompletedTasksForCurrentDay: LiveData<List<Task>>
    val taskCompletionPercentage = MediatorLiveData<Int>()
    private val _taskById = MutableLiveData<Task?>()
    val taskById: LiveData<Task?> get() = _taskById
    init {
        repository = TaskRepository(application)
        allTasks = repository.allTasks
        tasksByCompletionTimeDESC = repository.tasksByCompletionTimeDESC
        pendingTasks = repository.pendingTasks
        completedTasks = repository.completedTasks
        urgentTasks = repository.urgentTasks
        tasksForCurrentDay = repository.tasksForCurrentDay
        getUrgentTasksForCurrentDay = repository.getUrgentTasksForCurrentDay
        getPendingTasksForCurrentDay = repository.getPendingTasksForCurrentDay
        getCompletedTasksForCurrentDay = repository.getCompletedTasksForCurrentDay
        categories = repository.categories
        val totalTasks = repository.getTotalTaskCount()
        val completedTasks = repository.getCompletedTaskCount()

        taskCompletionPercentage.addSource(totalTasks) { totalCount ->
            Log.d("TaskViewModel", "Total Task Count: $totalCount")
            updateTaskCompletionPercentage(totalCount, completedTasks.value)
        }
        taskCompletionPercentage.addSource(completedTasks) { completedCount ->
            Log.d("TaskViewModel", "Completed Task Count: $completedCount")
            updateTaskCompletionPercentage(totalTasks.value, completedCount)
        }
        //insertSampleDataInCategory()
        //insertSampleData()

    }
    private fun updateTaskCompletionPercentage(total: Int?, completed: Int?) {
        if (total != null && completed != null && total > 0) {
            taskCompletionPercentage.value = (completed.toDouble() / total * 100).toInt()
            Log.d("TaskViewModel", "Completion Percentage: ${taskCompletionPercentage.value}")
        } else {
            taskCompletionPercentage.value = 0
            Log.d("TaskViewModel", "Completion Percentage: 0")
        }
    }
    fun getTotalTaskCount(): LiveData<Int> {
        return repository.getTotalTaskCount()
    }

    fun getCompletedTaskCount(): LiveData<Int> {
        return repository.getCompletedTaskCount()
    }
    fun getTasksByCategory(categoryId: Int): LiveData<List<Task>> {
        return repository.getTasksByCategory(categoryId)
    }


    fun getTasksByCategoryForCurrentDay(categoryId: Int): LiveData<List<Task>> {
        return repository.getTasksByCategoryForCurrentDay(categoryId)
    }
    fun insert(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    fun insertCategory(categoryName: String) {
        if (categoryName.isNotEmpty()) {
            val newCategory = Category(name = categoryName) // Assuming the Category class has an appropriate constructor
            viewModelScope.launch {
                repository.insertCategory(newCategory)
            }
        } else {
            Log.e("Category", "Error inserting new category")
        }


    }

    suspend fun insertCategoryAndGetId(categoryName: String): Int {
        if (categoryName.isEmpty()) {
            Log.e("TaskViewModel", "Category name is empty")
            return -1
        }
        repository.insertCategory(Category(name = categoryName))
        return repository.getLastCategoryId()
    }
    suspend fun addAttachment(attachment: Attachment) {
        repository.addAttachment(attachment)
    }

    fun getAttachmentsForTask(taskId: Int): LiveData<List<Attachment>> {
        return repository.getAttachmentsForTask(taskId)
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

    fun deleteCategory(category: Category){
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    private fun insertSampleData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sampleTasks = listOf(
            Task(
                title = "Gym",
                description = "Go to the gym. Don't skip leg day",
                creationTime = System.currentTimeMillis(),
                completionTime = dateFormat.parse("2024-06-13 21:00")!!,
                isCompleted = false,
                notificationEnabled = false,
                categoryId = 1,
                hasAttachments = false
            ),
            Task(
                title = "Business meeting with Microsoft",
                description = "This is a sample task description 2",
                creationTime = System.currentTimeMillis(),
                completionTime = dateFormat.parse("2024-06-012 15:00")!!,
                isCompleted = true,
                notificationEnabled = false,
                categoryId = 2,
                hasAttachments = true
            ),
            Task(
                title = "Sample Task new",
                description = "This is a sample task description new",
                creationTime = System.currentTimeMillis(),
                completionTime = dateFormat.parse("2024-06-11 05:00")!!,
                isCompleted = true,
                notificationEnabled = false,
                categoryId = 3,
                hasAttachments = false
            )
        )
        sampleTasks.forEach { task ->
            insert(task)
        }
    }

    private fun insertSampleDataInCategory() {

        val Category = listOf(
            Category(
                name = "Personal",
            ),
            Category(
                name = "Business",
            ),
            Category(
                name = "Groceries",
            ),
        )
        Category.forEach { category ->
            insertCategory(category.name)
        }
    }

    suspend fun getLastTaskId(): Int {
        return repository.getLastTaskId()
    }

    suspend fun getLastCategoryId(): Int{
        return repository.getLastCategoryId()
    }
    suspend fun getCategoryId(name : String): Int {
        return repository.getCategoryId(name)
    }

    suspend fun getTasksByID(taskId: Int): Task? {
        return repository.getTasksByID(taskId)
    }
    fun loadTaskById(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTasksByID(taskId)
            Log.d("ViewModel", "Fetching task: $taskId")
            if (task != null) {
                Log.d("ViewModel", "Task fetched: ${task.title}")
                _taskById.postValue(task)
            } else {
                Log.d("ViewModel", "Task not found")
            }
        }
    }
}
