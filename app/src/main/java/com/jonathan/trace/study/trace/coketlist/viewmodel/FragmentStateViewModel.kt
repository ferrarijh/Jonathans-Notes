package com.jonathan.trace.study.trace.coketlist.viewmodel

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Note

class FragmentStateViewModel: ViewModel(){

    //for EditNoteFragment
    val isPaletteOpen = MutableLiveData<Boolean>()
    val colorSelected = MutableLiveData<String>()
    init{
        isPaletteOpen.value = false
    }

    //for SearchFragment
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