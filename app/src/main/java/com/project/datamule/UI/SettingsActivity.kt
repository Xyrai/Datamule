package com.project.datamule.UI

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import kotlinx.android.synthetic.main.activity_settings.*
import android.graphics.Typeface
import com.project.datamule.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
    }

    private fun initView() {
        var fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf")
        tvPushArrow.setTypeface(fontAwesomeFont)
        tvPushArrowInfo.setTypeface(fontAwesomeFont)
        ivBack.setOnClickListener { onClickBack() }
        setStorage()
    }

    fun onClickBack() {
        finish()
    }

    private fun setStorage() {
        tvTotalStorage.text = getString(R.string.settings_total_storage, doubleMBToStringGB(getTotalStorageInMB()))
        tvUsed.text = doubleMBToStringGB(getUsedStorageInMB()) + " GB"
        tvFree.text = doubleMBToStringGB(getFreeStorageInMB()) + " GB"
        //TODO fix getsize of bin folder

        setProgressBar()
    }

    private fun doubleMBToStringGB(mb: Double): String {
        // /1000 is for mb->gb, 100.0 is for amount of decimals (2)
        return (Math.round((mb / 1000) * 100.0) / 100.0).toString()
    }

    private fun getTotalStorageInMB(): Double {
        val stat = StatFs(Environment.getExternalStorageDirectory().getPath())
        val bytesTotal: Long = stat.blockSizeLong * stat.blockCountLong
        val mbTotal: Double = (bytesTotal / 1048576).toDouble()
        return mbTotal
    }

    private fun getUsedStorageInMB(): Double {
        return getTotalStorageInMB() - getFreeStorageInMB()
    }

    private fun getFreeStorageInMB(): Double {
        val path = Environment.getDataDirectory()
        val stats = StatFs(path.path)
        val bytesAvailable = stats.blockSizeLong * stats.availableBlocksLong
        val mbFree: Double = (bytesAvailable / 1048576).toDouble()
        return mbFree
    }

    private fun setProgressBar() {
        //1: green, 2:black 3:orange

        pbStorageUsed.max = getTotalStorageInMB().toInt()

        //todo plus cache size
//        pbStorageFree.setProgress((getFreeStorageInMB() * 0.5).toInt())
//
//
//
//        pbStorageCache.setProgress((getUsedStorageInMB() + getFreeStorageInMB() * 0.5).toInt())

        var cache = (getUsedStorageInMB() + getFreeStorageInMB() * 0.5).toInt()

//        pbStorageUsed.setProgress(getUsedStorageInMB().toInt())
        pbStorageUsed.progress = getUsedStorageInMB().toInt()
//        pbStorageUsed.
        pbStorageUsed.secondaryProgress = cache
    }




}
