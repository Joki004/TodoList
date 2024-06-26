package com.example.todolist.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.adapter.AttachmentAdapter
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Task
import com.example.todolist.helpers.Categories
import com.example.todolist.helpers.addNewCategory
import com.example.todolist.helpers.formatDateTime
import com.example.todolist.helpers.getDateTimeMillis
import com.example.todolist.helpers.getFileName
import com.example.todolist.helpers.handleFileSelections
import com.example.todolist.helpers.observeTaskById
import com.example.todolist.helpers.scheduleNotification
import com.example.todolist.viewmodel.TaskViewModel
import hasReadMediaPermissions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import requestReadMediaPermissions
import takePersistableUriPermission
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

class EditTaskFragment : Fragment(R.layout.fragment_edit_task) {

    private lateinit var taskViewModel: TaskViewModel
    private var taskMain: Task? = null
    private lateinit var title: EditText
    private lateinit var description: EditText
    private lateinit var date: EditText
    private lateinit var time: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var hasNotification: Switch

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var isCompleted: Switch
    private var lenCategory: Int = 0
    private var attachmentList: MutableList<Attachment> = mutableListOf()
    private lateinit var attachmentsRecyclerView: RecyclerView
    private lateinit var attachmentAdapter: AttachmentAdapter
    private var selectedUris: MutableList<Uri> = mutableListOf()
    private lateinit var pickFileLauncher: ActivityResultLauncher<Array<String>>
    private var attachments: MutableList<Attachment> = mutableListOf()

    companion object {

        const val READ_MEDIA_PERMISSIONS_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EditTaskFragment", "onCreate")
        pickFileLauncher =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri>? ->
                uris?.let {
                    selectedUris.addAll(it)
                    uris.forEach { uri ->
                        val fileName = getFileName(uri, requireContext())
                        val fileType = context?.contentResolver?.getType(uri)
                        val attachmentSelected = Attachment(
                            taskId = 0,
                            filePath = fileName,
                            fileType = fileType ?: "unknown",
                            uri = uri,
                            contentProviderAuthority = uri.authority.toString()
                        )
                        attachments.add(attachmentSelected)
                        takePersistableUriPermission(uri, requireContext())
                    }
                    updateAttachmentList()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d("EditTaskFragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_edit_task, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
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
                // Observe attachments for the given task ID
                taskViewModel.getAttachmentsByTaskId(taskId)
                    .observe(viewLifecycleOwner) { attachments ->
                        attachmentList.clear()
                        attachmentList.addAll(attachments)
                        attachmentAdapter.notifyDataSetChanged()
                    }
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

        attachmentsRecyclerView = view.findViewById(R.id.attachments_recycler_view)
        attachmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        attachmentAdapter =
            AttachmentAdapter(
                attachmentList,
                ::showDeleteAttachmentConfirmation,
                ::handleOpenAttachment
            )
        attachmentsRecyclerView.adapter = attachmentAdapter

        view.findViewById<Button>(R.id.task_attachment_button).setOnClickListener {
            pickFileLauncher.launch(arrayOf("*/*"))
        }
    }

    private fun handleOpenAttachment(attachment: Attachment) {
        if (!hasReadMediaPermissions(requireContext())) {
            requestReadMediaPermissions(requireActivity())
        } else {
            openFile(attachment)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_MEDIA_PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectedUris.lastOrNull()?.let { uri ->
                attachmentList.firstOrNull { it.uri == uri }?.let { openFile(it) }
            }
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showPermissionDeniedDialog()
            } else {
                Log.d("OpenAFile", "Permission denied to access external Storage")
            }
        }
    }

    private fun openFile(attachment: Attachment) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(attachment.uri, attachment.fileType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.d("OpenAFile", "No application found to open this file type.")
        } catch (e: SecurityException) {
            Log.d("OpenAFile", "Permission denied to access the file.")
            if (!hasReadMediaPermissions(requireContext())) {
                requestReadMediaPermissions(requireActivity())
            } else {
                takePersistableUriPermission(attachment.uri, requireContext())
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app needs permission to read external storage. Please enable it in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent =
                    Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setListeners() {
        date.setOnClickListener { showDatePickerDialog() }
        time.setOnClickListener { showTimePickerDialog() }
        saveButton.setOnClickListener { handleSaveButtonClick() }
    }

    private fun showDeleteAttachmentConfirmation(attachment: Attachment) {
        AlertDialog.Builder(requireContext()).setTitle("Delete Attachment")
            .setMessage("Are you sure you want to delete this attachment?")
            .setPositiveButton("Yes") { _, _ ->
                deleteAttachment(attachment)
            }.setNegativeButton("No", null).show()
    }

    private fun deleteAttachment(attachment: Attachment) {
        val index = attachmentList.indexOf(attachment)
        if (index != -1) {
            attachmentList.removeAt(index)
            attachmentAdapter.notifyItemRemoved(index)

            val uriToRemove = selectedUris.firstOrNull { uri ->
                getFileName(uri, requireContext()) == attachment.filePath
            }
            uriToRemove?.let { selectedUris.remove(it) }

            attachments.remove(attachment)
            viewLifecycleOwner.lifecycleScope.launch {
                taskViewModel.deleteAttachmentByFilePath(attachment.filePath, attachment.taskId)
            }

        }
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
                    if (updateTask(categoryId)) showToast("Task ${taskMain?.title} updated successfully")
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
        }, hour, minute, true).show()
    }

    private fun populateCategorySpinner() {
        val categories = Categories.categoryList ?: listOf()
        lenCategory = categories.size
        val categoryNames = categories.map { it.name }.toMutableList().apply { add("New Category") }
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showNewCategoryDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_category, null)
        val editText = dialogView.findViewById<EditText>(R.id.new_category_name)

        val dialog =
            AlertDialog.Builder(requireContext()).setTitle("New Category").setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val newCategoryName = editText.text.toString()
                    if (newCategoryName.isNotEmpty()) {
                        GlobalScope.launch(Dispatchers.Main) {
                            addNewCategory(newCategoryName, taskViewModel) { categoryId ->
                                if (categoryId != -1) {
                                    showToast("Category added successfully")
                                    populateCategorySpinner()
                                    categorySpinner.setSelection(Categories.categoryList?.indexOfFirst { it.id == categoryId }
                                        ?: 0)
                                } else {
                                    showToast("Failed to add category")
                                }
                            }
                        }
                    } else {
                        showToast("Category name cannot be empty")
                    }
                }.setNegativeButton("Cancel", null).create()

