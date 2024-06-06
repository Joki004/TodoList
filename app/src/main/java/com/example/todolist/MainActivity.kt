package com.example.todolist

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolist.permissions.AskPermissions
import com.example.todolist.permissions.PickFile

class MainActivity : AppCompatActivity() {
    private lateinit var askPermissions: AskPermissions
    private lateinit var pickFile: PickFile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        askPermissions = AskPermissions(this, this)
        pickFile = PickFile(this)
        // Request permissions
        askPermissions.requestPermissions()

        // Open file picker
        pickFile.openFilePicker()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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