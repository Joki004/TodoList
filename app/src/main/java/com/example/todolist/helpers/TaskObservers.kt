package com.example.todolist.helpers

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.adapter.CategoryAdapter
import com.example.todolist.database.entities.Task
import com.example.todolist.viewmodel.TaskViewModel


fun observeAllTasks(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.tasksByCompletionTimeDESC.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })
}

fun observePendingTasks(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.pendingTasks.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })
}

fun observeCompletedTasks(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.completedTasks.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })
}

fun observeUrgentTasks(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.urgentTasks.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })

}

fun observeTasksForCurrentDay(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.tasksForCurrentDay.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })

}

fun observeUrgentTasksForCurrentDay(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.getUrgentTasksForCurrentDay.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })

}

fun observePendingTasksForCurrentDay(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.getPendingTasksForCurrentDay.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })

}



fun observeCompletedTasksForCurrentDay(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskAdapter: TaskAdapter
) {
    taskViewModel.getCompletedTasksForCurrentDay.observe(lifecycleOwner, Observer { tasks ->
        taskAdapter.submitList(tasks)
    })

}
fun observeCategories(
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    categoryAdapter: CategoryAdapter
) {
    taskViewModel.categories.observe(lifecycleOwner, Observer { categories ->
        Categories.categoryList = categories.toMutableList()
        categoryAdapter.submitList(Categories.categoryList)
    })
}


fun observeTaskById(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    taskViewModel: TaskViewModel,
    taskId: Int,
    taskDisplayFunction: (Task) -> Unit
) {
    taskViewModel.loadTaskById(taskId)
    taskViewModel.taskById.observe(lifecycleOwner, Observer { task ->
        task?.let {
            taskDisplayFunction(it)
        } ?: Toast.makeText(context, "Task not found.", Toast.LENGTH_SHORT).show()
    })
}



