package com.akirag.withgemini.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
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
import kotlin.math.abs

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams

    private var isMenuExpanded = false

    private lateinit var btnMain: TextView
    private lateinit var btnGemini: TextView
    private lateinit var btnAdd: TextView
    private lateinit var btnSettings: TextView
    private lateinit var btnClock: TextView

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
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

    // Har baar jab menu khulega, ye check karega ki koi app save hui hai ya nahi
    private fun updateAddButtonIcon() {
        val savedPackage = Prefs.getSavedAppPackage(this)
        if (savedPackage != null) {
            try {
                val icon = packageManager.getApplicationIcon(savedPackage)
                btnAdd.background = icon
                btnAdd.text = "" // Text hata do taake sirf app ka icon dikhe
            } catch (e: Exception) {
                // Agar app uninstall ho gayi toh wapas normal ho jao
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
            updateAddButtonIcon() // Menu khulne se pehle icon update karo
            
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
        
        // Short Click: App Launch karo ya Picker kholo
        btnAdd.setOnClickListener {
            toggleRadialMenu()
            val savedPackage = Prefs.getSavedAppPackage(this)
            if (savedPackage != null) {
                launchSavedApp(savedPackage)
            } else {
                openAppPicker()
            }
        }

        // Long Click: Hamesha App Picker kholo change karne ke liye
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
            Toast.makeText(this, "Timer engine aayega (Phase 4)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppPicker() {
        val intent = Intent(this, AppPickerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Toast.makeText(this, "Select App for Shortcut", Toast.LENGTH_SHORT).show()
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
            } else {
                Toast.makeText(this, "Gemini app install nahi hai bhai!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening Gemini", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
