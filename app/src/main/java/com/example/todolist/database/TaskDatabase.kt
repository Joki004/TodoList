package com.example.todolist.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todolist.database.dao.TaskDao
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
import com.example.todolist.database.entities.Task

@Database(entities = [Task::class, Attachment::class, Category::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    companion object {
        @Volatile
        private var instance: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}