package com.project.datamule.UI

import android.animation.AnimatorInflater
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.project.datamule.Constants
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.dialog_transfer_question.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.Runnable
import java.util.*
import java.util.concurrent.TimeUnit

const val PI_EXTRA = "PI_EXTRA"
private const val TAG = "MY_APP_DEBUG_TAG"

class DetailActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private val mainScope = CoroutineScope(Dispatchers.IO)
    val uuid = UUID.fromString("4b0164aa-1820-444e-83d4-3c702cfec373")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initViews()
    }

    private fun initViews() {
        // Initialize shared preferences
        prefs = getSharedPreferences("com.project.datamule", MODE_PRIVATE)

        btnTransferData.isEnabled = false

        // Initialize Buttons
        ivBack.setOnClickListener { onClickBack() }
        btnTransferData.setOnClickListener { buildDialogTransferQuestion() }

        val pi = intent.getParcelableExtra<Pi>(PI_EXTRA)
        if (pi != null) {
            tvPiName.text = pi.name
        }

        checkIfValidPi(pi)


    }

    private fun checkIfValidPi(pi: Pi) {
        val errorMessage = "Invalid Pi. No data transfer available."
        var failed = false

        var btSocket = pi.device.createRfcommSocketToServiceRecord(Constants.PI_UUID)

        mainScope.launch { withContext(Dispatchers.IO) {
            try {
                btSocket.connect()
            } catch (e: InterruptedException) {
                failed = true
            } catch (e: IOException) {
                failed = true
            } finally {
                btSocket.close()
            }
        }
            withContext(Dispatchers.Main) {
                if (failed) {
                    clDataAvailable.isVisible = false
                    clNoDataAvailable.isVisible = true
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                } else {
                    btnTransferData.isEnabled = true
                    // Check for auto transfer
                    autoTransfer(pi)
                }
            }}
    }

    private fun autoTransfer(pi: Pi) {
        val autoTransfer = prefs!!.getBoolean("auto_transfer", true)

        if (autoTransfer) {
            val autoTransferSeconds = prefs!!.getInt("auto_transfer_delay",  5)
            val autoTransferMillis: Long = (autoTransferSeconds * 1000).toLong()
            tvAutoTransfer.text = getString(R.string.detail_auto_transfer, autoTransferSeconds)
            ivUpdateAuto.setImageDrawable(getDrawable(R.drawable.ic_autoupdate_black))

            Handler().postDelayed(
                Runnable {
                    buildTransferDialog()
                    var btSocket = pi.device.createRfcommSocketToServiceRecord(uuid)
                    try {
                        btSocket.connect()
                    } catch (e: IOException) {
                        Log.d(TAG, e.message)
                        mainScope.launch { withContext(Dispatchers.Main) { buildFailedTransfer() } }
                    }

//                    Log.d(TAG, "TEEEST MOURAD() 1: " + btSocket.isConnected)
//                    Log.d(TAG, "TEEEST MOURAD() 3: " + btSocket.inputStream)
//                    Log.d(TAG, "TEEEST MOURAD() Available 1: " + btSocket.inputStream.available())
//                    Log.d(TAG, "TEEEST MOURAD() 4 - 1: " + btSocket.inputStream.read())
//                    Log.d(TAG, "TEEEST MOURAD() 4 - 2: " + btSocket.inputStream.read())
//                    Log.d(TAG, "TEEEST MOURAD() 4 - 3: " + btSocket.inputStream.read())
//                    Log.d(TAG, "TEEEST MOURAD() 4 - 4: " + btSocket.inputStream.read())
//                    Log.d(TAG, "TEEEST MOURAD() Available 2: " + btSocket.inputStream.available())
//                    var woordje = ""
//                    var woordje2: ByteArray = ByteArray(btSocket.inputStream.available())
//                    var bt = byteArrayOf()
//                    for (x in 0 until btSocket.inputStream.available()) {
////                    woordje = woordje + mmSocket.inputStream.read() + " "
//                        woordje2[x] = btSocket.inputStream.read().toByte()
//                    }
//                    Log.d(TAG, "TEEEST MOURAD() Available 3: " + btSocket.inputStream.available())
////                Log.d(TAG, "TEEEST MOURAD() woordje: " + woordje)
//                    Log.d(TAG, "TEEEST MOURAD() woordje string?: " + String(woordje2))
//                    createCacheFile(String(woordje2))

                },
                autoTransferMillis // seconds x 1000 = milliseconds
            )
        } else {
            tvAutoTransfer.text = getString(R.string.detail_no_auto_transfer)
            ivUpdateAuto.setImageDrawable(getDrawable(R.drawable.ic_do_not_disturb_black))
        }
    }

    private suspend fun buildFailedTransfer() {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer_data_failed)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        delay(TimeUnit.SECONDS.toMillis(2))
        dialog.cancel()
    }

    private fun buildDialogTransferQuestion() {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer_question)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        dialog.btnCancelTransfer.setOnClickListener {
            dialog.cancel()
        }

        dialog.btnConfirmTransfer.setOnClickListener {
            buildTransferDialog()
            dialog.cancel()
        }

//        val builder = android.app.AlertDialog.Builder(this)
//        builder.setView(layoutInflater.inflate(R.layout.dialog_transfer_question, null))
//        builder.create().show()
    }

    private fun buildTransferDialog() {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        var animatorSet = AnimatorInflater.loadAnimator(
            this@DetailActivity,
            R.animator.loading_animator
        )
        animatorSet.setTarget(dialog.ivTransferLoader)
        animatorSet.start()

        dialog.show()
    }

    private fun onClickBack() {
        finish()
    }

    private fun createCacheFile(jsonText: String) {
        //temporary create file for cache demo purposes
        val fileName = "PI-dataTESTTTMOURADISBAAS.json"
        val file = File(cacheDir, fileName)

        file.writeText(jsonText, Charsets.UTF_8)

        //for reading from json file
//        println(file.readText(Charsets.UTF_8))
    }
}
