package com.jonathan.trace.study.trace.coketlist.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var title: String,
    var body: String,
    var dateTimeCreated: String,
    var dateTimeModified: String,
    var trash: Int = 0,
    var color: String = "#FFFFFF",
    var pw: String? = null
): Serializable