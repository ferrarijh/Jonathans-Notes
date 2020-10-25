package com.jonathan.trace.study.trace.coketlist.thumbnail.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder.ThumbnailViewHolder
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder.ThumbnailPrivateViewHolder
import com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder.ThumbnailTrashViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThumbnailAdapter(
    private var thumbnails: MutableList<Note> = mutableListOf<Note>(),
    private val layoutResId: Int,
    private val viewHolderType: Int,
    private val listener: ThumbnailAdapterListener,
    private val longListener: ThumbnailAdapterLongListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object{
        const val HOME = 1
        const val PRIVATE = 2
        const val TRASH = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return when(viewHolderType) {
            HOME -> ThumbnailViewHolder(view)
            PRIVATE -> ThumbnailPrivateViewHolder(view)
            TRASH-> ThumbnailTrashViewHolder(view)
            else -> throw Exception("Unknown ViewHolder type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolderType){
            HOME->(holder as ThumbnailViewHolder).bind(thumbnails[position], position, listener, longListener)
            PRIVATE->(holder as ThumbnailPrivateViewHolder).bind(thumbnails[position], position, listener, longListener)
            TRASH-> (holder as ThumbnailTrashViewHolder).bind(thumbnails[position], position, listener, longListener)
            else -> throw Exception("Unknown ViewHolder type")
        }
    }

    override fun getItemCount(): Int = thumbnails.size

    interface ThumbnailAdapterListener{
        fun <T> onClickItem(item: T)
    }

    interface ThumbnailAdapterLongListener{
        fun <T> onLongClickItem(item: T)
    }

    fun updateList(newList: List<Note>) {
        val callback = ListDiffCallback(thumbnails, newList)
        var diffResult : DiffUtil.DiffResult? = null
        CoroutineScope(Dispatchers.Default).launch {
            diffResult = DiffUtil.calculateDiff(callback)    //this is 'expensive', so call it in worker thread
        }.invokeOnCompletion {
            CoroutineScope(Dispatchers.Main).launch{
                //thumbnails = newList    //this will NOT dynamically update search fragment
                thumbnails.clear()
                thumbnails.addAll(newList)
                diffResult?.dispatchUpdatesTo(this@ThumbnailAdapter)
            }
        }
    }

}