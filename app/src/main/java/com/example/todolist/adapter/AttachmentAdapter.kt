package com.example.todolist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.database.entities.Attachment

class AttachmentAdapter(private val attachments: MutableList<Attachment>, private val onDeleteClick: (Attachment) -> Unit) :
    RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    // ViewHolder inner class
    inner class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val attachmentName: TextView = itemView.findViewById(R.id.attachment_name)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.ic_delete)

        fun bind(attachment: Attachment) {
            attachmentName.text = attachment.filePath
            deleteIcon.setOnClickListener {
                onDeleteClick(attachment)
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
