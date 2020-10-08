package com.jonathan.trace.study.trace.coketlist.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface NoteDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNote(note: Note)

    @Query("SELECT * FROM note_table WHERE trash = 0 AND pw IS NULL ORDER BY dateTimeModified DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE trash = 0 AND pw IS NULL ORDER BY dateTimeCreated DESC")
    fun getAllNotesByCreated(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE trash = 0 AND pw IS NULL ORDER BY title DESC")
    fun getAllNotesByTitle(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE trash = 0 AND pw IS NULL ORDER BY body DESC")
    fun getAllNotesByBody(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE trash = 1 ORDER BY dateTimeModified DESC")
    fun getAllTrashNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE pw IS NOT NULL ORDER BY dateTimeModified DESC")
    fun getAllPrivateNotes(): LiveData<List<Note>>


    @Insert
    suspend fun addMultipleNotes(vararg note: Note)

    @Update
    suspend fun update(entity: Note)

    @Delete
    fun delete(entity: Note)

}