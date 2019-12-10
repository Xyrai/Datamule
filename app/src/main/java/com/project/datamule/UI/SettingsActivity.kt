package com.project.datamule.UI

import android.Manifest
import android.app.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_settings.tvPushArrowInfo
import kotlinx.android.synthetic.main.dialog_change_log.*
import kotlinx.android.synthetic.main.dialog_change_log.ivClose
import kotlinx.android.synthetic.main.dialog_support.*
import android.net.Uri
import com.project.datamule.Constants
import java.io.File
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.os.*
import androidx.core.content.ContextCompat.getColor
import com.project.datamule.R
import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.net.wifi.WifiManager
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.net.toFile
import com.project.datamule.UI.HomeActivity.Companion.bluetoothAdapter
import com.project.datamule.Utils.WifiStateReceiver


//1 MB = 1048576 bytes (1024 bytes * 1024 KB = 1048576 bytes = 1MB)
private const val BYTE_TO_MB_DIVIDER = 1048576.0

class SettingsActivity : AppCompatActivity() {

    private lateinit var fontAwesomeFont: Typeface
    private var prefs: SharedPreferences? = null
    private var wifiStateReceiver : BroadcastReceiver? =  null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf")

        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }
        initView()
    }

    override fun onResume() {
        super.onResume()

        if (!bluetoothAdapter!!.isEnabled) {
            buildAlertMessageNoBluetooth()
        }    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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

    private fun initView() {
        prefs = getSharedPreferences("com.project.datamule", MODE_PRIVATE)

        tvPushArrow.setTypeface(fontAwesomeFont)
        tvPushArrowInfo.setTypeface(fontAwesomeFont)
        ivBack.setOnClickListener { onClickBack() }
        clChangeLog.setOnClickListener { buildChangeLogDialog() }
        clSupport.setOnClickListener { buildSupportDialog() }
        btnDeleteCache.setOnClickListener { deleteCache() }

        pushNotificationSwitch()
        autoTransfer()

        configureReceiver()

        setStorage()
    }

    private fun configureReceiver() {
        val filter = IntentFilter()
        filter.addAction("com.project.datamule")
        wifiStateReceiver = WifiStateReceiver()
        registerReceiver(wifiStateReceiver, filter)
    }

    public override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    public override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiStateReceiver)
    }

    private fun pushNotificationSwitch() {
        sPushNotification.setChecked(prefs!!.getBoolean("notifications", true))

        clPushNotification.setOnClickListener {
            if(sPushNotification.isChecked) {
                sPushNotification.setChecked(false)
                setPushNotification(false)
            } else {
                sPushNotification.setChecked(true)
                setPushNotification(true)
            }
        }
        sPushNotification.setOnClickListener {
            if(sPushNotification.isChecked) setPushNotification(true)
            else setPushNotification(false)
        }
    }

    private fun autoTransfer() {
        val largeHeight = dpToPx(140)
        val smallHeight = dpToPx(40)

        //init of the constraintlayout
        val startVal = prefs!!.getBoolean("auto_transfer", false)
        if(startVal) valueAnimator(clAutoTransfer, smallHeight, largeHeight)
        sAutoTransfer.setChecked(startVal)

        //seekbar delay
        sbDelay.incrementProgressBy(1)
        val delay = prefs!!.getInt("auto_transfer_delay", 5)
        sbDelay.setProgress(delay)
        tvDelay.text = getString(R.string.settings_delay_sec, delay)

        sbDelay.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tvDelay.text = getString(R.string.settings_delay_sec, progress)
                prefs!!.edit().putInt("auto_transfer_delay", progress).apply()

            }
        })

        clAutoTransfer.setOnClickListener {
            if(sAutoTransfer.isChecked) {
                sAutoTransfer.setChecked(false)
                setAutoTransferPreference(false)
                //change height to small
                valueAnimator(clAutoTransfer, largeHeight, smallHeight)
            } else {
                sAutoTransfer.setChecked(true)
                setAutoTransferPreference(true)
                //set height larger
                valueAnimator(clAutoTransfer, smallHeight, largeHeight)
            }
        }

        sAutoTransfer.setOnClickListener {
            if(sAutoTransfer.isChecked) {
                //set height larger
                valueAnimator(clAutoTransfer, smallHeight, largeHeight)
                setAutoTransferPreference(true)
            } else {
                //change height to small
                valueAnimator(clAutoTransfer, largeHeight, smallHeight)
                setAutoTransferPreference(false)


            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density: Float = this.resources.displayMetrics.density
        return Math.round(dp * density)
    }

    private fun valueAnimator(cl: ConstraintLayout, startValue: Int, endValue: Int) {
        val va = ValueAnimator.ofInt(startValue, endValue)
        va.duration = 400
        va.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            cl.getLayoutParams().height = value
            cl.requestLayout()
        }
        va.start()
    }

    private fun setPushNotification(allowPushNotification: Boolean) {
        if(allowPushNotification) makeNotification("Push notifications", "Push notifications are now turned on.", 0)
        else makeNotification("Push notifications", "Push notifications are now turned off.", 0)
        setNotificationPreference(allowPushNotification)
    }

    private fun setNotificationPreference(allowPushNotification: Boolean) {
        prefs!!.edit().putBoolean("notifications", allowPushNotification).apply()
    }

    private fun setAutoTransferPreference(autoTransfer: Boolean) {
        prefs!!.edit().putBoolean("auto_transfer", autoTransfer).apply()
    }

    private fun makeNotification(title: String, content: String, notificationID: Int) {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationIntent = Intent(this, HomeActivity::class.java)

        // Create the TaskStackBuilder
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(notificationIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        var builder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_no_text)
            .setColor(getColor(this, R.color.colorPrimary))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, importance).apply {
                description = "channel"
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationID, builder.build())
    }

    private fun buildChangeLogDialog() {
        var dialog = Dialog(this@SettingsActivity)
        dialog.setContentView(R.layout.dialog_change_log)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.tvChangeLog.text = assets.open("1mbOfText.txt").bufferedReader().use {
            it.readText().substring(0, 700)
        }

        dialog.tvChangeLog.movementMethod = ScrollingMovementMethod()
        dialog.ivClose.setOnClickListener { dialog.cancel() }
        dialog.show()
    }

    private fun Boolean.toInt() = if (this) 1 else 0

    private fun buildSupportDialog() {
        var dialog = Dialog(this@SettingsActivity)
        dialog.setContentView(R.layout.dialog_support)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.tvPhoneIcon.setTypeface(fontAwesomeFont)
        dialog.tvEmailIcon.setTypeface(fontAwesomeFont)
        dialog.tvPushArrowInfo.setTypeface(fontAwesomeFont)
        dialog.tvPushArrowInfo2.setTypeface(fontAwesomeFont)


        dialog.clPhone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@SettingsActivity,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@SettingsActivity,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    Constants.REQUEST_PHONE_CALL
                )
            } else openPhoneApp()

        }

        dialog.clEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + Constants.DATAMULE_EMAIL))
            startActivity(intent)
        }

        dialog.ivClose.setOnClickListener { dialog.cancel() }
        dialog.show()
    }

    private fun openPhoneApp() {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Constants.DATAMULE_PHONE_NUMBER))
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.REQUEST_PHONE_CALL -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) openPhoneApp()
                return
            }
        }
    }

