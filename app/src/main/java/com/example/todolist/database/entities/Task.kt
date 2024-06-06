package com.example.todolist.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_table",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val creationTime: Long,
    val completionTime: Long,
    val isCompleted: Boolean,
    val notificationEnabled: Boolean,
    val categoryId: Int?,
    val hasAttachments: Boolean
)
