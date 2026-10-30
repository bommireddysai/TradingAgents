package com.example.quicktoggles

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

/** Shared behaviour for the three Quick Settings tiles. */
abstract class ToggleTileService : TileService() {

    protected abstract fun isOn(t: SecureToggles): Boolean
    protected abstract fun set(t: SecureToggles, on: Boolean): Boolean

    override fun onStartListening() = render()

    override fun onClick() {
        val toggles = SecureToggles(this)
        if (!set(toggles, !isOn(toggles))) {
            Toast.makeText(this, "Grant WRITE_SECURE_SETTINGS first (see README)", Toast.LENGTH_LONG).show()
        }
        render()
    }

    private fun render() {
        val tile = qsTile ?: return
        val toggles = SecureToggles(this)
        tile.state = when {
            !toggles.canWrite() -> Tile.STATE_UNAVAILABLE
            isOn(toggles) -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }
}

class DnsTileService : ToggleTileService() {
    override fun isOn(t: SecureToggles) = t.isDnsOn()
    override fun set(t: SecureToggles, on: Boolean) = t.setDns(on)
}

class DebugTileService : ToggleTileService() {
    override fun isOn(t: SecureToggles) = t.isDebugOn()
    override fun set(t: SecureToggles, on: Boolean) = t.setDebug(on)
}

class WifiAdbTileService : ToggleTileService() {
    override fun isOn(t: SecureToggles) = t.isWifiAdbOn()
    override fun set(t: SecureToggles, on: Boolean) = t.setWifiAdb(on)
}
