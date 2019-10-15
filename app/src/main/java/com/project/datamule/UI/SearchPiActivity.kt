package com.project.datamule.UI

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.annotation.TargetApi
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.annotation.SuppressLint
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.Constants.Companion.ONE_NEARBY_PI
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_search_pi.*
import kotlinx.android.synthetic.main.activity_settings.ivBack
import kotlinx.android.synthetic.main.item_pi.view.*

class SearchPiActivity : AppCompatActivity() {

    companion object {
        var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    lateinit var pairedDevices: Set<BluetoothDevice>

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi)}
    private var selectedPi: Pi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_pi)

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth(bluetoothAdapter!!)
        }

        initView()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
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
        ivBack.setOnClickListener { onClickBack() }
        btnSearchPi.setOnClickListener { onClickOpenPiList() }

        //Initialize RecyclerView
        rvSearchPi.layoutManager = LinearLayoutManager(this@SearchPiActivity, RecyclerView.VERTICAL, false)
        rvSearchPi.adapter = piAdapter
    }

    private fun updateRecyclerView() {
        if (pi_s.size == ONE_NEARBY_PI) {
            tvNearbyPiTitle.text = getString(R.string.one_nearby_pi_title, pi_s.size)
        } else {
            tvNearbyPiTitle.text = getString(R.string.nearby_pi_title, pi_s.size)
        }
        piAdapter.notifyDataSetChanged()
    }

    private fun onClickOpenPiList() {

        //Hide elements of Search Pi screen
        clRectangle.visibility = View.INVISIBLE
        btnSearchPi.visibility = View.INVISIBLE
        tvNearbyPiTitle.visibility = View.VISIBLE
        tvNearbyPiDesc.visibility = View.VISIBLE

        addPairedDeviceList()
        updateRecyclerView()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun onPiClicked(clickedPi: Pi) {
        var position = pi_s.indexOf(clickedPi)
        var clickedPiItem = rvSearchPi.get(position)

        when (selectedPi) {
            null -> { //select pi
                selectedPi = clickedPi
                clickedPiItem.background = getDrawable(R.drawable.rectangle_color_green)
                clickedPiItem.tvName.setTextColor(getColor(R.color.white))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi_white))
                btnAddPi.visibility = View.VISIBLE
            } // deselect pi
            clickedPi -> {
                selectedPi = null
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
