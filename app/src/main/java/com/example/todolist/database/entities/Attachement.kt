package com.example.todolist.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachment_table",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val filePath: String,
    val fileType: String
)
