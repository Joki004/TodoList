package com.example.todolist.helpers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.todolist.R
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
import com.example.todolist.database.entities.Task
import com.example.todolist.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


object Categories {
    var categoryIndex = 0
    var categoryList: MutableList<Category>? = null
}




fun observeTasksByCategory(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter,
    categoryId: Int?,
    isDaily: Boolean = false
) {
    if (isDaily) {
        if (categoryId == null) {
            taskViewModel.tasksForCurrentDay.observe(lifecycleOwner, Observer { tasks ->
                taskAdapter.submitList(tasks)
            })
        } else {
            taskViewModel.getTasksByCategoryForCurrentDay(categoryId)
                .observe(lifecycleOwner, Observer { tasks ->
                    taskAdapter.submitList(tasks)
                })
        }
    } else {
        if (categoryId == null) {
            taskViewModel.tasksByCompletionTimeDESC.observe(lifecycleOwner, Observer { tasks ->
                taskAdapter.submitList(tasks)
            })
        } else {
            taskViewModel.getTasksByCategory(categoryId).observe(lifecycleOwner, Observer { tasks ->
                taskAdapter.submitList(tasks)
            })
        }
    }

}

fun showCategoryDialog(
    context: Context,
    viewLifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter,
    button: Button,
    isDaily: Boolean = false
) {
    val categories = Categories.categoryList ?: listOf()
    Log.d("DB", categories.toString())
    val categoryNames = arrayOf("All categories") + categories.map { it.name }
    val currentSelection = Categories.categoryIndex

    val dialog = AlertDialog.Builder(context)
        .setTitle("Select Category")
        .setSingleChoiceItems(categoryNames, currentSelection) { dialog, which ->
            Categories.categoryIndex = which
            button.text = categoryNames[which]
            if (which == 0) {
                observeTasksByCategory(
                    viewLifecycleOwner,
                    taskViewModel,
                    taskAdapter,
                    null,
                    isDaily
                )

            } else {
                observeTasksByCategory(
                    viewLifecycleOwner,
                    taskViewModel,
                    taskAdapter,
                    categories[which - 1].id,
                    isDaily
                )
            }
            dialog.dismiss()
        }
        .create()

    dialog.show()
}


suspend fun handleFileSelections(taskId: Int,uris: List<Uri>, taskViewModel: TaskViewModel, context: Context, callback: (Boolean) -> Unit) {
    try {
        Log.d("FileSelection", "Handling files for taskId $taskId")
        if (taskId != -1) {
            uris.forEach { uri ->
                val fileName = withContext(Dispatchers.IO) { getFileName(uri, context) }
                val fileType = context.contentResolver.getType(uri)

                val attachment = Attachment(
                    taskId = taskId,
                    filePath = fileName,
                    fileType = fileType ?: "unknown"
                )

                withContext(Dispatchers.IO) {
                    taskViewModel.addAttachment(attachment)
                }
            }

            withContext(Dispatchers.Main) {
                callback(true)
            }
        }
        else {
            Log.e("FileSelection", "Failed to retrieve task ID for attachment")
            withContext(Dispatchers.Main) {
                callback(false)
            }
        }
    } catch (e: Exception) {
        Log.e("FileSelection", "Error handling file selection: ${e.message}")
        withContext(Dispatchers.Main) {
            callback(false)
        }
    }
}


@SuppressLint("Range")
fun getFileName(uri: Uri, context: Context): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    return result ?: uri.lastPathSegment ?: "unknown"
}

suspend fun addNewCategory(name: String, taskViewModel: TaskViewModel, callback: (Int) -> Unit) {
    val categories = Categories.categoryList ?: mutableListOf()

    val existingCategory = categories.find { it.name == name }
    if (existingCategory != null) {
        // If the category exists, return its ID
        callback(existingCategory.id)
    } else {

            val categoryId = taskViewModel.insertCategoryAndGetId(name)
            if (categoryId != -1) {
                Log.d("UI", "New category inserted with ID: $categoryId")
                callback(categoryId)
            } else {
                Log.e("UI", "Failed to insert new category")
                callback(-1)
            }

    }
}


suspend fun getCategoryId(name: String, taskViewModel: TaskViewModel): Int {
    return taskViewModel.getCategoryId(name)
}


fun getDateTimeMillis(dateEditText: EditText, timeEditText: EditText): Long? {
    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateTimeString = "${dateEditText.text} ${timeEditText.text}"
    return try {
        dateTimeFormat.parse(dateTimeString)?.time
    } catch (e: ParseException) {
        null
    }
}

fun formatDateTime(taskDate: Date): Pair<String, String> {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val dateString = dateFormat.format(taskDate)
    val timeString = timeFormat.format(taskDate)

    return Pair(dateString, timeString)
}

fun showDeleteConfirmationDialog(task: Task, context: Context, taskViewModel: TaskViewModel) {
    AlertDialog.Builder(context)
        .setTitle("Delete Task")
        .setMessage("Do you really want to delete ${task.title}?")
        .setPositiveButton("Yes") { _, _ ->
            taskViewModel.delete(task)
            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
        }
        .setNegativeButton("No", null)
        .show()
}

fun showDeleteCategoryConfirmationDialog(category: Category, context: Context, taskViewModel: TaskViewModel) {
    AlertDialog.Builder(context)
        .setTitle("Delete Category")
        .setMessage("Do you really want to delete this category?")
        .setPositiveButton("Yes") { _, _ ->
            taskViewModel.deleteCategory(category)
            Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
        }
        .setNegativeButton("No", null)
        .show()
}

object IconHelper {

    private val localIcons = mapOf(
        "Work" to R.drawable.ic_work,
        "Personal" to R.drawable.ic_personal,
        "Shopping" to R.drawable.ic_shopping,
        "reading" to R.drawable.ic_task_icon,
        "sport" to R.drawable.ic_sport,
        "Others" to R.drawable.ic_others
    )

    fun assignIcon(context: Context, categoryName: String, imageView: ImageView) {
        val categoryNameLowerCase = categoryName.lowercase(Locale.ROOT)
        val iconRes = localIcons[categoryNameLowerCase]
        if (iconRes != null) {
            imageView.setImageResource(iconRes)
        } else {
            val onlineIconUrl = "https://example.com/icons/$categoryName.png"
            Glide.with(context)
                .load(onlineIconUrl)
                .apply(RequestOptions().error(R.drawable.ic_others))
                .into(imageView)
        }
    }


}
@SuppressLint("ScheduleExactAlarm")
fun scheduleNotification(task: Task, context: Context) {
    val sharedPreferences = context.getSharedPreferences("TaskSettings", Context.MODE_PRIVATE)
    val notificationTimeBefore = sharedPreferences.getInt("notification_time", 10)  // default to 10 minutes

    val notificationTime = task.completionTime.time - (notificationTimeBefore * 60 * 1000)


    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("taskId", task.id)
        putExtra("taskTitle", task.title)
    }
    val pendingIntent = PendingIntent.getBroadcast(context, task.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
}








