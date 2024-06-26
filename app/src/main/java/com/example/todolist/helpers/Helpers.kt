package com.example.todolist.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
import com.example.todolist.database.entities.Task
import com.example.todolist.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
            taskViewModel.tasksForCurrentDay.observe(lifecycleOwner) { tasks ->
                taskAdapter.submitList(tasks)
            }
        } else {
            taskViewModel.getTasksByCategoryForCurrentDay(categoryId)
                .observe(lifecycleOwner) { tasks ->
                    taskAdapter.submitList(tasks)
                }
        }
    } else {
        if (categoryId == null) {
            taskViewModel.tasksByCompletionTimeDESC.observe(lifecycleOwner) { tasks ->
                taskAdapter.submitList(tasks)
            }
        } else {
            taskViewModel.getTasksByCategory(categoryId).observe(lifecycleOwner) { tasks ->
                taskAdapter.submitList(tasks)
            }
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

    val dialog = AlertDialog.Builder(context).setTitle("Select Category")
        .setSingleChoiceItems(categoryNames, currentSelection) { dialog, which ->
            Categories.categoryIndex = which
            button.text = categoryNames[which]
            if (which == 0) {
                observeTasksByCategory(
                    viewLifecycleOwner, taskViewModel, taskAdapter, null, isDaily
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
        }.create()

    dialog.show()
}


suspend fun handleFileSelections(
    taskId: Int,
    uris: List<Uri>,
    taskViewModel: TaskViewModel,
    context: Context,
    callback: (Boolean) -> Unit
) {
    try {
        Log.d("FileSelection", "Handling files for taskId $taskId")
        if (taskId != -1) {
            uris.forEach { uri ->
                val fileName = withContext(Dispatchers.IO) { getFileName(uri, context) }
                val fileType = context.contentResolver.getType(uri)

                // Copy the file from external to internal storage
                val copiedFileUri = withContext(Dispatchers.IO) { copyFileToInternalStorage(uri, fileName, taskId, context) }

                if (copiedFileUri != null) {
                    val attachment = Attachment(
                        taskId = taskId,
                        filePath = fileName,
                        fileType = fileType ?: "unknown",
                        uri = copiedFileUri,
                        contentProviderAuthority = copiedFileUri.authority.toString()
                    )

                    withContext(Dispatchers.IO) {
                        taskViewModel.addAttachment(attachment)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                callback(true)
            }
        } else {
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

private fun copyFileToInternalStorage(uri: Uri, fileName: String, taskId: Int, context: Context): Uri? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val taskDir = File(context.filesDir, taskId.toString())
        if (!taskDir.exists()) {
            taskDir.mkdirs()
        }
        val outputFile = File(taskDir, fileName)
        val outputStream: OutputStream = FileOutputStream(outputFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(1024)
                var length: Int
                while (input.read(buffer).also { length = it } > 0) {
                    output.write(buffer, 0, length)
                }
                output.flush()
            }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)
    } catch (e: Exception) {
        Log.e("FileCopy", "Error copying file: ${e.message}")
        null
    }
}


@SuppressLint("Range")
fun getFileName(uri: Uri, context: Context): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    return result ?: uri.lastPathSegment ?: "unknown"
}

fun deleteFileFromInternalStorage(filePath: String, taskId: Int, context: Context): Boolean {
    return try {
        val taskDir = File(context.filesDir, taskId.toString())
        val file = File(taskDir, filePath)
        var fileDeleted = false

        if (file.exists()) {
            fileDeleted = file.delete()
        }

        if (taskDir.isDirectory && taskDir.list().isNullOrEmpty()) {
            taskDir.delete()
        }

        fileDeleted
    } catch (e: Exception) {
        Log.e("FileDelete", "Error deleting file: ${e.message}")
        false
    }
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
    AlertDialog.Builder(context).setTitle("Delete Task")
        .setMessage("Do you really want to delete ${task.title}?")
        .setPositiveButton("Yes") { _, _ ->
            taskViewModel.delete(task)
            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
        }.setNegativeButton("No", null).show()
}

fun showDeleteCategoryConfirmationDialog(
    category: Category, context: Context, taskViewModel: TaskViewModel
) {
    AlertDialog.Builder(context).setTitle("Delete Category")
        .setMessage("Do you really want to delete this category?")
        .setPositiveButton("Yes") { _, _ ->
            taskViewModel.deleteCategory(category)
            Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
        }.setNegativeButton("No", null).show()
}

object IconHelper {

    private val localIcons = mapOf(
        "work" to R.drawable.ic_work,
        "personal" to R.drawable.ic_personal,
        "shopping" to R.drawable.ic_shopping,
        "reading" to R.drawable.ic_task_icon,
        "sport" to R.drawable.ic_sport,
        "others" to R.drawable.ic_others
    )

    fun assignIcon(context: Context, categoryName: String, imageView: ImageView) {
        val categoryNameLowerCase = categoryName.lowercase(Locale.ROOT)
        val iconRes = localIcons[categoryNameLowerCase]
        if (iconRes != null) {
            imageView.setImageResource(iconRes)
        } else {
            val onlineIconUrl = "https://example.com/icons/$categoryName.png"
            Glide.with(context).load(onlineIconUrl)
                .apply(RequestOptions().error(R.drawable.ic_others)).into(imageView)
        }
    }


}

@SuppressLint("ScheduleExactAlarm")
fun scheduleNotification(task: Task, context: Context) {
    val sharedPreferences = context.getSharedPreferences("TaskSettings", Context.MODE_PRIVATE)
    val notificationTimeBefore =
        sharedPreferences.getInt("notification_time", 10)

    val notificationTime = task.completionTime.time - (notificationTimeBefore * 60 * 1000)


    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("taskId", task.id)
        putExtra("taskTitle", task.title)
    }
    val pendingIntent =
        PendingIntent.getBroadcast(context, task.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
}

 fun cancelNotification(task: Task, context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("taskId", task.id)
        putExtra("taskTitle", task.title)
    }
    val pendingIntent = PendingIntent.getBroadcast(context, task.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.cancel(pendingIntent)
}

fun fileFromContentUri(activity: Activity, attachment: Attachment): File {
    val fileExtension = getFileExtension(activity, attachment.filePath)
    val fileName = "temporary_file.$fileExtension"

    val tempFile = File(activity.cacheDir, fileName)
    tempFile.createNewFile()

    try {
        val oStream = FileOutputStream(tempFile)
        val inputStream = activity.contentResolver.openInputStream(attachment.uri)

        inputStream?.let {
            copy(inputStream, oStream)
        }

        oStream.flush()

    }
    catch (e: SecurityException){
        e.printStackTrace()
    }
    catch (e: Exception) {

        e.printStackTrace()
    }

    return tempFile
}

fun getFileExtension(context: Context, path: String): String {
    return path.substringAfter('.')
}

@Throws(IOException::class)
fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}








