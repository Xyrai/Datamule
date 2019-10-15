package com.project.datamule.UI

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.Constants
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

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi)}
    private var selectedPi: Pi? = null

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device.name != null) {
                        pi_s.add(Pi(device.name))
                        updateRecyclerView()
                    }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_pi)
        rvSearchPi.visibility = View.INVISIBLE

        checkLocationPermission()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
//            buildAlertMessageNoBluetooth(bluetoothAdapter!!)
        }

        var discoveryIntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(broadCastReceiver, discoveryIntentFilter)

        initView()
    }

    private fun checkLocationPermission(): Boolean {
        var permissionCheck = ContextCompat.checkSelfPermission(this@SearchPiActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionCheck += ContextCompat.checkSelfPermission(this@SearchPiActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permissionCheck != 0) {

            ActivityCompat.requestPermissions(this@SearchPiActivity,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), Constants.REQUEST_ACCESS_LOCATION
            )
            return false
        } else {
            return true
        }
    }

    override fun finish() {
        unregisterReceiver(broadCastReceiver)
        super.finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
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
        rvSearchPi.visibility = View.VISIBLE

        bluetoothAdapter.startDiscovery()
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

    private fun onClickBack() {
        finish()
    }
}
