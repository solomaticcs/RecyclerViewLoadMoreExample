package com.tonyyangdev.example.recyclerviewloadmoreexample.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tonyyangdev.example.recyclerviewloadmoreexample.databinding.RecyclerviewFooterBinding
import com.tonyyangdev.example.recyclerviewloadmoreexample.extensions.toPx

class FooterAdapter : RecyclerView.Adapter<SimpleViewHolder>() {
    private lateinit var binding: RecyclerviewFooterBinding
    private var footerView: View? = null
    var isLoading: Boolean = false
    var isLoadingEnable: Boolean = false
    var isTextEnable: Boolean = true
    var viewHeight = DEFAULT_HEIGHT_IN_DP.toPx()
    var isBlankSpaceEnable: Boolean = false

    private var isHorizontalScroll = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        binding =
            RecyclerviewFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        footerView = binding.root
        footerView?.layoutParams?.height =
            if (isLoadingEnable || isBlankSpaceEnable) viewHeight else 0
        return SimpleViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        binding.loadingContainer.isVisible = isLoading
        if (isHorizontalScroll) {
            binding.statusTextView.visibility = View.GONE
            binding.root.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            binding.statusTextView.isVisible = isTextEnable
            binding.root.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun getItemCount(): Int = 1

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL)
                isHorizontalScroll = true
        }
    }

    override fun onViewAttachedToWindow(holder: SimpleViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
        super.onViewAttachedToWindow(holder)
    }

    fun startLoading() {
        if (isLoadingEnable) {
            isLoading = true
            if (isBlankSpaceEnable) {
                footerView?.run { binding.loadingContainer.isVisible = true }
            } else {
                footerView?.layoutParams?.height = viewHeight
            }
        }
    }

    fun finishLoading() {
        isLoading = false
        if (isBlankSpaceEnable) {
            footerView?.run { binding.loadingContainer.isVisible = false }
        } else {
            val lp = footerView?.layoutParams
            lp?.let {
                it.height = 0
                footerView?.layoutParams = it
            }
        }
    }

    fun updateFooterHeight(height: Int) {
        footerView?.let {
            val lp = it.layoutParams
            lp.height = height
            it.layoutParams = lp
        }
    }

    companion object {
        private const val DEFAULT_HEIGHT_IN_DP = 52
    }
}