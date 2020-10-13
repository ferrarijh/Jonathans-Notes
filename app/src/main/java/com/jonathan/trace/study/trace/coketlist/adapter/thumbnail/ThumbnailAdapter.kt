package com.jonathan.trace.study.trace.coketlist.adapter.thumbnail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThumbnailAdapter(
    private var thumbnails: MutableList<Note> = mutableListOf<Note>(),
    private val listener: ThumbnailAdapterListener,
    private val longListener: ThumbnailAdapterLongListener,
) : RecyclerView.Adapter<ThumbnailViewHolder>(){

    init{
        Log.d("", "ThumbnailAdapter created!")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(thumbnails[position], position, listener, longListener)
    }

    override fun getItemCount(): Int = thumbnails.size

    interface ThumbnailAdapterListener{
        fun <T> onClickItem(item: T)
    }

    interface ThumbnailAdapterLongListener{
        fun <T> onLongClickItem(item: T)
    }

    fun updateList(newList: List<Note>) {
        Log.d("", "newList.size: ${newList.size}")
        val callback = ListDiffCallback(thumbnails, newList)
        var diffResult : DiffUtil.DiffResult? = null
        CoroutineScope(Dispatchers.Default).launch {
            diffResult = DiffUtil.calculateDiff(callback)    //this is 'expensive', so call it in worker thread
        }.invokeOnCompletion {
            CoroutineScope(Dispatchers.Main).launch{
                //thumbnails = newList    //TODO("why this works?")
                thumbnails.clear()
                thumbnails.addAll(newList)
                Log.d("","in invokeOncompletion, thumbnails.size: ${thumbnails.size}")
                diffResult?.dispatchUpdatesTo(this@ThumbnailAdapter)
            }
        }
    }

}