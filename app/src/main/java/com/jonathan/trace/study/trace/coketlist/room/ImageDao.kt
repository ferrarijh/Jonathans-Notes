package com.jonathan.trace.study.trace.coketlist.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addImages(images: List<Image>)

    @Query("SELECT * FROM image_table WHERE noteId = :id ORDER BY name ASC")
    fun getImages(id: Int): LiveData<List<Image>>

    @Delete
    fun delete(entity: Image)
}