//    private fun copyToClipboard(text: String) {
//        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//        val clip = ClipData.newPlainText("DataMule", text)
//        clipboard.setPrimaryClip(clip)
//    }

    private fun getfolderSize(directory: File): Long {
        var bytesTotal: Long = 0
        for (file in directory.listFiles()) {
            if (file.isFile()) bytesTotal += file.length()
        }
        return bytesTotal
    }

    fun onClickBack() {
        finish()
    }

    private fun setStorage() {
        tvTotalStorage.text = getString(R.string.settings_total_storage, humanReadableByteCount(getTotalStorage()))
        tvUsed.text = humanReadableByteCount(getUsedStorage())
        tvFree.text = humanReadableByteCount(getFreeStorage())
        tvCache.text = humanReadableByteCount(getCacheStorage())
        setProgressBar()
    }

    private fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().getPath())
        val bytesTotal: Long = stat.blockSizeLong * stat.blockCountLong
        return bytesTotal
    }

    private fun getUsedStorage(): Long {
        return getTotalStorage() - getFreeStorage()
    }

    private fun getFreeStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().getPath())
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        return bytesAvailable
    }

    private fun getCacheStorage(): Long {
        if (filesDir.listFiles() == null) return 0
        else return getfolderSize(filesDir)
    }

    private fun setProgressBar() {
        //maxOfProgressbar:free, primaryprogress: usedstorage secondaryprogress: cache
        val total = (getTotalStorage() / BYTE_TO_MB_DIVIDER).toInt()
        val used = (getUsedStorage() / BYTE_TO_MB_DIVIDER).toInt()
        val cache = (getCacheStorage() / BYTE_TO_MB_DIVIDER).toInt() + used

        pbStorage.max = total
        pbStorage.progress = used
        pbStorage.secondaryProgress = cache
    }

    /**
     * Clear cache example for demo on 12th of November.
     */
    //TODO: Loop through filesDir
    private fun deleteCache() {
        var basePath = this.cacheDir.toString()
        var fileName = "/PI-data.json"
        val fileUri: Uri? = Uri.fromFile(File(basePath + fileName))

        if(fileUri?.toFile()!!.exists()) {
            fileUri.toFile().delete()
            tvCache.text = (Math.round(getCacheStorage() * 100.0) / 100.0).toString() + " MB"
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun humanReadableByteCount(bytes: Long, si: Boolean = true): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}
