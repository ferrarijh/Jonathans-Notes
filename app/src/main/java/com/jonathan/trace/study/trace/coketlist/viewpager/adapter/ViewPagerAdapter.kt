package com.jonathan.trace.study.trace.coketlist.viewpager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R
import com.jonathan.trace.study.trace.coketlist.dialog.fragment.ImageViewFragment
import com.jonathan.trace.study.trace.coketlist.room.Image
import com.jonathan.trace.study.trace.coketlist.viewpager.viewholder.PageViewHolder

class ViewPagerAdapter(
    var images: List<Image>?,
    private val imageViewer: ImageViewFragment,
    private val fragment: Fragment,
    private val longClickListener: View.OnLongClickListener
): RecyclerView.Adapter<PageViewHolder>() {

    init{
        if (images == null)
            images = listOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder =
        with(LayoutInflater.from(parent.context)){
            val view = this.inflate(R.layout.item_viewpager, parent, false)
            PageViewHolder(view, fragment, longClickListener)
        }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(images!![position], imageViewer)
    }

    override fun getItemCount(): Int = images!!.size

}