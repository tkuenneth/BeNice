package de.thomaskuenneth.benice

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class BeNiceTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile.run {
            state = Tile.STATE_INACTIVE
            this.updateTile()
        }
    }

    override fun onClick() {
        val resultIntent = Intent(this, AppChooserActivity::class.java).also {
            it.addFlags(FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val resultPendingIntent: PendingIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            startActivityAndCollapse(resultPendingIntent)
        } else {
            startActivityAndCollapse(resultIntent)
        }
    }
}
