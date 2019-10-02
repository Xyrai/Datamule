package com.project.datamule.UI

import android.animation.AnimatorInflater
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var animatorSet = AnimatorInflater.loadAnimator(this@SplashActivity, R.animator.loading_animator)
        animatorSet.setTarget(ivLoader)
        animatorSet.start()

        Handler().postDelayed({
            startActivity(
                Intent(
                    this@SplashActivity,
                    HomeActivity::class.java
                )
            )
            finish()
        }, 1000)

    }
}
