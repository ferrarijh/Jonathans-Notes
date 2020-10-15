package com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder

import android.graphics.Color
import android.view.View
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailAdapter
import kotlinx.android.synthetic.main.thumbnail_trash.view.*
import java.text.SimpleDateFormat
import java.util.*

class ThumbnailTrashViewHolder(
    view: View
): RecyclerView.ViewHolder(view){

    private val nViewModel: NoteViewModel
            by lazy{ ViewModelProvider(itemView.context as ViewModelStoreOwner).get(NoteViewModel::class.java)}

    //parem position: for future updates for multi selection
    fun bind(note: Note, position: Int, clickListener: ThumbnailAdapter.ThumbnailAdapterListener, longClickListener: ThumbnailAdapter.ThumbnailAdapterLongListener){

        val curDate = getDateTime().substring(0, 10)
        val noteDate = note.dateTimeModified.substring(0, 10)
        if(curDate == noteDate)
            itemView.tv_thumbnail_trash_date.text = note.dateTimeModified.substring(11)
        else
            itemView.tv_thumbnail_trash_date.text = noteDate

        val cv = itemView.findViewById<CardView>(R.id.cv_thumbnail_trash)
        cv.setBackgroundColor(Color.parseColor(note.color))

        itemView.apply{
            tv_thumbnail_trash_title.text = note.title
            tv_thumbnail_trash_body.text = note.body
            setOnClickListener{
                nViewModel.setNotePointed(layoutPosition, note)
                clickListener.onClickItem(note)
            }
            setOnLongClickListener{
                nViewModel.setNotePointed(layoutPosition, note)
                longClickListener.onLongClickItem(note)
                true
            }
        }
    }


    private fun getDateTime(): String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}
