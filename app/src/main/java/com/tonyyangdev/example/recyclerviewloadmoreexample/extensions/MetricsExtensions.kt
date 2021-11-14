package com.tonyyangdev.example.recyclerviewloadmoreexample.extensions

import android.content.res.Resources

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.toDp(): Float = this / Resources.getSystem().displayMetrics.density
fun Float.toPx(): Float = this * Resources.getSystem().displayMetrics.density