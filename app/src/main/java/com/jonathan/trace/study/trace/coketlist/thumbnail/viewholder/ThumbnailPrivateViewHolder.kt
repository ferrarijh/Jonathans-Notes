package com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder

import android.graphics.Color
import android.view.View
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.viewmodel.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailAdapter
import com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder.DateConverter.Companion.getDateTime
import kotlinx.android.synthetic.main.thumbnail_private.view.*
import java.text.SimpleDateFormat
import java.util.*

class ThumbnailPrivateViewHolder(
    view: View
): RecyclerView.ViewHolder(view){

    private val nViewModel: NoteViewModel
            by lazy{ ViewModelProvider(itemView.context as ViewModelStoreOwner).get(NoteViewModel::class.java)}

    fun bind(note: Note, clickListener: ThumbnailAdapter.ThumbnailAdapterListener, longClickListener: ThumbnailAdapter.ThumbnailAdapterLongListener){

        val curDate = getDateTime().substring(0, 10)
        val noteDate = note.dateTimeModified.substring(0, 10)
        var displayDate = if(curDate == noteDate)
            note.dateTimeModified.substring(11)
        else
            noteDate.substring(5) + '-' + noteDate.substring(0,4)
        displayDate += " "

        val cv = itemView.findViewById<CardView>(R.id.cv_thumbnail_private)
        cv.setBackgroundColor(Color.parseColor(note.color))

        itemView.apply{
            tv_thumbnail_private_date.text = displayDate
            tv_thumbnail_private_title.text = note.title
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
}