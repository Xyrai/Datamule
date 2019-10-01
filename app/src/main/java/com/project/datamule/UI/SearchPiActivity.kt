package com.project.datamule.UI

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_search_pi.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.ivBack

class SearchPiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_pi)

        initView()
    }

    private fun initView() {
//        var fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf")
//        tvBack.setTypeface(fontAwesomeFont)
        ivBack.setOnClickListener { onClickBack() }
        btnSearchPi.setOnClickListener { onClickOpenPiList() }

    }

    private fun onClickOpenPiList() {
//        val intent = Intent(this, PiListActivity::class.java)
//        startActivity(intent)
    }

    private fun onClickBack() {
        finish()
    }
}
