package com.project.datamule.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import kotlinx.android.synthetic.main.activity_settings.*
import android.graphics.Typeface
import com.project.datamule.R
import java.io.File

//1 MB = 1048576 bytes (1024 bytes * 1024 KB = 1048576 bytes = 1MB)
private const val BYTE_TO_MB_DIVIDER = 1048576.0

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
        createCacheFile()
        setStorage()
    }

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
        tvTotalStorage.text = getString(R.string.settings_total_storage, doubleMBToStringGB(getTotalStorageInMB()))
        tvUsed.text = doubleMBToStringGB(getUsedStorageInMB()) + " GB"
        tvFree.text = doubleMBToStringGB(getFreeStorageInMB()) + " GB"
        tvCache.text = doubleMBToStringGB(getCacheStorageInMB()) + " GB"

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




}
