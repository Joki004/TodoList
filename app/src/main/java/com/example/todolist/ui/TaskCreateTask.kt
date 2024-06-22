package com.example.todolist.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.todolist.R
import com.example.todolist.database.entities.Task
import com.example.todolist.helpers.Categories
import com.example.todolist.helpers.addNewCategory
import com.example.todolist.helpers.getCategoryId
import com.example.todolist.helpers.getDateTimeMillis
import com.example.todolist.helpers.handleFileSelection
import com.example.todolist.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class TaskCreateTask : DialogFragment() {
    private lateinit var taskViewModel: TaskViewModel
    private var title: EditText? = null
    private var description: EditText? = null
    private var date: EditText? = null
    private var time: EditText? = null
    private var category: EditText? = null
    private var saveButton: Button? = null
    private var warningMessage: TextView? = null
    private var attachement: ImageView? = null
    private var hasAttachment = false
    private var UrlFile: Uri? = null
    private lateinit var pickFileLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var taskNotificationToggle: Switch
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickFileLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    UrlFile = uri

                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        return inflater.inflate(R.layout.create_task, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById(R.id.task_title)
        description = view.findViewById(R.id.task_description)
        date = view.findViewById(R.id.editTextDate)
        time = view.findViewById(R.id.editTextTime)
        category = view.findViewById(R.id.editTextCategory)
        saveButton = view.findViewById(R.id.save_task_button)
        warningMessage = view.findViewById(R.id.warning_message)
        attachement = view.findViewById(R.id.task_attachment)
        taskNotificationToggle = view.findViewById(R.id.task_notification_toggle)

        warningMessage?.visibility = View.INVISIBLE
        saveButton?.setOnClickListener {
            if (checkNewTask(title!!, date!!, time!!, category!!)) {
                category?.text?.toString()?.let { categoryName ->
                    lifecycleScope.launch {
                        addNewCategory(categoryName, taskViewModel){ categoryId->
                            if(categoryId!=-1){
                                if(processTask(categoryId)){
                                    dismiss()
                                }
                            }
                            else {
                                Toast.makeText(
                                    context,
                                    "Failed to add category",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                        }

                    }
                } ?: run {
                    Toast.makeText(context, "Category is required", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            } else {
                warningMessage?.text =
                    "All fields including title, date, and time are required and must be set to future dates."
                warningMessage?.setTextColor(Color.RED)
                warningMessage?.visibility = View.VISIBLE
            }
        }


        attachement?.setOnClickListener {
            
            pickFileLauncher.launch(arrayOf("*/*"))
        }

        date?.setOnClickListener {
            showDatePickerDialog()
        }

        time?.setOnClickListener {
            showTimePickerDialog()
        }

        category?.setOnClickListener {
            showCategoryDialog(
                requireContext(),
                taskViewModel,
                it as EditText,
                object : CategorySelectListener {
                    override fun onCategorySelected(categoryName: String) {
                        category?.setText(categoryName)
                    }
                }
            )
        }

    }


    fun checkNewTask(title: EditText, date: EditText, time: EditText, category: EditText): Boolean {
        // Check if any field is empty, return false immediately
        if (title.text.isEmpty() || date.text.isEmpty() || time.text.isEmpty() || category.text.isEmpty()) {
            return false
        }

        // Get the combined date and time in milliseconds
        val taskDateTime = getDateTimeMillis(date, time)

        // Check if the date and time are in the future
        return taskDateTime?.let {
            it > System.currentTimeMillis()
        } ?: false  // Return false if taskDateTime is null
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            setLayout(width, height)
        }
    }


    companion object {
        fun newInstance() = TaskCreateTask()
    }

    fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                view?.findViewById<EditText>(R.id.editTextDate)?.setText(selectedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog =
            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->

                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                view?.findViewById<EditText>(R.id.editTextTime)?.setText(formattedTime)
            }, hour, minute, true)  // 'true' for 24-hour clock

        timePickerDialog.show()
    }

    interface CategorySelectListener {
        fun onCategorySelected(categoryName: String)
    }

    private fun showCategoryDialog(
        context: Context,
        taskViewModel: TaskViewModel,
        editText: EditText,
        callback: CategorySelectListener  // Pass the callback here

    ) {
        val categories = Categories.categoryList ?: listOf()
        val categoryNames = categories.map { it.name }.toMutableList()
        categoryNames.add("Add new category")

        val currentSelection =
            Categories.categoryIndex.takeIf { it >= 0 && it < categoryNames.size } ?: 0

        val dialog = AlertDialog.Builder(context)
            .setTitle("Select Category")
            .setSingleChoiceItems(categoryNames.toTypedArray(), currentSelection) { dialog, which ->
                if (which == categoryNames.size - 1) {
                    // Last option: Add new category
                    showAddCategoryDialog(context, taskViewModel) { categoryName ->
                        callback.onCategorySelected(categoryName)
                        editText.setText(categoryName)
                    }
                    dialog.dismiss()
                } else {
                    Categories.categoryIndex = which
                    callback.onCategorySelected(categoryNames[which])
                    editText.setText(categoryNames[which])
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    fun showAddCategoryDialog(
        context: Context,
        taskViewModel: TaskViewModel,
        callback: (String) -> Unit
    ) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        val addDialog = AlertDialog.Builder(context)
            .setTitle("Add New Category")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val categoryName = input.text.toString()
                if (categoryName.isNotEmpty()) {
                    callback(categoryName)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        addDialog.show()
    }

    private fun processTask(categoryId: Int):Boolean {
        val taskDateTimeMillis = getDateTimeMillis(date!!, time!!)
        if(UrlFile!=null)hasAttachment=true
        taskDateTimeMillis?.let {
            val taskDateTime = Date(it)
            val task = Task(
                title = title?.text.toString(),
                description = description?.text.toString(),
                creationTime = System.currentTimeMillis(),
                completionTime = taskDateTime,
                isCompleted = false,
                notificationEnabled = taskNotificationToggle.isChecked,
                categoryId = categoryId,
                hasAttachments = hasAttachment
            )
            taskViewModel.insert(task)
            if (hasAttachment && UrlFile != null) {
                lifecycleScope.launch {
                    handleFileSelection(UrlFile!!, taskViewModel, requireContext()) { success ->
                        if (success) {
                            // If the attachment was successfully added
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Attachment successfully added.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // If there was an error adding the attachment
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Failed to add attachment.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            return true
        } ?: run {
            Toast.makeText(context, "Invalid date or time", Toast.LENGTH_SHORT).show()
            return false
        }
    }





}
