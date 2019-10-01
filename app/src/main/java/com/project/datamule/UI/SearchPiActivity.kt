package com.project.datamule.UI

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.Adapter.PiAdapter
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_search_pi.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.ivBack
import kotlinx.android.synthetic.main.content_home.*

class SearchPiActivity : AppCompatActivity() {

    private var pi_s = arrayListOf<Pi>()
    private var piAdapter = PiAdapter(pi_s, {clickedPi: Pi -> onPiClicked(clickedPi)})

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

    private fun onPiClicked(clickedPi: Pi) {
//        val intent = Intent(this, DetailActivity::class.java)
//        intent.putExtra(PI_EXTRA, clickedPi)
//        startActivity(intent)
    }

    private fun onClickBack() {
        finish()
    }
}
