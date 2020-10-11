package com.jonathan.trace.study.trace.coketlist.cache

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jonathan.trace.study.trace.coketlist.room.Note

class NotesCache(){
    companion object{
        var notes: LiveData<MutableList<Note>>? = null
    }
}