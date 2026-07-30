package com.aimobile.tiles

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.aimobile.handlers.FlashlightHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class FlashlightTileService : TileService() {

    private var isTorchOn = false

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let {
            it.state = if (isTorchOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.label = "Flashlight"
            it.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        isTorchOn = !isTorchOn
        qsTile?.let {
            it.state = if (isTorchOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.updateTile()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val handler = FlashlightHandler(applicationContext)
            if (isTorchOn) handler.turnOn() else handler.turnOff()
        }
    }
}
