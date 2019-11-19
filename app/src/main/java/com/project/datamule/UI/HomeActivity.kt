package com.project.datamule.UI

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.Constants
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import java.io.File

class HomeActivity : AppCompatActivity() {

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi) }
    // Create a storage reference from our app
    private val storageRef = FirebaseStorage.getInstance().reference
    //TODO: Change this to where you want to safe it
    //Example data/test.txt creates a folder: data, in the storage with the file test.txt in it
    private var fileRef: StorageReference = storageRef.child("DoesItWork.json")
    //TAG for Logs
    private val TAG = "HomeActivity"
    private lateinit var auth: FirebaseAuth
    private var PROGRESS_MAX = 100
    private var PROGRESS_CURRENT = 0

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var pairedDevices: Set<BluetoothDevice>

    private val wifiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var wifiStateExtra =
                intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

            when (wifiStateExtra) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    Toast.makeText(context, "Wifi connection", Toast.LENGTH_LONG).show()
                    uploadFile()
                }
                WifiManager.WIFI_STATE_DISABLED -> {
                    Toast.makeText(context, "No wifi connection", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth(bluetoothAdapter!!)
        }

        // Initialize Firebase Auth
        initFirebase()

        //Upload file when connected to wifi
//        uploadFile()

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

    public override fun onStart() {
        super.onStart()
        var intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
    }

    public override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiStateReceiver)
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
            for (device: BluetoothDevice in pairedDevices) {
                if(device.name.startsWith(Constants.PI_PREFIX_NAME)) pi_s.add(Pi(device.name, device))
            }
        } else {
            clRectangle.visibility = View.VISIBLE
        }
    }


//
//    /**
//     * Sets up the options menu.
//     * @param menu The options menu.
//     * @return Boolean.
//     */
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_activity_home, menu)
//        return true
//    }
//
//    /**
//     * Handles a click on the menu option to get a place.
//     * @param item The menu item to handle.
//     * @return Boolean.
//     */
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.getItemId() === R.id.action_settings) {
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
//        }
//        return true
//    }

//    private fun isBluetoothAvailable() {
//        var manager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        var mBluetoothAdapter = manager.adapter
//
//        if (mBluetoothAdapter.isEnabled) {
//            Toast.makeText(this, "wel bluetooth!!!", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(this, "NOOOO bluetooth!!!", Toast.LENGTH_LONG).show()
////            buildAlertMessageNoBluetooth(mBluetoothAdapter)
//        }
//    }

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

//    private fun getConnectionType(context: Context): Boolean {
//        var result = false
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            cm?.run {
//                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
//                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                        result = true
//                    }
//                }
//            }
//        } else {
//            cm?.run {
//                cm.activeNetworkInfo?.run {
//                    if (type == ConnectivityManager.TYPE_WIFI) {
//                        result = true
//                    }
//                }
//            }
//        }
//        return result
//    }

    private fun updateUI(user: FirebaseUser?) {
        //TODO: do something if the user is authenticated || not
        if (user != null) {
        } else {
            //TODO: do something if the user isn't authenticated
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

    private fun uploadFile() {
//        val networkResult = getConnectionType(this)
        var basePath = this.cacheDir.toString()
        var fileName = "/PI-data.json"
        val fileUri: Uri? = Uri.fromFile(File(basePath + fileName))

        if (!fileUri?.toFile()!!.exists()) {
            Toast.makeText(this, "No file found", Toast.LENGTH_LONG).show()
            return
        }
        fileRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                Log.e(TAG, "Uri: " + taskSnapshot.uploadSessionUri)
                Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                uploadFinishedNotification(fileUri.toFile().name)
                // Uri: taskSnapshot.downloadUrl
                // Name: taskSnapshot.metadata!!.name
                // Path: taskSnapshot.metadata!!.path
                // Size: taskSnapshot.metadata!!.sizeBytes
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful uploads
            }
            .addOnProgressListener { taskSnapshot ->
                PROGRESS_MAX = taskSnapshot.totalByteCount.toInt() / 1000
                PROGRESS_CURRENT = taskSnapshot.bytesTransferred.toInt() / 1000
                makeNotification("Uploading file", "$PROGRESS_CURRENT KB / $PROGRESS_MAX KB", 0)
            }
            .addOnPausedListener { taskSnapshot ->
                // Upload is paused
            }
    }


    private fun makeNotification(title: String, content: String, notificationID: Int) {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var builder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_no_text)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).apply {
            // Issue the initial notification with zero progress
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            notify(0, builder.build())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    importance
                ).apply {
                    description = "channel"
                }
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(notificationID, builder.build())
        }

    }

    private fun uploadFinishedNotification(fileName: String) {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var builder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_no_text)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle("File uploaded")
            .setContentText("Done.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).apply {

            builder.setContentText(fileName)
                .setProgress(0, 0, false)
            notify(0, builder.build())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    importance
                ).apply {
                    description = "channel"
                }
                // Register the channel with the system
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0, builder.build())
        }

    }
}
