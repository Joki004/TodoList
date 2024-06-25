package com.example.todolist.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.database.entities.Attachment
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.jar.Manifest
import android.content.ActivityNotFoundException as ActivityNotFoundException1


class AttachmentAdapter(
    private val attachments: MutableList<Attachment>,
    private val onDeleteClick: (Attachment) -> Unit,
    private val onAttachmentClick: (Attachment) -> Unit,
) :
    RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    inner class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val attachmentName: TextView = itemView.findViewById(R.id.attachment_name)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.ic_delete)

        fun bind(attachment: Attachment) {
            attachmentName.text = attachment.filePath
            deleteIcon.setOnClickListener {
                onDeleteClick(attachment)
            }

            itemView.setOnClickListener {
                onAttachmentClick(attachment)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.attachment, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val attachment = attachments[position]
        holder.bind(attachment)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }


}
