package com.jonathan.trace.study.trace.coketlist.room

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "image_table", foreignKeys = [ForeignKey(entity=Note::class, parentColumns = ["id"], childColumns = ["noteId"], onDelete = CASCADE)])
data class Image(
    @PrimaryKey
    var path: String,   /** file name (NOT path!) **/
    var noteId: Int
)