package com.example.todolist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todolist.database.dao.TaskDao
import com.example.todolist.database.entities.Attachment
import com.example.todolist.database.entities.Category
import com.example.todolist.database.entities.Converters
import com.example.todolist.database.entities.Task

@Database(entities = [Task::class, Attachment::class, Category::class], version = 1,exportSchema = false)
@TypeConverters(Converters::class)
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
                ) .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }

        }
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop the table
                db.execSQL("DROP TABLE IF EXISTS task_database")
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migration logic, for example:
                // database.execSQL("ALTER TABLE task_table ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0")
                // For our example, if you change the type of a column, you might need to create a new table and copy the data
                db.execSQL("CREATE TABLE new_task_table (id INTEGER PRIMARY KEY NOT NULL, title TEXT NOT NULL, description TEXT, creationTime INTEGER NOT NULL, completionTime INTEGER NOT NULL, isCompleted INTEGER NOT NULL, notificationEnabled INTEGER NOT NULL, categoryId INTEGER, hasAttachments INTEGER NOT NULL)")
                db.execSQL("INSERT INTO new_task_table (id, title, description, creationTime, completionTime, isCompleted, notificationEnabled, categoryId, hasAttachments) SELECT id, title, description, creationTime, completionTime, isCompleted, notificationEnabled, categoryId, hasAttachments FROM task_table")
                db.execSQL("DROP TABLE task_table")
                db.execSQL("ALTER TABLE new_task_table RENAME TO task_table")
            }
        }
    }
}
