package com.tonyyangdev.example.recyclerviewloadmoreexample

import androidx.recyclerview.widget.RecyclerView
import com.tonyyangdev.example.recyclerviewloadmoreexample.databinding.ItemRowBinding

class ItemViewHolder(
    private val binding: ItemRowBinding,
    private val clickListener: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String) {
        binding.tvItem.text = text
        binding.root.setOnClickListener {
            clickListener.invoke(text)
        }
    }
}