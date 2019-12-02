package com.project.datamule.UI

import android.animation.AnimatorInflater
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import com.project.datamule.Constants.Companion.TAG_SOCKET

const val PI_EXTRA = "PI_EXTRA"
private const val TAG = "MY_APP_DEBUG_TAG"

class DetailActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private val mainScope = CoroutineScope(Dispatchers.IO)
    val uuid = UUID.fromString("4b0164aa-1820-444e-83d4-3c702cfec373")
    private lateinit var pi: Pi
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        pi = intent.getParcelableExtra<Pi>(PI_EXTRA)
        handler = Handler()

        initViews()
    }

    private fun initViews() {
        // Initialize shared preferences
        prefs = getSharedPreferences("com.project.datamule", MODE_PRIVATE)

        btnTransferData.isEnabled = false

        // Initialize Buttons
        ivBack.setOnClickListener { onClickBack() }
        btnTransferData.setOnClickListener { buildDialogTransferQuestion() }
        deletePi.setOnClickListener { buildDialogDeletePiQuestion() }

        tvPiName.text = pi.name

        // Check for valid pi
        // Auto
        isValidPi()
    }

    private fun unpairDevice(device: BluetoothDevice) {
        var message = ""
        mainScope.launch {
            try {
                device::class.java.getMethod("removeBond").invoke(device)
                message = "Succesfully unpaired Pi"
            } catch (e: Exception) {
                Log.e(TAG, "Removing bond has been failed. ${e.message}")
                message = "Failed unpairing"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Checks if a Pi is valid for data transfer.
     * It checks if the Pi has the correct UUID and it checks if it is able to connect to the phone.
     *
     */

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

            handler.postDelayed(
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

    private fun buildDialogDeletePiQuestion() {
        var dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer_question)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.ivTransferLoader.setImageResource(0)
        dialog.ivTransferLoader.setBackgroundResource(R.drawable.logo_delete_drawable_large)
        dialog.ivTransferLoader.backgroundTintList = ContextCompat.getColorStateList(getApplicationContext(), android.R.color.holo_red_light)
        dialog.tvDialogTitle.text = getString(R.string.detail_dialog_question_unpair_pi, pi.name)
        dialog.btnConfirmTransfer.text = getString(R.string.unpair)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.show()

        dialog.btnCancelTransfer.setOnClickListener {
            dialog.cancel()
        }

        dialog.btnConfirmTransfer.setOnClickListener {
            unpairDevice(pi.device)
            dialog.cancel()
            onClickBack()

        }
    }

    /**
     * Builds a dialog when executing transferData().
     *
     * @return Dialog - Returns a view.
     */
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


    /**
     * Starting a dialog when transfer does fail.
     */
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

    /**
     * Transfer data to this device. Checks if the bluetooth socket is connected and if
     * the inputstream is avaible.
     */
    private fun transferData() {
        var dialog = buildTransferDialog()
        var btSocket = pi.device.createRfcommSocketToServiceRecord(uuid)

        mainScope.launch {
            try {
                btSocket.connect()

                // InputStream forces you to read a byte before you can see the available amount,
                // so we save the first byte
                var byte = btSocket.inputStream.read().toByte()
                Log.d(TAG, "Test transferData() Available Before?: " + btSocket.inputStream.available() + 1)

                // Get the available bytes left plus the one we pulled before
                var availableBytes = btSocket.inputStream.available() + 1

                // Initialize the byteArray with size:1024 equal to 1 kB,
                // dataText is the string that holds the string in the inputStream
                var data = ByteArray(1024)
                var dataText = ""

                // If the availableBytes in the inputStream is less than 1024 bytes (1 kB), reSet the byteArray to the actual size
                if (availableBytes < 1024) {
                    data = ByteArray(availableBytes)
                }

                // Set the byte we pulled before
                data[0] = byte


                // Initialze the progressBar with the full size and zero downloaded size
                var maxBytes = btSocket.inputStream.available() + 1
                var maxSize = humanReadableByteCount(maxBytes.toLong(), true)
                var zeroData = humanReadableByteCount(0, true)
                withContext(Dispatchers.Main) {
                    dialog.progressBar.max = maxBytes
                    dialog.progressBar.progress = 0
                    dialog.tvProgressText.text = getString(R.string.detail_dialog_bytes, zeroData, maxSize)
                }

                // 'x' is for counting the bytes
                // 'y' is for counting the progress
                var x = 1
                var y = 1

                // As long as the inputstream still returns a byte, continue..
                while (btSocket.inputStream.available() > 0) {

                    var transferredData = humanReadableByteCount((y).toLong(), true)

                        // if the 1024th byte is initialized...
                        if (data.size == 1024 && data[1023].hashCode() != 0) {

                            // put the 1 kB of text to the string and reset the byteArray
                            dataText += String(data)
                            data = ByteArray(1024)

                            // Are the available bytes less than 1 kB, set that value
                            if (availableBytes < 1024) {
                                data = ByteArray(availableBytes)
                            }

                            // Reset the byte count
                            x = 0

                            // todo deze bs printjes verwijderen
                            println("byteArray was vol... is nu weer leeg")
                            println("AVAILABLE: " + (btSocket.inputStream.available() + 1))
                            println("Huidige string: $dataText")

                            // todo dat maxsize gebeuren fixen
                            if ((btSocket.inputStream.available() + 1) > maxBytes) {
                                maxBytes = btSocket.inputStream.available() + 1
                                maxSize = humanReadableByteCount(maxBytes.toLong(), true)

                                println("MAXSIZE IS GEUPDATED naar : $maxSize, in tekst :$maxBytes")

                                withContext(Dispatchers.Main) {
                                    dialog.progressBar.max = maxBytes
                                    dialog.tvProgressText.text = getString(
                                        R.string.detail_dialog_bytes,
                                        transferredData,
                                        maxSize
                                    )
                                }
                            }
                        }

                    // Read and save the byte in the (1 kB or less) byteArray
                    data[x] = btSocket.inputStream.read().toByte()

                    // Set the current progress
                    withContext(Dispatchers.Main) {
                        dialog.progressBar.progress = y
                        dialog.tvProgressText.text = getString(R.string.detail_dialog_bytes, transferredData, maxSize)
                    }

                    // Increment the byte and progress ('x' and 'y') count
                    // Reset the availableBytes
                    x++
                    y++
                    availableBytes = btSocket.inputStream.available()
                }

                // If the last 1kB byteArray doesn't reach the 1023th index
                dataText += String(data)

//                for (x in 0 until btSocket.inputStream.available()) {
//                    data[x + 1] = btSocket.inputStream.read().toByte()
//                    var transferredData = humanReadableByteCount((x + 1).toLong(), true)
//
////                    println("HIEROOO222 " + )
//
//                    withContext(Dispatchers.Main) {
//                        dialog.progressBar.progress = x + 1
//                        dialog.tvProgressText.text = getString(R.string.detail_dialog_bytes, transferredData, maxSize)
//                    }
//                }

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
                Log.d(TAG_SOCKET,"Socket successfully closed")
                dialog.cancel()
            }

        }
    }

    private fun onClickBack() {
        finish()
    }

    override fun finish() {
        super.finish()
        handler.removeCallbacksAndMessages(null)
    }

    private fun createCacheFile(jsonText: String) {
        var dateFromString = jsonText.substringBefore('{')
        var formattedDate = getSuffixFromDateString(dateFromString)

        //temporary create file for cache demo purposes
        val fileName = getString(R.string.data_file_prefix, formattedDate)
        val file = File(filesDir, fileName)

        file.writeText(jsonText.substringAfter('{'), Charsets.UTF_8)


        // Retrieve & save the Set of cacheFiles
        val set = prefs!!.getStringSet("dataFiles", HashSet<String>())
        set.add(fileName)
        prefs!!.edit().putStringSet("dataFiles", set).apply()

        Log.e("DATAFILES", set.toString())

//        println(set.)

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
