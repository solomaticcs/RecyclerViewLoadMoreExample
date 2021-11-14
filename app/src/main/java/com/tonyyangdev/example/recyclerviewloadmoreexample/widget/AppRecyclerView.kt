package com.tonyyangdev.example.recyclerviewloadmoreexample.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.*

class AppRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    // The minimum amount of items that are below your last visible item before loading more.
    private var loadMoreThreshold = 5
    var isPullRefreshEnabled: Boolean = true
        set(value) {
            field = value
            refreshAdapter.isPullRefreshEnable = value
        }
    var isLoadingMoreEnabled: Boolean = false
        set(value) {
            field = value
            footerAdapter.isLoadingEnable = value
        }
    val refreshState: RefreshState
        get() {
            return refreshAdapter.refreshState
        }

    var isBlankFooterEnabled: Boolean = false
        set(value) {
            field = value
            footerAdapter.isBlankSpaceEnable = value
        }

    private val headerViewTypes: ArrayList<Int> = arrayListOf()
    private val headerViews: ArrayList<View> = arrayListOf()
    private var loadingListener: LoadingListener? = null
    private var isPulling = false
    private var lastY: Float = -1f
    private val refreshAdapter: RefreshAdapter by lazy {
        RefreshAdapter().apply { onRefresh = { loadingListener?.onRefresh() } }
    }
    private val headerAdapter: HeaderAdapter by lazy {
        HeaderAdapter(headerViews, headerViewTypes)
    }
    private var listAdapter: Adapter<*>? = null
    private val footerAdapter: FooterAdapter by lazy { FooterAdapter() }

    init {
        overScrollMode = OVER_SCROLL_NEVER
        setHasFixedSize(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (refreshAdapter.refreshState == RefreshState.REFRESHING || refreshAdapter.refreshState == RefreshState.DONE)
            return false

        if (lastY == ACTION_NONE)
            lastY = e.y // Add this because some of the ViewGroup will intercept the ACTION_DOWN event.

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = e.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPullRefreshEnabled && isOnTop) {
                    val deltaY = e.y - lastY
                    if (deltaY > 0) {
                        isPulling = true
                        refreshAdapter.visibleHeight = (deltaY / 2f).toInt()
                    }
                } else if (!isOnTop && refreshAdapter.visibleHeight > 0) {
                    refreshAdapter.releasePulling()
                }
            }
            MotionEvent.ACTION_UP -> {
                lastY = ACTION_NONE
                isPulling = false
                refreshAdapter.releasePulling()
            }
        }
        return super.onTouchEvent(e)
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)

        if (!isLoadingMoreEnabled || (dy <= 0 && dx <= 0)) return

        val totalItemCount = layoutManager?.itemCount ?: 0
        synchronized(this) {
            if (listAdapter!!.itemCount > 0
                && !footerAdapter.isLoading
                && totalItemCount <= lastVisibleItemPosition + loadMoreThreshold
            ) {
                footerAdapter.startLoading()
                loadingListener?.onLoadMore()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        completeAll()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(
            if (adapter != null)
                ConcatAdapter(refreshAdapter, headerAdapter, adapter, footerAdapter)
            else
                null
        )
        listAdapter = adapter
    }


    fun setGridLayoutManager(manager: GridLayoutManager?) {
        super.setLayoutManager(manager)
        manager?.run {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isHeaderOrFooterView(position))
                        spanCount
                    else 1
                }
            }
        }
    }

    fun isHeaderOrFooterView(position: Int): Boolean {
        val totalTopItemCount = refreshAdapter.itemCount + headerViews.size
        val totalItemCount = totalTopItemCount + (adapter?.itemCount ?: 0) + footerAdapter.itemCount

        return (position < totalTopItemCount || (footerAdapter.isLoading && position == totalItemCount - 1))
    }

    fun getHeaderTopItemCount() = refreshAdapter.itemCount + headerViews.size

    fun loadMoreComplete() {
        footerAdapter.finishLoading()
    }

    fun refreshComplete() {
        refreshAdapter.finishRefreshing()
    }

    fun completeAll() {
        loadMoreComplete()
        refreshComplete()
    }

    fun setLoadingListener(loadingListener: LoadingListener?) {
        this.loadingListener = loadingListener
    }

    fun addHeaderView(view: View) {
        headerViewTypes.add(HEADER_INIT_INDEX + headerViews.size)
        headerViews.add(view)
        headerAdapter.notifyDataSetChanged()
    }

    fun refresh() {
        refreshAdapter.startRefreshing()
        loadingListener?.onRefresh()
    }

    fun setFooterHeight(height: Int) {
        footerAdapter.viewHeight = height
    }

    fun updateFooterHeight(height: Int) {
        footerAdapter.viewHeight = height
        footerAdapter.updateFooterHeight(height)
    }

    var isFooterTextEnable: Boolean = true
        set(value) {
            footerAdapter.isTextEnable = value
            field = value
        }

    private val isOnTop: Boolean
        get() {
            return refreshAdapter.isOnTop()
        }

    private val lastVisibleItemPosition: Int
        get() = when (layoutManager) {
            is LinearLayoutManager -> (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
            is GridLayoutManager -> (layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                var lastPositions = IntArray(2)
                lastPositions =
                    (layoutManager as StaggeredGridLayoutManager).findLastCompletelyVisibleItemPositions(lastPositions)
                lastPositions[0].coerceAtLeast(lastPositions[1])
            }
            else -> throw Exception("${layoutManager?.javaClass?.simpleName} is not supported.")
        }

    companion object {
        private const val ACTION_NONE = -1f
        const val HEADER_INIT_INDEX = 10002
    }

    interface LoadingListener {
        fun onRefresh()
        fun onLoadMore()
    }
}