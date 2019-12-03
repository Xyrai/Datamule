package com.project.datamule.UI

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.Constants
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import com.project.datamule.Utils.IntentService
import com.project.datamule.Utils.WifiStateReceiver
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*

class HomeActivity : AppCompatActivity() {

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi) }
    private lateinit var auth: FirebaseAuth

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var pairedDevices: Set<BluetoothDevice>
    private var wifiStateReceiver: BroadcastReceiver? = null

    override fun onResume() {
        super.onResume()

        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //removes all the notifications, useful when user enters the app by clicking on the notifications
        notificationManager.cancelAll()

        pairedDeviceList()
        piAdapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val intent = Intent(this, IntentService::class.java)
        startService(intent)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth(bluetoothAdapter!!)
        }

        //Initialize Firebase Auth
        initFirebase()

        //Configures the WifiStateReceiver
        configureReceiver()

        initViews()
    }

    private fun initViews() {
        clRectangle.visibility = View.INVISIBLE

        //Initialize Buttons
        ivSettings.setOnClickListener { onClickOpenSettings() }
        btnNewPi.setOnClickListener { onClickOpenSearchPi() }

        //Initialize RecyclerView
        rvPiList.layoutManager =
            LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
        rvPiList.adapter = piAdapter

        pairedDeviceList()

        piAdapter.notifyDataSetChanged()
    }

    private fun configureReceiver() {
        val filter = IntentFilter()
        filter.addAction("com.project.datamule")
        wifiStateReceiver = WifiStateReceiver()
        registerReceiver(wifiStateReceiver, filter)
    }

    public override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    public override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiStateReceiver)
    }

    private fun buildAlertMessageNoBluetooth(bluetoothAdapter: BluetoothAdapter) {
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(R.string.bluetooth_alert_title)
//            .setMessage(R.string.bluetooth_alert_text)
//            .setCancelable(false)
//            .setPositiveButton(R.string.bluetooth_alert_positive_button
//            ) { dialog, id ->
//                // Button for going to wifi settings
//                bluetoothAdapter.enable()
////                val enableBtIntent = Intent()
////                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//            }
//            .setNegativeButton(R.string.bluetooth_alert_negative_button
//            ) { dialog, which ->
//                // Cancel button, brings you back to main activity
//                val intent = Intent(Intent.ACTION_MAIN)
//                intent.addCategory(Intent.CATEGORY_HOME)
//                startActivity(intent)
//            }
//        val alert = builder.create()
//        alert.show()
    }

    private fun onClickOpenSearchPi() {
        val intent = Intent(this, SearchPiActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
    }

    private fun onClickOpenSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun onPiClicked(clickedPi: Pi) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(PI_EXTRA, clickedPi)
        startActivity(intent)
    }

    private fun pairedDeviceList() {
        pi_s.clear()
        pairedDevices = bluetoothAdapter!!.bondedDevices

        if (pairedDevices.isNotEmpty()) {
            clRectangle.visibility = View.INVISIBLE
            for (device: BluetoothDevice in pairedDevices) {
                if (device.name.startsWith(Constants.PI_PREFIX_NAME)) pi_s.add(
                    Pi(
                        device.name,
                        device
                    )
                )
            }
        } else {
            clRectangle.visibility = View.VISIBLE
        }
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()

        //TODO: Anonymous authentication to Firebase, maybe change this later on?
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }

                // ...
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        //TODO: do something if the user is authenticated || not
        if (user != null) {
        } else {
            //TODO: do something if the user isn't authenticated
        }
    }

    private fun isBluetoothAvailable() {
        var manager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var mBluetoothAdapter = manager.adapter

        if (mBluetoothAdapter.isEnabled) {
            Toast.makeText(this, "wel bluetooth!!!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "NOOOO bluetooth!!!", Toast.LENGTH_LONG).show()
//            buildAlertMessageNoBluetooth(mBluetoothAdapter)
        }
    }

//    private fun buildAlertMessageNoBluetooth(bluetoothAdapter: BluetoothAdapter) {
////        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(R.string.bluetooth_alert_title)
//            .setMessage(R.string.bluetooth_alert_text)
//            .setCancelable(false)
//            .setPositiveButton(R.string.bluetooth_alert_positive_button
//            ) { dialog, id ->
//                // Button for going to wifi settings
//                bluetoothAdapter.enable()
////                val enableBtIntent = Intent()
////                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//            }
//            .setNegativeButton(R.string.bluetooth_alert_negative_button
//            ) { dialog, which ->
//                // Cancel button, brings you back to main activity
//                val intent = Intent(Intent.ACTION_MAIN)
//                intent.addCategory(Intent.CATEGORY_HOME)
//                startActivity(intent)
//            }
//        val alert = builder.create()
//        alert.show()
//    }
}
