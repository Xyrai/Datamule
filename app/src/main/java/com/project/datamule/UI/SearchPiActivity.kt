package com.project.datamule.UI

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_search_pi.*
import kotlinx.android.synthetic.main.activity_settings.ivBack
import kotlinx.android.synthetic.main.item_pi.view.*

class SearchPiActivity : AppCompatActivity() {

    private var pi_s = arrayListOf<Pi>()
    private lateinit var piAdapter: PiAdapter
    private var selectedPi: Pi? = null

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

    private fun updateUI() {
        tvNearbyPiTitle.text = getString(R.string.nearby_pi_title, pi_s.size)
    }

    private fun onClickOpenPiList() {
        piAdapter = PiAdapter(pi_s) { clickedPi: Pi -> onPiClicked(clickedPi)}

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
        updateUI()
    }

    @SuppressLint("NewApi")
    private fun onPiClicked(clickedPi: Pi) {
        var position = pi_s.indexOf(clickedPi)
        var clickedPiItem = rvSearchPi.get(position)

        when (selectedPi) {
            null -> { //select pi
                selectedPi = clickedPi
                clickedPiItem.background = getDrawable(R.drawable.rectangle_color_green)
                clickedPiItem.tvName.setTextColor(getColor(R.color.white))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi_white))
                btnAddPi.visibility = View.VISIBLE
            } // deselect pi
            clickedPi -> {
                selectedPi = null
                clickedPiItem.background = getDrawable(R.drawable.button_rectangle_custom)
                clickedPiItem.tvName.setTextColor(getColor(R.color.colorAccent))
                clickedPiItem.ivPi.setImageDrawable(getDrawable(R.drawable.logo_pi))
                btnAddPi.visibility = View.INVISIBLE
            }
            else -> //warning/shake animation
                clickedPiItem.startAnimation(AnimationUtils.loadAnimation(this,R.anim.button_shaker))
        }
    }

    private fun onClickBack() {
        finish()
    }
}
