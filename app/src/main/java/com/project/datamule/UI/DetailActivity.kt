package com.project.datamule.UI

import android.animation.AnimatorInflater
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.project.datamule.Constants
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.dialog_transfer.*
import kotlinx.android.synthetic.main.dialog_transfer_question.*
import kotlinx.android.synthetic.main.dialog_transfer_question.ivTransferLoader
import kotlinx.android.synthetic.main.dialog_transfer_question.tvDialogTitle
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import android.bluetooth.BluetoothDevice

const val PI_EXTRA = "PI_EXTRA"
private const val TAG = "MY_APP_DEBUG_TAG"

class DetailActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private val mainScope = CoroutineScope(Dispatchers.IO)
    val uuid = UUID.fromString("4b0164aa-1820-444e-83d4-3c702cfec373")
    private lateinit var pi: Pi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        pi = intent.getParcelableExtra<Pi>(PI_EXTRA)

        initViews()
    }

    private fun initViews() {
        // Initialize shared preferences
        prefs = getSharedPreferences("com.project.datamule", MODE_PRIVATE)

        btnTransferData.isEnabled = false

        // Initialize Buttons
        ivBack.setOnClickListener { onClickBack() }
        btnTransferData.setOnClickListener { buildDialogTransferQuestion() }
        deletePi.setOnClickListener { unpairDevice() }

        tvPiName.text = pi.name

        // Check for valid pi
        // Auto
        isValidPi()
    }

    //TODO finish unpair
    private fun unpairDevice() {
        println("TESTINGFNFGN")
        var device = pi.device.type
//        try {
//            val m = device.javaClass
//                .getMethod("removeBond",  null)
//            m.invoke(device, null as Array<Any>?)
//        } catch (e: Exception) {
//            Toast.makeText(this, "Failed to unbond", Toast.LENGTH_LONG)
//            Log.e(TAG, e.message)
//        }

    }

    private fun isValidPi() {
        var valid = true
        var btSocket = pi.device.createRfcommSocketToServiceRecord(Constants.PI_UUID)

        mainScope.launch {
////            TODO uncomment when PI resets connections
//            try {
//                btSocket.connect()
//            } catch (e: InterruptedException) {
//                valid = false
//            } catch (e: IOException) {
//                valid = false
//            } finally {
//                btSocket.close()
//            }

            withContext(Dispatchers.Main) {
                println("DATTE2 " + valid)

                if (valid) {
                    autoTransfer()
                    btnTransferData.isEnabled = true
                    println("VALIDDE " + valid)
                    Toast.makeText(applicationContext, "Ready for data transfer.", Toast.LENGTH_LONG).show()
                } else {
                    clDataAvailable.isVisible = false
                    clNoDataAvailable.isVisible = true
                    Toast.makeText(applicationContext, "Invalid Pi. No data transfer available.", Toast.LENGTH_LONG).show()
                }
            }}
    }

    private fun autoTransfer() {
        val autoTransfer = prefs!!.getBoolean("auto_transfer", true)

        if (autoTransfer) {
            println("HIERO")
            val autoTransferSeconds = prefs!!.getInt("auto_transfer_delay",  5)
            val autoTransferMillis: Long = (autoTransferSeconds * 1000).toLong()
            tvAutoTransfer.text = getString(R.string.detail_auto_transfer, autoTransferSeconds)
            ivUpdateAuto.setImageDrawable(getDrawable(R.drawable.ic_autoupdate_black))

            Handler().postDelayed(
                {
                    transferData()
                },
                autoTransferMillis // seconds x 1000 = milliseconds
            )
        } else {
            println("HIERO2")
            tvAutoTransfer.text = getString(R.string.detail_no_auto_transfer)
            ivUpdateAuto.setImageDrawable(getDrawable(R.drawable.ic_do_not_disturb_black))
        }
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
            transferData()
            dialog.cancel()
        }

