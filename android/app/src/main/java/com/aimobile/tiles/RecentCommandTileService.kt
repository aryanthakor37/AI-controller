package com.aimobile.tiles

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.aimobile.models.CommandRequest
import com.aimobile.router.IntentRouter
import com.aimobile.widget.WidgetHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class RecentCommandTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val recentCmds = WidgetHelper.getRecentCommands(applicationContext)
        val lastCmd = recentCmds.firstOrNull() ?: "Recent Cmd"
        qsTile?.let {
            it.state = Tile.STATE_ACTIVE
            it.label = if (lastCmd.length > 12) lastCmd.take(12) + "…" else lastCmd
            it.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val recentCmds = WidgetHelper.getRecentCommands(applicationContext)
        val lastCmd = recentCmds.firstOrNull() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val router = IntentRouter(applicationContext)
            val clean = lastCmd.lowercase()
            val req = when {
                clean.contains("flashlight") || clean.contains("torch") -> CommandRequest(intent = "FLASHLIGHT_ON")
                clean.contains("camera") -> CommandRequest(intent = "OPEN_CAMERA")
                clean.contains("weather") -> CommandRequest(intent = "CHECK_WEATHER")
                else -> CommandRequest(intent = "OPEN_APP", app = lastCmd)
            }
            router.route(req)
        }
    }
}
