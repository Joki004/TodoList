package com.example.todolist.permissions
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns


class PickFile(private val activity: Activity) {

    companion object {
        const val PICK_FILE_REQUEST_CODE = 1002
    }

    fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        activity.startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): List<String> {
        val filePaths = mutableListOf<String>()
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let { intentData ->
                if (intentData.clipData != null) {
                    val clipData = intentData.clipData
                    for (i in 0 until clipData!!.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val filePath = getFilePathFromUri(uri)
                        filePath?.let { filePaths.add(it) }
                    }
                } else if (intentData.data != null) {
                    val uri = intentData.data
                    val filePath = getFilePathFromUri(uri)
                    filePath?.let { filePaths.add(it) }
                }
            }
        }
        return filePaths
    }

    private fun getFilePathFromUri(uri: Uri?): String? {
        uri?.let {
            val cursor: Cursor? = activity.contentResolver.query(it, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    return it.getString(columnIndex)
                }
            }
        }
        return null
    }
}