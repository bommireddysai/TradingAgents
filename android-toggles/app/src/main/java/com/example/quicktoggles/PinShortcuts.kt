package com.example.quicktoggles

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon

/** Asks the launcher to pin the four shortcuts to the home screen. */
object PinShortcuts {

    private val all = listOf(
        Triple("lan_share", "LAN share", ShortcutActivity.ACTION_LAN_SHARE),
        Triple("dns", "DNS on/off", ShortcutActivity.ACTION_DNS),
        Triple("dev_options", "Developer options", ShortcutActivity.ACTION_DEV_OPTIONS),
        Triple("wifi_debug", "Wi-Fi debugging", ShortcutActivity.ACTION_WIFI_DEBUG),
    )

    fun requestAll(context: Context): Boolean {
        val sm = context.getSystemService(ShortcutManager::class.java)
        if (!sm.isRequestPinShortcutSupported) return false
        all.forEach { (id, label, action) ->
            val info = ShortcutInfo.Builder(context, id)
                .setShortLabel(label)
                .setIcon(Icon.createWithResource(context, R.drawable.ic_tile))
                .setIntent(
                    Intent(action).setClass(context, ShortcutActivity::class.java)
                )
                .build()
            sm.requestPinShortcut(info, null)
        }
        return true
    }
}
