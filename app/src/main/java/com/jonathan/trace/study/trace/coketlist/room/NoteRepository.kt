package com.jonathan.trace.study.trace.coketlist.room

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao){
    fun getAllNotes(): LiveData<List<Note>> = noteDao.getAllNotes()
    fun getAllNotesByCreated(): LiveData<List<Note>> = noteDao.getAllNotesByCreated()
    fun getAllNotesByTitle(): LiveData<List<Note>> = noteDao.getAllNotesByTitle()
    fun getAllNotesByBody(): LiveData<List<Note>> = noteDao.getAllNotesByBody()
    fun getAllNotesByColor() = noteDao.getAllNotesByColor()

    fun getAllTrashNotes(): LiveData<List<Note>> = noteDao.getAllTrashNotes()
    fun getAllPrivateNotes(): LiveData<List<Note>> = noteDao.getAllPrivateNotes()

    fun getIdLastSaved(): LiveData<Int> = noteDao.getIdLastSaved()

    suspend fun addNote(note: Note){
        noteDao.addNote(note)
    }

    suspend fun update(note: Note){
        noteDao.update(note)
    }

    fun delete(note: Note){
        noteDao.delete(note)
    }

    fun deleteAllTrashed(){
        noteDao.deleteAllTrashed()
    }
}