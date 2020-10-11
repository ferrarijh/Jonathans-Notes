package com.jonathan.trace.study.trace.coketlist.room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jonathan.trace.study.trace.coketlist.cache.NotesCache

class NoteRepository(private val noteDao: NoteDao){
    fun getAllNotes(): LiveData<List<Note>> {
        Log.d("", "getAllNotes() called.")
        return noteDao.getAllNotes()
    }
    fun getAllNotesByCreated(): LiveData<List<Note>>{
        Log.d("", "getAllNotesByCreated() called.")
        return noteDao.getAllNotesByCreated()
    }
    fun getAllNotesByTitle(): LiveData<List<Note>>{
        Log.d("", "getAllNotesByTitle() called.")
        return noteDao.getAllNotesByTitle()
    }
    fun getAllNotesByBody(): LiveData<List<Note>> = noteDao.getAllNotesByBody()
    fun getAllNotesByColor() = noteDao.getAllNotesByColor()

    fun getAllTrashNotes(): LiveData<List<Note>> = noteDao.getAllTrashNotes()
    fun getAllPrivateNotes(): LiveData<List<Note>> = noteDao.getAllPrivateNotes()


    suspend fun addNote(note: Note){
        noteDao.addNote(note)
    }

    suspend fun update(note: Note){
        noteDao.update(note)
    }

    fun delete(note: Note){
        noteDao.delete(note)
    }

    fun deleteAll(){
        noteDao.deleteAll()
    }
}