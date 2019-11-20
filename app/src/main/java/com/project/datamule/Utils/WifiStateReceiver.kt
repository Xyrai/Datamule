package com.project.datamule.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.widget.Toast

class WifiStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val fireBase = Firebase
        val wifiStateExtra =
            intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

        when (wifiStateExtra) {
            WifiManager.WIFI_STATE_ENABLED -> {
                Toast.makeText(context, "Connecting to wifi", Toast.LENGTH_LONG).show()
                fireBase.uploadFile(context)
            }

            WifiManager.WIFI_STATE_DISABLED -> {
                Toast.makeText(context, "No wifi connection", Toast.LENGTH_LONG).show()
            }
        }
    }
}
