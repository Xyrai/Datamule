package com.project.datamule.UI

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import kotlinx.android.synthetic.main.activity_settings.*
import android.graphics.Typeface
import android.net.Uri
import com.project.datamule.R
import java.io.File


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

        //temporary create folder (should be run at launch of the app)
//        val folder = File(
//            (Environment.getExternalStorageDirectory()).toString() +
//            File.separator + "DataMule"
//                )
//
//
//
//        var success = true
//        if (!folder.exists()) {
//        success = folder.mkdirs()
//        }
//        if (success) {
//         // Do something on success
//            println("deze")
//            if(folder.listFiles() != null) println("HIEROO" + getfolderSize(folder))
//        }
//        else {
//         // Do something else on failure
//        }

        getTempFile(this, "/testing")
        println(getfolderSize(cacheDir))



        setProgressBar()
    }

    private fun getTempFile(context: Context, url: String): File? =
        Uri.parse(url)?.lastPathSegment?.let { filename ->
            File.createTempFile(filename, null, context.cacheDir)
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

    private fun getfolderSize(directory: File): Long {
        var length: Long = 0

        for (file in directory.listFiles()) {
            println(file.name)
            if (file.isFile())
                length += file.length()
//            else
//                length += folderSize(file)
        }
        return length
    }



    private fun setProgressBar() {
        //1: green, 2:black 3:orange
        pbStorage.max = getTotalStorageInMB().toInt()

        //todo plus cache size
        var cache = (getFreeStorageInMB() * 0.02).toInt() + getUsedStorageInMB().toInt()

        pbStorage.progress = getUsedStorageInMB().toInt()
        pbStorage.secondaryProgress = cache
    }




}
