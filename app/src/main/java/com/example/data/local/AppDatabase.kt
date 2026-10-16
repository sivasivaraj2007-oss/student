package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSetting
import com.example.data.model.AttendanceAuditLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.Faculty
import com.example.data.model.FacultySubjectAssignment
import com.example.data.model.Student
import com.example.data.model.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Student::class,
        Subject::class,
        Faculty::class,
        FacultySubjectAssignment::class,
        AttendanceSession::class,
        AttendanceRecord::class,
        AttendanceAuditLog::class,
        AppSetting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun facultyDao(): FacultyDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iie_attendance_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate on database creation
                            INSTANCE?.let { appDb ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    DataSeeder.seedDatabaseIfEmpty(appDb)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Also ensure initial seed check on app launch
                CoroutineScope(Dispatchers.IO).launch {
                    DataSeeder.seedDatabaseIfEmpty(instance)
                }

                instance
            }
        }
    }
}
