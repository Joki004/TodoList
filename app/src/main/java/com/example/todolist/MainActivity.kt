package com.example.todolist


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.todolist.adapter.AttachmentAdapter.Companion.attachmentGlobal

import com.example.todolist.filepicker.PickFile
import com.example.todolist.helpers.getUriFromDatabase
import com.example.todolist.helpers.openFileFromUri
import com.example.todolist.permissions.AskPermissions
import com.example.todolist.viewmodel.TaskViewModel
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var askPermissions: AskPermissions
    private lateinit var pickFile: PickFile
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pickFile = PickFile(this)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.editTaskFragment,
                R.id.allTasksFragment,
                R.id.DailyTasksFragment
            ), drawerLayout
        )
        navView.setupWithNavController(navController)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val filePaths = pickFile.handleActivityResult(requestCode, resultCode, data)
        // Save the file paths or handle them as needed
    }

    companion object {
        const val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 98
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val uri =
                    attachmentGlobal?.let { getUriFromDatabase(it) }
                if (uri != null) {
                    attachmentGlobal?.let { openFileFromUri(this, it) }
                }
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
