package com.jonathan.trace.study.trace.coketlist.adapter.thumbnail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Note
import kotlinx.android.synthetic.main.thumbnail.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ThumbnailAdapter(
    private var thumbnails: MutableList<Note> = mutableListOf<Note>(),
    private val listener: ThumbnailAdapterListener,
    private val longListener: ThumbnailAdapterLongListener
) : RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder>(){
    val thumbnailsVisible
        get() = thumbnails


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {

        val note = thumbnails[position]
        val curDate = getDateTime().substring(0, 10)
        val noteDate = note.dateTimeModified.substring(0, 10)
        if(curDate == noteDate)
            holder.itemView.tv_thumbnail_date.text = note.dateTimeModified.substring(11)
        else
            holder.itemView.tv_thumbnail_date.text = noteDate
        holder.itemView.tv_thumbnail_title.text = note.title
        holder.itemView.tv_thumbnail_body.text = note.body

        holder.itemView.setOnClickListener{
            listener.onClickItem(note)
        }

        holder.itemView.setOnLongClickListener{
            longListener.onLongClickItem(note)
            true
        }

    }

    override fun getItemCount(): Int = thumbnails.size

    class ThumbnailViewHolder(view: View): RecyclerView.ViewHolder(view)

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
                thumbnails.clear()
                thumbnails.addAll(newList)
                diffResult?.dispatchUpdatesTo(this@ThumbnailAdapter)
            }
        }
    }

    private fun getDateTime(): String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}