package com.example.todolist.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.adapter.CategoryAdapter
import com.example.todolist.adapter.TaskAdapter
import com.example.todolist.helpers.observeAllTasks
import com.example.todolist.helpers.observeCategories
import com.example.todolist.helpers.observeCompletedTasks
import com.example.todolist.helpers.observePendingTasks
import com.example.todolist.helpers.observeUrgentTasks
import com.example.todolist.helpers.observeUrgentTasksForCurrentDay
import com.example.todolist.helpers.searchTasks
import com.example.todolist.helpers.showCategoryDialog
import com.example.todolist.helpers.showDeleteCategoryConfirmationDialog
import com.example.todolist.helpers.showDeleteConfirmationDialog
import com.example.todolist.viewmodel.TaskViewModel
import com.google.android.material.navigation.NavigationView

class AllTasksFragment : Fragment(R.layout.fragment_task_item) {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var taskViewModel: TaskViewModel
    private var currentFilter: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_task_item, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)

        HomeFragment.isDaily = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter(isDaily = false, onTaskClick = { taskId ->
            Log.d("Navigation", "Task ID: $taskId")
            val action = HomeFragmentDirections.actionHomeFragmentToEditTaskFragment(taskId.toString())
            Log.d("Navigation", "Action: $action")
            NavHostFragment.findNavController(requireParentFragment()).navigate(action)
        }, onTaskLongClick = { task ->
            showDeleteConfirmationDialog(task, requireContext(), taskViewModel)
        })

        recyclerView.adapter = taskAdapter

        categoryAdapter = CategoryAdapter(isDaily = false, onCategoryLongClick = { category ->
            showDeleteCategoryConfirmationDialog(category, requireContext(), taskViewModel)
        })
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        observeAllTasks(viewLifecycleOwner, taskViewModel, taskAdapter)
        observeCategories(viewLifecycleOwner,taskViewModel, categoryAdapter)

        arguments?.getString("search_query")?.let { query ->
            if (query.isNotEmpty()) {
                searchTasks(viewLifecycleOwner, taskViewModel, taskAdapter, query)
            }
        }

        val filterButton: ImageView = rootView.findViewById(R.id.ic_filter)
        filterButton.setOnClickListener {
            showFilterOptions(currentFilter)
        }

        val categoriesButtons: Button = rootView.findViewById(R.id.category_name)
        categoriesButtons.setOnClickListener {
            showCategoryDialog(requireContext(),viewLifecycleOwner,taskViewModel,taskAdapter,categoriesButtons)
        }

        return rootView
    }


    private fun showFilterOptions(currentFilter: Int) {
        val filterOptions = arrayOf("All Tasks", "Pending Tasks", "Completed Tasks", "Urgent Tasks")

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Filter")
            .setSingleChoiceItems(filterOptions, currentFilter) { dialog, which ->
                when (which) {
                    0 -> observeAllTasks(viewLifecycleOwner, taskViewModel, taskAdapter)
                    1 -> observePendingTasks(viewLifecycleOwner, taskViewModel, taskAdapter)
                    2 -> observeCompletedTasks(viewLifecycleOwner, taskViewModel, taskAdapter)
                    3 -> observeUrgentTasksForCurrentDay(viewLifecycleOwner, taskViewModel, taskAdapter)
                }
                this.currentFilter = which
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

}