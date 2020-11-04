package com.jonathan.trace.study.trace.coketlist.adapter

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.dialog.fragment.ImageViewFragment
import com.jonathan.trace.study.trace.coketlist.room.Image
import com.jonathan.trace.study.trace.coketlist.viewmodel.FragmentStateViewModel
import kotlinx.android.synthetic.main.item_viewpager.view.*
import java.io.File

class ViewPagerAdapter(
    private val imageViewer: ImageViewFragment,
    private val fragment: Fragment,
    private val longClickListener: View.OnLongClickListener
    ): ListAdapter<Image, ViewPagerAdapter.PageViewHolder>(
    object: DiffUtil.ItemCallback<Image>(){
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean = oldItem.name == newItem.name
    }){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder =
        with(LayoutInflater.from(parent.context)){
            val view = this.inflate(R.layout.item_viewpager, parent, false)
            PageViewHolder(view, fragment, longClickListener)
        }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItem(position), imageViewer)
    }

    class PageViewHolder(itemView: View, fragment: Fragment, private val longClickListener: View.OnLongClickListener): RecyclerView.ViewHolder(itemView){
        private val fViewModel by lazy{ ViewModelProvider(fragment).get(FragmentStateViewModel::class.java) }
        fun bind(image: Image, imageViewer: ImageViewFragment){
            val fileName = image.name
            val filesDir = itemView.context.filesDir

            val fullDir = "${filesDir.absolutePath}/Pictures/${image.noteId}/$fileName"
            val file = File(fullDir)
            Log.d("", "file path: ${file.absolutePath}")
            val uri = Uri.fromFile(file)
            itemView.iv_item.setImageURI(uri)

//            val marginPx = itemView.context.resources.getDimensionPixelOffset(R.dimen.pageMargin)
//            val widthPx = itemView.context.resources.displayMetrics.widthPixels
//            Glide.with(itemView.context).load(uri).override(widthPx - 2*marginPx, 200).into(itemView.iv_item)
//            Log.d("", "widthPx: $widthPx, marginPx: $marginPx")

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
}