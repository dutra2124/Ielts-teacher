package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ModuleEntity::class,
        VocabularyEntity::class,
        SentenceDrillEntity::class,
        ExamEntity::class,
        PersonalMaterialEntity::class,
        AudioBlobEntity::class,
        StudyLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ApexLocalDatabase : RoomDatabase() {
    abstract fun localDao(): ApexLocalDao

    companion object {
        @Volatile
        private var INSTANCE: ApexLocalDatabase? = null

        fun getInstance(context: Context): ApexLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ApexLocalDatabase::class.java,
                    "apex_ielts_offline_hybrid.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
