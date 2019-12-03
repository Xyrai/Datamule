package com.project.datamule.UI

import android.annotation.TargetApi
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.Constants.Companion.ONE_NEARBY_PI
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_new_user.*
import kotlinx.android.synthetic.main.item_pi.view.*
import android.graphics.Paint


class NewUserActivity : AppCompatActivity() {

    var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    lateinit var pairedDevices: Set<BluetoothDevice>

    private var pi_s = arrayListOf<Pi>()
    private lateinit var piAdapter: PiAdapter
    private var selectedPi: Pi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user)

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }

        initView()
    }

    override fun onResume() {
        super.onResume()

        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }
    }

    private fun addPairedDeviceList() {
        pairedDevices = bluetoothAdapter!!.bondedDevices
        if (pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in pairedDevices) {
                pi_s.add(Pi(device.name, device))
            }
        }
    }

    private fun initView() {
//        var fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf")
//        tvBack.setTypeface(fontAwesomeFont)
        tvSkipStep.paintFlags = tvSkipStep.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvSkipStep.setOnClickListener { onClickSkipToHome()}
        btnNewSearchPi.setOnClickListener { onClickOpenPiList() }
    }

    private fun updateUI() {
        if (pi_s.size == ONE_NEARBY_PI) {
            tvNewNearbyPiTitle.text = getString(R.string.one_nearby_pi_title, pi_s.size)
        } else {
            tvNewNearbyPiTitle.text = getString(R.string.nearby_pi_title, pi_s.size)
        }
    }

    private fun onClickOpenPiList() {
        piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi)}

        //Hide elements of Search Pi screen
        clRectangle.visibility = View.INVISIBLE
        btnNewSearchPi.visibility = View.INVISIBLE
        tvNewUserHey.visibility = View.INVISIBLE
        tvNewUserToApp.visibility = View.INVISIBLE

        //Show elements of Search Pi screen
        tvNewNearbyPiTitle.visibility = View.VISIBLE
        tvNewNearbyPiDesc.visibility = View.VISIBLE

        //Initialize RecyclerView
        rvNewSearchPi.layoutManager = LinearLayoutManager(this@NewUserActivity, RecyclerView.VERTICAL, false)
        rvNewSearchPi.adapter = piAdapter

        addPairedDeviceList()
        piAdapter.notifyDataSetChanged()
        updateUI()
    }

    private fun onClickSkipToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun onPiClicked(clickedPi: Pi) {
        var position = pi_s.indexOf(clickedPi)
        var clickedPiItem = rvNewSearchPi.get(position)

        when (selectedPi) {
            null -> { //select pi
                selectedPi = clickedPi
                clickedPiItem.background = getDrawable(R.drawable.rectangle_color_green)
                clickedPiItem.tvName.setTextColor(getColor(R.color.white))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi_white))
                btnNewAddPi.visibility = View.VISIBLE
            } // deselect pi
            clickedPi -> {
                selectedPi = null
                clickedPiItem.background = getDrawable(R.drawable.button_rectangle_custom)
                clickedPiItem.tvName.setTextColor(getColor(R.color.colorAccent))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi))
                btnNewAddPi.visibility = View.INVISIBLE
            }
            else -> //warning/shake animation
                clickedPiItem.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_shaker))
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
//            Toast.makeText(this, "wel bluetooth!!!", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(this, "NOOOO bluetooth!!!", Toast.LENGTH_LONG).show()
//            buildAlertMessageNoBluetooth(mBluetoothAdapter)
//        }
//    }

    fun buildAlertMessageNoBluetooth() {
        AlertDialog.Builder(this)
            .setTitle(R.string.bluetooth_alert_title)
            .setMessage(R.string.bluetooth_alert_text)
            .setCancelable(false)
            .setPositiveButton(R.string.bluetooth_alert_positive_button)
            { _, _ ->
                bluetoothAdapter?.enable()
            }
            .setNegativeButton(R.string.bluetooth_alert_negative_button)
            { _, _ ->
                finish()
            }
            .create()
            .show()
    }
}