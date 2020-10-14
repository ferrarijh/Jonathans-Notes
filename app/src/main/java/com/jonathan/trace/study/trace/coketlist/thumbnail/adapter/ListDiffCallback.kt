package com.jonathan.trace.study.trace.coketlist.thumbnail.adapter

import androidx.recyclerview.widget.DiffUtil
import com.jonathan.trace.study.trace.coketlist.room.Note

class ListDiffCallback(
    private val oldList: List<Note>,
    private val newList: List<Note>
): DiffUtil.Callback(){
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}