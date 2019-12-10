package com.project.datamule.Utils

import android.content.Intent
import android.app.IntentService
import android.app.Service
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import android.R
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.NotificationCompat


class IntentService : Service() {
    private var wifiStateReceiver: BroadcastReceiver? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
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
        var date = Date();
        val formatter = SimpleDateFormat("MMM dd yyyy HH:mm:ssa")
        val answer: String = formatter.format(date)
        Log.e("START", "START START START $answer")
        //TODO: Adjust notification
        val notification = NotificationCompat.Builder(this, "1")
            .setContentTitle("title")
            .setContentText("text")
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        var date = Date();
        val formatter = SimpleDateFormat("MMM dd yyyy HH:mm:ssa")
        val answer: String = formatter.format(date)
        unregisterReceiver(wifiStateReceiver)
        Log.e("DESTROY", "DESTROY DESTROY DESTROY $answer")
        super.onDestroy()
    }
}