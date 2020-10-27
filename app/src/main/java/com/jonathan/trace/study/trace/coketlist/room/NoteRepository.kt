package com.jonathan.trace.study.trace.coketlist.room

import androidx.lifecycle.LiveData

object NoteRepository{
    private var noteDao: NoteDao? = null
    private var imageDao: ImageDao? = null

    fun setNoteDao(nDao: NoteDao){
        noteDao = noteDao ?: nDao
    }
    fun setImageDao(iDao: ImageDao){
        imageDao = imageDao ?: iDao
    }

    fun getAllNotes(): LiveData<List<Note>> = noteDao!!.getAllNotes()
    fun getAllNotesByCreated(): LiveData<List<Note>> = noteDao!!.getAllNotesByCreated()
    fun getAllNotesByTitle(): LiveData<List<Note>> = noteDao!!.getAllNotesByTitle()
    fun getAllNotesByBody(): LiveData<List<Note>> = noteDao!!.getAllNotesByBody()
    fun getAllNotesByColor() = noteDao!!.getAllNotesByColor()

    fun getAllTrashNotes(): LiveData<List<Note>> = noteDao!!.getAllTrashNotes()
    fun getAllPrivateNotes(): LiveData<List<Note>> = noteDao!!.getAllPrivateNotes()

    fun getIdLastSaved(): LiveData<Int> = noteDao!!.getIdLastSaved()

    suspend fun addNote(note: Note){
        noteDao!!.addNote(note)
    }

    suspend fun update(note: Note){
        noteDao!!.update(note)
    }

    fun delete(note: Note){
        noteDao!!.delete(note)
    }

    fun deleteAllTrashed(){
        noteDao!!.deleteAllTrashed()
    }

    //imageDao
    fun getImages(noteId : Int): LiveData<List<Image>> = imageDao!!.getImages(noteId)
    fun deleteImage(img: Image) = imageDao!!.delete(img)
    suspend fun addImages(images: List<Image>) = imageDao!!.addImages(images)
}