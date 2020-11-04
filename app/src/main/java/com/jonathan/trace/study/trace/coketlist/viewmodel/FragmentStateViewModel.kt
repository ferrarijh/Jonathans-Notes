package com.jonathan.trace.study.trace.coketlist.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Image
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteRepository
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

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
    fun processImageUri(uri: Uri?, parent: Activity, noteId: Int, isNewAndNotSaved: Boolean){
        CoroutineScope(Dispatchers.Default).launch {
            if (uri == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(parent, "Something went wrong - null uri :(", Toast.LENGTH_SHORT).show()
                }
                this.cancel()
            }

            val scaledBitmap = parseScaledBitmap(uri!!, parent)
            val fileName = saveToStorage(scaledBitmap, noteId, parent)

            if (!isNewAndNotSaved) {
                val imgProfile = Image(fileName, noteId)
                addImages(listOf(imgProfile))
            } else {
                imagesForNewNote.value!!.add(Image(fileName, 0))
                withContext(Dispatchers.Main) {
                    imagesForNewNote.value = imagesForNewNote.value!!
                }
            }
        }
    }

    private fun saveToStorage(bitmap: Bitmap, noteId: Int, parent: Activity): String{
        val filesDir = parent.filesDir
        val dir = File(filesDir.absolutePath + "/Pictures/$noteId")
        dir.mkdirs()

        val fileName = String.format("%d.jpeg", System.currentTimeMillis())
        val outputFile = File(dir, fileName)
        var oStream: FileOutputStream? = null
        try{
            oStream = FileOutputStream(outputFile)
        }catch (e: Exception){
            e.printStackTrace()
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream)

        return fileName
    }

    private fun parseScaledBitmap(uri: Uri, parent: Activity): Bitmap{

        val contentResolver = parent.contentResolver
        var bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            (ImageDecoder.createSource(contentResolver, uri)).let { ImageDecoder.decodeBitmap(it) }
            else MediaStore.Images.Media.getBitmap(contentResolver, uri)

        val w = bitmap.width
        val h = bitmap.height
        val max = if(w>h) w else h
        val scaleFactor = if(max > 1000){
            1000 / max.toFloat()
        }else 1f

        if (scaleFactor != 1f)
            bitmap = Bitmap.createScaledBitmap(
                bitmap, (w * scaleFactor).toInt(), (h * scaleFactor).toInt(), true
            )

        return bitmap
    }

    fun deleteImageCallback(parent: Activity, curItemPos: Int){
        val imagePointed = imagePointed!!
        val isNewAndNotSaved = imagePointed.noteId == 0

        if(isNewAndNotSaved){
            val newImagesLive = imagesForNewNote
            newImagesLive.value!!.remove(imagePointed)
            newImagesLive.value = newImagesLive.value!!
            setPageIndicator(newImagesLive.value!!.size, curItemPos)
        }else {
            deleteImage(imagePointed)
        }
        val fullDir = parent.filesDir.absolutePath + "/Pictures/${imagePointed.noteId}"
        val file = File(fullDir, imagePointed.name)

        if(file.exists()){
            if(file.delete())
                Log.d("", "file deleted: ${imagePointed.name}")
            else
                Log.d("", "deletion failed: ${imagePointed.name}")
        }

    }

    fun deleteNewImages(parent: Activity){
        val fullPath = parent.filesDir.absolutePath+"/Pictures/0"
        imagesForNewNote.value!!.forEach{
            val fileName = it.name
            val file = File(fullPath, fileName)
            if(file.exists()){
                if(file.delete())
                    Log.d("", "file deleted: $fileName")
                else
                    Log.d("", "deletion failed: $fileName")
            }
        }
    }

    fun setPageIndicator(size: Int, curItemPos: Int){
        if(size == 0)
            pageIndicator.value = ""
        else {
            var indicator = ""
            for(i in 0 until size){
                indicator += if(i == curItemPos) "●" else "○"
            }
            pageIndicator.value = indicator
        }
    }

    /** call ONLY when saving unsaved new note**/
    fun saveNewImagesToDb(noteId: Int, parent: Activity): LiveData<List<Image>>{
        val newImages = imagesForNewNote.value!!
        if(newImages.isNotEmpty()) {
            newImages.forEach {
                it.noteId = noteId
            }

            CoroutineScope(Dispatchers.IO).launch {
                addImages(newImages)
            }
            val path = parent.filesDir.absolutePath + "/Pictures"
            val oldDir = File(path, "0")
            val newDir = File(path, noteId.toString())
            if (oldDir.renameTo(newDir))
                Log.d("", "SUCCESS: change dir from 0 to $noteId")
            else
                throw Exception("FAILED: change dir name from 0 to $noteId")
        }
        setImages(noteId)
        return repository.getImages(noteId)
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