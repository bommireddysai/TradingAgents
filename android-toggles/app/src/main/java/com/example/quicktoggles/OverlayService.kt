package com.example.quicktoggles

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Switch
import android.widget.Toast

/** Draws a draggable floating panel with the three switches over every app. */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var toggles: SecureToggles
    private var panel: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        toggles = SecureToggles(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(1, buildNotification())
        showPanel()
    }

    override fun onDestroy() {
        panel?.let { windowManager.removeView(it) }
        panel = null
        super.onDestroy()
    }

    private fun showPanel() {
        val themed = ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault)
        val view = LayoutInflater.from(themed).inflate(R.layout.overlay, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 200
        }

        bind(view.findViewById(R.id.swDns), toggles::isDnsOn, toggles::setDns)
        bind(view.findViewById(R.id.swDebug), toggles::isDebugOn, toggles::setDebug)
        bind(view.findViewById(R.id.swWifiAdb), toggles::isWifiAdbOn, toggles::setWifiAdb)
        view.findViewById<View>(R.id.close).setOnClickListener { stopSelf() }
        makeDraggable(view.findViewById(R.id.dragHandle), view, params)

        windowManager.addView(view, params)
        panel = view
    }

    private val rows = mutableListOf<Triple<Switch, () -> Boolean, (Boolean) -> Boolean>>()
    private var syncing = false

    private fun bind(sw: Switch, read: () -> Boolean, write: (Boolean) -> Boolean) {
        rows += Triple(sw, read, write)
        sw.isChecked = read()
        sw.setOnCheckedChangeListener { _, checked ->
            if (syncing) return@setOnCheckedChangeListener
            if (!write(checked)) {
                Toast.makeText(this, "Grant WRITE_SECURE_SETTINGS first (see README)", Toast.LENGTH_LONG).show()
            }
            // Switches depend on each other (Wi-Fi ADB needs USB debugging), so resync all from system state.
            syncing = true
            rows.forEach { (s, r, _) -> s.isChecked = r() }
            syncing = false
        }
    }

    private fun makeDraggable(handle: View, view: View, params: WindowManager.LayoutParams) {
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        handle.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x; startY = params.y
                    touchX = e.rawX; touchY = e.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (e.rawX - touchX).toInt()
                    params.y = startY + (e.rawY - touchY).toInt()
                    windowManager.updateViewLayout(view, params)
                }
            }
            false
        }
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "On-screen toggles", NotificationManager.IMPORTANCE_MIN)
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle("Quick Toggles is showing")
            .build()
    }

    companion object {
        private const val CHANNEL = "overlay"
    }
}
