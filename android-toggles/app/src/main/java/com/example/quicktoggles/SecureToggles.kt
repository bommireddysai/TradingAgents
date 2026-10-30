package com.example.quicktoggles

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

/** Reads and writes the system settings behind the three switches. */
class SecureToggles(private val context: Context) {

    private val resolver get() = context.contentResolver
    private val prefs = context.getSharedPreferences("toggles", Context.MODE_PRIVATE)

    var dnsHostname: String
        get() = prefs.getString(KEY_DNS_HOST, DEFAULT_DNS_HOST) ?: DEFAULT_DNS_HOST
        set(value) = prefs.edit().putString(KEY_DNS_HOST, value.trim()).apply()

    fun canWrite(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED

    fun isDnsOn(): Boolean = Settings.Global.getString(resolver, "private_dns_mode") == "hostname"

    fun setDns(on: Boolean): Boolean = write {
        if (on) {
            Settings.Global.putString(resolver, "private_dns_specifier", dnsHostname)
            Settings.Global.putString(resolver, "private_dns_mode", "hostname")
        } else {
            Settings.Global.putString(resolver, "private_dns_mode", "off")
        }
    }

    fun isDebugOn(): Boolean = Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0) == 1

    fun setDebug(on: Boolean): Boolean = write {
        val v = if (on) 1 else 0
        if (on) Settings.Global.putInt(resolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1)
        Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, v)
    }

    fun isWifiAdbOn(): Boolean = Settings.Global.getInt(resolver, "adb_wifi_enabled", 0) == 1

    /** Wireless debugging exists on Android 11+; turning it on also needs USB debugging enabled. */
    fun setWifiAdb(on: Boolean): Boolean = write {
        if (on) {
            Settings.Global.putInt(resolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1)
            Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1)
        }
        Settings.Global.putInt(resolver, "adb_wifi_enabled", if (on) 1 else 0)
    }

    private inline fun write(block: () -> Unit): Boolean =
        try {
            block()
            true
        } catch (e: SecurityException) {
            false
        }

    companion object {
        const val DEFAULT_DNS_HOST = "dns.google"
        private const val KEY_DNS_HOST = "dns_host"
    }
}
