package com.example.todolist.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.R
import com.example.todolist.database.entities.Task
import com.example.todolist.helpers.*
import com.example.todolist.viewmodel.TaskViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EditTaskFragment : Fragment(R.layout.fragment_edit_task) {

    private lateinit var taskViewModel: TaskViewModel
    private var taskMain: Task? = null
    private lateinit var title: EditText
    private lateinit var description: EditText
    private lateinit var date: EditText
    private lateinit var time: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var hasNotification: Switch
    private lateinit var isCompleted: Switch
    private var lenCategory: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EditTaskFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("EditTaskFragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_edit_task, container, false)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("EditTaskFragment", "onViewCreated")
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        // Initialize all views
        initializeViews(view)

        // Set up the category spinner
        populateCategorySpinner()

        // Set up listeners
        setListeners()

        // Handle arguments
        arguments?.getString("taskId")?.toIntOrNull()?.let { taskId ->
            observeTaskById(requireContext(), viewLifecycleOwner, taskViewModel, taskId) { task ->
                displayTask(task)
            }
        } ?: Toast.makeText(context, "Invalid task ID", Toast.LENGTH_SHORT).show()
    }

    private fun initializeViews(view: View) {
        title = view.findViewById(R.id.task_title_input)
        description = view.findViewById(R.id.task_description_input)
        date = view.findViewById(R.id.editTextDate)
        time = view.findViewById(R.id.editTextTime)
        categorySpinner = view.findViewById(R.id.task_category_spinner)
        hasNotification = view.findViewById(R.id.task_notification_toggle)
        isCompleted = view.findViewById(R.id.task_status_toggle)
        saveButton = view.findViewById(R.id.save_button)
    }

    private fun setListeners() {
        date.setOnClickListener { showDatePickerDialog() }
        time.setOnClickListener { showTimePickerDialog() }
        saveButton.setOnClickListener { handleSaveButtonClick() }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleSaveButtonClick() {
        val selectedPosition = categorySpinner.selectedItemPosition
        if (selectedPosition != Spinner.INVALID_POSITION) {
            if (selectedPosition >= lenCategory) {
                showNewCategoryDialog()
            } else {
                val categoryId = Categories.categoryList?.get(selectedPosition)?.id ?: -1
                if (categoryId != -1) {
                    if(updateTask(categoryId))showToast("Task ${taskMain?.title} updated successfully")
                } else {
                    showToast("Invalid category selection")
                }
            }
        }
    }

    private fun displayTask(task: Task) {
        Log.d("EditTaskFragment", "Displaying task: ${task.title}")
        taskMain = task
        title.setText(task.title)
        description.setText(task.description)
        val (dateString, timeString) = formatDateTime(task.completionTime)
        date.setText(dateString)
        time.setText(timeString)
        task.categoryId?.let { setCategorySpinnerSelection(it) }
        hasNotification.isChecked = task.notificationEnabled
        isCompleted.isChecked = task.isCompleted
    }

    private fun setCategorySpinnerSelection(categoryId: Int) {
        val categoryPosition = Categories.categoryList?.indexOfFirst { it.id == categoryId } ?: 0
        categorySpinner.setSelection(categoryPosition)
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            date.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day).show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            time.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
        }, hour, minute, true).show()  // 'true' for 24-hour clock
    }

    private fun populateCategorySpinner() {
        val categories = Categories.categoryList ?: listOf()
        lenCategory = categories.size
        val categoryNames = categories.map { it.name }.toMutableList().apply { add("New Category") }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showNewCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_category, null)
        val editText = dialogView.findViewById<EditText>(R.id.new_category_name)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("New Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val newCategoryName = editText.text.toString()
                if (newCategoryName.isNotEmpty()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        addNewCategory(newCategoryName, taskViewModel) { categoryId ->
                            if (categoryId != -1) {
                                showToast("Category added successfully")
                                populateCategorySpinner()
                                categorySpinner.setSelection(Categories.categoryList?.indexOfFirst { it.id == categoryId } ?: 0)
                            } else {
                                showToast("Failed to add category")
                            }
                        }
                    }
                } else {
                    showToast("Category name cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateTask(categoryId: Int): Boolean {
        val taskDateTimeMillis = getDateTimeMillis(date, time)
        taskDateTimeMillis?.let {
            val taskDateTime = Date(it)
            taskMain?.let { mainTask ->
                val task = taskMain?.id?.let { it1 ->
                    Task(
                        id = it1,
                        title = title.text.toString(),
                        description = description.text.toString(),
                        creationTime = mainTask.creationTime,
                        completionTime = taskDateTime,
                        isCompleted = isCompleted.isChecked,
                        notificationEnabled = hasNotification.isChecked,
                        categoryId = categoryId,
                        hasAttachments = mainTask.hasAttachments
                    )
                }
                if (task != null) {
                    taskViewModel.update(task)
                }
                return true
            } ?: showToast("Task data is missing")
        } ?: showToast("Invalid date or time")
        return false
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
