package com.example.todolist.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.todolist.adapter.AttachmentAdapter


class AskPermissions(private val context: Context, private val activity: Activity) {
    companion object {
        const val PERMISSIONS_REQUEST_CODE = 100

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    }



    fun checkAndRequestPermissions() {
        if (checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                AttachmentAdapter.READ_EXTERNAL_STORAGE_PERMISSION_CODE)
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionsToRequest = REQUIRED_PERMISSIONS.filter {
                checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), AttachmentAdapter.READ_EXTERNAL_STORAGE_PERMISSION_CODE)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun showRationaleAndRequestPermissions() {
        ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
    }

    private lateinit var storageActivityResultLauncher: ActivityResultLauncher<Intent>


    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (R) and above
            Environment.isExternalStorageManager()
        } else {
            // Below Android 11
            val write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun registerActivityResultLauncher(activityResultLauncher: ActivityResultLauncher<Intent>) {
        storageActivityResultLauncher = activityResultLauncher
    }
}
