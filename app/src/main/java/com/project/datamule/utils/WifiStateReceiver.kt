package com.project.datamule.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import com.project.datamule.Constants.Companion.TAG_WIFI_STATUS

/**
 * Class to check if user's device has WIFI disabled, on or is connected to a network
 */
class WifiStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val fireBase = Firebase
        val wifiStateExtra =
            intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

        when (wifiStateExtra) {
            WifiManager.WIFI_STATE_ENABLED -> {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                val netInfo = cm!!.activeNetworkInfo
                if (netInfo != null) {
                    cm.allNetworks.forEach { network ->
                        cm.getNetworkInfo(network).apply {
                            if (type == ConnectivityManager.TYPE_WIFI) {
                                Log.e("WIFI-STATUS: ", "CONNECTED TO NETWORK")
                                fireBase.uploadFile(context)
                            }
                        }
                    }
                }
                Log.e(TAG_WIFI_STATUS, "NO CONNECTION")
                Log.e(TAG_WIFI_STATUS, "ON")
            }

            WifiManager.WIFI_STATE_DISABLED -> {
                Log.e(TAG_WIFI_STATUS, "OFF")
            }
        }
    }
}
