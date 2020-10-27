package com.jonathan.trace.study.trace.coketlist.room

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    fun getAllNotes() = repository.getAllNotes()
    fun getAllTrashNotes() = repository.getAllTrashNotes()
    fun getAllPrivateNotes() = repository.getAllPrivateNotes()
    fun getAllNotesByCreated() = repository.getAllNotesByCreated()
    fun getAllNotesByTitle() = repository.getAllNotesByTitle()
    fun getAllNotesByBody() = repository.getAllNotesByBody()
    fun getAllNotesByColor() = repository.getAllNotesByColor()

    fun getIdLastSaved(): LiveData<Int> = repository.getIdLastSaved()

    init {

        val noteDao = NoteDatabase.getDatabase(app).getNoteDao()
        val imageDao = NoteDatabase.getDatabase(app).getImageDao()
        repository.setNoteDao(noteDao)
        repository.setImageDao(imageDao)
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
            repository.addNote(note)
        }
    }

    fun delete(note: Note) {
        val fullDir = filesPath + "/Pictures/${note.id}"
        val file = File(fullDir)
        file.deleteRecursively()
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(note)
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
            repository.deleteAllTrashed()
        }
    }

    fun update(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(note)
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