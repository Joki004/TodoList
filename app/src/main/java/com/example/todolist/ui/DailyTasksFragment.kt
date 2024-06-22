package com.example.todolist.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.helpers.observeAllTasks
import com.example.todolist.helpers.observeCompletedTasks
import com.example.todolist.helpers.observeCompletedTasksForCurrentDay
import com.example.todolist.helpers.observePendingTasks
import com.example.todolist.helpers.observePendingTasksForCurrentDay
import com.example.todolist.helpers.observeTasksForCurrentDay
import com.example.todolist.helpers.observeUrgentTasks
import com.example.todolist.helpers.observeUrgentTasksForCurrentDay
import com.example.todolist.helpers.showCategoryDialog
import com.example.todolist.viewmodel.TaskViewModel

class DailyTasksFragment : Fragment(R.layout.fragment_task_item_timeline) {
    private var currentFilter: Int=0
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskViewModel: TaskViewModel
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_task_item_timeline, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(isDaily = false) { taskId ->
            Log.d("Navigation", "Task ID: $taskId")
            val action = HomeFragmentDirections.actionHomeFragmentToEditTaskFragment(taskId.toString())
            Log.d("Navigation", "Action: $action")
            NavHostFragment.findNavController(requireParentFragment()).navigate(action)
            Toast.makeText(context, "Clicked task ID: $taskId", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = taskAdapter

        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        observeTasksForCurrentDay(viewLifecycleOwner, taskViewModel, taskAdapter)
        val filterButton: ImageView = rootView.findViewById(R.id.ic_filter)
        filterButton.setOnClickListener {
            showFilterOptions(currentFilter)
        }

        val categoriesButtons: Button = rootView.findViewById(R.id.category_name)
        categoriesButtons.setOnClickListener {
            showCategoryDialog(requireContext(),viewLifecycleOwner,taskViewModel,taskAdapter,categoriesButtons,true)
        }
        return rootView
    }

    private fun showFilterOptions(currentFilter: Int) {
        val filterOptions = arrayOf("All Tasks", "Pending Tasks", "Completed Tasks", "Urgent Tasks")

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Filter")
            .setSingleChoiceItems(filterOptions, currentFilter) { dialog, which ->
                when (which) {
                    0 -> observeTasksForCurrentDay(viewLifecycleOwner, taskViewModel, taskAdapter)
                    1 -> observePendingTasksForCurrentDay(viewLifecycleOwner, taskViewModel, taskAdapter)
                    2 -> observeCompletedTasksForCurrentDay(viewLifecycleOwner, taskViewModel, taskAdapter)
                    3 -> observeUrgentTasksForCurrentDay(viewLifecycleOwner, taskViewModel, taskAdapter)
                }
                this.currentFilter = which
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}
