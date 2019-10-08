package com.project.datamule.UI

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_search_pi.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.ivBack
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.item_pi.view.*
import java.util.*

class SearchPiActivity : AppCompatActivity() {

    private var pi_s = arrayListOf<Pi>()
    private lateinit var piAdapter: PiAdapter

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
        piAdapter = PiAdapter(pi_s, {clickedPi: Pi -> onPiClicked(clickedPi)})

        //Hide elements of Search Pi screen
        clRectangle.visibility = View.INVISIBLE
        btnSearchPi.visibility = View.INVISIBLE
        tvNearbyPiTitle.visibility = View.VISIBLE
        tvNearbyPiDesc.visibility = View.VISIBLE

        //Initialize RecyclerView
        rvSearchPi.layoutManager = LinearLayoutManager(this@SearchPiActivity, RecyclerView.VERTICAL, false)
        rvSearchPi.adapter = piAdapter

        for (i in Pi.PI_S.indices) {
            pi_s.add(Pi(Pi.PI_S[i]))
        }
        piAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NewApi", "ResourceAsColor")
    private fun onPiClicked(clickedPi: Pi) {
        var position = pi_s.indexOf(clickedPi)
        var clickedPiItem = rvSearchPi.get(position)


        var tcUsed = clickedPiItem.tvName.currentTextColor
        var tcWhite = getColor(R.color.white)
        var tcBlack = getColor(R.color.colorAccent)

        if (tcUsed == tcWhite) {
            clickedPiItem.background = getDrawable(R.drawable.button_rectangle_custom)
            clickedPiItem.tvName.setTextColor(getColor(R.color.colorAccent))
            clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi))
        } else if (tcUsed == tcBlack) {
            clickedPiItem.background = getDrawable(R.drawable.rectangle_color_green)
            clickedPiItem.tvName.setTextColor(getColor(R.color.white))
            clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi_white))
        }
    }

    private fun onClickBack() {
        finish()
    }
}
