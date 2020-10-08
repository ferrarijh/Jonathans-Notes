package com.jonathan.trace.study.trace.coketlist.room

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel (app: Application): AndroidViewModel(app){
    private val repository: NoteRepository

    //TODO("bug - val getAllNotes error")
    /*
    val getAllNotes: LiveData<List<Note>>
    val getAllTrashNotes: LiveData<List<Note>>
    val getAllPrivateNotes: LiveData<List<Note>>
    val getAllNotesByCreated: LiveData<List<Note>>
    val getAllNotesByTitle: LiveData<List<Note>>
    val getAllNotesByBody: LiveData<List<Note>>
     */

    fun getAllNotes() = repository.getAllNotes()
    fun getAllTrashNotes() = repository.getAllTrashNotes()
    fun getAllPrivateNotes() = repository.getAllPrivateNotes()
    fun getAllNotesByCreated() = repository.getAllNotesByCreated()
    fun getAllNotesByTitle() = repository.getAllNotesByTitle()
    fun getAllNotesByBody() = repository.getAllNotesByBody()

    val sortState = MutableLiveData<SortState>(SortState.MODIFIED)


    init{
        val noteDao = NoteDatabase.getDatabase(app).getNoteDao()
        repository = NoteRepository(noteDao)
/*
        //getAllNotes = repository.getAllNotes()
        getAllTrashNotes = repository.getAllTrashNotes()
        getAllPrivateNotes = repository.getAllPrivateNotes()
        getAllNotesByCreated = repository.getAllNotesByCreated()
        getAllNotesByTitle = repository.getAllNotesByTitle()
        getAllNotesByBody = repository.getAllNotesByBody()

 */
    }

    fun addNote(note: Note){
        viewModelScope.launch(Dispatchers.IO){
            repository.addNote(note)
        }
    }

    fun delete(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(note)
        }
    }

    fun update(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(note)
        }
    }

    enum class SortState{ MODIFIED, CREATED, TITLE, BODY}
}