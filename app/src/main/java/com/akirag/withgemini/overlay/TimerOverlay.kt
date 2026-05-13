package com.akirag.withgemini.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.akirag.withgemini.R

class TimerOverlay(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null
    private var tvTime: TextView? = null
    private var isShowing = false

    // Stopwatch logic variables
    private var seconds = 0
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                seconds++
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                // Format: HH:MM:SS
                tvTime?.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun show() {
        if (isShowing) return

        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER // Screen ke theek beech mein aayega
        }

        view = LayoutInflater.from(context).inflate(R.layout.layout_timer_overlay, null)
        tvTime = view?.findViewById(R.id.tv_time)

        // Button Clicks
        view?.findViewById<TextView>(R.id.btn_play)?.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                handler.post(runnable)
            }
        }

        view?.findViewById<TextView>(R.id.btn_pause)?.setOnClickListener {
            isRunning = false
        }

        view?.findViewById<TextView>(R.id.btn_close)?.setOnClickListener {
            hide()
        }

        windowManager.addView(view, params)
        isShowing = true
    }

    private fun hide() {
        if (!isShowing) return
        isRunning = false
        seconds = 0
        tvTime?.text = "00:00:00"
        view?.let { windowManager.removeView(it) }
        view = null
        isShowing = false
    }
}
