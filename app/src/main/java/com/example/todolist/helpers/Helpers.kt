package com.example.todolist.helpers

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
import com.example.todolist.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
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


suspend fun handleFileSelection(uri: Uri, taskViewModel: TaskViewModel, context: Context) {
    withContext(Dispatchers.IO) {  // Ensure you are on the IO thread for file and database operations
        val fileName = getFileName(uri, context)
        val fileType = context.contentResolver.getType(uri)
        val taskId =
            taskViewModel.getLastTaskId()  // Make sure this is handled appropriately in your ViewModel

        val attachment = Attachment(
            taskId = taskId,
            filePath = fileName,
            fileType = fileType ?: "unknown"
        )

        taskViewModel.addAttachment(attachment)  // Update your database or LiveData here
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






