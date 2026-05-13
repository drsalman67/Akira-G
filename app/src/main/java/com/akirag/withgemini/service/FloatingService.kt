package com.akirag.withgemini.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.akirag.withgemini.R
import com.akirag.withgemini.apppicker.AppPickerActivity
import com.akirag.withgemini.utils.Prefs
import com.akirag.withgemini.overlay.TimerOverlay
import kotlin.math.abs

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    
    private lateinit var timerOverlay: TimerOverlay

    private var isMenuExpanded = false

    private lateinit var btnMain: TextView
    private lateinit var btnGemini: TextView
    private lateinit var btnAdd: TextView
    private lateinit var btnSettings: TextView
    private lateinit var btnClock: TextView

    // Naya Signal Receiver!
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                [span_5](start_span)"AKIRA_NOTIF_POSTED" -> setNeonColor("#FF3131") // Notification aane par RED[span_5](end_span)
                "AKIRA_NOTIF_CLEARED" -> setNeonColor("#39FF14") // Clear hone par wapas GREEN
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        // Receiver Register karna
        val filter = IntentFilter()
        filter.addAction("AKIRA_NOTIF_POSTED")
        filter.addAction("AKIRA_NOTIF_CLEARED")
        registerReceiver(notificationReceiver, filter)

        timerOverlay = TimerOverlay(this) 
        
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_button, null)
        
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 200

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)

        btnMain = floatingView.findViewById(R.id.btn_floating_icon)
        btnGemini = floatingView.findViewById(R.id.btn_gemini)
        btnAdd = floatingView.findViewById(R.id.btn_add)
        btnSettings = floatingView.findViewById(R.id.btn_settings)
        btnClock = floatingView.findViewById(R.id.btn_clock)

        setupDraggingAndClick()
        setupMenuClicks()
    }

    // Dynamic Color Changer Logic
    private fun setNeonColor(hexColor: String) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.parseColor(hexColor))
        drawable.setStroke(4, Color.WHITE) // Border thickness
        btnMain.background = drawable
        btnMain.setTextColor(Color.BLACK)
    }

    private fun updateAddButtonIcon() {
        val savedPackage = Prefs.getSavedAppPackage(this)
        if (savedPackage != null) {
            try {
                val icon = packageManager.getApplicationIcon(savedPackage)
                btnAdd.background = icon
                btnAdd.text = "" 
            } catch (e: Exception) {
                btnAdd.setBackgroundResource(R.drawable.mini_circle_background)
                btnAdd.text = "+"
            }
        } else {
            btnAdd.setBackgroundResource(R.drawable.mini_circle_background)
            btnAdd.text = "+"
        }
    }

    private fun setupDraggingAndClick() {
        btnMain.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f
            private var isDrag = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDrag = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (abs(event.rawX - initialTouchX) > 10 || abs(event.rawY - initialTouchY) > 10) {
                            isDrag = true
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(floatingView, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDrag) {
                            toggleRadialMenu()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun toggleRadialMenu() {
        val duration = 300L
        val offset = 180f 

        if (isMenuExpanded) {
            btnGemini.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnGemini.visibility = View.INVISIBLE }.start()
            btnAdd.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnAdd.visibility = View.INVISIBLE }.start()
            btnSettings.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnSettings.visibility = View.INVISIBLE }.start()
            btnClock.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnClock.visibility = View.INVISIBLE }.start()
            
            btnMain.text = "G"
        } else {
            updateAddButtonIcon() 
            
            btnGemini.visibility = View.VISIBLE
            btnAdd.visibility = View.VISIBLE
            btnSettings.visibility = View.VISIBLE
            btnClock.visibility = View.VISIBLE

            btnGemini.animate().translationX(0f).translationY(-offset).setDuration(duration).start()
            btnAdd.animate().translationX(offset).translationY(0f).setDuration(duration).start()
            btnSettings.animate().translationX(0f).translationY(offset).setDuration(duration).start()
            btnClock.animate().translationX(-offset).translationY(0f).setDuration(duration).start()

            btnMain.text = "X" 
        }
        isMenuExpanded = !isMenuExpanded
    }

    private fun setupMenuClicks() {
        btnGemini.setOnClickListener {
            toggleRadialMenu() 
            launchGeminiApp()
        }
        
        btnAdd.setOnClickListener {
            toggleRadialMenu()
            val savedPackage = Prefs.getSavedAppPackage(this)
            if (savedPackage != null) {
                launchSavedApp(savedPackage)
            } else {
                openAppPicker()
            }
        }

        btnAdd.setOnLongClickListener {
            toggleRadialMenu()
            openAppPicker()
            true
        }

        btnSettings.setOnClickListener {
            toggleRadialMenu()
            Toast.makeText(this, "Settings screen aayegi (Phase 6)", Toast.LENGTH_SHORT).show()
        }
        
        btnClock.setOnClickListener {
            toggleRadialMenu()
            timerOverlay.show()
        }
    }

    private fun openAppPicker() {
        val intent = Intent(this, AppPickerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun launchSavedApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this, "App found nahi hui!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error launching app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGeminiApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.bard")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening Gemini", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
