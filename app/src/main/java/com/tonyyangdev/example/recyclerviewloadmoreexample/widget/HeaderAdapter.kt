package com.tonyyangdev.example.recyclerviewloadmoreexample.widget

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tonyyangdev.example.recyclerviewloadmoreexample.widget.AppRecyclerView.Companion.HEADER_INIT_INDEX

class HeaderAdapter(
    private val headerViews: List<View>,
    private val headerViewTypes: List<Int>
) : RecyclerView.Adapter<SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SimpleViewHolder(headerViews[viewType - HEADER_INIT_INDEX])
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return headerViews.size
    }

    override fun getItemViewType(position: Int): Int {
        return headerViewTypes[position]
    }

    override fun onViewAttachedToWindow(holder: SimpleViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
        super.onViewAttachedToWindow(holder)
    }
}