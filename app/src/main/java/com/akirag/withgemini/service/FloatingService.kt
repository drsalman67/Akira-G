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
import kotlin.math.abs

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams

    // Menu state
    private var isMenuExpanded = false

    // Buttons
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

        // Init views
        btnMain = floatingView.findViewById(R.id.btn_floating_icon)
        btnGemini = floatingView.findViewById(R.id.btn_gemini)
        btnAdd = floatingView.findViewById(R.id.btn_add)
        btnSettings = floatingView.findViewById(R.id.btn_settings)
        btnClock = floatingView.findViewById(R.id.btn_clock)

        setupDraggingAndClick()
        setupMenuClicks()
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
                        // Agar user ne thoda zyada finger move kiya, toh wo click nahi, drag maana jayega
                        if (abs(event.rawX - initialTouchX) > 10 || abs(event.rawY - initialTouchY) > 10) {
                            isDrag = true
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(floatingView, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Agar drag nahi tha, toh iska matlab user ne click kiya hai!
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
        val offset = 180f // Kitna door nikalna hai buttons ko main button se

        if (isMenuExpanded) {
            // Collapse Menu (Wapas andar le jao)
            btnGemini.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnGemini.visibility = View.INVISIBLE }.start()
            btnAdd.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnAdd.visibility = View.INVISIBLE }.start()
            btnSettings.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnSettings.visibility = View.INVISIBLE }.start()
            btnClock.animate().translationX(0f).translationY(0f).setDuration(duration).withEndAction { btnClock.visibility = View.INVISIBLE }.start()
            
            btnMain.text = "G"
        } else {
            // Expand Menu (Bahar nikalo)
            btnGemini.visibility = View.VISIBLE
            btnAdd.visibility = View.VISIBLE
            btnSettings.visibility = View.VISIBLE
            btnClock.visibility = View.VISIBLE

            // Upar ki taraf (Gemini)
            btnGemini.animate().translationX(0f).translationY(-offset).setDuration(duration).start()
            // Right ki taraf (Add)
            btnAdd.animate().translationX(offset).translationY(0f).setDuration(duration).start()
            // Neeche ki taraf (Settings)
            btnSettings.animate().translationX(0f).translationY(offset).setDuration(duration).start()
            // Left ki taraf (Clock)
            btnClock.animate().translationX(-offset).translationY(0f).setDuration(duration).start()

            btnMain.text = "X" // Menu open hone pe icon change
        }
        isMenuExpanded = !isMenuExpanded
    }

    private fun setupMenuClicks() {
        btnGemini.setOnClickListener {
            toggleRadialMenu() // Menu band karo pehle
            launchGeminiApp()
        }
        btnAdd.setOnClickListener {
            toggleRadialMenu()
            Toast.makeText(this, "Add App Picker aayega (Phase 3)", Toast.LENGTH_SHORT).show()
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

    private fun launchGeminiApp() {
        try {
            // Intent se direct package call karna
            val intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.bard")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Service se Activity kholne ke liye zaroori
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
