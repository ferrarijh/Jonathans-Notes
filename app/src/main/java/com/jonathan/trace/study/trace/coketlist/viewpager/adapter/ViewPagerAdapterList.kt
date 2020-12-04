package com.jonathan.trace.study.trace.coketlist.viewpager.adapter

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.dialog.fragment.ImageViewFragment
import com.jonathan.trace.study.trace.coketlist.room.Image
import com.jonathan.trace.study.trace.coketlist.viewmodel.FragmentStateViewModel
import com.jonathan.trace.study.trace.coketlist.viewpager.viewholder.PageViewHolder
import kotlinx.android.synthetic.main.item_viewpager.view.*
import java.io.File

//TODO("check if ListAdapter works well with Glide")
@Deprecated("position glitch")
class ViewPagerAdapterList(
    private val imageViewer: ImageViewFragment,
    private val fragment: Fragment,
    private val longClickListener: View.OnLongClickListener
    ): ListAdapter<Image, PageViewHolder>(
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


}