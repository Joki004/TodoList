package com.example.todolist.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
import com.example.todolist.database.entities.Task

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task_table ORDER BY id ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 0 ORDER BY completionTime ASC")
    fun getPendingTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 1 ORDER BY completionTime ASC")
    fun getCompletedTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE categoryId = :categoryId ORDER BY completionTime ASC")
    fun getTasksByCategory(categoryId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE id = :taskId")
    suspend fun getTasksByID(taskId: Int): Task?


    @Query("SELECT * FROM task_table ORDER BY creationTime DESC")
    fun getAllTasksOrderByCreationTimeDesc(): LiveData<List<Task>>

    @Query("SELECT * FROM TASK_TABLE ORDER BY completionTime DESC")
    fun getAllTasksOrderByCompletionTimeDESC(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 0 ORDER BY completionTime ASC")
    fun getUrgentTasks(): LiveData<List<Task>>
    @Query("SELECT id FROM task_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastTaskId(): Int

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////Daily Tasks/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    @Query("SELECT * FROM task_table WHERE date(completionTime / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    fun getTasksForCurrentDay(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 0 and date(completionTime / 1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY completionTime ASC")
    fun getUrgentTasksForCurrentDay(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 0 and date(completionTime / 1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY completionTime ASC")
    fun getPendingTasksForCurrentDay(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 1 and date(completionTime / 1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY completionTime ASC")
    fun getCompletedTasksForCurrentDay(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE categoryId = :categoryId and date(completionTime / 1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY completionTime ASC")
    fun getTasksByCategoryForCurrentDay(categoryId: Int): LiveData<List<Task>>

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////OTHER CRUD'S/////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    @Query("SELECT * FROM task_table WHERE title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' ORDER BY completionTime ASC")
    fun searchTasks(searchQuery: String): LiveData<List<Task>>

    @Insert
    suspend fun insertAttachment(attachment: Attachment)

    @Query("SELECT * FROM attachment_table WHERE taskId = :taskId")
    fun getAttachmentsForTask(taskId: Int): LiveData<List<Attachment>>


    @Delete
    suspend fun deleteAttachments(attachment: Attachment)
    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)
    @Query("SELECT id FROM category_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastCategoryId(): Int

    @Query("SELECT id FROM category_table WHERE name = :name ORDER BY id DESC LIMIT 1")
    suspend fun getCategoryId(name : String): Int
    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM category_table ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT COUNT(*) FROM task_table")
    fun getTotalTaskCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM task_table WHERE isCompleted = 1")
    fun getCompletedTaskCount(): LiveData<Int>
    @Query("DELETE FROM attachment_table WHERE filePath = :filePath and taskId = :taskId")
    suspend fun deleteAttachmentByFilePath(filePath: String, taskId: Int)
    @Query("UPDATE task_table SET hasAttachments = :hasAttachment WHERE id = :taskId")
    suspend fun updateHasAttachment(hasAttachment: Boolean, taskId: Int)

}
