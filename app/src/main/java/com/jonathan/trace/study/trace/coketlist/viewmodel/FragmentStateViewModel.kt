package com.jonathan.trace.study.trace.coketlist.viewmodel

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Image
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentStateViewModel: ViewModel(){

    private val repository = NoteRepository

    /**for EditNoteFragment - palette**/
    val isPaletteOpen = MutableLiveData<Boolean>()
    val colorSelected = MutableLiveData<String>()
    init{
        isPaletteOpen.value = false
    }

    /**for EditNoteFragment - image viewer (ImageViewFragment)**/
    private var _images: LiveData<List<Image>>? = null
    val images: LiveData<List<Image>>?
        get() = _images

    val imagesForNewNote = MutableLiveData<MutableList<Image>>(mutableListOf())

    suspend fun addImages(images: List<Image>) = repository.addImages(images)

    fun setImages(noteId: Int){
        _images = _images ?: repository.getImages(noteId)// as MutableLiveData<List<Image>>
    }

    fun deleteImage(img: Image){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteImage(img)
        }
    }

    var imagePointed: Image? = null
    val pageIndicator = MutableLiveData("")

    /**for EditNoteFragment - image data processing**/
    private fun processImageUri(uri: Uri?, parent: Activity, isNewAndNotSaved: Boolean){
    }

    /**for SearchFragment**/
    val curNotes = MutableLiveData<MutableList<Note>>()
    private var _curNotesAll: LiveData<List<Note>>? = null
    val curNotesAll
        get() = _curNotesAll

    init{
        curNotes.value = mutableListOf()
    }

    fun setCurNotesAll(liveData: LiveData<List<Note>>){
        _curNotesAll = liveData
    }
}