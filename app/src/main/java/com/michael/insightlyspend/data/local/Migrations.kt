package com.michael.insightlyspend.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object InsightlyDatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE categories ADD COLUMN nameEn TEXT")
            db.execSQL("ALTER TABLE categories ADD COLUMN nameAr TEXT")
        }
    }
}
