package com.example.todolist.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.todolist.R
import com.example.todolist.ui.EditTaskActivity
import com.example.todolist.ui.EditTaskFragment

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("taskId", -1)
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task Reminder"

        if (taskId != -1) {
            createNotificationChannel(context)

            val resultIntent = Intent(context, EditTaskActivity::class.java).apply {
                putExtra("taskId", taskId.toString())
            }

            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val notification = NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(taskTitle)
                .setContentText("Task is due soon")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build()

            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                try {
                    with(NotificationManagerCompat.from(context)) {
                        notify(taskId, notification)
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(context, "Notification permission is required", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Notification permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminder"
            val descriptionText = "Channel for task reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}