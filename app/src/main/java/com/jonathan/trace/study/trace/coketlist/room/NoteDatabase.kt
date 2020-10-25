package com.jonathan.trace.study.trace.coketlist.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Note::class, Image::class], version = 6, exportSchema = false)
abstract class NoteDatabase: RoomDatabase(){
    abstract fun getNoteDao(): NoteDao
    abstract fun getImageDao(): ImageDao

    companion object{
        @Volatile
        private var INSTANCE: NoteDatabase? = null

//        private val MIGRATION_4_5 = object : Migration(4,5) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE note_table ADD COLUMN pw VARCHAR")
//            }
//        }

        private val MIGRATION_5_6 = object : Migration(5,6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS image_table(noteId INTEGER NOT NULL, path VARCHAR NOT NULL, PRIMARY KEY(path), FOREIGN KEY(noteId) REFERENCES note_table(id) ON DELETE CASCADE)"
                )
            }
        }

        fun getDatabase(context: Context): NoteDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null)
                return tempInstance

            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).addMigrations(MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}