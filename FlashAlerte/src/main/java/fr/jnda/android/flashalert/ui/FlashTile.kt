package fr.jnda.android.flashalert.ui

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import fr.jnda.android.flashalert.R


@RequiresApi(Build.VERSION_CODES.N)
class FlashTile : TileService() {

    override fun onClick() {
        super.onClick()
        updateTile(true)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile(false)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile(false)
    }

    private fun updateTile(inverse: Boolean){
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        var state = preferenceManager.getBoolean("isActivate", true)

        if(inverse)
            state = !state

        preferenceManager.edit {
            putBoolean("isActivate",state)
        }
        if(state) {
            qsTile.icon = Icon.createWithResource(this, R.drawable.ic_flash_enabled)
            qsTile.label = getString(R.string.tile_enabled)
            qsTile.state = Tile.STATE_ACTIVE
        }
        else {
            qsTile.icon = Icon.createWithResource(this, R.drawable.ic_flash_disabled)
            qsTile.label = getString(R.string.tile_disabled)
            qsTile.state = Tile.STATE_INACTIVE
        }

        qsTile.updateTile()
    }
}