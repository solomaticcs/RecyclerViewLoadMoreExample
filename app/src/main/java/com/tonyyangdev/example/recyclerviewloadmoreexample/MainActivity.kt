package com.tonyyangdev.example.recyclerviewloadmoreexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tonyyangdev.example.recyclerviewloadmoreexample.databinding.ActivityMainBinding
import com.tonyyangdev.example.recyclerviewloadmoreexample.widget.AppRecyclerView

class MainActivity : AppCompatActivity(), AppRecyclerView.LoadingListener {

    private lateinit var binding: ActivityMainBinding

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        MainAdapter()
    }

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setLoadingListener(this)
        binding.recyclerView.isPullRefreshEnabled = true
        binding.recyclerView.isLoadingMoreEnabled = true
        adapter.clickListener = { text ->
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }
        setData()
    }

    private fun setData() {
        adapter.setData(generateList(INITIAL_NUM))
    }

    override fun onRefresh() {
        val list = generateList(INITIAL_NUM)
        adapter.setData(list)
        binding.recyclerView.refreshComplete()
    }

    override fun onLoadMore() {
        val list = generateList(LOAD_MORE_NUM)
        adapter.setData(list, true)
        binding.recyclerView.loadMoreComplete()
    }

    private fun generateList(num: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in 1..num) {
            list.add("${++count}")
        }
        return list
    }

    companion object {
        private const val INITIAL_NUM = 25
        private const val LOAD_MORE_NUM = 5
    }
}