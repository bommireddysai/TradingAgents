package com.example.quicktoggles

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

/** Invisible activity that runs a launcher shortcut and closes straight away. */
class ShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toggles = SecureToggles(this)
        when (intent.action) {
            ACTION_LAN_SHARE -> openLanShare()
            ACTION_DNS -> flip("Private DNS", toggles.isDnsOn(), toggles::setDns)
            ACTION_DEV_OPTIONS -> open(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            ACTION_WIFI_DEBUG -> flip("Wi-Fi debugging", toggles.isWifiAdbOn(), toggles::setWifiAdb)
        }
        finish()
    }

    private fun flip(name: String, current: Boolean, set: (Boolean) -> Boolean) {
        val msg = if (set(!current)) "$name ${if (current) "off" else "on"}"
        else "Grant WRITE_SECURE_SETTINGS first (see README)"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /** Apps can't switch tethering themselves, so jump to the screen that has the switch. */
    private fun openLanShare() {
        val tether = Intent().setComponent(
            ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        )
        if (!open(tether)) open(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    private fun open(i: Intent): Boolean = try {
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true
    } catch (e: Exception) {
        false
    }

    companion object {
        const val ACTION_LAN_SHARE = "com.example.quicktoggles.LAN_SHARE"
        const val ACTION_DNS = "com.example.quicktoggles.DNS"
        const val ACTION_DEV_OPTIONS = "com.example.quicktoggles.DEV_OPTIONS"
        const val ACTION_WIFI_DEBUG = "com.example.quicktoggles.WIFI_DEBUG"
    }
}
