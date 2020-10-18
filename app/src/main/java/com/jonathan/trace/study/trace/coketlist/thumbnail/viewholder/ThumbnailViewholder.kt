package com.jonathan.trace.study.trace.coketlist.thumbnail.viewholder

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailAdapter
import kotlinx.android.synthetic.main.thumbnail.view.*
import java.text.SimpleDateFormat
import java.util.*


class ThumbnailViewHolder(
    view: View
): RecyclerView.ViewHolder(view){

    private val nViewModel: NoteViewModel
            by lazy{ViewModelProvider(itemView.context as ViewModelStoreOwner).get(NoteViewModel::class.java)}

    private val disappear by lazy{AnimationUtils.loadAnimation(itemView.context, R.anim.global_disappear)}
    private val appear by lazy{AnimationUtils.loadAnimation(itemView.context, R.anim.btn_appear)}

    fun bind(note: Note, position: Int, clickListener: ThumbnailAdapter.ThumbnailAdapterListener, longClickListener: ThumbnailAdapter.ThumbnailAdapterLongListener){
        val curDate = getDateTime().substring(0, 10)
        val noteDate = note.dateTimeModified.substring(0, 10)
        var displayDate = if(curDate == noteDate)
            note.dateTimeModified.substring(11)
        else
            noteDate
        displayDate += " "

        val cv = itemView.findViewById<CardView>(R.id.cv_thumbnail)
        cv.setBackgroundColor(Color.parseColor(note.color))

        itemView.apply{
            tv_thumbnail_date.text = displayDate

            tv_thumbnail_title.text = note.title
            tv_thumbnail_body.text = note.body
            iv_cover.visibility = View.GONE
            iv_checkbox.visibility = View.GONE

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
                nViewModel.setNotePointed(layoutPosition, note)

                if(nViewModel.getSelMode() == NoteViewModel.OFF){
                    setBackgroundToSel()    //prepare to enter multi sel mode
                    longClickListener.onLongClickItem(note)
                }
                true
            }
        }
    }

    private fun setBackgroundToSel(){
        itemView.apply{
            iv_cover.visibility = View.VISIBLE
            iv_checkbox.visibility = View.VISIBLE
            iv_checkbox.startAnimation(appear)
        }
    }

    private fun setBackgroundToNotSel(){
        itemView.apply{
            iv_cover.visibility = View.GONE
            iv_checkbox.visibility = View.GONE
            iv_checkbox.startAnimation(disappear)
        }
    }

    private fun getDateTime(): String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}