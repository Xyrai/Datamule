package com.project.datamule.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import kotlinx.android.synthetic.main.activity_settings.*
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_home.*
import com.project.datamule.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
    }

    private fun initView() {
        var fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf")
        tvBack.setTypeface(fontAwesomeFont)
        tvBack.setOnClickListener { onClickBack() }
        setStorage()
    }

    fun onClickBack() {
        finish()
    }

    private fun setStorage() {

        setProgressBar()
        //storage
        //TODO fix internal storage size (its the SDcard size now)
        val stat: StatFs = StatFs(Environment.getExternalStorageDirectory().getPath())
        val bytesAvailable: Long = stat.blockSizeLong * stat.blockCountLong
        val megAvailable: Long = bytesAvailable / 1048576
        tvStorage.text = getString(R.string.settings_max_storage, megAvailable)


        //RAM
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val availableRam = memInfo.availMem / 0x100000L
        println("HIEROO" + availableRam)
        tvRam.text = getString(R.string.settings_ram, availableRam)
    }

    private fun setProgressBar() {
        pbStorage.setProgress(45)
    }



}
