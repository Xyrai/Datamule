package com.project.datamule.UI

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.os.Handler
import com.project.datamule.UI.HomeActivity.Companion.bluetoothAdapter
import kotlinx.android.synthetic.main.dialog_connecting.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.TimeUnit

class SearchPiActivity : AppCompatActivity() {

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi) }
    private var selectedPi: Pi? = null
    private val mainScope = CoroutineScope(Dispatchers.IO)

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device.name != null && device.name.startsWith(Constants.PI_PREFIX_NAME)
                    ) {
                        pi_s.add(Pi(device.name, device))
                        updateRecyclerView()
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()

        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_pi)

        checkLocationPermission()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_no_bluetooth, Toast.LENGTH_SHORT).show()
            finish()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }

        var discoveryIntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(broadCastReceiver, discoveryIntentFilter)

        initView()
    }

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

    private fun checkLocationPermission(): Boolean {
        var permissionCheck = ContextCompat.checkSelfPermission(
            this@SearchPiActivity,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        permissionCheck += ContextCompat.checkSelfPermission(
            this@SearchPiActivity,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionCheck != 0) {

            ActivityCompat.requestPermissions(
                this@SearchPiActivity,
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
        bluetoothAdapter?.cancelDiscovery()
        super.finish()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    private fun initView() {
        startSearch()

        // Set the views visible/invisible
        tvNearbyPiTitle.visibility = View.VISIBLE
        tvNearbyPiDesc.visibility = View.INVISIBLE
        ivLoader2.visibility = View.VISIBLE
        ivRerunSearch.visibility = View.INVISIBLE
        clRectangle.visibility = View.INVISIBLE
        btnSearchPi.visibility = View.INVISIBLE
        rvSearchPi.visibility = View.VISIBLE
        btnAddPi.visibility = View.VISIBLE
        btnAddPi.isEnabled = false

        // Set onClick listeners
        ivBack.setOnClickListener { onClickBack() }
        btnSearchPi.setOnClickListener { onClickSearchPi() }
        btnAddPi.setOnClickListener { mainScope.launch { withContext(Dispatchers.Main) { onClickAddPi() } } }
        ivRerunSearch.setOnClickListener { startSearch() }

        // Initialize RecyclerView
        rvSearchPi.layoutManager =
            LinearLayoutManager(this@SearchPiActivity, RecyclerView.VERTICAL, false)
        rvSearchPi.adapter = piAdapter
    }

    private fun startSearch() {
        // Clear the bluetooth devices list
        pi_s.clear()

        // Run the discovery
        bluetoothAdapter?.startDiscovery()
        ivLoader2.visibility = View.VISIBLE
        ivRerunSearch.visibility = View.INVISIBLE
        btnAddPi.visibility = View.VISIBLE
        updateRecyclerView()

        var animatorSet =
            AnimatorInflater.loadAnimator(this@SearchPiActivity, R.animator.loading_animator)
        animatorSet.setTarget(ivLoader2)
        animatorSet.start()

        // After 10 seconds stop the search
        Handler().postDelayed(
            Runnable {
                if (pi_s.size == 0) {
                    stopSearch()
                } else {
                    pauseSearch()
                }
            },
            10000 // 10 seconds
        )
    }

    private fun pauseSearch() {
        bluetoothAdapter?.cancelDiscovery()
        ivLoader2.visibility = View.INVISIBLE
        ivRerunSearch.visibility = View.VISIBLE

    }

    private fun stopSearch() {
        // Stop the discovery
        bluetoothAdapter?.cancelDiscovery()

        // Hide elements of Search Pi screen
        clRectangle.visibility = View.VISIBLE
        btnSearchPi.visibility = View.VISIBLE
        btnAddPi.visibility = View.INVISIBLE
        tvNearbyPiTitle.visibility = View.INVISIBLE
        tvNearbyPiDesc.visibility = View.INVISIBLE
        rvSearchPi.visibility = View.INVISIBLE
        ivLoader2.visibility = View.INVISIBLE
        ivRerunSearch.visibility = View.INVISIBLE
    }

    private fun updateRecyclerView() {
        when {
            pi_s.size < ONE_NEARBY_PI -> {
                tvNearbyPiTitle.text = getString(R.string.searching_title)
                tvNearbyPiDesc.visibility = View.INVISIBLE
            }
            pi_s.size == ONE_NEARBY_PI -> {
                tvNearbyPiTitle.text = getString(R.string.one_nearby_pi_title, pi_s.size)
                tvNearbyPiDesc.visibility = View.VISIBLE
            }
            else -> {
                tvNearbyPiTitle.text = getString(R.string.nearby_pi_title, pi_s.size)
                tvNearbyPiDesc.visibility = View.VISIBLE
            }
        }
        piAdapter.notifyDataSetChanged()
    }

    private fun onClickSearchPi() {
        // Hide elements of Search Pi screen
        clRectangle.visibility = View.INVISIBLE
        btnSearchPi.visibility = View.INVISIBLE
        tvNearbyPiTitle.visibility = View.VISIBLE
        tvNearbyPiDesc.visibility = View.VISIBLE
        rvSearchPi.visibility = View.VISIBLE

        startSearch()
    }

    private suspend fun onClickAddPi() {
        pauseSearch()
        var device = selectedPi!!.device
        device.createBond()

        var dialog = Dialog(this@SearchPiActivity)
        dialog.setContentView(R.layout.dialog_connecting)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var animatorSet =
            AnimatorInflater.loadAnimator(this@SearchPiActivity, R.animator.loading_animator)
        animatorSet.setTarget(dialog.ivConnectingLoader)
        animatorSet.start()

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.show()

        for (i in 0..15) {
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                dialog.tvDialogTitle.text = getString(R.string.dialog_could_not_connect)
                dialog.tvSubMessage.text = getString(R.string.dialog_sub_3)
                animatorSet.end()
                dialog.ivConnectingLoader.setImageDrawable(getDrawable(R.drawable.ic_error_outline_black))

                delay(TimeUnit.SECONDS.toMillis(2))
                dialog.cancel()
                break
            }
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                dialog.tvDialogTitle.text = getString(R.string.dialog_connected)
                dialog.tvSubMessage.text = getString(R.string.dialog_sub_2)
                animatorSet.end()
                dialog.ivConnectingLoader.setImageDrawable(getDrawable(R.drawable.ic_check_black))

                delay(TimeUnit.SECONDS.toMillis(2))
                dialog.cancel()
                finish()
                break
            }

            delay(TimeUnit.SECONDS.toMillis(1))
        }

        dialog.tvDialogTitle.text = getString(R.string.dialog_could_not_connect)
        dialog.tvSubMessage.text = getString(R.string.dialog_sub_3)
        animatorSet.end()
        dialog.ivConnectingLoader.setImageDrawable(getDrawable(R.drawable.ic_error_outline_black))

        delay(TimeUnit.SECONDS.toMillis(2))
        dialog.cancel()
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun onPiClicked(clickedPi: Pi) {
        var position = pi_s.indexOf(clickedPi)
        var clickedPiItem = rvSearchPi.get(position)

        when (selectedPi) {
            null -> { // select pi
                selectedPi = clickedPi
                clickedPiItem.background = getDrawable(R.drawable.rectangle_color_green)
                clickedPiItem.tvName.setTextColor(getColor(R.color.white))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi_white))
                btnAddPi.isEnabled = true
            } // deselect pi
            clickedPi -> {
                selectedPi = null
                clickedPiItem.background = getDrawable(R.drawable.button_rectangle_custom)
                clickedPiItem.tvName.setTextColor(getColor(R.color.colorAccent))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi))
                btnAddPi.isEnabled = false
            }
            else -> {// selecting other pi
                var selectedPiItem: View? = null
                if (selectedPi != null) {
                    var positionSelected = pi_s.indexOf(selectedPi!!)
                    selectedPiItem = rvSearchPi.get(positionSelected)
                }

                // Deselect the already selected
                selectedPiItem!!.background = getDrawable(R.drawable.button_rectangle_custom)
                selectedPiItem!!.tvName.setTextColor(getColor(R.color.colorAccent))
                selectedPiItem!!.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi))

                // Select the newely clicked
                selectedPi = clickedPi
                clickedPiItem.background = getDrawable(R.drawable.rectangle_color_green)
                clickedPiItem.tvName.setTextColor(getColor(R.color.white))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi_white))
                btnAddPi.isEnabled = true
            }
        }
    }

    private fun onClickBack() {
        finish()
    }
}
