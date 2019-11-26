package com.project.datamule.Utils

import android.content.Intent
import android.app.IntentService
import android.app.Service
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.wifi.WifiManager


class IntentService : IntentService("BackgroundService") {
    private var wifiStateReceiver: BroadcastReceiver? = null

    override fun onHandleIntent(intent : Intent) {
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction("com.project.datamule")
        wifiStateReceiver = WifiStateReceiver()
        registerReceiver(wifiStateReceiver, filter)

        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onHandleIntent(intent)
        return Service.START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(wifiStateReceiver)
        super.onDestroy()
    }
}