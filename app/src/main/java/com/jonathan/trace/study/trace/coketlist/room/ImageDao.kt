package com.jonathan.trace.study.trace.coketlist.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addImages(vararg image: Image)

    @Query("SELECT * FROM image_table WHERE noteId = :id ORDER BY path DESC")
    fun getImages(id: Int): LiveData<List<Image>>

    @Delete
    suspend fun delete(entity: Image)
}