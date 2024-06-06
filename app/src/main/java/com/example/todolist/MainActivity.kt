package com.example.todolist

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.database.entities.Task
import com.example.todolist.permissions.AskPermissions
import com.example.todolist.filepicker.PickFile
import com.example.todolist.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var askPermissions: AskPermissions
    private lateinit var pickFile: PickFile
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermissions = AskPermissions(this, this)
        pickFile = PickFile(this)

        // Request permissions
        // askPermissions.requestPermissions()

        // Open file picker
        // pickFile.openFilePicker()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val adapter = TaskAdapter()
        recyclerView.adapter = adapter

        taskViewModel.allTasks.observe(this, Observer { tasks ->
            tasks?.let {
                adapter.submitList(it)
            }
        })

        // Example: Insert a new task
        val newTask = Task(
            title = "Example Task",
            description = "This is an example task",
            creationTime = System.currentTimeMillis(),
            completionTime = System.currentTimeMillis() + 100000,
            isCompleted = false,
            notificationEnabled = false,
            categoryId = null,
            hasAttachments = false
        )
        taskViewModel.insert(newTask)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val filePaths = pickFile.handleActivityResult(requestCode, resultCode, data)
        // Save the file paths or handle them as needed
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AskPermissions.PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All requested permissions have been granted
            } else {
                // Handle the case where permissions are not granted
            }
        }
    }
}
