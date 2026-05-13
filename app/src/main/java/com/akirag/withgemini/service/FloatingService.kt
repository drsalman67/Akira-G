package com.akirag.withgemini.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.akirag.withgemini.R
import com.akirag.withgemini.SettingsActivity
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
    private var isNotifActive = false // Naya check taake notification aur battery aapas mein na ladein

    private lateinit var btnMain: TextView
    private lateinit var btnGemini: TextView
    private lateinit var btnAdd: TextView
    private lateinit var btnSettings: TextView
    private lateinit var btnClock: TextView

    // Notification Receiver
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Prefs.isNotifLightEnabled(this@FloatingService)) {
                when (intent?.action) {
                    "AKIRA_NOTIF_POSTED" -> {
                        isNotifActive = true
                        setNeonColor("#FF3131") 
                    }
                    "AKIRA_NOTIF_CLEARED" -> {
                        isNotifActive = false
                        setNeonColor("#39FF14") // Default Green
                    }
                }
            }
        }
    }

    // NAYA BATTERY AUR CHARGER RECEIVER 🔋⚡
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    // Agar notification aayi hui hai, toh battery color change mat karo
                    if (!isNotifActive && Prefs.isBatteryColorEnabled(this@FloatingService)) {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val batteryPct = level * 100 / scale.toFloat()
                        
                        when {
                            batteryPct >= 80 -> setNeonColor("#39FF14") // Green
                            batteryPct >= 40 -> setNeonColor("#FFFF00") // Yellow
                            else -> setNeonColor("#FF3131") // Red
                        }
                    }
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    if (Prefs.isChargeAnimEnabled(this@FloatingService)) {
                        btnMain.text = "⚡"
                        val rotateAnim = AnimationUtils.loadAnimation(this@FloatingService, R.anim.rotate_charging)
                        btnMain.startAnimation(rotateAnim)
                    }
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    btnMain.clearAnimation()
                    if (!isMenuExpanded) btnMain.text = "G"
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        // Notifications aur Battery dono ke signals catch karna
        val notifFilter = IntentFilter()
        notifFilter.addAction("AKIRA_NOTIF_POSTED")
        notifFilter.addAction("AKIRA_NOTIF_CLEARED")
        registerReceiver(notificationReceiver, notifFilter, RECEIVER_NOT_EXPORTED)

        val batteryFilter = IntentFilter()
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        batteryFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(batteryReceiver, batteryFilter)

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

    private fun setNeonColor(hexColor: String) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.parseColor(hexColor))
        drawable.setStroke(4, Color.WHITE) 
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
            val intent = Intent(this, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
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
        // App band hone par dono receivers ko safely kill karna
        unregisterReceiver(notificationReceiver)
        unregisterReceiver(batteryReceiver)
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
