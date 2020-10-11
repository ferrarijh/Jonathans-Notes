package com.jonathan.trace.study.trace.coketlist.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathan.trace.study.trace.coketlist.room.Note

class FragmentStateViewModel: ViewModel(){
    val isPaletteOpen = MutableLiveData<Boolean>()
    val curNotes = mutableListOf<Note>()
    var curNotesAll = listOf<Note>()

    init{
        isPaletteOpen.value = false
        Log.d("", "ViewModel() initialized.")
    }
}