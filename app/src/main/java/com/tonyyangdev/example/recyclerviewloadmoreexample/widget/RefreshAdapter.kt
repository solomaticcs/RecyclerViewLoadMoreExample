package com.tonyyangdev.example.recyclerviewloadmoreexample.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tonyyangdev.example.recyclerviewloadmoreexample.R
import com.tonyyangdev.example.recyclerviewloadmoreexample.databinding.RecyclerviewRefreshBinding
import com.tonyyangdev.example.recyclerviewloadmoreexample.extensions.toPx

class RefreshAdapter : RecyclerView.Adapter<SimpleViewHolder>() {
    private lateinit var binding: RecyclerviewRefreshBinding
    var isPullRefreshEnable: Boolean = true
    var view: View? = null
    var onRefresh: () -> Unit = {}
    var visibleHeight: Int = 0
        set(value) {
            view?.let {
                field = if (value >= 0) value else 0
                val layoutParams = it.layoutParams
                layoutParams.height = field
                it.layoutParams = layoutParams
                updateRefreshState()
            }
        }

    var refreshState: RefreshState = RefreshState.NONE
        set(value) {
            if (value == field) return
            field = value
            updateHeaderView()
        }

    private val refreshAnimator: ValueAnimator
        get() {
            return ValueAnimator.ofFloat(
                visibleHeight.toFloat(),
                REFRESH_THRESHOLD_HEIGHT.toFloat()
            )
                .apply {
                    duration = 300
                    interpolator = FastOutLinearInInterpolator()
                    addUpdateListener {
                        visibleHeight = (it.animatedValue as Float).toInt()
                    }
                }
        }
    private val collapseAnimator: ValueAnimator
        get() {
            return ValueAnimator.ofFloat(visibleHeight.toFloat(), 0f)
                .apply {
                    duration = (visibleHeight / 2.toPx() * 10).toLong()
                    interpolator = FastOutLinearInInterpolator()
                    addUpdateListener {
                        if ((it.animatedValue as Float) < visibleHeight)
                            visibleHeight = (it.animatedValue as Float).toInt()
                    }
                    doOnEnd {
                        refreshState = RefreshState.NONE
                    }
                }
        }
    private val arrowRotateUp: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.statusImageView, "rotation", 0f, 180f)
            .apply {
                duration = 300
            }
    }
    private val arrowRotateDown: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.statusImageView, "rotation", 180f, 0f)
            .apply {
                duration = 300
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        binding =
            RecyclerviewRefreshBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view = binding.root
        return SimpleViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {}

    override fun getItemCount(): Int = if (isPullRefreshEnable) 1 else 0

    override fun onViewAttachedToWindow(holder: SimpleViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
        super.onViewAttachedToWindow(holder)
    }

    fun releasePulling() {
        if (isPullRefreshEnable) {
            if (visibleHeight > REFRESH_THRESHOLD_HEIGHT) {
                startRefreshing()
                onRefresh()
            } else {
                collapseAnimator.start()
            }
        }
    }

    fun startRefreshing() {
        refreshState = RefreshState.REFRESHING
        refreshAnimator.start()
    }

    fun finishRefreshing() {
        refreshState = RefreshState.DONE
        Handler(Looper.getMainLooper()).postDelayed({
            collapseAnimator.start()
        }, COLLAPSE_DELAY_TIME_ON_FINISH)
    }

    fun isOnTop(): Boolean = view?.parent != null

    private fun updateRefreshState() {
        if (refreshState == RefreshState.REFRESHING || refreshState == RefreshState.DONE)
            return
        refreshState = when (visibleHeight) {
            0 -> {
                RefreshState.NONE
            }
            in 1 until REFRESH_THRESHOLD_HEIGHT -> {
                if (refreshState == RefreshState.PREPARE_TO_REFRESH)
                    view?.run { arrowRotateDown.start() }
                RefreshState.PULLING
            }
            else -> {
                if (refreshState == RefreshState.PULLING)
                    view?.run { arrowRotateUp.start() }
                RefreshState.PREPARE_TO_REFRESH
            }
        }
    }

    private fun updateHeaderView() {
        view?.let {
            binding.apply {
                when (refreshState) {
                    RefreshState.PULLING -> {
                        statusTextView.setText(R.string.app_recyclerview_pull_to_refresh)
                        statusImageView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        statusImageView.setImageResource(R.drawable.ic_arrow_down)
                    }
                    RefreshState.PREPARE_TO_REFRESH -> {
                        statusTextView.setText(R.string.app_recyclerview_prepare_to_refresh)
                        statusImageView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                    RefreshState.REFRESHING -> {
                        statusTextView.setText(R.string.app_recyclerview_refreshing)
                        statusImageView.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                    }
                    RefreshState.DONE -> {
                        arrowRotateUp.cancel()
                        statusTextView.setText(R.string.app_recyclerview_refresh_finish)
                        statusImageView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        statusImageView.setImageResource(R.drawable.ic_check)
                        statusImageView.rotation = 0f
                    }
                    RefreshState.NONE -> {
                    }
                }
            }
        }
    }

    companion object {
        private val REFRESH_THRESHOLD_HEIGHT = 64.toPx()
        private const val COLLAPSE_DELAY_TIME_ON_FINISH = 500L
    }
}

