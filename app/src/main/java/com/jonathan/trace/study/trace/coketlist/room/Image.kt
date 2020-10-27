package com.jonathan.trace.study.trace.coketlist.room

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "image_table", foreignKeys = [ForeignKey(entity=Note::class, parentColumns = ["id"], childColumns = ["noteId"], onDelete = CASCADE)])
data class Image(
    @PrimaryKey
    var name: String,   /** file name (NOT path!) **/

    @ColumnInfo(index = true)
    var noteId: Int
)