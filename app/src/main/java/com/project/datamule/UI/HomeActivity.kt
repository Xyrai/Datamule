package com.project.datamule.UI

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*

class HomeActivity : AppCompatActivity() {

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s, {clickedPi: Pi -> onPiClicked(clickedPi)})

    override fun onCreate(savedInstanceState: Bundle?) {
//        isBluetoothAvailable()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
    }

    fun initViews() {
        //Initialize Buttons
        ivSettings.setOnClickListener { onClickOpenSettings() }

        btnNewPi.setOnClickListener { onClickOpenSearchPi() }

        //Initialize RecyclerView
        rvPiList.layoutManager = LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
        rvPiList.adapter = piAdapter

        for (i in Pi.PI_S.indices) {
            pi_s.add(Pi(Pi.PI_S[i]))
        }
        piAdapter.notifyDataSetChanged()
    }

    fun onClickOpenSearchPi() {
        val intent = Intent(this, SearchPiActivity::class.java)
        startActivity(intent)
    }

    fun onClickOpenSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun onPiClicked(clickedPi: Pi) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(PI_EXTRA, clickedPi)
        startActivity(intent)
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


}
