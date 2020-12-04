package com.jonathan.trace.study.trace.coketlist.viewpager.viewholder

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.dialog.fragment.ImageViewFragment
import com.jonathan.trace.study.trace.coketlist.room.Image
import com.jonathan.trace.study.trace.coketlist.viewmodel.FragmentStateViewModel
import kotlinx.android.synthetic.main.item_viewpager.view.*
import java.io.File

class PageViewHolder(itemView: View, fragment: Fragment, private val longClickListener: View.OnLongClickListener): RecyclerView.ViewHolder(itemView){
    private val fViewModel by lazy{ ViewModelProvider(fragment).get(FragmentStateViewModel::class.java) }
    fun bind(image: Image, imageViewer: ImageViewFragment){
        val fileName = image.name
        val filesDir = itemView.context.filesDir

        val fullDir = "${filesDir.absolutePath}/Pictures/${image.noteId}/$fileName"
        val file = File(fullDir)
//            Log.d("", "file path: ${file.absolutePath}")
        val uri = Uri.fromFile(file)

        itemView.iv_item.setImageURI(uri)

        val marginPx = itemView.context.resources.getDimensionPixelOffset(R.dimen.pageMargin)
        val widthPx = itemView.context.resources.displayMetrics.widthPixels
        val heightPx = itemView.context.resources.displayMetrics.heightPixels

        val height = heightPx/4 //itemView.context.resources.getDimensionPixelOffset(R.dimen.pageHeight)
        val orgWidth = itemView.iv_item.layoutParams.width
        val maxWidth = widthPx - 2*marginPx

        itemView.iv_item.layoutParams.height = height
        itemView.iv_item.layoutParams.width = if (orgWidth>maxWidth) maxWidth else orgWidth

//            Glide.with(itemView.context).load(uri).fitCenter().into(itemView.iv_item)

        itemView.setOnClickListener{
            val parent = itemView.context as AppCompatActivity

            val args = Bundle()
            args.putString("fullDir", fullDir)
            imageViewer.arguments = args

            if(!imageViewer.isAdded)
                imageViewer.show(parent.supportFragmentManager, "image_viewer")
        }
        itemView.setOnLongClickListener{
            fViewModel.imagePointed = image
            longClickListener.onLongClick(it)
            true
        }
    }
}