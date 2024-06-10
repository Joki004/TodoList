package com.example.todolist.helpers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.todolist.TaskAdapter
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