//        val builder = android.app.AlertDialog.Builder(this)
//        builder.setView(layoutInflater.inflate(R.layout.dialog_transfer_question, null))
//        builder.create().show()
    }

    private fun buildTransferDialog(): Dialog {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        var maxSize = humanReadableByteCount(0, true)
        var zeroData = humanReadableByteCount(0, true)

        dialog.tvProgressText.text = getString(R.string.detail_dialog_bytes, zeroData, maxSize)

        var animatorSet = AnimatorInflater.loadAnimator(
            this@DetailActivity,
            R.animator.loading_animator
        )
        animatorSet.setTarget(dialog.ivTransferLoader)
        animatorSet.start()

        dialog.show()
        return dialog

    }

    private suspend fun buildFailedTransfer() {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer)

        //failed
        dialog.tvDialogTitle.text = getString(R.string.detail_dialog_failed_transfer, pi.name)
        dialog.textView8.text = getString(R.string.detail_dialog_failed_transfer_small)
        dialog.ivTransferLoader.setImageResource(0)
        dialog.ivTransferLoader.setBackgroundResource(R.drawable.ic_error_outline_black)
        dialog.ivTransferLoader.backgroundTintList = ContextCompat.getColorStateList(getApplicationContext(), android.R.color.holo_red_light)
        dialog.progressBar.visibility = View.GONE
        dialog.tvProgressText.visibility = View.GONE

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
        delay(TimeUnit.SECONDS.toMillis(2))
        dialog.cancel()
    }

    private suspend fun buildSuccessTransfer(packageSize: String) {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer)

        //success
        dialog.tvDialogTitle.text = getString(R.string.detail_dialog_success_transfer, packageSize, pi.name)
        dialog.textView8.text = getString(R.string.detail_dialog_success_transfer_small)
        dialog.ivSubTextSuccessfull.visibility = View.VISIBLE
        dialog.ivTransferLoader.setImageResource(R.drawable.logo_success)
        dialog.progressBar.visibility = View.GONE
        dialog.tvProgressText.visibility = View.GONE

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
        delay(TimeUnit.SECONDS.toMillis(5))
        dialog.cancel()
    }

    private fun transferData() {
        var dialog = buildTransferDialog()
        var btSocket = pi.device.createRfcommSocketToServiceRecord(uuid)

        mainScope.launch {
            try {
                btSocket.connect()
                Log.d(TAG, "Test transferData() isConnected?: " + btSocket.isConnected)
                var byte = btSocket.inputStream.read().toByte()
                Log.d(TAG, "Test transferData() Available Before?: " + btSocket.inputStream.available() + 1)
                var data = ByteArray(btSocket.inputStream.available() + 1)
                data[0] = byte

                var maxSize = humanReadableByteCount(data.size.toLong(), true)
                var zeroData = humanReadableByteCount(0, true)

                withContext(Dispatchers.Main) {
                    dialog.progressBar.max = data.size
                    dialog.progressBar.progress = 0
                    dialog.tvProgressText.text = getString(R.string.detail_dialog_bytes, zeroData, maxSize)
                }

                for (x in 0 until btSocket.inputStream.available()) {
                    data[x + 1] = btSocket.inputStream.read().toByte()
                    var transferredData = humanReadableByteCount((x + 1).toLong(), true)

                    withContext(Dispatchers.Main) {
                        dialog.progressBar.progress = x + 1
                        dialog.tvProgressText.text = getString(R.string.detail_dialog_bytes, transferredData, maxSize)
                    }
                }
                Log.d(TAG, "Test transferData() Available After?: " + btSocket.inputStream.available())
                Log.d(TAG, "Test transferData() data string?: " + String(data))
                createCacheFile(String(data))

                withContext(Dispatchers.Main) {
                    buildSuccessTransfer(maxSize)
                }
            } catch (e: IOException) {
                Log.d(TAG, e.message)
                withContext(Dispatchers.Main) { buildFailedTransfer() }
            } finally {
                btSocket.close()
                dialog.cancel()
            }

        }
    }

    private fun onClickBack() {
        finish()
    }

    private fun createCacheFile(jsonText: String) {
        var dateFromString = jsonText.substringBefore('{')
        var formattedDate = getSuffixFromDateString(dateFromString)

        //temporary create file for cache demo purposes
        val fileName = getString(R.string.data_file_prefix, formattedDate)
        val file = File(cacheDir, fileName)

        file.writeText(jsonText, Charsets.UTF_8)

        //for reading from json file
//        println(file.readText(Charsets.UTF_8))
    }

    private fun getSuffixFromDateString(date: String): String {
        return date.replace("/", "").replace(":", "").replace(" ", "")
    }

    private fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}
