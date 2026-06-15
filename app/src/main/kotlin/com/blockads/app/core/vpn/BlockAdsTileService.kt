package com.blockads.app.core.vpn

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.blockads.app.domain.model.VpnState
import com.blockads.app.i18n.StringsFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockAdsTileService : TileService() {
    private var scope: CoroutineScope? = null

    override fun onStartListening() {
        super.onStartListening()
        scope?.cancel()
        scope = CoroutineScope(Dispatchers.Main + Job())

        scope?.launch {
            BlockAdsVpnService.vpnState.collectLatest { state ->
                updateTile(state)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        scope?.cancel()
        scope = null
    }

    override fun onClick() {
        super.onClick()
        val currentState = BlockAdsVpnService.vpnState.value
        val action =
            when (currentState) {
                VpnState.ACTIVE, VpnState.CONNECTING, VpnState.PAUSED -> BlockAdsVpnService.ACTION_STOP
                VpnState.STOPPED, VpnState.ERROR -> BlockAdsVpnService.ACTION_START
            }

        val intent =
            Intent(this, BlockAdsVpnService::class.java).apply {
                this.action = action
            }

        try {
            startService(intent)
        } catch (e: Exception) {
            val appIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (appIntent != null) {
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val pendingIntent =
                    android.app.PendingIntent.getActivity(
                        this,
                        0,
                        appIntent,
                        android.app.PendingIntent.FLAG_IMMUTABLE,
                    )
                startActivityAndCollapse(pendingIntent)
            }
        }
    }

    private fun updateTile(state: VpnState) {
        val tile = qsTile ?: return
        val strings = StringsFactory.getStrings(this)

        tile.label = strings.tileLabel

        when (state) {
            VpnState.ACTIVE -> {
                tile.state = Tile.STATE_ACTIVE
                tile.subtitle = strings.vpnActive
            }
            VpnState.CONNECTING -> {
                tile.state = Tile.STATE_ACTIVE
                tile.subtitle = strings.vpnConnecting
            }
            VpnState.PAUSED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = strings.pauseVpn
            }
            VpnState.STOPPED, VpnState.ERROR -> {
                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = strings.vpnStopped
            }
        }
        tile.updateTile()
    }
}
