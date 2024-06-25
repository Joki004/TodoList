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

@Database(
    entities = [Task::class, Attachment::class, Category::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: TaskDatabase? = null



        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new temporary table with the updated schema including foreign key and default values
                db.execSQL("""
            CREATE TABLE attachment_table_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                taskId INTEGER NOT NULL,
                filePath TEXT NOT NULL,
                fileType TEXT NOT NULL,
                uri TEXT NOT NULL,
                contentProviderAuthority TEXT NOT NULL DEFAULT '',
                FOREIGN KEY (taskId) REFERENCES task_table (id) ON DELETE CASCADE
            )
        """)

                // Copy the data from the old table to the new table
                db.execSQL("""
            INSERT INTO attachment_table_new (id, taskId, filePath, fileType, uri, contentProviderAuthority)
            SELECT id, taskId, filePath, fileType, uri, contentProviderAuthority
            FROM attachment_table
        """)

                // Drop the old table
                db.execSQL("DROP TABLE attachment_table")

                // Rename the new table to the old table name
                db.execSQL("ALTER TABLE attachment_table_new RENAME TO attachment_table")
            }
        }

        // Helper function to get or create an instance of the database
        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, TaskDatabase::class.java, "task_database")
                    .fallbackToDestructiveMigration()
                    .build()

            }
        }
    }
}
