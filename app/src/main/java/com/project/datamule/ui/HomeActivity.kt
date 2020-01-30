package com.project.datamule.ui

import android.app.AlertDialog
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import com.project.datamule.adapter.PiAdapter
import com.project.datamule.Constants
import com.project.datamule.model.Pi
import com.project.datamule.R
import com.project.datamule.ui.DetailActivity.Companion.PI_EXTRA
import com.project.datamule.utils.WifiStateReceiver
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*

/**
 * Class for the base of the application
 */
class HomeActivity : AppCompatActivity() {

    companion object {
        var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    }

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi) }
    private lateinit var auth: FirebaseAuth
    lateinit var pairedDevices: Set<BluetoothDevice>
    private var wifiStateReceiver: BroadcastReceiver? = null

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    override fun onResume() {
        super.onResume()

        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //removes all the notifications, useful when user enters the app by clicking on the notifications
        notificationManager.cancelAll()

        updatePiList()
    }

    /**
     * Perform initialization of all fragments.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //TODO: Fix implementation of the Background service/Job Scheduler
        //val intent = Intent(this, BackgroundService::class.java)
        //startService(intent)

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }

        //Initialize Firebase Auth
        initFirebase()

        //Configures the WifiStateReceiver
        configureReceiver()

        initViews()
    }

    /**
     * Initializes anything UI related
     */
    private fun initViews() {
        clRectangle.visibility = View.INVISIBLE

        //Initialize Buttons
        ivSettings.setOnClickListener { onClickOpenSettings() }
        btnNewPi.setOnClickListener { onClickOpenSearchPi() }

        //Initialize RecyclerView
        rvPiList.layoutManager =
            LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
        rvPiList.adapter = piAdapter

        updatePiList()
    }

    /**
     * Updates the Pi List on the home screen
     */
    private fun updatePiList() {
        pairedDeviceList()
        piAdapter.notifyDataSetChanged()
    }

    /**
     * Configures the WifiStateReceiver
     */
    private fun configureReceiver() {
        val filter = IntentFilter()
        filter.addAction("com.project.datamule")
        wifiStateReceiver = WifiStateReceiver()
        registerReceiver(wifiStateReceiver, filter)
    }

    /**
     * On Activity Start register the wifiStateReceiver
     */
    public override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    /**
     * On Activity Stop unregister the wifiStateReceiver
     */
    public override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiStateReceiver)
    }

//    private fun buildAlertMessageNoBluetooth(bluetoothAdapter: BluetoothAdapter) {
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
//    }

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

    /**
     * Opens {DetailActivity} with the info of the pi that has been clicked
     */
    private fun onPiClicked(clickedPi: Pi) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(PI_EXTRA, clickedPi)
        startActivity(intent)
    }

    /**
     * Adds all paired devices with the correct prefix to the Device List
     */
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
        }

        if (pi_s.isEmpty()) {
            clRectangle.visibility = View.VISIBLE
        }
    }

    /**
     * Firebase initialization for authentication & file uploads
     */
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
            }
    }

    //TODO: Add authentication rules here if needed
    /**
     * Updates the Firebase interface when a user logs in
     * @params user Information of the person using the Firebase service
     */
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {

        } else {

        }
    }

    //TODO: Duplicate method
    /**
     * Method builds alert message dialog for
     * when there is no bluetooth available
     */
    private fun buildAlertMessageNoBluetooth() {
        AlertDialog.Builder(this)
            .setTitle(R.string.bluetooth_alert_title)
            .setMessage(R.string.bluetooth_alert_text)
            .setCancelable(false)
            .setPositiveButton(R.string.bluetooth_alert_positive_button)
            { _, _ ->
                bluetoothAdapter?.enable()
                updatePiList()
            }
            .setNegativeButton(R.string.bluetooth_alert_negative_button)
            { _, _ ->
                finish()
            }
            .create()
            .show()
    }
}
