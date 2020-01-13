package com.project.datamule.ui

import android.animation.AnimatorInflater
import android.app.AlertDialog
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
import com.project.datamule.Constants
import com.project.datamule.model.Pi
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
import com.project.datamule.ui.HomeActivity.Companion.bluetoothAdapter
import kotlinx.android.synthetic.main.dialog_connecting.*
import kotlin.math.ln
import kotlin.math.pow

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DetailActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var pi: Pi
    private lateinit var handler: Handler
    private lateinit var connectingDialog: Dialog

    companion object {
        const val PI_EXTRA = "PI_EXTRA"
        private const val TAG = "MY_APP_DEBUG_TAG"
    }

    /**
     * Perform initialization of all fragments.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        pi = intent.getParcelableExtra(PI_EXTRA)
        handler = Handler()

        if (!bluetoothAdapter!!.isEnabled) buildAlertMessageNoBluetooth()

        initViews()
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    override fun onResume() {
        super.onResume()

        if (!bluetoothAdapter!!.isEnabled) buildAlertMessageNoBluetooth()
    }

    /**
     * Method builds alert message dialog for
     * when there is no bluetooth available
     */
    private fun buildAlertMessageNoBluetooth() {
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

    /**
     * Initializes anything UI related
     */
    private fun initViews() {
        // Initialize shared preferences
        prefs = getSharedPreferences("com.project.datamule", MODE_PRIVATE)

        btnTransferData.isEnabled = false

        // Initialize Buttons
        ivBack.setOnClickListener { onClickBack() }
        btnTransferData.setOnClickListener { buildDialogTransferQuestion() }
        deletePi.setOnClickListener { buildDialogDeletePiQuestion() }

        tvPiName.text = pi.name

        connectingDialog = buildConnectingDialog()

        // Check if the phone has data saved from this Pi
        setsavedPiData()

        // Check for valid pi
        // (inside validPi check for Autotransfer)
        isValidPi()
    }

    /**
     * Method calculates the amount of save data from this pi that is on the phone.
     * It then sets that value in the availableData TextView in the activity.
     */
    private fun setsavedPiData() {
        var bytes: Long = 0
        var sizeOfFiles = ""

        for (file in filesDir.listFiles()) {
            if (file.isFile && file.name.startsWith(Constants.PI_PREFIX_NAME) && file.name.startsWith(pi.name)) {
                bytes += file.length()
            }
            sizeOfFiles = humanReadableByteCount(bytes)
        }

        if (bytes.toInt() == 0) {
            imageView4.backgroundTintList = ContextCompat.getColorStateList(applicationContext, R.color.deleteCacheColorDarkGreyPressed)
            tvNoDataAvailable.visibility = View.VISIBLE
            tvThereIs.visibility = View.INVISIBLE
            tvEndText.visibility = View.INVISIBLE
            availableData.visibility = View.INVISIBLE
        } else {
            imageView4.backgroundTintList = null
            tvNoDataAvailable.visibility = View.INVISIBLE
            tvThereIs.visibility = View.VISIBLE
            tvEndText.visibility = View.VISIBLE
            availableData.visibility = View.VISIBLE
        }

        availableData.text = getString(R.string.detail_data_size, sizeOfFiles)
    }


    /**
     * Method removes the selected BluetoothDevice from the paired list on the phone
     * @param device the BluetoothDevice to be unpaired
     */
    private fun unpairDevice(device: BluetoothDevice) {
        var message: String
        ioScope.launch {
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
     */
    private fun isValidPi() {
        var valid = true
        val btSocket = pi.device.createRfcommSocketToServiceRecord(Constants.PI_UUID)

        ioScope.launch {
            try {
                btSocket.connect()
            } catch (e: InterruptedException) {
                valid = false
            } catch (e: IOException) {
                valid = false
            } finally {
                btSocket.close()
            }

            withContext(Dispatchers.Main) {
                if (valid) {
                    btnTransferData.isEnabled = true
                    tvSubText1.text = getString(R.string.detail_text_active_1)
                    tvSubText2.text = getString(R.string.detail_text_sub_1)
                    connectingDialog.cancel()
                    autoTransfer()
                } else {
                    tvSubText1.text = getString(R.string.detail_text_active_2)
                    tvSubText2.text = getString(R.string.detail_text_sub_2)
                    connectingDialog.cancel()
                }
            }}
    }

    /**
     * Method checks if autoTransfer is turned on from the sharedPreferences.
     * If it is turned on it gets the auto transfer delay time and start the data transfer after that time.
     */
    private fun autoTransfer() {
        val autoTransfer = prefs!!.getBoolean("auto_transfer", false)

        if (autoTransfer) {
            val autoTransferSeconds = prefs!!.getInt("auto_transfer_delay",  5)
            val autoTransferMillis: Long = (autoTransferSeconds * 1000).toLong()
            tvAutoTransfer.text = getString(R.string.detail_auto_transfer, autoTransferSeconds)
            ivUpdateAuto.setImageDrawable(getDrawable(R.drawable.ic_autoupdate_black))

            btnTransferData.isEnabled = false
            btnTransferData.text = getString(R.string.detail_auto_transfer_btn)
            deletePi.isEnabled = false

            handler.postDelayed(
                {
                    transferData()
                },
                autoTransferMillis // seconds x 1000 = milliseconds
            )
        } else {
            tvAutoTransfer.text = getString(R.string.detail_no_auto_transfer)
            ivUpdateAuto.setImageDrawable(getDrawable(R.drawable.ic_do_not_disturb_black))
        }
    }

    private fun buildDialogTransferQuestion() {
        val dialog = Dialog(this@DetailActivity)
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
    }

    private fun buildDialogDeletePiQuestion() {
        val dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer_question)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.ivTransferLoader.setImageResource(0)
        dialog.ivTransferLoader.setBackgroundResource(R.drawable.logo_delete_drawable_large)
        dialog.ivTransferLoader.backgroundTintList = ContextCompat.getColorStateList(
            applicationContext, android.R.color.holo_red_light)
        dialog.tvDialogTitle.text = getString(R.string.detail_dialog_question_unpair_pi, pi.name)
        dialog.btnConfirmTransfer.text = getString(R.string.unpair)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.show()

        dialog.btnCancelTransfer.setOnClickListener {
            dialog.cancel()
        }

        dialog.btnConfirmTransfer.setOnClickListener {
            dialog.cancel()

            unpairDevice(pi.device)

            mainScope.launch {
                withContext(Dispatchers.Main) {
                    val unpairDialog = Dialog(this@DetailActivity)
                    unpairDialog.setContentView(R.layout.dialog_connecting)
                    unpairDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


                    val animatorSet =
                        AnimatorInflater.loadAnimator(this@DetailActivity, R.animator.loading_animator)
                    animatorSet.setTarget(unpairDialog.ivConnectingLoader)
                    animatorSet.start()

                    unpairDialog.setCanceledOnTouchOutside(false)
                    unpairDialog.setCancelable(false)

                    unpairDialog.tvDialogTitle.text = getString(R.string.detail_dialog_unpairing)

                    unpairDialog.show()
                    delay(1000)
                    animatorSet.end()
                    unpairDialog.cancel()

                    onClickBack()
                }
            }

        }
    }

    /**
     * Builds a dialog when executing transferData().
     *
     * @return Dialog - Returns a view.
     */
    private fun buildTransferDialog(): Dialog {
        val dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        val maxSize = humanReadableByteCount(0)
        val zeroData = humanReadableByteCount(0)

        dialog.tvProgressText.text = getString(R.string.detail_dialog_bytes, zeroData, maxSize)

        val animatorSet = AnimatorInflater.loadAnimator(
            this@DetailActivity,
            R.animator.loading_animator
        )
        animatorSet.setTarget(dialog.ivTransferLoader)
        animatorSet.start()

        dialog.show()
        return dialog

    }


    /**
     * Build and show a dialog when transfer fails.
     */
    private suspend fun buildFailedTransfer() {
        val dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_transfer)

        //failed
        dialog.tvDialogTitle.text = getString(R.string.detail_dialog_failed_transfer, pi.name)
        dialog.textView8.text = getString(R.string.detail_dialog_failed_transfer_small)
        dialog.ivTransferLoader.setImageResource(0)
        dialog.ivTransferLoader.setBackgroundResource(R.drawable.ic_error_outline_black)
        dialog.ivTransferLoader.backgroundTintList = ContextCompat.getColorStateList(
            applicationContext, android.R.color.holo_red_light)
        dialog.progressBar.visibility = View.GONE
        dialog.tvProgressText.visibility = View.GONE

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
        delay(TimeUnit.SECONDS.toMillis(2))
        dialog.cancel()
    }

    /**
     * Build and show a dialog when transfer succeeds.
     */
    private suspend fun buildSuccessTransfer(packageSize: String) {
        setsavedPiData()

        val dialog = Dialog(this@DetailActivity)
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
     * Build and show a dialog for while connecting with the BluetoothDevice.
     */
    private fun buildConnectingDialog(): Dialog {
        val dialog = Dialog(this@DetailActivity)
        dialog.setContentView(R.layout.dialog_connecting)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val animatorSet =
            AnimatorInflater.loadAnimator(this@DetailActivity, R.animator.loading_animator)
        animatorSet.setTarget(dialog.ivConnectingLoader)
        animatorSet.start()

        dialog.tvDialogTitle.text = getString(R.string.detail_dialog_connecting)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.show()

        return dialog
    }

    /**
     * Transfer data from the connected BluetoothSocket. Checks if the BluetoothSocket is connected and if
     * the InputStream is available.
     */
    private fun transferData() {
        val dialog = buildTransferDialog()
        val btSocket = pi.device.createRfcommSocketToServiceRecord(Constants.PI_UUID)

        mainScope.launch {
            try {
                btSocket.connect()

                val buffer = ByteArray(5120)
                var readMessage = ""

                // Initialize the progressBar with the full size and zero downloaded size
                var maxBytes = btSocket.inputStream.available()
                val maxSize = humanReadableByteCount(maxBytes.toLong())
                var transferredBytes = 0
                val transferredSize = humanReadableByteCount(transferredBytes.toLong())
                dialog.progressBar.max = maxBytes
                dialog.progressBar.progress = 0
                dialog.tvProgressText.text =
                    getString(R.string.detail_dialog_bytes, transferredSize, maxSize)

                // Data transfer is too quick for Progress Dialog to show :)
                delay(200)

                while (btSocket.inputStream.available() > 0) {
                    try {
                        val bytes = btSocket.inputStream.read(buffer)
                        readMessage += String(buffer, 0, bytes)
                    } catch (e: IOException) {
                        print(e.stackTrace)
                        break
                    }

                     // If the available bytes in the inputStream suddenly gets bigger, update the progressbar
                    if (btSocket.inputStream.available() > maxBytes) {
                        maxBytes = btSocket.inputStream.available()
                        Log.e("DataTransfer", "MAXSIZE IS GEUPDATED naar : ${humanReadableByteCount(maxBytes.toLong())}, in tekst :$maxBytes")
                    }

                    // If the bytecount exceeds the earlier counted maxBytes (maxBytes sometimes suddenly grow)
                    if (transferredBytes > maxBytes) {
                        maxBytes += (btSocket.inputStream.available())
                        Log.e("DataTransfer", "MAXSIZE IS GEUPDATED naar : ${humanReadableByteCount(maxBytes.toLong())}, in tekst :$maxBytes")
                    }

                    transferredBytes += buffer.size
                    updateProgressBarInDialog(dialog, buffer.size, maxBytes)
                    Log.e("UPDATE PROGRESS", "Huidige proces = ${dialog.progressBar.progress}, Max proces = ${maxBytes}, ")
                    // Data transfer is too quick for Progress Dialog to show :)
                    delay(200)
                }

                // Data transfer is too quick for Progress Dialog to show :)
                delay(200)

                if (readMessage !== "") {
                    Log.e("THE MESSAGE", readMessage)
                    createCacheFile(readMessage)
                }

                dialog.cancel()
                buildSuccessTransfer(humanReadableByteCount(maxBytes.toLong()))
            } catch (e: IOException) {
                Log.e(TAG, e.message)
                    dialog.cancel()
                    buildFailedTransfer()
            } finally {
                btSocket.close()
                mainScope.launch {
                    btnTransferData.text = getString(R.string.detail_data_button)
                    btnTransferData.isEnabled = true
                    deletePi.isEnabled = true
                }
                Log.d("BluetoothSocket","Socket successfully closed")
            }
        }
    }

    private fun updateProgressBarInDialog(dialog: Dialog, progress: Int, maxSize: Int) {
        dialog.progressBar.progress += progress
        dialog.progressBar.max = maxSize
        val progressText = humanReadableByteCount(dialog.progressBar.progress.toLong())
        val maxSizeText = humanReadableByteCount(maxSize.toLong())
        dialog.tvProgressText.text =
            getString(R.string.detail_dialog_bytes, progressText, maxSizeText)
    }

    private fun onClickBack() {
        finish()
    }

    override fun finish() {
        super.finish()
        handler.removeCallbacksAndMessages(null)
    }

    private fun createCacheFile(jsonText: String) {
        val dateFromString = jsonText.substring(0, 17)
        val formattedDate = getSuffixFromDateString(dateFromString)

        //temporary create file for cache demo purposes
        val fileName = getString(R.string.data_file_prefix, pi.name, formattedDate)
        val file = File(filesDir, fileName)

        file.writeText(jsonText.substring(jsonText.indexOf('{')), Charsets.UTF_8)


        // Retrieve & save the Set of cacheFiles
        val set = prefs!!.getStringSet("dataFiles", HashSet<String>())
        set.add(fileName)
        prefs!!.edit().putStringSet("dataFiles", set).apply()

        Log.e("DATAFILES", set.toString())
    }

    /**
     *
     */
    private fun getSuffixFromDateString(date: String): String {
        return date.replace("/", "").replace(":", "").replace(" ", "")
    }

    /**
     * Method returns human readable data size from Long: bytes
     * @param bytes large numbers of bytes that you want to convert to readable size
     * @param si return in metric system or not
     */
    private fun humanReadableByteCount(bytes: Long, si: Boolean = true): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }
}
