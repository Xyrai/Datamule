package com.project.datamule.UI

import android.animation.AnimatorInflater
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.project.datamule.R
import kotlinx.android.synthetic.main.activity_splash.*
import android.content.SharedPreferences
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T

class SplashActivity : AppCompatActivity() {

    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefs = getSharedPreferences("com.project.datamule", MODE_PRIVATE)
        prefs!!.getBoolean("firstrun",true)

        if (prefs!!.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            var animatorSet = AnimatorInflater.loadAnimator(this@SplashActivity, R.animator.loading_animator)
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
            // using the following line to edit/commit prefs
            prefs!!.edit().putBoolean("firstrun", false).commit();
        } else {
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
//    override fun onResume() {
//        super.onResume();
//
//        if (prefs!!.getBoolean("firstrun", true)) {
//            // Do first run stuff here then set 'firstrun' as false
//            var animatorSet = AnimatorInflater.loadAnimator(this@SplashActivity, R.animator.loading_animator)
//            animatorSet.setTarget(ivLoader)
//            animatorSet.start()
//
//            Handler().postDelayed({
//                startActivity(
//                    Intent(
//                        this@SplashActivity,
//                        NewUserActivity::class.java
//                    )
//                )
//                finish()
//            }, 1000)
//            // using the following line to edit/commit prefs
//            prefs!!.edit().putBoolean("firstrun", false).commit();
//        } else {
//            var animatorSet = AnimatorInflater.loadAnimator(this@SplashActivity, R.animator.loading_animator)
//            animatorSet.setTarget(ivLoader)
//            animatorSet.start()
//
//            Handler().postDelayed({
//                startActivity(
//                    Intent(
//                        this@SplashActivity,
//                        HomeActivity::class.java
//                    )
//                )
//                finish()
//            }, 1000)
//        }
//    }
}
