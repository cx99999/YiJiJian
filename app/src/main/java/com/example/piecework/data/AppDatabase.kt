package com.example.piecework.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProductEntity::class,
        WorkRecordEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun workRecordDao(): WorkRecordDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE products SET unitPriceCents = unitPriceCents * 10")
                db.execSQL(
                    """
                    UPDATE work_records
                    SET subsidyCents = subsidyCents * 10,
                        deductionCents = deductionCents * 10
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "piecework.db"
                )
                    .addMigrations(Migration1To2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
