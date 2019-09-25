package com.project.datamule.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_settings.*

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
    }

    fun onClickBack() {
        finish()
    }
}
