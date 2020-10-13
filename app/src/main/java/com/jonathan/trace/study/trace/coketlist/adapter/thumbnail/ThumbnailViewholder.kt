package com.jonathan.trace.study.trace.coketlist.adapter.thumbnail

import android.graphics.Color
import android.view.View
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.thumbnail.view.*
import java.text.SimpleDateFormat
import java.util.*


class ThumbnailViewHolder(
    view: View
): RecyclerView.ViewHolder(view){

    private val nViewModel: NoteViewModel
            by lazy{ViewModelProvider(itemView.context as ViewModelStoreOwner).get(NoteViewModel::class.java)}

    fun bind(note: Note, position: Int, clickListener: ThumbnailAdapter.ThumbnailAdapterListener, longClickListener: ThumbnailAdapter.ThumbnailAdapterLongListener){
        val curDate = getDateTime().substring(0, 10)
        val noteDate = note.dateTimeModified.substring(0, 10)
        if(curDate == noteDate)
            itemView.tv_thumbnail_date.text = note.dateTimeModified.substring(11)
        else
            itemView.tv_thumbnail_date.text = noteDate

        val cv = itemView.findViewById<CardView>(R.id.cv_thumbnail)
        cv.setBackgroundColor(Color.parseColor(note.color))

        itemView.apply{
            tv_thumbnail_title.text = note.title
            tv_thumbnail_body.text = note.body
            if(nViewModel.selected[position] != null)
                setBackgroundToSel()

            nViewModel.selMode.observe(context as LifecycleOwner){
                if(it == NoteViewModel.OFF)
                    setBackgroundToNotSel()
            }

            setOnClickListener{
                val curPos = layoutPosition
                nViewModel.setNotePointed(curPos, note)
                if(nViewModel.getSelMode() == NoteViewModel.OFF)
                    clickListener.onClickItem(note)
                else{
                    nViewModel.toggleSelectedWith(curPos, note)
                    if(nViewModel.selected[curPos] == null)
                        setBackgroundToNotSel()
                    else
                        setBackgroundToSel()
                }
            }
            setOnLongClickListener{
                val curPos = layoutPosition
                nViewModel.setNotePointed(curPos, note)
                if(nViewModel.getSelMode() == NoteViewModel.OFF){
                    setBackgroundToSel()    //prepare to enter multi sel mode
                    longClickListener.onLongClickItem(note)
                }
                true
            }
        }
    }

    private fun setBackgroundToSel(){
        itemView.alpha = 0.6f
    }

    private fun setBackgroundToNotSel(){
        itemView.alpha = 1f
    }

    private fun getDateTime(): String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}