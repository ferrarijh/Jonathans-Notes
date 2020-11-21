package com.jonathan.trace.study.trace.coketlist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteDatabase
import com.jonathan.trace.study.trace.coketlist.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class NoteViewModel (app: Application): AndroidViewModel(app) {
    private val filesPath = app.filesDir.absolutePath

    /**
     * ROOM components
     * **/
    private val repository: NoteRepository = NoteRepository

    //TODO("val getAllNotes gives error")
    /*
    val getAllNotes: LiveData<List<Note>>
    val getAllTrashNotes: LiveData<List<Note>>
    val getAllPrivateNotes: LiveData<List<Note>>
    val getAllNotesByCreated: LiveData<List<Note>>
    val getAllNotesByTitle: LiveData<List<Note>>
    val getAllNotesByBody: LiveData<List<Note>>
     */

    fun getAllNotes() = NoteRepository.getAllNotes()
    fun getAllTrashNotes() = NoteRepository.getAllTrashNotes()
    fun getAllPrivateNotes() = NoteRepository.getAllPrivateNotes()
    fun getAllNotesByCreated() = NoteRepository.getAllNotesByCreated()
    fun getAllNotesByTitle() = NoteRepository.getAllNotesByTitle()
    fun getAllNotesByBody() = NoteRepository.getAllNotesByBody()
    fun getAllNotesByColor() = NoteRepository.getAllNotesByColor()

    fun getIdLastSaved(): LiveData<Int> = NoteRepository.getIdLastSaved()

    init {

        val noteDao = NoteDatabase.getDatabase(app).getNoteDao()
        val imageDao = NoteDatabase.getDatabase(app).getImageDao()
        NoteRepository.setNoteDao(noteDao)
        NoteRepository.setImageDao(imageDao)
        Log.d("", "NoteViewModel initialized!")
/*
        //getAllNotes = repository.getAllNotes()
        getAllTrashNotes = repository.getAllTrashNotes()
        getAllPrivateNotes = repository.getAllPrivateNotes()
        getAllNotesByCreated = repository.getAllNotesByCreated()
        getAllNotesByTitle = repository.getAllNotesByTitle()
        getAllNotesByBody = repository.getAllNotesByBody()
 */
    }

    fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            NoteRepository.addNote(note)
        }
    }

    fun delete(note: Note) {
        val fullDir = filesPath + "/Pictures/${note.id}"
        val file = File(fullDir)
        file.deleteRecursively()
        viewModelScope.launch(Dispatchers.IO) {
            NoteRepository.delete(note)
        }
    }

    val allTrashed: LiveData<List<Note>>
    init{
        allTrashed = getAllTrashNotes()
    }
    fun deleteAllTrashed(){
        viewModelScope.launch(Dispatchers.IO){
            allTrashed.value?.let{
                it.forEach{
                    val fullDir = filesPath + "/Pictures/${it.id}"
                    val file = File(fullDir)
                    file.deleteRecursively()
                }
            }
            NoteRepository.deleteAllTrashed()
        }
    }

    fun update(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            NoteRepository.update(note)
        }
    }

    /**
     * selected note on click
     */
    private var notePointed: Pair<Int, Note>? = null
    fun setNotePointed(pos: Int, note: Note){
        notePointed = Pair(pos, note)
    }
    fun getNotePointed() = notePointed

    /**
     * for multi selection
     * **/
    companion object{
        const val ON = true
        const val OFF = false

        const val MODIFIED = 1
        const val CREATED = 2
        const val TITLE = 3
        const val BODY = 4
        const val COLOR = 5
    }

    private val _prevSort = MutableLiveData<Int>()
    val prevSort
        get() = _prevSort
    fun setPrevSort(sortBy: Int){
        _prevSort.value = sortBy
    }

    val selMode = MutableLiveData<Boolean>()
    val selected = mutableMapOf<Int, Note?>()

    init {
        _prevSort.value = MODIFIED
        selMode.value = false
    }

    fun getSelMode(): Boolean = selMode.value!!
    fun setSelMode(mode: Boolean){  //update livedata & clear selected IF new mode is OFF
        selMode.value = mode
        if(selMode.value == OFF){
            selected.clear()
        }
    }

    fun toggleSelectedWith(pos: Int, note: Note){
        if(!selMode.value!!)
            throw Exception("toggleSelectedWith() should NOT be called when selMode.value == OFF")

        if(selected[pos] == null)
            selected[pos] = note
        else
            selected.remove(pos)
    }

    fun deleteSelectedAndUpdateWith(): LiveData<List<Note>>{
        if(!selMode.value!!)
            throw Exception("deletedSelected() should NOT be called when selMode.value == OFF")

        val iter = selected.iterator()
        while(iter.hasNext()){
            val note = iter.next().value!!
            note.trash = 1
            update(note)
            iter.remove()
        }
        val newListLive = when(prevSort.value!!){
            MODIFIED -> getAllNotes()
            CREATED -> getAllNotesByCreated()
            TITLE -> getAllNotesByTitle()
            BODY -> getAllNotesByBody()
            COLOR -> getAllNotesByColor()
            else -> getAllNotes()
        }

        //TODO("updating adapter should be ASYNCHRONOUS!!")
        //adapter.updateList(newListLive.value!!)

        setSelMode(OFF)
        return newListLive
    }

}