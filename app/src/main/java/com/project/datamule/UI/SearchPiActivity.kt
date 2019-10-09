package com.project.datamule.UI

import android.annotation.TargetApi
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_search_pi.*
import kotlinx.android.synthetic.main.activity_settings.ivBack
import kotlinx.android.synthetic.main.item_pi.view.*

class SearchPiActivity : AppCompatActivity() {

    companion object {
        var bluetoothSocket: BluetoothSocket? = null
//        lateinit var progress: ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        var isConnected: Boolean = false
    }

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var pairedDevices: Set<BluetoothDevice>

    private var pi_s = arrayListOf<Pi>()
    private lateinit var piAdapter: PiAdapter
    private var selected_pi: Pi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_pi)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth(bluetoothAdapter!!)
        }

        initView()
    }

    private fun sendCommand(input: String) {

    }

    private fun disconnect() {

    }

    private class ConnectToDevice(c: Context): AsyncTask<Void, Void, String>() {
        private var connectSucces: Boolean = true
        private val context: Context

        init {
            context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }

    }

    private fun addPairedDeviceList() {
        pairedDevices = bluetoothAdapter!!.bondedDevices
        if (pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in pairedDevices) {
                pi_s.add(Pi(device.name))
            }
        }
    }

    private fun initView() {
//        var fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf")
//        tvBack.setTypeface(fontAwesomeFont)
        ivBack.setOnClickListener { onClickBack() }
        btnSearchPi.setOnClickListener { onClickOpenPiList() }
        
    }

    private fun onClickOpenPiList() {
        piAdapter = PiAdapter(pi_s, {clickedPi: Pi -> onPiClicked(clickedPi)})

        //Hide elements of Search Pi screen
        clRectangle.visibility = View.INVISIBLE
        btnSearchPi.visibility = View.INVISIBLE
        tvNearbyPiTitle.visibility = View.VISIBLE
        tvNearbyPiDesc.visibility = View.VISIBLE

        //Initialize RecyclerView
        rvSearchPi.layoutManager = LinearLayoutManager(this@SearchPiActivity, RecyclerView.VERTICAL, false)
        rvSearchPi.adapter = piAdapter

        addPairedDeviceList()
        piAdapter.notifyDataSetChanged()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun onPiClicked(clickedPi: Pi) {
        var position = pi_s.indexOf(clickedPi)
        var clickedPiItem = rvSearchPi.get(position)

        when (selected_pi) {
            null -> { //select pi
                selected_pi = clickedPi
                clickedPiItem.background = getDrawable(R.drawable.rectangle_color_green)
                clickedPiItem.tvName.setTextColor(getColor(R.color.white))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi_white))
                btnAddPi.visibility = View.VISIBLE
            } // deselect pi
            clickedPi -> {
                selected_pi = null
                clickedPiItem.background = getDrawable(R.drawable.button_rectangle_custom)
                clickedPiItem.tvName.setTextColor(getColor(R.color.colorAccent))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi))
                btnAddPi.visibility = View.INVISIBLE
            }
            else -> //warning/shake animation
                clickedPiItem.startAnimation(AnimationUtils.loadAnimation(this,R.anim.button_shaker))
        }
    }

//        private fun isBluetoothAvailable() {
//        var manager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        var mBluetoothAdapter = manager.adapter
//
//            mBluetoothAdapter.startDiscovery()
//            mBluetoothAdapter.
//
//        if (mBluetoothAdapter.isEnabled) {
////            Toast.makeText(this, "wel bluetooth!!!", Toast.LENGTH_LONG).show()
//        } else {
////            Toast.makeText(this, "NOOOO bluetooth!!!", Toast.LENGTH_LONG).show()
////            buildAlertMessageNoBluetooth(mBluetoothAdapter)
//        }
//    }

    private fun buildAlertMessageNoBluetooth(bluetoothAdapter: BluetoothAdapter) {
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.bluetooth_alert_title)
            .setMessage(R.string.bluetooth_alert_text)
            .setCancelable(false)
            .setPositiveButton(R.string.bluetooth_alert_positive_button
            ) { dialog, id ->
                // Button for going to wifi settings
                bluetoothAdapter.enable()
//                val enableBtIntent = Intent()
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            .setNegativeButton(R.string.bluetooth_alert_negative_button
            ) { dialog, which ->
                // Cancel button, brings you back to main activity
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                startActivity(intent)
            }
        val alert = builder.create()
        alert.show()
    }

    private fun onClickBack() {
        finish()
    }
}
