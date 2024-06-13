package com.example.todolist.helpers

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Button
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.viewmodel.TaskViewModel
import com.example.todolist.database.entities.Category

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
    if(isDaily){
        if (categoryId == null) {
            taskViewModel.tasksForCurrentDay.observe(lifecycleOwner, Observer { tasks ->
                taskAdapter.submitList(tasks)
            })
        } else {
            taskViewModel.getTasksByCategoryForCurrentDay(categoryId).observe(lifecycleOwner, Observer { tasks ->
                taskAdapter.submitList(tasks)
            })
        }
    }else{
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
                observeTasksByCategory(viewLifecycleOwner, taskViewModel, taskAdapter, null,isDaily)

            } else {
                observeTasksByCategory(viewLifecycleOwner, taskViewModel, taskAdapter, categories[which - 1].id, isDaily)
            }
            dialog.dismiss()
        }
        .create()

    dialog.show()
}
