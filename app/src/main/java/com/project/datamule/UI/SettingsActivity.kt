package com.project.datamule.UI

import android.Manifest
import android.app.*
import android.content.Context
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
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.net.toFile


//1 MB = 1048576 bytes (1024 bytes * 1024 KB = 1048576 bytes = 1MB)
private const val BYTE_TO_MB_DIVIDER = 1048576.0

class SettingsActivity : AppCompatActivity() {

    private lateinit var fontAwesomeFont: Typeface

    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf")
        initView()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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

        createCacheFile()
        setStorage()
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
        val startVal = prefs!!.getBoolean("auto_transfer", true)
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

    private fun createCacheFile() {
        //temporary create file for cache demo purposes
        val fileName = "PI-data.json"
        val file = File(cacheDir, fileName)

        //this is a fake json file put inside the cache of the app
        val json = assets.open("1mbOfText.txt").bufferedReader().use {
            it.readText()
        }
        file.writeText(json, Charsets.UTF_8)

        //for reading from json file
//        println(file.readText(Charsets.UTF_8))
    }

    private fun getfolderSizeInMB(directory: File): Double {
        var bytesTotal: Long = 0
        for (file in directory.listFiles()) {
            if (file.isFile()) bytesTotal += file.length()
        }
        val sizeInMb: Double = bytesTotal / BYTE_TO_MB_DIVIDER
        return sizeInMb
    }

    fun onClickBack() {
        finish()
    }

    private fun setStorage() {
        tvTotalStorage.text =
            getString(R.string.settings_total_storage, doubleMBToStringGB(getTotalStorageInMB()))
        tvUsed.text = doubleMBToStringGB(getUsedStorageInMB()) + " GB"
        tvFree.text = doubleMBToStringGB(getFreeStorageInMB()) + " GB"
        //display in MB instead of GB
        tvCache.text = (Math.round(getCacheStorageInMB() * 100.0) / 100.0).toString() + " MB"
        setProgressBar()
    }

    private fun doubleMBToStringGB(mb: Double): String {
        // /1000 is for mb->gb, 100.0 is for amount of decimals (2)
        return (Math.round((mb / 1000) * 100.0) / 100.0).toString()
    }

    private fun getTotalStorageInMB(): Double {
        val stat = StatFs(Environment.getExternalStorageDirectory().getPath())
        val bytesTotal: Long = stat.blockSizeLong * stat.blockCountLong
        val mbTotal: Double = (bytesTotal / BYTE_TO_MB_DIVIDER)
        return mbTotal
    }

    private fun getUsedStorageInMB(): Double {
        return getTotalStorageInMB() - getFreeStorageInMB()
    }

    private fun getFreeStorageInMB(): Double {
        val path = Environment.getDataDirectory()
        val stats = StatFs(path.path)
        val bytesAvailable = stats.blockSizeLong * stats.availableBlocksLong
        val mbFree: Double = (bytesAvailable / BYTE_TO_MB_DIVIDER)
        return mbFree
    }

    private fun getCacheStorageInMB(): Double {
        if (cacheDir.listFiles() == null) return 0.0
        else return getfolderSizeInMB(cacheDir)
    }

    private fun setProgressBar() {
        //maxOfProgressbar:free, primaryprogress: usedstorage secondaryprogress: cache
        pbStorage.max = getTotalStorageInMB().toInt()

        pbStorage.progress = getUsedStorageInMB().toInt()
        val cache = getCacheStorageInMB().toInt() + getUsedStorageInMB().toInt()
        pbStorage.secondaryProgress = cache
    }

    /**
     * Clear cache example for demo on 12th of November.
     */
    private fun deleteCache() {
        val fileUri: Uri? = Uri.fromFile(File("/data/user/0/com.project.datamule/cache/PI-data.json"))

        if(fileUri?.toFile()!!.exists()) {
            fileUri.toFile().delete()
            tvCache.text = (Math.round(getCacheStorageInMB() * 100.0) / 100.0).toString() + " MB"
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
        }
    }
}
