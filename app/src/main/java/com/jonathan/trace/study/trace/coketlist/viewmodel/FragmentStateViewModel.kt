package com.jonathan.trace.study.trace.coketlist.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathan.trace.study.trace.coketlist.room.Note

class FragmentStateViewModel: ViewModel(){

    //for EditNoteFragment
    val isPaletteOpen = MutableLiveData<Boolean>()

    //for SearchFragment
    val curNotes = MutableLiveData<MutableList<Note>>()
    private var _curNotesAll: LiveData<List<Note>>? = null
    val curNotesAll
        get() = _curNotesAll

    init{
        isPaletteOpen.value = false
        Log.d("", "ViewModel() initialized.")

        curNotes.value = mutableListOf()
    }

    fun setCurNotesAll(liveData: LiveData<List<Note>>){
        _curNotesAll = liveData
    }
}