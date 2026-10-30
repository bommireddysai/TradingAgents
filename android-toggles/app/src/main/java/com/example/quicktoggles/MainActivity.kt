package com.example.quicktoggles

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var toggles: SecureToggles
    private lateinit var status: TextView
    private lateinit var dnsHost: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toggles = SecureToggles(this)
        status = findViewById(R.id.status)
        dnsHost = findViewById(R.id.dnsHost)
        dnsHost.setText(toggles.dnsHostname)

        findViewById<Button>(R.id.btnOverlayPerm).setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            toggles.dnsHostname = dnsHost.text.toString().ifBlank { SecureToggles.DEFAULT_DNS_HOST }
            if (Settings.canDrawOverlays(this)) {
                startForegroundService(Intent(this, OverlayService::class.java))
            }
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val overlay = if (Settings.canDrawOverlays(this)) "granted" else "missing"
        val secure = if (toggles.canWrite()) "granted" else "missing (run the adb command in the README)"
        status.text = "Overlay permission: $overlay\nWRITE_SECURE_SETTINGS: $secure"
    }
}
