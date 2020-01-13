package com.project.datamule.ui

import android.animation.AnimatorInflater
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_splash.*
import android.content.SharedPreferences

/**
 * Class to display a Splash screen when the applications starts up
 */
class SplashActivity : AppCompatActivity() {

    var prefs: SharedPreferences? = null

    /**
     * Perform initialization of all fragments.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefs = getSharedPreferences("com.project.datamule", MODE_PRIVATE)

        /**
         * WIP - The {NewUserActivity} is currently outdated, update this before making use of it.
         *
         * Set this to true to enable {NewUserActivity} screen for new users.
         */
        if (prefs!!.getBoolean("firstrun", false)) {
            //Running app for the first time
            val animatorSet = AnimatorInflater.loadAnimator(this@SplashActivity, R.animator.loading_animator)
            animatorSet.setTarget(ivLoader)
            animatorSet.start()

            Handler().postDelayed({
                startActivity(
                    Intent(
                        this@SplashActivity,
                        NewUserActivity::class.java
                    )
                )
                finish()
            }, 1000)
            //Edit/commit prefs
            prefs!!.edit().putBoolean("firstrun", false).apply()
        } else {
            val animatorSet = AnimatorInflater.loadAnimator(this@SplashActivity, R.animator.loading_animator)
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
}