        dialog.show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateTask(categoryId: Int): Boolean {
        val taskDateTimeMillis = getDateTimeMillis(date, time)
        var taskId: Int? = null
        taskDateTimeMillis?.let { millis ->
            val taskDateTime = Date(millis)

            taskMain?.let { mainTask ->
                val task = Task(
                    id = mainTask.id,
                    title = title.text.toString(),
                    description = description.text.toString(),
                    creationTime = mainTask.creationTime,
                    completionTime = taskDateTime,
                    isCompleted = isCompleted.isChecked,
                    notificationEnabled = hasNotification.isChecked,
                    categoryId = categoryId,
                    hasAttachments = attachmentList.isNotEmpty()
                )
                taskId = mainTask.id

                taskViewModel.update(task)
                if (task.notificationEnabled) {
                    scheduleNotification(task, requireContext())
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        Log.d("TaskUpdate", "Task updated with ID: $taskId")

                        // Handle new attachments
                        if (selectedUris.isNotEmpty()) {
                            taskId?.let { id ->
                                handleFileSelections(
                                    id, selectedUris, taskViewModel, requireContext()
                                ) { success ->
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            if (success) "Attachments successfully added." else "Failed to add attachments.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    } catch (e: CancellationException) {
                        Log.e("TaskUpdate", "Coroutine was cancelled", e)
                    } catch (e: Exception) {
                        Log.e("TaskUpdate", "Error updating task or managing attachments", e)
                    }
                }
                return true
            } ?: showToast("Task data is missing")
        } ?: showToast("Invalid date or time")
        return false
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAttachmentList() {
        attachmentList.addAll(attachments)
        attachmentAdapter.notifyDataSetChanged()
    }
}
