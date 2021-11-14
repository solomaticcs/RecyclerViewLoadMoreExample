package com.tonyyangdev.example.recyclerviewloadmoreexample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonyyangdev.example.recyclerviewloadmoreexample.databinding.ItemRowBinding

class MainAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList = mutableListOf<String>()

    var clickListener: (String) -> Unit = { }

    fun setData(dataList: List<String>, append: Boolean = false) {
        if (!append) this.dataList.clear()
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            ItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            clickListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bind(dataList[position])
        }
    }

    override fun getItemCount(): Int = dataList.size
}