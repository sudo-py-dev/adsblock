package com.blockads.app.core.util

import android.content.Context
import android.os.PowerManager
import android.provider.Settings

object DnsSettingsHelper {
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isPrivateDnsStrict(context: Context): Boolean {
        return try {
            val mode = Settings.Global.getString(context.contentResolver, "private_dns_mode")
            mode == "hostname"
        } catch (e: Exception) {
            false
        }
    }
}
